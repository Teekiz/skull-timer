# Runelite Skulled Timer Plugin ![image](https://img.shields.io/endpoint?url=https://api.runelite.net/pluginhub/shields/rank/plugin/emblem-trader-skull-timer) ![image](https://img.shields.io/endpoint?url=https://api.runelite.net/pluginhub/shields/installs/plugin/emblem-trader-skull-timer)


 ![image](https://github.com/Teekiz/skull-timer/blob/master/readmeimages/timer.png) <br>
This plugin tracks the players skull duration and status. 

This is particularly useful for ironmen and lower-level main accounts aiming to maximize drop rates in Revenant Caves or for tracking the duration of the skull status in PvP scenarios.

## Table of contents
1. [Features](#features)
2. [How it works](#how-it-works)
3. [Configuration](#configuration)
4. [Feedback](#feedback)
5. [Credits](#credits)
6. [Changelog](#changelog)

## Features
- Real-time Skull Status Timer
- PvP Combat Detection
- Equipment Monitoring
- Abyss Teleport Handling
- Emblem Trader Interactions

## How it works

1. Receiving a Skull: The plugin detects when the player gets a skull icon and starts a timer accordingly.

2. Timer Visibility: A skull countdown timer is displayed in an infobox.

3. Automatic Updates: The timer adjusts based on interactions with NPCs and other players.

4. Expiration & Removal: The timer is removed when the skull expires or the player dies.

## Configuration

<details>
<summary>Text and Warning Text Colours:</summary>
  
![image](https://github.com/Teekiz/skull-timer/blob/master/readmeimages/settings1.png) <br>
This will change the colour of the text on the timer. When the timer has 30 seconds remaining or less, it will use the warning text colour. 
</details>

<details>
<summary>Enable PVP timer:</summary>
  
![image](https://github.com/Teekiz/skull-timer/blob/master/readmeimages/settings2.PNG) <br>
If checked, whenever you engage in a PVP encounter where you receive a skull icon, a timer will start. This timer may not be 100% accurate in some scenarios.
</details>

## Feedback

If you have encountered any bugs, please create an issue [here](https://github.com/Teekiz/skull-timer/issues/new) with all relevent details. Any feedback or feature requests are also welcome!

## Credits

- [juusokarjanlahti](https://github.com/juusokarjanlahti) - Original equipment timer feature, development support (including code readability and feature suggestions/improvements) and improved README documentation.
- [Old School Runescape Wiki](https://oldschool.runescape.wiki/) - [Source](https://oldschool.runescape.wiki/w/File:Skull_(status)_icon.png) of the image used for the plugins icon and a useful source of information which aided the develop this plugin (primarily [Skull (status)](https://oldschool.runescape.wiki/w/Skull_(status)) and [weapons](https://oldschool.runescape.wiki/w/Weapons) pages).

## Changelog
<details>
<summary>Version 2.0:</summary> 
Renamed plugin from 'Emblem Trader Skull Timer' to 'Skulled Timer'.<br>
Added timer for other skulled status sources, including: <br>
- Attacking another player unprovoked. <br>
- Entering the Abyss without an Abyssal bracelet. <br>
- (Un)equipping items that provide a skull icon such as the Amulet of Avarice and Cape of skulls. <br>
Added new configuration options: <br>
- Enable pvp timer.
</details>

<details>
<summary>Version 1.1 (09/10/2024):</summary>
Compatibility update. <br>
Minor code and logging changes.
</details> 

<details>
<summary>Version 1.0 (25/07/2023):</summary>
Plugin release. <br>
Added skull timer for when the player interacts with the emblem trader and receives a skull icon. <br>
Added new configuration options: <br>
- Text colour. <br>
- Warning text colour.
</details>


