package me.sintaxlabs.chunkShield.listeners;

import me.sintaxlabs.chunkShield.main;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

public class entitySummonsCheck implements Listener
{
    ////////////////////////////////////////////////////////////////////////////
    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event)
    {
        Entity entity = event.getEntity();
        EntityType type = entity.getType();
        Chunk chunk = entity.getLocation().getChunk();

        // Is THIS entity named?
        boolean isNamed = entity.customName() != null;

        // Named entities: enforce namedEntityLimits (if configured) against ONLY named peers
        if (isNamed && main.Global.theNamedEntityLimits.containsKey(type))
        {
            int cap = Math.max(0, main.Global.theNamedEntityLimits.get(type));
            int namedCount = 0;

            for (Entity e : chunk.getEntities())
            {
                if (e == entity) continue; // don't count the one spawning
                if (e.getType() != type) continue;
                if (e.customName() != null) namedCount++;
            }

            if (namedCount >= cap)
            {
                event.setCancelled(true);
                return;
            }
        }

        // Unnamed entities: enforce entityLimits (if configured) against ONLY unnamed peers
        if (!isNamed && main.Global.theEntityLimits.containsKey(type))
        {
            int cap = Math.max(0, main.Global.theEntityLimits.get(type));
            int unnamedCount = 0;

            for (Entity e : chunk.getEntities())
            {
                if (e == entity) continue; // don't count the one spawning
                if (e.getType() != type) continue;
                if (e.customName() == null) unnamedCount++;
            }

            if (unnamedCount >= cap)
            {
                event.setCancelled(true);
            }
        }
    }
}
