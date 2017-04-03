package de.timmyrs.suprdiscordbot;

import de.timmyrs.suprdiscordbot.apis.DiscordAPI;
import de.timmyrs.suprdiscordbot.scripts.ScriptManager;
import de.timmyrs.suprdiscordbot.scripts.ScriptWatcher;
import de.timmyrs.suprdiscordbot.websocket.WebSocketHeart;

import java.io.File;

/**
 * SuprDiscordBot
 *
 * @author timmyRS
 * @version 1.0
 * @see DiscordAPI
 * @see de.timmyrs.suprdiscordbot.apis.ScriptAPI
 * @see de.timmyrs.suprdiscordbot.apis.ConsoleAPI
 * @see de.timmyrs.suprdiscordbot.structures.Guild
 */
public class Main
{
	private static final File valuesDir = new File("values");
	private final static File confFile = new File("config.json");
	public static Configuration configuration;
	public static ScriptManager scriptManager;
	public static DiscordAPI discordAPI;
	public static boolean ready = false;

	public static void main(String[] args)
	{
		Main.scriptManager = new ScriptManager();
		Main.configuration = new Configuration(confFile);
		if(Main.configuration.has("botToken"))
		{
			new ScriptWatcher();
			Main.discordAPI = new DiscordAPI();
			new WebSocketHeart();
			DiscordAPI.getWebSocket();
			new RAMCleaner();
		} else
		{
			Main.configuration.set("botToken", "BOT TOKEN");
			System.out.println("[Setup] Please create a Discord Application at https://discordapp.com/developers/applications/me");
			System.out.println("[Setup] and add your bot token to config.json");
			System.out.println("[Setup] Make your bot join your server by navigating to https://discordapp.com/oauth2/authorize?client_id=CLIENT_ID&scope=bot&permissions=2146958463");
			System.out.println("[Setup] wherein you replace 'CLIENT_ID' with your App's Client ID.");
		}
	}

	public static Configuration getValuesConfig(String name)
	{
		if(!valuesDir.exists())
		{
			valuesDir.mkdir();
		}
		return new Configuration(new File(valuesDir.getAbsolutePath() + "/" + name + ".json"));
	}
}