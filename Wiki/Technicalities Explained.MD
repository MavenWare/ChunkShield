*This document is meant to be transparent on how our checks are performed.*


# Check 1 - 60 Second Chunk Scans
60 second scans are performed in case mobs or other entities such as boats move or get pushed into a chunk.

The first criteria checked is if any listed items are over limit, if not, the scan for that chunk is complete. 

This has near zero impact due to all following checks keeping things in balance. 

## You may ask, why not only do the chunks the players are in?
Scans could easily be bypassed if players send boats or mobs from a distance.


# Check 2 - Block Placement
Every time a block is placed, a check is made. 

The first criteria is if said block is illegal, 99% of the time, no.

This has no impact on performance due to simply there being no additional work to be done.


# Check 3 - Mob Spawning
Everytime a mob spawns, a check is made. 

Just like Check 2, the first criteria is checking the limit of mobs in said chunk is under. 

If so, the check is immediately done. 


# Check 4 - Boat/Minecart Placement
Just as before, no actual impact is done to the server except for blocking the placement if the limit is reached.

