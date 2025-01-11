package com.skulltimer.managers;

import com.skulltimer.enums.WorldAreas;
import javax.inject.Inject;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.Varbits;
import net.runelite.api.coords.WorldPoint;

/**
 * A class that is used to check a players world location to identify if a skull timer is required.
 */
public class LocationManager
{
	@Inject
	private final Client client;
	@Setter
	private boolean hasBeenTeleportedIntoAbyss = false;

	/**
	 * The constructor for a {@link LocationManager} object.
	 * @param client Runelite's {@link Client} object.
	 */
	public LocationManager(Client client)
	{
		this.client = client;
	}

	/**
	 * A method used to check if the player has been teleported into the abyss.
	 * @return {@code true} if the player has been previously teleported and is now in the abyss, {@code false} if not.
	 */
	public boolean isInAbyss()
	{
		if (isInArea(WorldAreas.ABYSS.getX(), WorldAreas.ABYSS.getY(), getPlayersLocation()) && hasBeenTeleportedIntoAbyss){
			hasBeenTeleportedIntoAbyss = false;
			return true;
		} else {
			hasBeenTeleportedIntoAbyss = false;
			return false;
		}
	}

	/**
	 * A method to check to see if the current world point is within the given range.
	 * @param worldPointA The first world point.
	 * @param worldPointB The second world point.
	 * @param currentWorldPoint The world point to be checked.
	 * @return {@code true} if the {@code currentWorldPoint} is within the specified range. {@code false} if null or not within the given location.
	 */
	private boolean isInArea(WorldPoint worldPointA, WorldPoint worldPointB, WorldPoint currentWorldPoint)
	{
		if (worldPointA == null || worldPointB == null || currentWorldPoint == null)
		{
			return false;
		}

		return checkCoordinates(worldPointA.getX(), worldPointB.getX(), currentWorldPoint.getX())
			&& checkCoordinates(worldPointA.getY(), worldPointB.getY(), currentWorldPoint.getY())
			&& checkCoordinates(worldPointA.getPlane(), worldPointB.getPlane(), currentWorldPoint.getPlane());
	}

	/**
	 * A method to check to see if the current coordinate is within the given range.
	 * @param a The first coordinate.
	 * @param b The second coordinate.
	 * @param c The coordinate to be checked.
	 * @return {@code true} if the c coordinate is within the specified range. {@code false} if not.
	 */
	private boolean checkCoordinates(int a, int b, int c)
	{
		int lowest = Math.min(a, b);
		int largest = Math.max(a, b);
		return c >= lowest && c <= largest;
	}

	/**
	 * A method to get the {@link Player}'s location.
	 * @return The {@link Player}'s {@link WorldPoint} if the player is not null. Otherwise, returns {@code null}.
	 */
	private WorldPoint getPlayersLocation()
	{
		Player player = client.getLocalPlayer();

		if (player != null)
		{
			return player.getWorldLocation();
		}

		return null;
	}

	/**
	 * A method used to check if the player is in the wilderness.
	 * @return {@code true} if the player is in the wilderness, {@code false} if not.
	 */
	public boolean isInWilderness(){
		return client.getVarbitValue(Varbits.IN_WILDERNESS) == 1;
	}
}
