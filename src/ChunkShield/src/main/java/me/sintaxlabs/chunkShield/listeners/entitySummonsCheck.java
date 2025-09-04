package me.sintaxlabs.chunkShield.listeners;

import me.sintaxlabs.chunkShield.main;
import net.kyori.adventure.text.Component;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static org.bukkit.Bukkit.getServer;

public final class entitySummonsCheck implements Listener
{
    ////////////////////////////////////////////////////////////////////////////
    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent e)
    {
        if (main.Global.configToggleEntityCheck)
        {
            if (main.Global.configToggleEntityCheck_50)
            {
                if (ThreadLocalRandom.current().nextBoolean())
                {entityCheck(e);}
            }
            else entityCheck(e);
        }
    }

    /////////////////////////////////////////////////////////////////////////////
    private static void entityCheck(EntitySpawnEvent e)
    {
        Entity entity1 = e.getEntity();
        Chunk chunk = entity1.getLocation().getChunk();




        //Stopper 3: Did the owner clear their lists?
        if (main.Global.theEntityLimits.isEmpty() & main.Global.theNamedEntityLimits.isEmpty()) return;

        // ===== ENTITIES ===== //
        Map<EntityType, List<Entity>> namedMap   = new HashMap<>();
        Map<EntityType, List<Entity>> unnamedMap = new HashMap<>();

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
            EntityType type1 = entry.getKey();
            Integer noNameOBJ = main.Global.theEntityLimits.get(type1);
            if (noNameOBJ == null) continue;
            int limit = Math.max(0, noNameOBJ);
            List<Entity> list = entry.getValue();
            if (list.size() > limit)
            {
                int toRemove = list.size() - limit;
                Collections.shuffle(list); // optional fairness
                for (int i = 0; i < toRemove; i++) list.get(i).remove(); getServer().broadcast(Component.text("§eChunkshield§7: §6Removed an entity: §e" + type1) , "chunkShield.verbose");
            }
        }

        // Named Limits
        for (Map.Entry<EntityType, List<Entity>> entry : namedMap.entrySet())
        {
            EntityType type2 = entry.getKey();
            Integer namedOBJ = main.Global.theNamedEntityLimits.get(type2);
            if (namedOBJ == null) continue;
            int cap = Math.max(0, namedOBJ);
            List<Entity> list = entry.getValue();
            if (list.size() > cap)
            {
                int toRemove = list.size() - cap;
                Collections.shuffle(list);
                for (int i = 0; i < toRemove; i++) list.get(i).remove(); getServer().broadcast(Component.text("§eChunkshield§7: §6Removed a named entity: §e" + type2) , "chunkShield.verbose");
            }
        }
    }
}
