package me.sintaxlabs.chunkShield.listeners;

import me.sintaxlabs.chunkShield.main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static org.bukkit.Bukkit.*;

public final class entitySummonsCheck implements Listener
{
    ////////////////////////////////////////////////////////////////////////////
    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent e)
    {
        int x = (int) e.getEntity().getLocation().getX();
        int z = (int) e.getEntity().getLocation().getZ();
        int y = (int) e.getEntity().getLocation().getY();

        if (main.Global.configToggleEntityCheck)
        {
            if (main.Global.configToggleEntityCheck_50)
            {
                if (ThreadLocalRandom.current().nextBoolean())
                {
                    Bukkit.getScheduler().runTaskLater(JavaPlugin.getProvidingPlugin(getClass()), () -> entityCheck(e, x, z, y), 1L);
                }
            }
            else Bukkit.getScheduler().runTaskLater(JavaPlugin.getProvidingPlugin(getClass()), () -> entityCheck(e, x, z, y), 1L);
        }
    }

    /////////////////////////////////////////////////////////////////////////////
    private static void entityCheck(EntitySpawnEvent e, int x, int z, int y)
    {
        // ===== ENTITIES ===== //
        Map<EntityType, List<Entity>> namedMap   = new HashMap<>();
        Map<EntityType, List<Entity>> unnamedMap = new HashMap<>();

        Entity entity1 = e.getEntity();
        Chunk chunk = entity1.getLocation().getChunk();

        //Stopper 3: Did the owner clear their lists?
        if (main.Global.theEntityLimits.isEmpty() & main.Global.theNamedEntityLimits.isEmpty()) return;



        for (Entity entity : chunk.getEntities())
        {
            EntityType type = entity.getType();
            //If the chunk contains no listed entities, move on.
            if (!main.Global.theEntityLimits.containsKey(type)) continue;

            boolean isNamed = entity.customName() != null;
            (isNamed ? namedMap : unnamedMap)
                    .computeIfAbsent(type, k -> new ArrayList<>())
                    .add(entity);
        }

        //Stopper 4: Did we even find any mobs?
        if (namedMap.isEmpty() & unnamedMap.isEmpty()) return;

        // Unnamed Limits
        for (Map.Entry<EntityType, List<Entity>> entry : unnamedMap.entrySet())
        {
            EntityType type = entry.getKey();
            Integer noNameOBJ = main.Global.theEntityLimits.get(type);
            if (noNameOBJ == null) continue;
            int limit = noNameOBJ;
            List<Entity> list = entry.getValue();
            if (list.size() > limit)
            {
                int toRemove = list.size() - limit;
                Collections.shuffle(list); // optional fairness
                for (int i = 0; i < toRemove; i++)
                {
                    if (main.Global.configToggleAlertEntityLimit)
                    {
                        Component message = Component.text("§c■ " + "§eEntity: §6" + type + " §ewas removed at§7: §a[" + x + ", " + y + ", " + z + "§a]" + " §c■")
                                .clickEvent(ClickEvent.copyToClipboard(x + " " + y + " " + z))
                                .hoverEvent(HoverEvent.showText(Component.text("§aClick to copy coordinates.")));

                        getServer().broadcast(message, "chunkShield.alerts");
                    }
                    list.get(i).remove();
                    main.Global.entitiesRemoved++;
                }
            }
        }

        // Named Limits
        for (Map.Entry<EntityType, List<Entity>> entry : namedMap.entrySet())
        {
            EntityType type = entry.getKey();
            Integer namedOBJ = main.Global.theNamedEntityLimits.get(type);
            if (namedOBJ == null) continue;
            int cap = Math.max(0, namedOBJ);
            List<Entity> list = entry.getValue();
            if (list.size() > cap)
            {
                int toRemove = list.size() - cap;
                Collections.shuffle(list);
                for (int i = 0; i < toRemove; i++)
                {
                    if (main.Global.configToggleAlertEntityLimit)
                    {
                        Component message = Component.text("§c■ " + "§eNamed Entity: §6" + type + " §ewas removed at§7: §a[" + x + ", " + y + ", " + z + "§a]" + " §c■")
                                .clickEvent(ClickEvent.copyToClipboard(x + " " + y + " " + z))
                                .hoverEvent(HoverEvent.showText(Component.text("§aClick to copy coordinates.")));

                        getServer().broadcast(message, "chunkShield.alerts");
                    }
                    list.get(i).remove();
                    main.Global.entitiesRemoved++;
                }
            }
        }
    }
}
