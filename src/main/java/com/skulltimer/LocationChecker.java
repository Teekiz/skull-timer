package com.skulltimer;

import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Varbits;

public class LocationChecker
{
	@Inject
	private final Client client;

	public LocationChecker(Client client)
	{
		this.client = client;
	}

	public boolean isInWilderness(){
		return client.getVarbitValue(Varbits.IN_WILDERNESS) == 1;
	}
}
