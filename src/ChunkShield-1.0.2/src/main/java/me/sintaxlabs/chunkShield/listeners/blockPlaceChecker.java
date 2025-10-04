package me.sintaxlabs.chunkShield.listeners;

import me.sintaxlabs.chunkShield.main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.*;
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
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import static org.bukkit.Bukkit.getServer;


public final class blockPlaceChecker implements Listener
{
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent e)
    {
        boolean isCaveUpdate;

        Material placedType = e.getBlock().getType();
        Chunk chunk = e.getBlock().getChunk();
        Block block = e.getBlock();
        Player player = e.getPlayer();
        PlayerInventory inventory = e.getPlayer().getInventory();
        ItemStack secondHand = inventory.getItemInOffHand();

        // Quick check for if the server is pre or post 1.18
        World world = chunk.getWorld();
        int yCheck = world.getMinHeight();
        isCaveUpdate = yCheck < 0;


        int x = e.getBlock().getX();
        int y = e.getBlock().getY();
        int z = e.getBlock().getZ();

        @NotNull String name = e.getPlayer().getName();

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
                    {blockChunkCheck(chunk, placedType, x, y, z, name, isCaveUpdate);}
                }
                else blockChunkCheck(chunk, placedType, x, y, z, name, isCaveUpdate);
            }

        }
        else if (main.Global.configToggleBlockCheck)
        {
            if (main.Global.configToggleBlockCheck_50)
            {
                if (ThreadLocalRandom.current().nextBoolean())
                {blockChunkCheck(chunk, placedType, x, y, z, name, isCaveUpdate);}
            }
            else blockChunkCheck(chunk, placedType, x, y, z, name, isCaveUpdate);
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
                    main.Global.blocksPrevented++;
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
                    main.Global.blocksPrevented++;
                    inventory.remove(Material.END_PORTAL_FRAME);
                }
            }
        }
        else
        {
            e.setCancelled(true);
            main.Global.blocksPrevented++;
            inventory.remove(Material.END_PORTAL_FRAME);
            if (secondHand.getType() == Material.END_PORTAL_FRAME) inventory.setItemInOffHand(null);
        }
    }

    /////////////////////////////////////////////////////////////////////////////
    private static void blockChunkCheck(Chunk chunk, Material placedType, int x, int y, int z, String name, boolean isCaveUpdate)
    {
        if (!main.Global.theBlockLimits.isEmpty())
        {
            World world = chunk.getWorld();
            World.Environment environment = chunk.getWorld().getEnvironment();

            // Patch to make sure we don't destroy natural Bedrock.
            if (environment == World.Environment.NORMAL)
            {
                if (isCaveUpdate) main.Global.minY = -59;
                else main.Global.minY = 5;
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
                for (int yLevel = main.Global.minY; yLevel < main.Global.maxY; yLevel++)
                {
                    for (int xLevel = 0; xLevel < 16; xLevel++)
                    {
                        for (int zLevel = 0; zLevel < 16; zLevel++)
                        {
                            Block b = chunk.getBlock(xLevel, yLevel, zLevel);
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
                            //getServer().broadcast(Component.text("§eChunkshield§7: §6A BlockLimit was reached for§7: §e" + b.getType()) , "chunkShield.verbose");
                            if (main.Global.configToggleAlertBlockLimit)
                            {
                                Component message = Component.text("§c■ " + "§6" + name + " §ereached " + "§c" + b.getType() +" §elimit at§7: §a[" + x + ", " + y + ", " + z + "§a]" + " §c■")
                                        .clickEvent(ClickEvent.copyToClipboard(x + " " + y + " " + z))
                                        .hoverEvent(HoverEvent.showText(Component.text("§aClick to copy coordinates.")));

                                getServer().broadcast(message, "chunkShield.alerts");
                            }
                            //b.breakNaturally(false,false); 1.21 API
                            b.breakNaturally();
                        }
                        else
                        {
                            //getServer().broadcast(Component.text("§eChunkshield§7: §6A BlockLimit was reached for§7: §e" + b.getType()) , "chunkShield.verbose");
                            if (main.Global.configToggleAlertBlockLimit)
                            {
                                Component message = Component.text("§c■ " + "§6" + name + " §ereached " + "§c" + b.getType() +" §elimit at§7: §a[" + x + ", " + y + ", " + z + "§a]" + " §c■")
                                        .clickEvent(ClickEvent.copyToClipboard(x + " " + y + " " + z))
                                        .hoverEvent(HoverEvent.showText(Component.text("§aClick to copy coordinates.")));

                                getServer().broadcast(message, "chunkShield.alerts");
                            }
                            b.setType(Material.AIR, false); // no physics to avoid cascades
                        }
                        main.Global.blocksPrevented++;
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
                if (isCaveUpdate) main.Global.minY = -59;
                else main.Global.minY = 5;
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
            for (int yLevel = main.Global.minY; yLevel < main.Global.maxY; yLevel++)
            {
                for (int xLevel = 0; xLevel < 16; xLevel++)
                {
                    for (int zLevel = 0; zLevel < 16; zLevel++)
                    {
                        Block b = chunk.getBlock(xLevel, yLevel, zLevel);
                        BlockData data = b.getBlockData();

                        // Count bottom halves of doors once
                        if (data instanceof Door d)
                        {
                            if (d.getHalf() == Door.Half.TOP) continue;
                            doorCount++;
                            if (doorCount > main.Global.configCollectiveDoorLimit)
                            {
                                //getServer().broadcast(Component.text("§eChunkshield§7: §6Door limit was reached via trapdoor and prevented overload.") , "chunkShield.verbose");
                                if (main.Global.configToggleAlertBlockLimit)
                                {
                                    Component message = Component.text("§c■ " + "§6" + name + " §ereached " + "§cDoor Limit §eat§7: §a[" + x + ", " + y + ", " + z + "§a]" + " §c■")
                                            .clickEvent(ClickEvent.copyToClipboard(x + " " + y + " " + z))
                                            .hoverEvent(HoverEvent.showText(Component.text("§aClick to copy coordinates.")));

                                    getServer().broadcast(message, "chunkShield.alerts");
                                }
                                //b.breakNaturally(false,false); 1.21 API
                                b.breakNaturally();
                                main.Global.blocksPrevented++;
                            }
                        }
                        else if (data instanceof TrapDoor)
                        {
                            doorCount++;
                            if (doorCount > main.Global.configCollectiveDoorLimit)
                            {
                                //getServer().broadcast(Component.text("§eChunkshield§7: §6Door limit was reached via door and prevented overload.") , "chunkShield.verbose");
                                if (main.Global.configToggleAlertBlockLimit)
                                {
                                    Component message = Component.text("§c■ " + "§6" + name + " §ereached " + "§cDoor Limit §eat§7: §a[" + x + ", " + y + ", " + z + "§a]" + " §c■")
                                            .clickEvent(ClickEvent.copyToClipboard(x + " " + y + " " + z))
                                            .hoverEvent(HoverEvent.showText(Component.text("§aClick to copy coordinates.")));

                                    getServer().broadcast(message, "chunkShield.alerts");
                                }
                                //b.breakNaturally(false,false); 1.21 API
                                b.breakNaturally();
                                main.Global.blocksPrevented++;
                            }
                        }
                    }
                }
            }
        }
    }
}
