package me.sintaxlabs.chunkShield.listeners;

import me.sintaxlabs.chunkShield.main;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class blockPlaceChecker implements Listener
{
    ////////////////////////////////////////////////////////////////////////////
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event)
    {
        int blockTotal = 0;
        Material placedType = event.getBlock().getType();

        if (main.Global.theBlockLimit.containsKey(placedType))
        {
            Chunk chunk = event.getBlock().getChunk();
            int limit = main.Global.theBlockLimit.get(placedType);


            for (int y = chunk.getWorld().getMinHeight(); y < chunk.getWorld().getMaxHeight(); y++)
            {
                for (int x = 0; x < 16; x++)
                {
                    for (int z = 0; z < 16; z++)
                    {
                        Block block = chunk.getBlock(x, y, z);
                        if (block.getType() == placedType)
                        {
                            blockTotal++;
                            if (blockTotal > limit)
                            {
                                event.setCancelled(true);
                                blockTotal = 0;
                            }
                        }
                    }
                }
            }
            // ---- Category totals: DOORS/TRAPDOORS (blocks) ----
            if (main.Global.configCollectiveDoorLimit < 0) return;
            int doorTotal = 0;

            var world = chunk.getWorld();
            for (int y = world.getMinHeight(); y < world.getMaxHeight(); y++)
            {
                for (int x = 0; x < 16; x++)
                {
                    for (int z = 0; z < 16; z++)
                    {
                        Block b = chunk.getBlock(x, y, z);
                        BlockData data = b.getBlockData();

                        if (data instanceof Door d)
                        {
                            if (d.getHalf() == Door.Half.TOP) continue; // count door once
                            if (++doorTotal > main.Global.configCollectiveDoorLimit)
                            {
                                Block top = b.getRelative(0, 1, 0);
                                if (top.getBlockData() instanceof Door)
                                {
                                    top.setType(Material.AIR, false);
                                }
                                b.setType(Material.AIR, false);
                            }
                        }
                        else if (data instanceof TrapDoor)
                        {
                            if (++doorTotal > main.Global.configCollectiveDoorLimit)
                            {
                                b.setType(Material.AIR, false);
                            }
                        }
                    }
                }
            }
        }
    }
}
