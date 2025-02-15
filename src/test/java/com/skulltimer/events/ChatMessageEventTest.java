package com.skulltimer.events;

import com.skulltimer.enums.TimerDurations;
import com.skulltimer.mocks.PluginMocks;
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ChatMessageEventTest extends PluginMocks
{
	@Mock
	ChatMessage chatMessage;

	@BeforeEach
	public void startUp() throws NoSuchFieldException
	{
		super.startUp();
		when(chatMessage.getType()).thenReturn(ChatMessageType.MESBOX);
	}

	@Test
	public void EmblemTraderDialogue_Standard()
	{
		when(chatMessage.getMessage()).thenReturn("You are now skulled.");
		eventBus.post(chatMessage);
		verify(timerManager, times(1)).addTimer(TimerDurations.TRADER_AND_ITEM_DURATION.getDuration());
	}

	@Test
	public void EmblemTraderDialogue_Extended()
	{
		when(chatMessage.getMessage()).thenReturn("Your PK skull will now last for the full 20 minutes.");
		eventBus.post(chatMessage);
		verify(timerManager, times(1)).addTimer(TimerDurations.TRADER_AND_ITEM_DURATION.getDuration());
	}

	@Test
	public void MessageEvent_UnrelatedMessage()
	{
		when(chatMessage.getMessage()).thenReturn("A test message");
		eventBus.post(chatMessage);
		verify(timerManager, times(0)).addTimer(TimerDurations.TRADER_AND_ITEM_DURATION.getDuration());
	}

	@Test
	public void MessageEvent_UnrelatedMessageType()
	{
		when(chatMessage.getType()).thenReturn(ChatMessageType.UNKNOWN);
		eventBus.post(chatMessage);
		verify(timerManager, times(0)).addTimer(TimerDurations.TRADER_AND_ITEM_DURATION.getDuration());
	}
}
