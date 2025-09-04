package me.sintaxlabs.chunkShield.listeners;

import me.sintaxlabs.chunkShield.main;
import net.kyori.adventure.text.Component;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.bukkit.Bukkit.getServer;


public final class blockPlaceChecker implements Listener
{

    public static class blockGlobal
    {
        public static int xCord;
        public static int yCord;
        public static int zCord;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent e)
    {

        Material placedType = e.getBlock().getType();
        Chunk chunk = e.getBlock().getChunk();
        Block block = e.getBlock();
        Player player = e.getPlayer();
        PlayerInventory inventory = e.getPlayer().getInventory();
        ItemStack secondHand = inventory.getItemInOffHand();

        blockGlobal.xCord = e.getBlock().getX();
        blockGlobal.yCord = e.getBlock().getY();
        blockGlobal.zCord = e.getBlock().getZ();

        // Patch to prevent unnecessary portal destruction.
        if (block.getType() == Material.END_PORTAL_FRAME)
        {
            if (main.Global.configToggleEndPortalFix)
            {
                againstEndPortalCheck(e, player, secondHand, inventory);
            }
            else if (main.Global.configToggleBlockCheck)
            {
                if (main.Global.configToggleBlockCheck_50)
                {
                    if (ThreadLocalRandom.current().nextBoolean())
                    {blockChunkCheck(chunk, placedType);}
                }
                else blockChunkCheck(chunk, placedType);
            }

        }
        else if (main.Global.configToggleBlockCheck)
        {
            if (main.Global.configToggleBlockCheck_50)
            {
                if (ThreadLocalRandom.current().nextBoolean())
                {blockChunkCheck(chunk, placedType);}
            }
            else blockChunkCheck(chunk, placedType);
        }
    }

    /////////////////////////////////////////////////////////////////////////////
    private static void againstEndPortalCheck(BlockPlaceEvent e, Player player, ItemStack secondHand, PlayerInventory inventory)
    {
        Block against = e.getBlockAgainst();
        if (against.getType() == Material.END_PORTAL_FRAME)
        {
            // Main Hand Check
            if (player.getInventory().getItemInMainHand().getType() == Material.ENDER_EYE)
            {
                // Check1
                if (secondHand.getType() == Material.END_PORTAL_FRAME)
                {
                    e.setCancelled(true);
                    inventory.setItemInOffHand(null);
                    inventory.remove(Material.END_PORTAL_FRAME);
                }
            }
            // Offhand Check
            else if (player.getInventory().getItemInOffHand().getType() == Material.ENDER_EYE)
            {
                // Check1
                if (player.getInventory().getItemInMainHand().getType() == Material.END_PORTAL_FRAME)
                {
                    e.setCancelled(true);
                    inventory.remove(Material.END_PORTAL_FRAME);
                }
            }
        }
        else
        {
            e.setCancelled(true);
            inventory.remove(Material.END_PORTAL_FRAME);
            if (secondHand.getType() == Material.END_PORTAL_FRAME)
            {
                inventory.setItemInOffHand(null);
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////
    private static void blockChunkCheck(Chunk chunk, Material placedType)
    {
        if (!main.Global.theBlockLimits.isEmpty())
        {
            World world = chunk.getWorld();
            World.Environment environment = chunk.getWorld().getEnvironment();

            // Patch to make sure we don't destroy natural Bedrock.
            if (environment == World.Environment.NORMAL)
            {
                main.Global.minY = -59;
                main.Global.maxY = world.getMaxHeight();
            }
            if (environment == World.Environment.NETHER)
            {
                main.Global.minY = 5;
                main.Global.maxY = 122;
            }
            if (environment == World.Environment.THE_END)
            {
                main.Global.minY = world.getMinHeight();
                main.Global.maxY = world.getMaxHeight();
            }

            // Iterate each configured block type and prune extras within this chunk
            for (Map.Entry<Material, Integer> rule : main.Global.theBlockLimits.entrySet())
            {
                Material target = rule.getKey();
                int limit = Math.max(0, rule.getValue());

                // Collect all blocks of this type in deterministic order
                List<Block> matches = new ArrayList<>();
                for (int y = main.Global.minY; y < main.Global.maxY; y++)
                {
                    for (int x = 0; x < 16; x++)
                    {
                        for (int z = 0; z < 16; z++)
                        {
                            Block b = chunk.getBlock(x, y, z);
                            if (b.getType() == target) matches.add(b);
                        }
                    }
                }

                // Remove extras beyond the limit
                if (matches.size() > limit)
                {
                    for (int i = limit; i < matches.size(); i++)
                    {
                        Block b = matches.get(i);
                        if (placedType.isItem())
                        {
                            getServer().broadcast(Component.text("§eChunkshield§7: §6A BlockLimit was reached for§7: §e" + b.getType()) , "chunkShield.verbose");
                            b.breakNaturally(false,false);
                        }
                        else
                        {
                            getServer().broadcast(Component.text("§eChunkshield§7: §6A BlockLimit was reached for§7: §e" + b.getType()) , "chunkShield.verbose");
                            b.setType(Material.AIR, false); // no physics to avoid cascades
                        }
                    }
                }
            }
        }

        // ===== 3) BLOCKS: collective DOOR/TRAPDOOR cap =====
        // Mirrors your category total logic without the placement event
        if (main.Global.configCollectiveDoorLimit == -1) return;

        if (main.Global.configCollectiveDoorLimit >= 0)
        {
            World world = chunk.getWorld();
            World.Environment environment = chunk.getWorld().getEnvironment();

            if (environment == World.Environment.NORMAL)
            {
                main.Global.minY = -62;
                main.Global.maxY = world.getMaxHeight();
            }
            if (environment == World.Environment.NETHER)
            {
                main.Global.minY = 2;
                main.Global.maxY = 125;
            }
            if (environment == World.Environment.THE_END)
            {
                main.Global.minY = world.getMinHeight();
                main.Global.maxY = world.getMaxHeight();
            }

            int doorCount = 0;

            // We will remove anything beyond the cap. Deterministic scan order.
            for (int y = main.Global.minY; y < main.Global.maxY; y++)
            {
                for (int x = 0; x < 16; x++)
                {
                    for (int z = 0; z < 16; z++)
                    {
                        Block b = chunk.getBlock(x, y, z);
                        BlockData data = b.getBlockData();

                        // Count bottom halves of doors once
                        if (data instanceof Door d)
                        {
                            if (d.getHalf() == Door.Half.TOP) continue;
                            doorCount++;
                            if (doorCount > main.Global.configCollectiveDoorLimit)
                            {
                                b.breakNaturally(false,false);
                                getServer().broadcast(Component.text("§eChunkshield§7: §6Door limit was reached via trapdoor and prevented overload.") , "chunkShield.verbose");
                            }
                        }
                        else if (data instanceof TrapDoor)
                        {
                            doorCount++;
                            if (doorCount > main.Global.configCollectiveDoorLimit)
                            {
                                b.breakNaturally(false,false);
                                getServer().broadcast(Component.text("§eChunkshield§7: §6Door limit was reached via door and prevented overload.") , "chunkShield.verbose");
                            }
                        }
                    }
                }
            }
        }
    }
}
