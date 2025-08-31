package me.sintaxlabs.chunkShield.listeners;

import me.sintaxlabs.chunkShield.main;
import org.bukkit.Chunk;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleCreateEvent;

public class vehicleSummonsCheck implements Listener
{
    ////////////////////////////////////////////////////////////////////////////
    @EventHandler
    public void onVehicleCreate(VehicleCreateEvent event)
    {
        Vehicle vehicle = event.getVehicle();
        EntityType type = vehicle.getType();
        Chunk chunk = event.getVehicle().getLocation().getChunk();

        if (main.Global.theEntityLimits.containsKey(type))
        {
            int vehicleCount = 0;
            for (Entity e : chunk.getEntities())
            {
                if (e.getType() == type) vehicleCount++;
            }

            if (vehicleCount >= main.Global.theEntityLimits.get(type))
            {
                event.setCancelled(true);
            }
        }
        // ---- Category totals: BOATS ----
        if (main.Global.configCollectiveBoatLimit >= 0)
        {
            int total = 0;
            for (Entity e : chunk.getEntities()) if (e instanceof Boat) total++;

            int toRemove = total - main.Global.configCollectiveBoatLimit;
            if (toRemove > 0)
            {
                for (Entity e : chunk.getEntities())
                {
                    if (e instanceof Boat) {
                        e.remove();
                        if (--toRemove == 0) break; // don't return; let minecart check run
                    }
                }
            }
        }

        // ---- Category totals: MINECARTS ----
        if (main.Global.configCollectiveMinecartLimit >= 0)
        {
            int total = 0;
            for (Entity e : chunk.getEntities()) if (e instanceof Minecart) total++;

            int toRemove = total - main.Global.configCollectiveMinecartLimit;
            if (toRemove > 0)
            {
                for (Entity e : chunk.getEntities())
                {
                    if (e instanceof Minecart)
                    {
                        e.remove();
                        if (--toRemove == 0) break;
                    }
                }
            }
        }
    }
}
