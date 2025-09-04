package me.sintaxlabs.chunkShield.listeners;

import me.sintaxlabs.chunkShield.main;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;

import static org.bukkit.Bukkit.getServer;

public final class vehicleSummonsCheck implements Listener
{
    public static class vehicleGlobal
    {
        public static Vehicle theVehicle;
        public static EntityType theVehicleType;
        public static VehicleMoveEvent theMoveEvent;
        public static VehicleCreateEvent theCreateEvent;
        public static boolean vehicleFlagged;
    }

    ////////////////////////////////////////////////////////////////////////////
    @EventHandler
    public void onVehicleCreate(VehicleCreateEvent e)
    {
        if (main.Global.configToggleVehicleRadiusCheck)
        {
            if (main.Global.configCollectiveVehicletLimit == -1) return;

            vehicleGlobal.theVehicle = e.getVehicle();
            vehicleGlobal.theVehicleType = vehicleGlobal.theVehicle .getType();
            vehicleGlobal.theCreateEvent = e;
            vehicleRadiusScan();

            if(vehicleGlobal.vehicleFlagged)
            {
                e.setCancelled(true);
                vehicleGlobal.vehicleFlagged = false;
                getServer().broadcast(Component.text("§eChunkshield§7: §6VehicleLimit was reached and prevented.") , "chunkShield.verbose");
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////
    @EventHandler
    public void vehicleMoveCheck (VehicleMoveEvent e)
    {
        if (main.Global.configToggleVehicleRadiusCheck)
        {
            if (main.Global.configCollectiveVehicletLimit == -1) return;

            vehicleGlobal.theVehicle = e.getVehicle();
            vehicleGlobal.theVehicleType = vehicleGlobal.theVehicle .getType();
            vehicleGlobal.theMoveEvent = e;

            vehicleRadiusScan();
            if(vehicleGlobal.vehicleFlagged)
            {
                cleanupProcess();
                vehicleGlobal.vehicleFlagged = false;
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////
    private void vehicleRadiusScan()
    {
        int count = 0;

        for (Entity entity : vehicleGlobal.theVehicle.getNearbyEntities(main.Global.configRadiusLimit, main.Global.configRadiusLimit, main.Global.configRadiusLimit))
        {
            if (entity instanceof Minecart) count++;
            else if (entity instanceof Boat) count++;
        }
        if (count >= main.Global.configCollectiveVehicletLimit) vehicleGlobal.vehicleFlagged = true;
    }

    /////////////////////////////////////////////////////////////////////////////
    private static void cleanupProcess()
    {
        // ---- Category totals: BOATS & MINE CARTS
        if (main.Global.configCollectiveVehicletLimit > 0)
        {
            int total = 1;
            for (Entity entity : vehicleGlobal.theVehicle.getNearbyEntities(main.Global.configRadiusLimit, main.Global.configRadiusLimit, main.Global.configRadiusLimit))
            {
                if (entity instanceof Boat || entity instanceof Minecart) total++;
            }

            int toRemove = total - main.Global.configCollectiveVehicletLimit;
            if (toRemove > 0)
            {
                for (Entity entity : vehicleGlobal.theVehicle.getNearbyEntities(main.Global.configRadiusLimit, main.Global.configRadiusLimit, main.Global.configRadiusLimit))
                {
                    if (entity instanceof Boat || entity instanceof Minecart)
                    {
                        getServer().broadcast(Component.text("§eChunkshield§7: §6Removed vehicle.") , "chunkShield.verbose");
                        entity.remove();
                        if (--toRemove == 0) break; // don't return; let minecart check run
                    }
                }
            }
        }
    }











}
