package org.RaidRandomizer;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MessageNode;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@PluginDescriptor(name = "Raid Randomizer")
public class ExamplePlugin extends Plugin
{
	@Inject private Client client;
	@Inject private ClientThread clientThread;
	@Inject private ScheduledExecutorService executor;
	@Inject private ExampleConfig config;
	@Inject private RaidIconManager raidIconManager;

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			raidIconManager.load();
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		ChatMessageType type = event.getType();

		if (type != ChatMessageType.PUBLICCHAT &&
				type != ChatMessageType.CLAN_CHAT &&
				type != ChatMessageType.FRIENDSCHAT &&
				type != ChatMessageType.PRIVATECHAT &&
				type != ChatMessageType.PRIVATECHATOUT)
		{
			return;
		}

		if (!"!raid".equalsIgnoreCase(event.getMessage()))
			return;

		MessageNode node = event.getMessageNode();
		List<String> available = getAvailableRaids();

		if (available.isEmpty())
		{
			node.setRuneLiteFormatMessage("<col=ffffff>No raids enabled</col>");
			client.refreshChat();
			return;
		}

		// capture deterministic bucket for THIS spin
		long pendingBucket = getUtcEpoch() / 4;

		int totalSpins = 28;
		long accumulatedDelay = 0;

		for (int i = 0; i < totalSpins; i++)
		{
			int base = config.spinSpeed() == ExampleConfig.SpinSpeed.FAST ? 5
					: config.spinSpeed() == ExampleConfig.SpinSpeed.SLOW ? 70
					: 35;

			double progress = (double) i / totalSpins;
			long delayStep = (long) (base + (progress * base * 3));

			accumulatedDelay += delayStep;

			int index = i;
			long scheduledTime = accumulatedDelay;

			executor.schedule(() ->
							clientThread.invoke(() ->
							{
								String name = available.get(index % available.size());

								node.setRuneLiteFormatMessage(
										"<col=ffff00>🎰 " + name + "</col>"
								);
								client.refreshChat();

								if (config.enableSounds())
								{
									client.playSoundEffect(227);
								}
							}),
					scheduledTime,
					TimeUnit.MILLISECONDS
			);
		}

		// near miss
		accumulatedDelay += 300;
		executor.schedule(() ->
						clientThread.invoke(() ->
						{
							node.setRuneLiteFormatMessage(
									"<col=ff0000>🎰 " + available.get(0) + "</col>"
							);
							client.refreshChat();
						}),
				accumulatedDelay,
				TimeUnit.MILLISECONDS
		);

		// final reveal (deterministic, independent)
		accumulatedDelay += 600;
		executor.schedule(() ->
						clientThread.invoke(() ->
						{
							String result = rollRaid(pendingBucket);

							if (config.enableSounds())
							{
								client.playSoundEffect(199);
							}

							node.setRuneLiteFormatMessage("<col=00ff00>" + result + "</col>");
							client.refreshChat();
						}),
				accumulatedDelay,
				TimeUnit.MILLISECONDS
		);
	}

	private List<String> getAvailableRaids()
	{
		List<String> list = new ArrayList<>();

		if (config.useUtcSync())
		{
			list.add("Chambers of Xeric");
			list.add("Theatre of Blood");
			list.add("Tombs of Amascut");
		}
		else
		{
			if (config.enableCox()) list.add("Chambers of Xeric");
			if (config.enableTob()) list.add("Theatre of Blood");
			if (config.enableToa()) list.add("Tombs of Amascut");
		}

		return list;
	}

	private long getUtcEpoch()
	{
		return Instant.now().getEpochSecond();
	}

	/**
	 * Deterministic raid selection using captured 4-second bucket.

	 */
	private String rollRaid(long bucket)
	{
		List<String> pool = config.useUtcSync()
				? Arrays.asList("Chambers of Xeric", "Theatre of Blood", "Tombs of Amascut")
				: getAvailableRaids();

		if (pool.isEmpty())
		{
			return "<col=ffffff>No raids enabled</col>";
		}

		Random random = new Random(bucket);

		String selected = pool.get(random.nextInt(pool.size()));

		switch (selected)
		{
			case "Chambers of Xeric":
				return "<img=" + raidIconManager.getCoxChatIndex() + "> <col=00ff00>Chambers of Xeric</col>";

			case "Theatre of Blood":
				return "<img=" + raidIconManager.getTobChatIndex() + "> <col=ff0000>Theatre of Blood</col>";

			default:
				return "<img=" + raidIconManager.getToaChatIndex() + "> <col=cc6600>Tombs of Amascut</col>";
		}
	}

	@Provides
	ExampleConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ExampleConfig.class);
	}
}