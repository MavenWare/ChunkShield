package me.sintaxlabs.chunkShield;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import java.util.*;


public final class ChunkShield extends JavaPlugin implements Listener
{

    private final Map<EntityType, Integer> entitylimits = new HashMap<>();
    private final Map<Material, Integer> blockLimits = new HashMap<>();
    private final Map<EntityType, Integer> namedEntityLimits = new HashMap<>();

    @Override
    public void onEnable()
    {
        saveDefaultConfig();
        loadLimitsFromConfig();
        loadNamedEntityLimits();
        Objects.requireNonNull(getCommand("reloadconfig")).setExecutor(this);
        getServer().getPluginManager().registerEvents(this,this);
        getLogger().info("ChunkShield enabled.");

        Global.configCollectiveMinecartLimit = this.getConfig().getInt("collective-minecart-limit");

        Global.configCollectiveDoorLimit = this.getConfig().getInt("collective-door-limit");

        Global.configCollectiveBoatLimit = this.getConfig().getInt("collective-boat-limit");

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                for (World world : getServer().getWorlds())
                {
                    for (Chunk chunk : world.getLoadedChunks())
                    {
                        cleanupChunkEntities(chunk);
                    }
                }
            }
        }.runTaskTimer(this, 0L, 100); // 200 ticks = 10 seconds
    }
    ////////////////////////////////////////////////////////////////////////////
    public static class Global
    {
        public static int configCollectiveMinecartLimit;
        public static int configCollectiveDoorLimit;
        public static int configCollectiveBoatLimit;
    }
    ////////////////////////////////////////////////////////////////////////////
    private void loadNamedEntityLimits()
    {
        ConfigurationSection section = getConfig().getConfigurationSection("named-entity-limits");
        if (section != null)
        {
            for (String key : section.getKeys(false))
            {
                try
                {
                    EntityType type = EntityType.valueOf(key.toUpperCase());
                    int limit = section.getInt(key);
                    namedEntityLimits.put(type, limit);
                }
                catch (IllegalArgumentException ex)
                {
                    getLogger().warning("Invalid entity type in named-entity-limits: " + key);
                }
            }
        }
    }
    ////////////////////////////////////////////////////////////////////////////
    private void loadLimitsFromConfig()
    {
        FileConfiguration config = getConfig();
        entitylimits.clear();
        blockLimits.clear();

        Global.configCollectiveMinecartLimit = this.getConfig().getInt("collective-minecart-limit");
        Global.configCollectiveDoorLimit = this.getConfig().getInt("collective-door-limit");
        Global.configCollectiveBoatLimit = this.getConfig().getInt("collective-boat-limit");

        if (config.isConfigurationSection("entity-limits"))
        {
            for (String key : Objects.requireNonNull(config.getConfigurationSection("entity-limits")).getKeys(false))
            {
                try
                {
                    EntityType type = EntityType.valueOf(key.toUpperCase());
                    int elimit = config.getInt("entity-limits." + key);
                    entitylimits.put(type, elimit);
                } catch (IllegalArgumentException e)
                {
                    getLogger().warning("Invalid entity type in config: " + key);
                }
            }
        }

        ConfigurationSection section = config.getConfigurationSection("block-limits");
        if (section != null)
        {
            for (String key : Objects.requireNonNull(config.getConfigurationSection("block-limits")).getKeys(false))
            {
                try
                {
                    Material material = Material.valueOf(key.toUpperCase());
                    int blimit = section.getInt(key);
                    blockLimits.put(material, blimit);
                } catch (IllegalArgumentException e)
                {
                    getLogger().warning("Invalid block material in config: " + key);
                }
            }
        }
    }
    ////////////////////////////////////////////////////////////////////////////
    private void cleanupChunkEntities(Chunk chunk)
    {
        Map<EntityType, List<Entity>> namedMap   = new HashMap<>();
        Map<EntityType, List<Entity>> unnamedMap = new HashMap<>();

        // Build buckets FIRST so named are exempt from normal limits
        for (Entity entity : chunk.getEntities())
        {
            EntityType type = entity.getType();
            if (!entitylimits.containsKey(type)) continue; // only manage types we care about

            boolean isNamed = entity.customName() != null; // modern API (not deprecated)
            (isNamed ? namedMap : unnamedMap)
                    .computeIfAbsent(type, k -> new ArrayList<>())
                    .add(entity);
        }

        // UNNAMED limits (named are exceptions in this pass)
        for (Map.Entry<EntityType, List<Entity>> entry : unnamedMap.entrySet())
        {
            EntityType type = entry.getKey();
            Integer limitObj = entitylimits.get(type);         // should be non-null because of the earlier containsKey
            if (limitObj == null) continue;                    // extra safety
            int limit = Math.max(0, limitObj);                 // guard against negative values

            List<Entity> list = entry.getValue();              // this is a mutable list we created above
            if (list.size() > limit) {
                int toRemove = list.size() - limit;
                Collections.shuffle(list);                     // optional fairness
                for (int i = 0; i < toRemove; i++) {
                    list.get(i).remove();
                }
            }
        }

        // NAMED limits (only if a cap exists for that type)
        for (Map.Entry<EntityType, List<Entity>> entry : namedMap.entrySet())
        {
            EntityType type = entry.getKey();
            Integer capObj = namedEntityLimits.get(type);      // <-- this was likely null and caused NPE
            if (capObj == null) continue;                      // no cap configured => skip
            int cap = Math.max(0, capObj);

            List<Entity> list = entry.getValue();
            if (list.size() > cap) {
                int toRemove = list.size() - cap;
                Collections.shuffle(list);
                for (int i = 0; i < toRemove; i++) {
                    list.get(i).remove();
                }
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////
    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        Entity entity = event.getEntity();
        EntityType type = entity.getType();
        Chunk chunk = entity.getLocation().getChunk();

        // Is THIS entity named?
        boolean isNamed = entity.customName() != null;

        // Named entities: enforce namedEntityLimits (if configured) against ONLY named peers
        if (isNamed && namedEntityLimits.containsKey(type)) {
            int cap = Math.max(0, namedEntityLimits.get(type));
            int namedCount = 0;

            for (Entity e : chunk.getEntities()) {
                if (e == entity) continue; // don't count the one spawning
                if (e.getType() != type) continue;
                if (e.customName() != null) namedCount++;
            }

            if (namedCount >= cap) {
                event.setCancelled(true);
                return;
            }
        }

        // Unnamed entities: enforce entitylimits (if configured) against ONLY unnamed peers
        if (!isNamed && entitylimits.containsKey(type)) {
            int cap = Math.max(0, entitylimits.get(type));
            int unnamedCount = 0;

            for (Entity e : chunk.getEntities()) {
                if (e == entity) continue; // don't count the one spawning
                if (e.getType() != type) continue;
                if (e.customName() == null) unnamedCount++;
            }

            if (unnamedCount >= cap) {
                event.setCancelled(true);
            }
        }
    }
    ////////////////////////////////////////////////////////////////////////////
    @EventHandler
    public void onVehicleCreate(VehicleCreateEvent event)
    {
        Vehicle vehicle = event.getVehicle();
        EntityType type = vehicle.getType();
        Chunk chunk = event.getVehicle().getLocation().getChunk();

        if (entitylimits.containsKey(type))
        {
            int vehicleCount = 0;
            for (Entity e : chunk.getEntities())
            {
                if (e.getType() == type) vehicleCount++;
            }

            if (vehicleCount >= entitylimits.get(type))
            {
                event.setCancelled(true);
            }
        }
        // ---- Category totals: BOATS ----
        if (Global.configCollectiveBoatLimit >= 0) {
            int total = 0;
            for (Entity e : chunk.getEntities()) if (e instanceof Boat) total++;

            int toRemove = total - Global.configCollectiveBoatLimit;
            if (toRemove > 0) {
                for (Entity e : chunk.getEntities()) {
                    if (e instanceof Boat) {
                        e.remove();
                        if (--toRemove == 0) break; // don't return; let minecart check run
                    }
                }
            }
        }

        // ---- Category totals: MINECARTS ----
        if (Global.configCollectiveMinecartLimit >= 0) {
            int total = 0;
            for (Entity e : chunk.getEntities()) if (e instanceof Minecart) total++;

            int toRemove = total - Global.configCollectiveMinecartLimit;
            if (toRemove > 0) {
                for (Entity e : chunk.getEntities()) {
                    if (e instanceof Minecart) {
                        e.remove();
                        if (--toRemove == 0) break;
                    }
                }
            }
        }
    }
    ////////////////////////////////////////////////////////////////////////////
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event)
    {
        int blockTotal = 0;
        Material placedType = event.getBlock().getType();

        if (blockLimits.containsKey(placedType))
        {
            Chunk chunk = event.getBlock().getChunk();
            int limit = blockLimits.get(placedType);


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
            if (Global.configCollectiveDoorLimit < 0) return;
            int doorTotal = 0;

            var w = chunk.getWorld();
            for (int y = w.getMinHeight(); y < w.getMaxHeight(); y++)
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
                            if (++doorTotal > Global.configCollectiveDoorLimit)
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
                            if (++doorTotal > Global.configCollectiveDoorLimit)
                            {
                                b.setType(Material.AIR, false);
                            }
                        }
                    }
                }
            }
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
            if (sender.hasPermission("chunkshield.reload"))
            {
                reloadConfig();
                loadLimitsFromConfig();
                loadNamedEntityLimits();
                sender.sendMessage("§7[§6ChunkShield§7] §aConfig reloaded.");
            } else {
                sender.sendMessage("§cYou don't have permission to do that.");
            }
            return true;
        }

        sender.sendMessage("§cUnknown subcommand.");
        return true;
    }
}
