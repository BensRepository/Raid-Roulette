# Raid Randomizer 

A fun plugin that randomizes your next raid to take on in Old School RuneScape.                  


![preview](https://github.com/user-attachments/assets/5d09af49-a6e6-4b79-a19b-07815ca58f95)


------

## Getting started
1. Enable the plugin in the plugin list

2. Configure settings (raids, sync, speed, sounds)

3. In chat, type: ```!raid```

4. Gear up and take on the suggested raid!

## Config

- Select your Raids
- Spin speed presets (Fast / Medium / Slow)
- Sound effects (optional)
- Sync results (explained below)

------

## Sync Mode Explained

You must enable the sync toggle in the plugin configuration if you want results to be shared across players. They must also have the plugin installed to see results in RuneLite.

![sync](https://github.com/user-attachments/assets/0334ce8e-b193-4a7c-ac58-26915c69dd32)

When **Sync Results** is enabled:

- The plugin uses a deterministic UTC time buckets (4 seconds) 
- All players with sync enabled will roll the same result in that window. There is a small chance results may fall outside the bucket depending on timing.
- If the result is identical to a previous roll, wait a bit before rolling again to allow time for a new bucket.
- All available raids are considered in the roll when sync is active to ensure config selections wont skew results between players.

**Sync Troubleshooting**

If the sync isn’t working properly for you and your friends, enable the **Chat Time Stamps** plugin and set the format to **[HH:mm:ss:ms]**.

Compare the timestamps between players — if they differ significantly, run a **Windows time sync** from **Date & Time Settings** to ensure your system clocks are aligned.

Author
------
**ArtilleryRS**

Huge thanks to **Xartu** for their help with testing

------
License
-------
Raid Randomizer is licensed under the BSD 2-Clause License License. See LICENSE for details.


