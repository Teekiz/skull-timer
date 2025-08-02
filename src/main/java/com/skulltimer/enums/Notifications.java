package com.skulltimer.enums;

import lombok.Getter;

@Getter
public enum Notifications
{
	EXPIRING_SOON("Your skull icon will expire in one minute."),
	EXPIRED("Your skull icon has expired.");

	private final String message;

	Notifications(String message)
	{
		this.message = message;
	}
}
