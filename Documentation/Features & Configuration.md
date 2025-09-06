# ChunkShield Features & Configuration
You can find all information for what ChunkShield offers here.


# Features
ChunkShield is a drop and forget style of protection that is meant to prevent users from spamming boats, minecarts, entities, and blocks of your choice.

This can prevent users from trashing an area or attempting to crash your server via snowballs or enchanting table towers.

For more information on how ChunkShield operates, check the [Technicalities Explained Doc.](https://github.com/SintaxLabs/ChunkShield/blob/main/Documentation/Technicalities%20Explained.MD)

## Chunk Limitations
You can simply set limits for any entity or block of your choice within a chunk.

Once conditions are met, ChunkShield will naturally break the block if a limit is met or remove the entity.

## Vehicle Radius Limitations
By default, there can be no more than 20 boats and minecarts altogether within a 32 block radius.

# Configuration

- MinimumEntityRequirement: 10
> This many entities have to be within a chunk for a scan to be made for a chunk loaded or misc event.

- Scan-Chunks-Upon-Loading: true
- Scan-Chunks-Upon-Loading-50%: false
> Togglable. Chunks have to be meet 6 criteria to be scanned upon getting loaded. You can cut this down by 50% chance if you run a very large server.

- BlockCheck: true
- BlockCheck-50%: false
> Togglable. Every time a player places a block, the chunk will meet a couple of criteria before making sure the block placed is okay to be done.

- EntityCheck: true
- EntityCheck-50%: false
> Togglable. When an entity spawns via a mob or projectile shot, a check is made to ensure limitation once a few criteria are met.

- Scan-Chunk-Upon-Crafting: false
- Scan-Chunk-Upon-Entity-Dying: false
- Scan-Chunk-Upon-Opening-Container: false
> 10% Misc events that can trigger the same 6 criteria chunk scan. These are great for some extra measures if you have players plotting or being extra careful.

- VehicleRadiusCheck: true
> As discussed above, this toggles whether boats and minecarts will get limited within a radius if the amount is reached.

- end-portal-fix: true
> This blocks End Portal Frames by default and also allows users to still place ender eyes in the frame without getting blocked.
> 
> Recommend never disabling this as there is no need to.

- ChunkScanned: true
- BlockLimits: true
- EntityLimits: true
- VehicleLimits: true
> Alert toggles for those with the alert permission.

- ChunkDoorLimit: 40
- ChunkVehicleLimit: 20
- vehicle-radius: 32
> Doors and Boats/Minecarts are hardcoded to all be bundled as one in their own categories.
> This is simply a chunk limit for both categories. Cuts down your lists and is future proof. (Nice try, Bamboo Rafts)

## Block Limits / Entity Limits / Named Entity Limits
Simply list your item in their respective category.

Please make sure you're aware of what goes where.
> Chests are blocks, Armor Stands are entities, Redstone Wire is technically a block.
> 
> ETC
