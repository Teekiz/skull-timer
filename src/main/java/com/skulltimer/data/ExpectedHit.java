package com.skulltimer.data;

import com.skulltimer.enums.equipment.AttackType;

import java.util.Objects;
import lombok.Data;

@Data
public class ExpectedHit
{
	private final String playerName;
	private final AttackType attackType;

	/**
	 * A boolean to check if the weapon type used to attack
	 * @return {@code true} if the attack type can splash (i.e. magic). Otherwise, returns {@code false}.
	 */
	public boolean doesApplySplash()
	{
		return attackType == AttackType.MAGIC;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ExpectedHit that = (ExpectedHit) o;
		return Objects.equals(playerName, that.playerName);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(playerName);
	}
}
