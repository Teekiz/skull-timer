package com.skulltimer.enums;

import java.time.Duration;
import lombok.Getter;

/**
 * An enumeration of skull events that provide different timers.
 */
@Getter
public enum TimerDurations
{
	TRADER_AND_ITEM_DURATION(Duration.ofMinutes(20)),
	ABYSS_DURATION(Duration.ofMinutes(10)),
	PVP_DURATION(Duration.ofMinutes(20));

	private final Duration duration;

	TimerDurations(Duration duration){
		this.duration = duration;
	}
}
