package de.timmyrs.suprdiscordbot.websocket;

import de.timmyrs.suprdiscordbot.Main;
import de.timmyrs.suprdiscordbot.apis.DiscordAPI;

public class WebSocketHeart extends Thread
{
	static boolean gotACK = true;
	static int interval;
	private static long lastHeartbeat = 0;

	public WebSocketHeart()
	{
		new Thread(this, "WebSocketHeart").start();
	}

	public void run()
	{
		//noinspection InfiniteLoopStatement
		do
		{
			if(interval != 0)
			{
				if(lastHeartbeat < System.currentTimeMillis() - interval)
				{
					if(gotACK)
					{
						gotACK = false;
						Main.discordAPI.send(1, WebSocket.lastSeq);
					}
					else
					{
						interval = 0;
						DiscordAPI.closeWebSocket("Discord did not answer heartbeat.");
					}
					lastHeartbeat = System.currentTimeMillis();
				}
			}
			try
			{
				Thread.sleep(200);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		while(true);
	}
}
