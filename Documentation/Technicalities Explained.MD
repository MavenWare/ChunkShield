*This document is meant to be transparent on how our checks are performed.*


# Measure 1 - Chunk Scan Upon Loading
When a chunk is loaded, it has to meet a criteria before getting scanned for entities.
- 1. Is it Enabled?
- 2. Are there any entities at all?
- 3. Are there the minimum amount of entities set by the player met?
- 4. Are the entity lists in the config empty?
- 5. Finally, are the listed entities even in the chunk?
Once we can confirm these five criteria are met then the chunk will be scanned. 


# Measure 2 - Block Placement
Every time a block is placed, a check is made. 

The first criteria is if said block is illegal, 99% of the time, no.

This has no impact on performance due to simply there being no additional work to be done.


# Measure 3 - Mob Spawning
Every time a mob spawns, a check is made. 

Just like Check 2, the first criteria is checking the limit of mobs in said chunk is under. 

If so, the check is immediately done. 


# Measure 4 - Boat/Minecart Placement
Just as before, no actual impact is done to the server except for blocking the placement if the limit is reached.

