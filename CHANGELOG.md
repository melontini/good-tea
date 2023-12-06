## What's new:

* [NEW] Data pack support!
  * You can now add custom behaviors with commands using data packs!
  * Example:

Create a file in `good_tea/behaviors`.

```json
{
  "item_id": "minecraft:nether_star", //can be an array. []
  "disabled": false //disables any behavior. False by default.
  "complement": true //Should this behavior complement or replace others. True by default.
  "user_commands": [ //Any commands executed on user. Supports @s and @p
    "/kill @s"
  ],
  "server_commands": [ //Executed as server. No user or pos context.
    
  ]
}
```

* Item group items are sorted by rawId now.
* Teas with no behavior will fall back to item's default behavior. None for most, eating for food.
* Updated Dark Matter.