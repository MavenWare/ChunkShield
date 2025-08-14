*This document is meant to be transparent on how our checks are performed.*


# Check 1 - 60 Second Chunk Scans
60 Second Scans are done due to mobs or items wondering into a chunk. 
Such as moving mobs or boats into chunk to bypass the limtations. 
Every 60 seconds, loaded chunks are scanned for any listed items.
The first criteria is if any listed items are over limit, if not, the check is done. 
This has near zero impact due to all following checks keeping things in balance. 
You may ask, why not only do the chunks the players are in?
Players are likely not in the chunk that the excess may be taking place. 
This could easily be exploited if players sent boats or mobs down via water from a distance.


# Check 2 - Block Placement
Every time a block is placed, a check is made. 
The first criteria is if said block is illegal, 99% of the time, no.
This has no impact on performance due to simply there being no additional work to be done.


# Check 3 - Mob Spawning
Everytime a mob spawns, a check is made. 
Just like Check 2, the first criteria is checking the limit of mobs in said chunk is under. 
If so, the check is immediately done. 


# Check 4 - Boat/Minecart Placement
Just as before, no actual impact is done to the server excepting blocking the placement. 

