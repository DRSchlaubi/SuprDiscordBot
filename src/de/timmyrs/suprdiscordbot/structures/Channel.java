package de.timmyrs.suprdiscordbot.structures;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.timmyrs.suprdiscordbot.Configuration;
import de.timmyrs.suprdiscordbot.Main;
import de.timmyrs.suprdiscordbot.apis.DiscordAPI;

/**
 * Channel Structure.
 * You can retrieve an array of channel structures using {@link Guild#getChannels()} and {@link DiscordAPI#getDMs()}.
 *
 * @author timmyRS
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class Channel extends Structure
{
	/**
	 * The ID of this channel.
	 */
	public String id;
	/**
	 * Name of the channel.
	 *
	 * @see Channel#getName()
	 */
	public String name;
	/**
	 * What type of Channel this is:
	 * <ul>
	 * <li>0 = Guild Text Channel</li>
	 * <li>1 = DM Channel</li>
	 * <li>2 = Guild Voice Channel</li>
	 * <li>3 = Group DM Channel</li>
	 * <li>4 = Guild Category</li>
	 * </ul>
	 *
	 * @see Channel#isPartOfGuild()
	 * @since 1.2
	 */
	public int type;
	/**
	 * The sorting position of this channel.
	 */
	public int position;

	/**
	 * An array of {@link Override} objects
	 */
	public Overwrite[] permission_overwrites;
	/**
	 * The {@link User} object of the recipient. DM-only.
	 */
	public User[] recipients;
	/**
	 * The channel topic. 0-1024 chars. Guild- &amp; Text-only.
	 */
	public String topic;
	/**
	 * The ID of the last message sent in this channel. Guild- &amp; Text-only.
	 */
	public String last_message_id;
	/**
	 * The bitrate (in bits) of the voice channel. Guild- &amp; Voice-only.
	 */
	public int bitrate;
	/**
	 * The user limit of this channel. Guild- &amp; Voice-only.
	 */
	public int user_limit;
	/**
	 * The ID of the guild this channel is part of.
	 * Use {@link Member#getGuild()} to get a {@link Guild} object.
	 */
	public String guild_id;

	/**
	 * @return {@link Configuration} object.
	 */
	public Configuration getValues()
	{
		return Main.getValuesConfig("c" + this.id);
	}

	/**
	 * @return Name of the channel.
	 */
	public String getName()
	{
		if(this.type == 1)
		{
			return "private";
		}
		return "#" + name;
	}

	/**
	 * Returns true if this channel is part of a Guild.
	 *
	 * @return True if this channel is part of a Guild.
	 * @since 1.2
	 */
	public boolean isPartOfGuild()
	{
		return this.type == 0 || this.type == 2 || this.type == 3;
	}

	/**
	 * @return {@link Guild} Guild this channel is part of.
	 */
	public Guild getGuild()
	{
		if(this.isPartOfGuild())
		{
			return Main.discordAPI.getGuild(guild_id);
		}
		return null;
	}

	/**
	 * @param id Message ID
	 * @return {@link Message} object with the given ID.
	 */
	public Message getMessage(String id)
	{
		return (Message) DiscordAPI.request("GET", "/channels/" + this.id + "/messages/" + id, new Message());
	}

	private Message[] getMessages(int count, String arg)
	{
		if(count < 1)
		{
			count = 1;
		}
		else if(count > 100)
		{
			count = 100;
		}
		return (Message[]) DiscordAPI.request("GET", "/channels/" + id + "/messages", "limit=" + count + arg, new Message());
	}

	/**
	 * Gets X Messages.
	 * <p>
	 * <code>
	 * // Deletes the last 3 messages<br>
	 * script.each(channel.getMessages(3), function(msg)<br>
	 * {<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;msg.delete();<br>
	 * });
	 * </code>
	 *
	 * @param count Number of messages to be gathered; must be between 1 and 100 (X)
	 * @return An array of {@link Message} objects.
	 * @see Message#delete()
	 */
	public Message[] getMessages(int count)
	{
		return getMessages(count, "");
	}

	/**
	 * Gets X Messages Before Y.
	 *
	 * @param count  Number of messages to be gathered; must be between 1 and 100 (X)
	 * @param before Message ID (Y)
	 * @return An array of {@link Message} objects.
	 * @see Channel#getMessages(int)
	 * @since 1.2
	 */
	public Message[] getMessagesBefore(int count, String before)
	{
		return getMessages(count, "&before=" + before);
	}

	/**
	 * Gets X Messages Around Y.
	 *
	 * @param count  Number of messages to be gathered; must be between 1 and 100 (X)
	 * @param around Message ID (Y)
	 * @return An array of {@link Message} objects.
	 * @see Channel#getMessages(int)
	 * @since 1.2
	 */
	public Message[] getMessagesAround(int count, String around)
	{
		return getMessages(count, "&around=" + around);
	}

	/**
	 * Gets X Messages After Y.
	 *
	 * @param count Number of messages to be gathered; must be between 1 and 100 (X)
	 * @param after Message ID (Y)
	 * @return An array of {@link Message} objects.
	 * @see Channel#getMessages(int)
	 * @since 1.2
	 */
	public Message[] getMessagesAfter(int count, String after)
	{
		return getMessages(count, "&after=" + after);
	}

	/**
	 * @param ids Array of IDs of messages to be deleted
	 * @return this
	 */
	public Channel deleteMessages(String[] ids)
	{
		if(ids.length == 1)
		{
			DiscordAPI.request("DELETE", "/channels/" + id + "/messages/" + ids[0]);
		}
		else if(ids.length > 0)
		{
			JsonArray snowflakes = new JsonArray();
			for(String id : ids)
			{
				if(id != null)
				{
					snowflakes.add(new JsonPrimitive(id));
				}
			}
			JsonObject json = new JsonObject();
			json.add("messages", snowflakes);
			DiscordAPI.request("POST", "/channels/" + id + "/messages/bulk-delete", json.toString());
		}
		return this;
	}

	/**
	 * @param overwrite {@link Overwrite} object
	 * @return this
	 * @see DiscordAPI#createOverwrite()
	 * @since 1.1
	 */
	public Channel overwritePermissions(Overwrite overwrite)
	{
		JsonObject json = new JsonObject();
		json.addProperty("type", overwrite.type);
		json.addProperty("allow", overwrite.allow);
		json.addProperty("deny", overwrite.deny);
		DiscordAPI.request("PUT", "/channels/" + id + "/permissions/" + overwrite.id, json.toString());
		return this;
	}

	/**
	 * @param u {@link User} object
	 * @return {@link Overwrite} object for that user or null if not found.
	 * @since 1.1
	 */
	public Overwrite getOverwrite(User u)
	{
		for(Overwrite o : permission_overwrites)
		{
			if(o.type.equals("member") && o.id.equals(u.id))
			{
				return o;
			}
		}
		return null;
	}

	/**
	 * @param r {@link Role} object
	 * @return {@link Overwrite} object for that role or null if not found.
	 * @since 1.1
	 */
	public Overwrite getOverwrite(Role r)
	{
		for(Overwrite o : permission_overwrites)
		{
			if(o.type.equals("member") && o.id.equals(r.id))
			{
				return o;
			}
		}
		return null;
	}

	/**
	 * Show everyone in this channel that we are typing/working.
	 *
	 * @return this
	 */
	public Channel sendTyping()
	{
		DiscordAPI.request("POST", "/channels/" + id + "/typing");
		return this;
	}

	/**
	 * @param content Content of the message to be sent
	 * @return The newly sent message.
	 */
	public Message sendMessage(String content)
	{
		JsonObject json = new JsonObject();
		json.addProperty("content", content);
		return (Message) DiscordAPI.request("POST", "/channels/" + id + "/messages", json.toString(), new Message());
	}

	/**
	 * @param content Content of the message to be sent
	 * @param tts     Whether this is a TTS message or not
	 * @return The newly sent message.
	 * @since 1.2
	 */
	public Message sendMessage(String content, boolean tts)
	{
		JsonObject json = new JsonObject();
		json.addProperty("content", content);
		json.addProperty("tts", tts);
		return (Message) DiscordAPI.request("POST", "/channels/" + id + "/messages", json.toString(), new Message());
	}

	/**
	 * @param embed {@link Embed} object to be sent
	 * @return The newly sent message.
	 * @see DiscordAPI#createEmbed()
	 */
	public Message sendMessage(Embed embed)
	{
		return sendMessage("", embed);
	}

	/**
	 * @param content Content of the message to be sent
	 * @param embed   {@link Embed} object to be sent
	 * @return The newly sent message.
	 * @see DiscordAPI#createEmbed()
	 */
	public Message sendMessage(String content, Embed embed)
	{
		JsonObject json = new JsonObject();
		json.addProperty("content", content);
		json.add("embed", Main.gson.toJsonTree(embed));
		return (Message) DiscordAPI.request("POST", "/channels/" + id + "/messages", json.toString(), new Message());
	}

	/**
	 * Returns the Handle of this channel to be used in a message
	 *
	 * @return The Handle of this channel to be used in a message
	 */
	public String getHandle()
	{
		return "<#" + id + ">";
	}

	/**
	 * Returns the Type as String of this Channel.
	 *
	 * @return The Type of this Channel.
	 * @see Channel#type
	 */
	public String getTypeName()
	{
		String type_;
		if(type == 0)
		{
			type_ = "Guild Text";
		}
		else if(type == 1)
		{
			type_ = "DM";
		}
		else if(type == 2)
		{
			type_ = "Guild Voice";
		}
		else if(type == 3)
		{
			type_ = "Group DM";
		}
		else if(type == 4)
		{
			type_ = "Guild Category";
		}
		else
		{
			type_ = "[[Unknown Type " + type + "]]";
		}
		return type_;
	}

	public Channel[] getArray(int size)
	{
		return new Channel[size];
	}

	public String toString()
	{
		return "{" + this.getTypeName() + " Channel \"" + getName() + "\" #" + id + "}";
	}
}
