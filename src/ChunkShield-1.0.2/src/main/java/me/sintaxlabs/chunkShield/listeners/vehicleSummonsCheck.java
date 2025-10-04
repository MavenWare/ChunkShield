package me.sintaxlabs.chunkShield.listeners;

import me.sintaxlabs.chunkShield.main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
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
        int x = (int) e.getVehicle().getLocation().getX();
        int y = (int) e.getVehicle().getLocation().getY();
        int z = (int) e.getVehicle().getLocation().getZ();

        if (main.Global.configToggleVehicleRadiusCheck)
        {
            if (main.Global.configCollectiveVehicletLimit == -1) return;

            vehicleGlobal.theVehicle = e.getVehicle();
            vehicleGlobal.theVehicleType = vehicleGlobal.theVehicle .getType();
            vehicleGlobal.theCreateEvent = e;
            vehicleRadiusScan();

            if(vehicleGlobal.vehicleFlagged)
            {
                main.Global.vehiclesPrevented++;
                cleanupProcess(x, y, z);
                vehicleGlobal.vehicleFlagged = false;
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////
    @EventHandler
    public void vehicleMoveCheck (VehicleMoveEvent e)
    {
        int x = (int) e.getVehicle().getLocation().getX();
        int y = (int) e.getVehicle().getLocation().getY();
        int z = (int) e.getVehicle().getLocation().getZ();

        if (main.Global.configToggleVehicleRadiusCheck)
        {
            if (main.Global.configCollectiveVehicletLimit == -1) return;

            vehicleGlobal.theVehicle = e.getVehicle();
            vehicleGlobal.theVehicleType = vehicleGlobal.theVehicle .getType();
            vehicleGlobal.theMoveEvent = e;

            vehicleRadiusScan();
            if(vehicleGlobal.vehicleFlagged)
            {
                cleanupProcess(x, y, z);
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
    private static void cleanupProcess(int x, int y, int z)
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
                        //getServer().broadcast(Component.text("§eChunkshield§7: §6Removed vehicle.") , "chunkShield.verbose");
                        if (main.Global.configToggleAlertVehicleLimit)
                        {
                            Component message = Component.text("§c■ " + "§cVehicle Limit §ewas reached at§7: §a[" + x + ", " + y + ", " + z + "§a]" + " §c■")
                                    .clickEvent(ClickEvent.copyToClipboard(x + " " + y + " " + z))
                                    .hoverEvent(HoverEvent.showText(Component.text("§aClick to copy coordinates.")));

                            getServer().broadcast(message, "chunkShield.alerts");
                        }
                        entity.remove();
                        main.Global.vehiclesRemoved++;
                        if (--toRemove == 0) break; // don't return; let minecart check run
                    }
                }
            }
        }
    }











}