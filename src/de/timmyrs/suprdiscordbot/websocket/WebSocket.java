package de.timmyrs.suprdiscordbot.websocket;

import com.google.gson.JsonObject;
import com.sun.istack.internal.Nullable;
import de.timmyrs.suprdiscordbot.Main;
import de.timmyrs.suprdiscordbot.RAMCleaner;
import de.timmyrs.suprdiscordbot.apis.DiscordAPI;
import de.timmyrs.suprdiscordbot.scripts.ScriptWatcher;
import de.timmyrs.suprdiscordbot.structures.*;

import java.net.URI;
import java.util.Arrays;

public class WebSocket
{
	public static JsonObject afterConnectSend;
	static int lastSeq;
	private static String session_id = "";

	public WebSocket(String url)
	{
		try
		{
			Main.webSocketEndpoint = new WebSocketEndpoint(new URI(url));
			Main.webSocketEndpoint.addMessageHandler(message->
			{
				JsonObject json = Main.jsonParser.parse(message).getAsJsonObject();
				GatewayPayload payload = Main.gson.fromJson(json, GatewayPayload.class);
				switch(payload.op)
				{
					default:
						if(Main.debug)
						{
							Main.log("Socket", "Unhandled Operation: " + json);
						}
						break;
					case 0:
						lastSeq = payload.s;
						User u;
						Guild g;
						Member m;
						Presence p;
						JsonObject data = json.get("d").getAsJsonObject();
						switch(payload.t)
						{
							default:
								if(Main.debug)
								{
									Main.log("Socket", "Unhandled Event " + payload.t + ": " + json.get("d").toString());
								}
								break;
							case "READY":
								JsonObject d = json.get("d").getAsJsonObject();
								session_id = d.get("session_id").getAsString();
								DiscordAPI.guilds.clear();
								Main.discordAPI.user = Main.gson.fromJson(d.get("user"), User.class);
								if(!Main.ready)
								{
									new ScriptWatcher();
									new RAMCleaner();
									Main.ready = true;
								}
							case "RESUMED":
								if(afterConnectSend != null)
								{
									send(afterConnectSend);
									afterConnectSend = null;
								}
								Main.scriptManager.fireEvent("CONNECTED");
								break;
							case "GUILD_CREATE":
								g = Main.gson.fromJson(data, Guild.class);
								for(Channel c : g.getChannels())
								{
									c.guild_id = g.id;
								}
								for(Member member : g.members)
								{
									member.guild_id = g.id;
								}
								for(Presence presence : g.presences)
								{
									presence.guild_id = g.id;
									presence.user = g.getMember(presence.user.id).user;
								}
								for(VoiceState vs : g.voice_states)
								{
									vs.guild_id = g.id;
								}
								DiscordAPI.guilds.add(g);
								Main.scriptManager.fireEvent("GUILD_CREATE", g);
								break;
							case "GUILD_DELETE":
								g = Main.discordAPI.getGuild(data.get("id").getAsString());
								DiscordAPI.guilds.remove(g);
								Main.scriptManager.fireEvent("GUILD_DELETE", g);
								break;
							case "GUILD_MEMBER_ADD":
								m = Main.gson.fromJson(data, Member.class);
								m.getGuild().addMember(m);
								Main.scriptManager.fireEvent("USER_JOIN", m);
								break;
							case "GUILD_MEMBER_REMOVE":
								p = Main.gson.fromJson(data, Presence.class);
								p.getGuild().removeMember(p.user.id);
								p.getGuild().removePresence(p.user.id);
								Main.scriptManager.fireEvent("USER_REMOVE", p);
								break;
							case "PRESENCE_UPDATE":
								g = Main.discordAPI.getGuild(data.get("guild_id").getAsString());
								p = Main.gson.fromJson(data, Presence.class);
								Presence cp = g.getPresence(p.user.id);
								m = g.getMember(p.user.id);
								if(cp == null)
								{
									if(!p.status.equals("offline"))
									{
										p.user = m.user;
										g.addPresence(p);
										Main.scriptManager.fireEvent("PRESENCE_GO_ONLINE", p);
									}
									break;
								}
								if(p.status.equals("offline"))
								{
									Main.scriptManager.fireEvent("PRESENCE_GO_OFFLINE", g.getPresence(p.user.id));
									g.removePresence(cp.user.id);
									break;
								}
								if(p.user.username == null || p.user.discriminator == null || p.user.avatar == null)
								{
									p.user = cp.user;
								}
								else
								{
									cp.user = p.user;
								}
								if(!p.status.equals(cp.status))
								{
									Main.scriptManager.fireEvent("PRESENCE_UPDATE_STATUS", new Object[]{p, cp.status});
									cp.status = p.status;
								}
								if(cp.game == null)
								{
									if(p.game != null)
									{
										Main.scriptManager.fireEvent("PRESENCE_UPDATE_GAME", new Object[]{p, null});
										cp.game = p.game;
									}
								}
								else
								{
									if(p.game == null)
									{
										Main.scriptManager.fireEvent("PRESENCE_UPDATE_GAME", new Object[]{p, cp.game});
										p.game = null;
									}
									else if(!cp.game.name.equals(p.game.name))
									{
										Main.scriptManager.fireEvent("PRESENCE_UPDATE_GAME", new Object[]{p, cp.game});
										cp.game = p.game;
									}
								}
								if(!cp.user.username.equals(p.user.username) || !cp.user.username.equals(p.user.discriminator))
								{
									Main.scriptManager.fireEvent("PRESENCE_UPDATE_USER", new Object[]{p, cp.user});
									p.user = cp.user;
								}
								g.addPresence(cp);
								break;
							case "GUILD_MEMBER_UPDATE":
								m = Main.gson.fromJson(data, Member.class);
								g = m.getGuild();
								Member cm = m.getGuild().getMember(m.user.id);
								if(cm.nick == null)
								{
									if(m.nick != null)
									{
										Main.scriptManager.fireEvent("MEMBER_UPDATE_NICK", new Object[]{m, null});
										cm.nick = m.nick;
									}
								}
								else
								{
									if(m.nick == null)
									{
										Main.scriptManager.fireEvent("MEMBER_UPDATE_NICK", new Object[]{m, cm.nick});
										cm.nick = null;
									}
									else if(!cm.nick.equals(m.nick))
									{
										Main.scriptManager.fireEvent("MEMBER_UPDATE_NICK", new Object[]{m, cm.nick});
										cm.nick = m.nick;
									}
								}
								if(!Arrays.equals(cm.roles, m.roles))
								{
									Main.scriptManager.fireEvent("MEMBER_UPDATE_ROLES", new Object[]{m, cm.roles});
								}
								g.addMember(m);
								break;
							case "TYPING_START":
								Channel c = Main.discordAPI.getChannel(data.get("channel_id").getAsString());
								if(c.type == 1)
								{
									u = c.recipients[0];
								}
								else if(c.type == 3)
								{
									u = Main.discordAPI.getUser(data.get("user_id").getAsString());
								}
								else
								{
									u = c.getGuild().getMember(data.get("user_id").getAsString()).user;
								}
								Main.scriptManager.fireEvent("TYPING_START", new Object[]{c, u});
								break;
							case "CHANNEL_UPDATE":
								g = Main.discordAPI.getGuild(data.get("guild_id").getAsString());
								c = Main.gson.fromJson(data, Channel.class);
								Channel cc = g.getChannel(c.id);
								if(!cc.getName().equals(c.getName()))
								{
									cc.name = c.name;
									Main.scriptManager.fireEvent("CHANNEL_UPDATE_NAME", new Object[]{c, cc.getName()});
								}
								if(c.topic != null && !cc.topic.equals(c.topic))
								{
									cc.topic = c.topic;
									Main.scriptManager.fireEvent("CHANNEL_UPDATE_TOPIC", new Object[]{c, cc.topic});
								}
								if(cc.position != c.position)
								{
									cc.position = c.position;
									Main.scriptManager.fireEvent("CHANNEL_UPDATE_POSITION", new Object[]{c, cc.position});
								}
								if(c.permission_overwrites != null && !Arrays.equals(cc.permission_overwrites, c.permission_overwrites))
								{
									cc.permission_overwrites = c.permission_overwrites;
									Main.scriptManager.fireEvent("CHANNEL_UPDATE_OVERWRITES", new Object[]{c, cc.permission_overwrites});
								}
								g.addChannel(cc);
								break;
							case "MESSAGE_CREATE":
								Message msg = Main.gson.fromJson(data, Message.class);
								c = msg.getChannel();
								if(c.isPartOfGuild())
								{
									g = c.getGuild();
									c.last_message_id = msg.id;
									g.addChannel(c);
								}
							case "MESSAGE_UPDATE":
							case "MESSAGE_DELETE":
								Main.scriptManager.fireEvent(payload.t, Main.gson.fromJson(data, Message.class));
								break;
						}
						break;
					case 7:
						DiscordAPI.closeWebSocket("Gateway requested reconnect.");
						DiscordAPI.getWebSocket();
						break;
					case 9:
						DiscordAPI.closeWebSocket("Resume failed.");
						DiscordAPI.getWebSocket();
						break;
					case 10:
						JsonObject d = new JsonObject();
						if(session_id.equals(""))
						{
							d.addProperty("token", Main.configuration.getString("botToken"));
							JsonObject properties = new JsonObject();
							properties.addProperty("$os", "linux");
							properties.addProperty("$browser", "SuprDiscordBot");
							properties.addProperty("$device", "SuprDiscordBot");
							properties.addProperty("$referrer", "");
							properties.addProperty("$referring_domain", "");
							d.add("properties", properties);
							d.addProperty("compress", false);
							d.addProperty("large_threshold", 50);
							d.add("shard", Main.jsonParser.parse("[0,1]").getAsJsonArray());
							Main.discordAPI.send(2, d);
						}
						else
						{
							d.addProperty("token", Main.configuration.getString("botToken"));
							d.addProperty("session_id", session_id);
							d.addProperty("seq", lastSeq);
							Main.discordAPI.send(6, d);
						}
						WebSocketHeart.interval = Main.jsonParser.parse(payload.d.toString()).getAsJsonObject().get("heartbeat_interval").getAsInt();
						break;
					case 11:
						WebSocketHeart.gotACK = true;
						break;
				}
			});
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void send(JsonObject json)
	{
		Main.webSocketEndpoint.send(json.toString());
	}

	public void close(@Nullable String reason)
	{
		try
		{
			if(reason != null)
			{
				Main.log("Socket", "Manually closing - " + reason);
			}
			if(Main.webSocketEndpoint.userSession != null && Main.webSocketEndpoint.userSession.isOpen())
			{
				Main.webSocketEndpoint.userSession.close();
			}
			if(reason == null)
			{
				Main.webSocketEndpoint = null;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
