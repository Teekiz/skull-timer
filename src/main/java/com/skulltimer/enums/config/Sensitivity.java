package com.skulltimer.enums.config;

/**
 * An enum to determine how attacks from other players should be handled:
 *
 * <ul>
 *     <li> <b>HIGH</b>: Any animation (regardless of ID) that occurs either on the same tick as a player interaction
 *     or that has an associated hitsplat (based on the distance and weapon hit delay) will cause the player to be
 *     considered an attacker. This setting maximizes detection but may introduce false positives.</li>
 *
 *     <li> <b>MEDIUM</b>: When an animation occurs, the system will estimate when a hit should land based on distance
 *     and weapon hit delay. If a hitsplat appears on the expected tick, the player will be considered an attacker.</li>
 *
 *     <li> <b>LOW</b>: Works similarly to MEDIUM, but only animations that explicitly match the weapon’s attack animations
 *     will be processed. This setting minimizes false positives but may result in more false negatives.</li>
 * </ul>
 */
public enum Sensitivity
{
	LOW,
	MEDIUM,
	HIGH
}
