## What's new:

This is a major data pack update.

* Support placement is now determined automatically.
* Kettle warm block states are now placed in `good-tea/kettle_block_states`. Example:
```json
{
  "block": "minecraft:campfire",
  "predicate": {
    "lit": "true"
  }
}
```
* Custom behaviors now require [Commander](https://constellation-mc.github.io/commander/). Example event declaration:
```json
{
  "event": "good-tea:drank_tea",
  "parameters": {
    "items": [
      "minecraft:diamond"
    ]
  },
  "commands": [
    ...
  ]
}
```