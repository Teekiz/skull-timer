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
	public void EmblemTraderDialogue()
	{
		when(chatMessage.getMessage()).thenReturn("Your PK skull will now last for the full 20 minutes.");
		eventBus.post(chatMessage);
		verify(timerManager).addTimer(TimerDurations.TRADER_AND_ITEM_DURATION.getDuration(), false);
	}
}
