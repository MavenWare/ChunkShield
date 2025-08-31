////////////////////////////////////
///                              ///
///         Sintax Labs          ///
///                              ///
////////////////////////////////////

// https://github.com/SintaxLabs
// Game: 1.21
// Supports: 1.21.x
// Version: 1.0.0


package me.sintaxlabs.chunkShield;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import java.util.*;


public final class main extends JavaPlugin implements Listener
{

    private final Map<EntityType, Integer> entityLimits = new HashMap<>();
    private final Map<Material, Integer> blockLimits = new HashMap<>();
    private final Map<EntityType, Integer> namedEntityLimits = new HashMap<>();

    @Override
    public void onEnable()
    {
        saveDefaultConfig();
        loadLimitsFromConfig();
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
        public static Map<EntityType, Integer> theEntityLimits;
        public static Map<EntityType, Integer> theNamedEntityLimits;
        public static Map<Material, Integer> theBlockLimit;
    }

    ////////////////////////////////////////////////////////////////////////////
    private void loadLimitsFromConfig()
    {
        FileConfiguration config = getConfig();
        entityLimits.clear();
        blockLimits.clear();
        namedEntityLimits.clear();

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
                    entityLimits.put(type, elimit);
                } catch (IllegalArgumentException e)
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
                    int blimit = section1.getInt(key);
                    blockLimits.put(material, blimit);
                } catch (IllegalArgumentException e)
                {
                    getLogger().warning("Invalid block material in config: " + key);
                }
            }
            Global.theBlockLimit = blockLimits;
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
                catch (IllegalArgumentException ex)
                {
                    getLogger().warning("Invalid entity type in named-entity-limits: " + key);
                }
            }
            Global.theNamedEntityLimits = namedEntityLimits;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    @EventHandler
    public void chunkLoadedCheck(ChunkLoadEvent e)
    {
        Chunk chunk = e.getChunk();

        cleanupChunkEntities(chunk);
    }
    private void cleanupChunkEntities(Chunk chunk)
    {
        Map<EntityType, List<Entity>> namedMap   = new HashMap<>();
        Map<EntityType, List<Entity>> unnamedMap = new HashMap<>();

        // Build buckets FIRST so named are exempt from normal limits
        for (Entity entity : chunk.getEntities())
        {
            EntityType type = entity.getType();
            if (!Global.theEntityLimits.containsKey(type)) continue; // only manage types we care about

            boolean isNamed = entity.customName() != null; // modern API (not deprecated)
            (isNamed ? namedMap : unnamedMap)
                    .computeIfAbsent(type, k -> new ArrayList<>())
                    .add(entity);
        }

        // UNNAMED limits (named are exceptions in this pass)
        for (Map.Entry<EntityType, List<Entity>> entry : unnamedMap.entrySet())
        {
            EntityType type = entry.getKey();
            Integer limitObj = Global.theEntityLimits.get(type);         // should be non-null because of the earlier containsKey
            if (limitObj == null) continue;                    // extra safety
            int limit = Math.max(0, limitObj);                 // guard against negative values

            List<Entity> list = entry.getValue();              // this is a mutable list we created above
            if (list.size() > limit)
            {
                int toRemove = list.size() - limit;
                Collections.shuffle(list);                     // optional fairness
                for (int i = 0; i < toRemove; i++)
                {
                    list.get(i).remove();
                }
            }
        }

        // NAMED limits (only if a cap exists for that type)
        for (Map.Entry<EntityType, List<Entity>> entry : namedMap.entrySet())
        {
            EntityType type = entry.getKey();
            Integer capObj = Global.theNamedEntityLimits.get(type);      // <-- this was likely null and caused NPE
            if (capObj == null) continue;                      // no cap configured => skip
            int cap = Math.max(0, capObj);

            List<Entity> list = entry.getValue();
            if (list.size() > cap)
            {
                int toRemove = list.size() - cap;
                Collections.shuffle(list);
                for (int i = 0; i < toRemove; i++)
                {
                    list.get(i).remove();
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
