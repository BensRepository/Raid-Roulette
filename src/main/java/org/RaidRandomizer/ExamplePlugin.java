package org.RaidRandomizer;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MessageNode;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@PluginDescriptor(
		name = "Raid Randomizer"
)
public class ExamplePlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ExampleConfig config;

	@Inject
	private RaidIconManager raidIconManager;

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			log.info("Game logged in → loading raid icons");
			raidIconManager.load();
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() != ChatMessageType.PUBLICCHAT)
		{
			return;
		}

		String message = event.getMessage();
		if (message == null || !message.equalsIgnoreCase("!raid"))
		{
			return;
		}

		String result = rollRaid();
		if (result == null)
		{
			return;
		}

		MessageNode node = event.getMessageNode();
		node.setRuneLiteFormatMessage(result);
		client.refreshChat();
	}

	/**
	 * UTC epoch seconds from system clock (no API).
	 */
	private long getUtcEpoch()
	{
		return Instant.now().getEpochSecond();
	}

	/**
	 * Deterministic raid selection using 2-second UTC bucket.
	 */
	private String rollRaid()
	{
		long utc = getUtcEpoch();
		if (utc < 0)
		{
			return "<col=ffffff>Time unavailable</col>";
		}

		// 2-second bucket shared across all clients with synced clocks
		long bucket = utc / 2;

		int seed = Long.hashCode(bucket);
		Random random = new Random(seed);

		List<String> allRaids = new ArrayList<>();
		allRaids.add("COX");
		allRaids.add("TOB");
		allRaids.add("TOA");

		String selected = allRaids.get(random.nextInt(allRaids.size()));

		switch (selected)
		{
			case "COX":
				return config.enableCox()
						? "<img=" + raidIconManager.getCoxChatIndex() + "> <col=00ff00>Chambers of Xeric</col>"
						: "<col=ffffff>Chambers of Xeric (disabled)</col>";

			case "TOB":
				return config.enableTob()
						? "<img=" + raidIconManager.getTobChatIndex() + "> <col=ff0000>Theatre of Blood</col>"
						: "<col=ffffff>Theatre of Blood (disabled)</col>";

			case "TOA":
			default:
				return config.enableToa()
						? "<img=" + raidIconManager.getToaChatIndex() + "> <col=cc6600>Tombs of Amascut</col>"
						: "<col=ffffff>Tombs of Amascut (disabled)</col>";
		}
	}

	@Provides
	ExampleConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ExampleConfig.class);
	}
}