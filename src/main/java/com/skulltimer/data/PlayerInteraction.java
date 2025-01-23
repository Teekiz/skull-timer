package com.skulltimer.data;

import lombok.Data;

@Data
public class PlayerInteraction
{
	private static final int defaultTickValue = -1;
	private int tickNumberOfLastAnimation = defaultTickValue;
	private int tickNumberOfLastInteraction = defaultTickValue;

	public void setAnimationTick(int tick){
		if (tick >= 0){
			tickNumberOfLastAnimation = tick;
		}
	}

	public void setInteractionTick(int tick){
		if (tick >= 0){
			tickNumberOfLastInteraction = tick;
		}
	}
}
