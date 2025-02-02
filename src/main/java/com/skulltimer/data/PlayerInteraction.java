/*
 * Copyright (c) 2023, Callum Rossiter
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.skulltimer.data;

import lombok.Data;

/**
 * An object representing a record of a players interaction with the local player. An interaction could be considered an animation or interaction.
 */
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

	/**
	 * A method used to determine if an interaction has occurred on the same tick.
	 * @return {@code true} if the animation and interaction occur on the same tick number and the tick value set is not -1.
	 * Returns {@code false} if one or more of those conditions are not met.
	 */
	public boolean hasInteractionAndAnimationOccurredOnTheSameTick()
	{
		return tickNumberOfLastInteraction != -1
			&& tickNumberOfLastAnimation != -1
			&& tickNumberOfLastInteraction == tickNumberOfLastAnimation;
	}
}
