////////////////////////////////////
///                              ///
///         Sintax Labs          ///
///                              ///
////////////////////////////////////

// https://github.com/MavenWare
// Supports: 1.16.x - 1.21.x
// Version: 1.0.2

//Dated: October 3, 2025


package me.sintaxlabs.chunkShield;
import me.sintaxlabs.chunkShield.listeners.*;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static org.bukkit.Bukkit.getServer;


public final class main extends JavaPlugin implements Listener
{

    private final Map<EntityType, Integer> entityLimits = new HashMap<>();
    private final Map<Material, Integer> blockLimits = new HashMap<>();
    private final Map<EntityType, Integer> namedEntityLimits = new HashMap<>();

    @Override
    public void onEnable()
    {
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new blockPlaceChecker(), this);
        getServer().getPluginManager().registerEvents(new entitySummonsCheck(), this);
        getServer().getPluginManager().registerEvents(new vehicleSummonsCheck(), this);

        saveDefaultConfig();
        loadLimitsFromConfig();
        getLogger().info("ChunkShield enabled.");
        Objects.requireNonNull(getCommand("reloadconfig")).setExecutor(this);
        Objects.requireNonNull(getCommand("statscommand")).setExecutor(this);

        Global.configMinEntityLimit = this.getConfig().getInt("MinimumEntityRequirement");
        Global.configToggleScanChunkUponLoad = this.getConfig().getBoolean("Scan-Chunks-Upon-Loading");
        Global.configToggleScanChunkUponLoad_50 = this.getConfig().getBoolean("Scan-Chunks-Upon-Loading-50%");
        Global.configToggleBlockCheck = this.getConfig().getBoolean("BlockCheck");
        Global.configToggleBlockCheck_50 = this.getConfig().getBoolean("BlockCheck-50%");
        Global.configToggleEntityCheck = this.getConfig().getBoolean("EntityCheck");
        Global.configToggleEntityCheck_50 = this.getConfig().getBoolean("EntityCheck-50%");
        Global.configToggleScanChunkUponCrafting = this.getConfig().getBoolean("Scan-Chunk-Upon-Crafting");
        Global.configToggleScanChunkUponEntityDying = this.getConfig().getBoolean("Scan-Chunk-Upon-Entity-Dying");
        Global.configToggleScanChunkUponOpeningContainer = this.getConfig().getBoolean("Scan-Chunk-Upon-Opening-Container");
        Global.configToggleVehicleRadiusCheck = this.getConfig().getBoolean("VehicleRadiusCheck");
        Global.configToggleEndPortalFix = this.getConfig().getBoolean("end-portal-fix");
        Global.configCollectiveDoorLimit = this.getConfig().getInt("ChunkDoorLimit");
        Global.configCollectiveVehicletLimit = this.getConfig().getInt("ChunkVehicleLimit");
        Global.configRadiusLimit = this.getConfig().getInt("vehicle-radius");

        Global.configToggleAlertChunkScanned = this.getConfig().getBoolean("ChunkScanned");
        Global.configToggleAlertBlockLimit = this.getConfig().getBoolean("BlockLimits");
        Global.configToggleAlertEntityLimit = this.getConfig().getBoolean("EntityLimits");
        Global.configToggleAlertVehicleLimit = this.getConfig().getBoolean("VehicleLimits");

    }
    ////////////////////////////////////////////////////////////////////////////
    public static class Global
    {
        public static int configRadiusLimit;
        public static int configCollectiveDoorLimit;
        public static boolean configToggleEndPortalFix;
        public static int configCollectiveVehicletLimit;

        public static int configMinEntityLimit;
        public static boolean configToggleScanChunkUponLoad;
        public static boolean configToggleScanChunkUponLoad_50;
        public static boolean configToggleBlockCheck;
        public static boolean configToggleBlockCheck_50;
        public static boolean configToggleEntityCheck;
        public static boolean configToggleEntityCheck_50;
        public static boolean configToggleScanChunkUponCrafting;
        public static boolean configToggleScanChunkUponEntityDying;
        public static boolean configToggleScanChunkUponOpeningContainer;
        public static boolean configToggleVehicleRadiusCheck;

        public static Map<Material, Integer> theBlockLimits;
        public static Map<EntityType, Integer> theEntityLimits;
        public static Map<EntityType, Integer> theNamedEntityLimits;

        public static int minY = 0;
        public static int maxY = 0;
        public static int chunkCount = 0;
        public static int blocksPrevented = 0;
        public static int entitiesRemoved = 0;
        public static int vehiclesPrevented = 0;
        public static int vehiclesRemoved = 0;

        public static int Chunk_unnamedCount = 0;
        public static int Chunk_namedCount = 0;

        public static int Entity_unnamedCount = 0;
        public static int Entity_namedCount = 0;

        public static boolean configToggleAlertChunkScanned;
        public static boolean configToggleAlertBlockLimit;
        public static boolean configToggleAlertEntityLimit;
        public static boolean configToggleAlertVehicleLimit;
    }

    ////////////////////////////////////////////////////////////////////////////
    private void loadLimitsFromConfig()
    {
        FileConfiguration config = getConfig();
        blockLimits.clear();
        entityLimits.clear();
        namedEntityLimits.clear();

        Global.configMinEntityLimit = this.getConfig().getInt("MinimumEntityRequirement");
        Global.configToggleScanChunkUponLoad = this.getConfig().getBoolean("Scan-Chunks-Upon-Loading");
        Global.configToggleScanChunkUponLoad_50 = this.getConfig().getBoolean("Scan-Chunks-Upon-Loading-50%");
        Global.configToggleBlockCheck = this.getConfig().getBoolean("BlockCheck");
        Global.configToggleBlockCheck_50 = this.getConfig().getBoolean("BlockCheck-50%");
        Global.configToggleEntityCheck = this.getConfig().getBoolean("EntityCheck");
        Global.configToggleEntityCheck_50 = this.getConfig().getBoolean("EntityCheck-50%");
        Global.configToggleScanChunkUponCrafting = this.getConfig().getBoolean("Scan-Chunk-Upon-Crafting");
        Global.configToggleScanChunkUponEntityDying = this.getConfig().getBoolean("Scan-Chunk-Upon-Entity-Dying");
        Global.configToggleScanChunkUponOpeningContainer = this.getConfig().getBoolean("Scan-Chunk-Upon-Opening-Container");
        Global.configToggleVehicleRadiusCheck = this.getConfig().getBoolean("VehicleRadiusCheck");
        Global.configToggleEndPortalFix = this.getConfig().getBoolean("end-portal-fix");
        Global.configCollectiveDoorLimit = this.getConfig().getInt("ChunkDoorLimit");
        Global.configCollectiveVehicletLimit = this.getConfig().getInt("ChunkVehicleLimit");
        Global.configRadiusLimit = this.getConfig().getInt("vehicle-radius");

        Global.configToggleAlertChunkScanned = this.getConfig().getBoolean("ChunkScanned");
        Global.configToggleAlertBlockLimit = this.getConfig().getBoolean("BlockLimits");
        Global.configToggleAlertEntityLimit = this.getConfig().getBoolean("EntityLimits");
        Global.configToggleAlertVehicleLimit = this.getConfig().getBoolean("VehicleLimits");

        ConfigurationSection section0 = config.getConfigurationSection("entity-limits");
        if (section0 != null)
        {
            for (String key : Objects.requireNonNull(config.getConfigurationSection("entity-limits")).getKeys(false))
            {
                try
                {
                    EntityType type = EntityType.valueOf(key.toUpperCase());
                    int eLimit = section0.getInt(key);
                    entityLimits.put(type, eLimit);
                }
                catch (IllegalArgumentException e)
                {
                    getLogger().warning("Invalid entity type in config: " + key);
                }
            }
            Global.theEntityLimits = entityLimits;
        }

        ConfigurationSection section1 = config.getConfigurationSection("block-limits");
        if (section1 != null)
        {
            for (String key : Objects.requireNonNull(config.getConfigurationSection("block-limits")).getKeys(false))
            {
                try
                {
                    Material material = Material.valueOf(key.toUpperCase());
                    int bLimit = section1.getInt(key);
                    blockLimits.put(material, bLimit);
                }
                catch (IllegalArgumentException e)
                {
                    getLogger().warning("Invalid block material in config: " + key);
                }
            }
            Global.theBlockLimits = blockLimits;
        }

        ConfigurationSection section2 = getConfig().getConfigurationSection("named-entity-limits");
        if (section2 != null)
        {
            for (String key : section2.getKeys(false))
            {
                try
                {
                    EntityType type = EntityType.valueOf(key.toUpperCase());
                    int limit = section2.getInt(key);
                    namedEntityLimits.put(type, limit);
                }
                catch (IllegalArgumentException e)
                {
                    getLogger().warning("Invalid entity type in named-entity-limits: " + key);
                }
            }
            Global.theNamedEntityLimits = namedEntityLimits;
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    // 20% - InventoryOpenEvent
    // 10% - CraftItemEvent
    // 20% - EntityDeathEvent
    // 100% - ChunkLoadEvent

    /////////////////////////////////////////////////////////////////////////////
    @EventHandler
    public void openInventory1(InventoryOpenEvent e)
    {
        int x = (int) e.getPlayer().getLocation().getX();
        int z = (int) e.getPlayer().getLocation().getZ();
        int y = (int) e.getPlayer().getLocation().getY();

        if (Global.configToggleScanChunkUponOpeningContainer)
        {
            Chunk chunk = e.getPlayer().getChunk();
            // 10% chance to check chunk upon opening any chest, barrel, enderChest.
            int roll = ThreadLocalRandom.current().nextInt(1, 11); // 1 to 3 inclusive
            if (roll == 10)
            {chunkCleansingCheck(chunk, x, z, y);}
        }
    }

    /////////////////////////////////////////////////////////////////////////////
    @EventHandler
    public void craftItem1(CraftItemEvent e)
    {
        int x = (int) e.getWhoClicked().getLocation().getX();
        int z = (int) e.getWhoClicked().getLocation().getZ();
        int y = (int) e.getWhoClicked().getLocation().getY();

        if (Global.configToggleScanChunkUponCrafting)
        {
            // 10% chance to check chunk upon crafting something.
            Chunk chunk = e.getWhoClicked().getChunk();
            int roll = ThreadLocalRandom.current().nextInt(1, 11); // 1 to 3 inclusive
            if (roll == 10) chunkCleansingCheck(chunk, x, z, y);
        }
    }

    /////////////////////////////////////////////////////////////////////////////
    @EventHandler
    public void entityDeath1(EntityDeathEvent e)
    {
        int x = (int) e.getEntity().getLocation().getX();
        int z = (int) e.getEntity().getLocation().getZ();
        int y = (int) e.getEntity().getLocation().getY();

        if (Global.configToggleScanChunkUponEntityDying)
        {
            // 10% chance to check chunk upon an Entity dying.
            Chunk chunk = e.getEntity().getChunk();
            int roll = ThreadLocalRandom.current().nextInt(1, 11); // 1 to 3 inclusive
            if (roll == 10) chunkCleansingCheck(chunk, x, z, y);
        }
    }

    /////////////////////////////////////////////////////////////////////////////
    @EventHandler
    public void chunkLoad1(ChunkLoadEvent e)
    {
        int x = e.getChunk().getX();
        int z = e.getChunk().getZ();
        int y = 80;

        if (Global.configToggleScanChunkUponLoad)
        {
            if (Global.configToggleScanChunkUponLoad_50)
            {
                if (ThreadLocalRandom.current().nextBoolean())
                {
                    if (e.isNewChunk())return;
                    Chunk chunk = e.getChunk();
                    chunkCleansingCheck(chunk, x, z, y);
                }
            }
            else
            {
                if (e.isNewChunk())return;
                Chunk chunk = e.getChunk();
                chunkCleansingCheck(chunk, x, z, y);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    private void chunkCleansingCheck(Chunk chunk, int x, int z, int y)
    {
        //Stopper 1: Chunk void of entities?
        if (chunk.getEntities() == null) return;

        //Stopper 2: Is the mob amount even worth chasing?
        int length = chunk.getEntities().length;
        if (length < Global.configMinEntityLimit) return;

        //Stopper 3: Did the owner clear their lists?
        if (Global.theEntityLimits.isEmpty() & Global.theNamedEntityLimits.isEmpty()) return;

        // ===== ENTITIES ===== //
        Map<EntityType, List<Entity>> namedMap   = new HashMap<>();
        Map<EntityType, List<Entity>> unnamedMap = new HashMap<>();

        for (Entity entity : chunk.getEntities())
        {
            //If the chunk contains no listed entities, move on.
            EntityType type = entity.getType();
            if (!Global.theEntityLimits.containsKey(type)) continue;

            boolean isNamed = entity.customName() != null;
            (isNamed ? namedMap : unnamedMap)
                    .computeIfAbsent(type, k -> new ArrayList<>())
                    .add(entity);
        }

        //Stopper 4: Did we even find any mobs?
        if (namedMap.isEmpty() & unnamedMap.isEmpty()) return;

        Global.chunkCount++;

        // Unnamed Limits
        for (Map.Entry<EntityType, List<Entity>> entry : unnamedMap.entrySet())
        {
            EntityType type = entry.getKey();
            Integer noNameOBJ = Global.theEntityLimits.get(type);
            if (noNameOBJ == null) continue;
            int limit = noNameOBJ;
            List<Entity> list = entry.getValue();
            if (list.size() > limit)
            {
                int toRemove = list.size() - limit;
                Collections.shuffle(list); // optional fairness
                for (Global.Chunk_unnamedCount = 0; Global.Chunk_unnamedCount < toRemove; Global.Chunk_unnamedCount++)
                {
                    list.get(Global.Chunk_unnamedCount).remove();
                }

                if (main.Global.Chunk_unnamedCount != 0)
                {
                    if (main.Global.configToggleAlertEntityLimit)
                    {
                        Component message = Component.text("§c■ " + "§eRemoved §ax"+main.Global.Chunk_unnamedCount +" §6" + type + " §eat§7: §a[" + x + ", " + y + ", " + z + "§a]" + " §c■")
                                .clickEvent(ClickEvent.copyToClipboard(x + " " + y + " " + z))
                                .hoverEvent(HoverEvent.showText(Component.text("§aClick to copy coordinates.")));

                        getServer().broadcast(message, "chunkShield.alerts");
                    }
                }
            }
        }

        // Named Limits
        for (Map.Entry<EntityType, List<Entity>> entry : namedMap.entrySet())
        {
            EntityType type = entry.getKey();
            Integer namedOBJ = Global.theNamedEntityLimits.get(type);
            if (namedOBJ == null) continue;
            int cap = namedOBJ;
            List<Entity> list = entry.getValue();
            if (list.size() > cap)
            {
                int toRemove = list.size() - cap;
                Collections.shuffle(list);
                for (Global.Chunk_namedCount = 0; Global.Chunk_namedCount < toRemove; Global.Chunk_namedCount++)
                {
                    list.get(Global.Chunk_namedCount).remove();
                }

                if (Global.Chunk_namedCount != 0)
                {
                    if (Global.configToggleAlertEntityLimit)
                    {
                        Component message = Component.text("§c■ " + "§eRemoved §ax"+Global.Chunk_namedCount +" §6Named " + type + " §eat§7: §a[" + x + ", " + y + ", " + z + "§a]" + " §c■")
                                .clickEvent(ClickEvent.copyToClipboard(x + " " + y + " " + z))
                                .hoverEvent(HoverEvent.showText(Component.text("§aClick to copy coordinates.")));

                        getServer().broadcast(message, "chunkShield.alerts");
                    }
                }
            }
        }

        if (Global.Chunk_namedCount == 0 && Global.Chunk_unnamedCount == 0) return;

        int totality = Global.Chunk_namedCount + Global.Chunk_unnamedCount;
        if (Global.configToggleAlertChunkScanned)
        {
            Component message = Component.text("§c■ " + "§cA ChunkScan met §66 conditions §cand removed §ax" + totality +" §centities at§7: §a[" + x + ", " + y + ", " + z + "§a]" + " §c■")
                    .clickEvent(ClickEvent.copyToClipboard(x + " " + y + " " + z))
                    .hoverEvent(HoverEvent.showText(Component.text("§aClick to copy coordinates.")));

            getServer().broadcast(message, "chunkShield.alerts");
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args)
    {
        if (args.length == 0)
        {
            sender.sendMessage("§eUsage: /chunkshield reload");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload"))
        {
            if (sender.hasPermission("chunkShield.reload"))
            {
                reloadConfig();
                loadLimitsFromConfig();
                sender.sendMessage("§7[§6ChunkShield§7] §aConfig reloaded.");
            }
            else sender.sendMessage("§cYou don't have permission to do that.");
            return true;
        }
        if (args[0].equalsIgnoreCase("stats"))
        {
            if (sender.hasPermission("chunkShield.info"))
            {
                sender.sendMessage("§a■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■"
                                    + "\n§6ChunkShield stats since §alast restart§7:" + "\n "
                                    + "\n§a" + Global.chunkCount + " §6Total Chunks Scanned"
                                    + "\n§a" + Global.blocksPrevented + " §eTotal Blocks Prevented"
                                    + "\n§a" + Global.entitiesRemoved + " §6Total Entities Removed"
                                    + "\n§a" + Global.vehiclesRemoved + " §eTotal Vehicles Removed"
                                    + "\n§a" + Global.vehiclesPrevented + " §6Total Vehicles Prevented"
                                    + "\n§a■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■");
            }
            else sender.sendMessage("§cYou don't have permission to do that.");
            return true;
        }

        sender.sendMessage("§cUnknown subcommand.");
        return true;
    }
}


//new BukkitRunnable()
//{
//@Override
//public void run()
//{
//for (World world : getServer().getWorlds())
//{
//for (Chunk chunk : world.getLoadedChunks())
//{
//cleanupChunkEntities(chunk);
//}
//}
//}
//}.runTaskTimer(this, 0L, 100); // 200 ticks = 10 seconds
