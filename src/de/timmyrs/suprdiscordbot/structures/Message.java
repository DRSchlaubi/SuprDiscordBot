package de.timmyrs.suprdiscordbot.structures;

import com.google.gson.JsonObject;
import de.timmyrs.suprdiscordbot.Main;
import de.timmyrs.suprdiscordbot.apis.DiscordAPI;

import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Message Structure.
 * You can retrieve an array of message structures using {@link Channel#getMessages(int)}.
 *
 * @author timmyRS
 */
public class Message extends Structure
{
	/**
	 * ID of the message
	 */
	public String id;
	/**
	 * The author of this message
	 */
	public User author;
	/**
	 * Contents of the message
	 */
	public String content;
	/**
	 * When this message was sent
	 */
	public String timestamp;
	/**
	 * When this message was edited or null
	 */
	public String edited_timestamp;
	/**
	 * Whether this is/was a TTS message
	 */
	public boolean tts;
	/**
	 * Whether this message mentions everyone
	 */
	public boolean mention_everyone;
	/**
	 * Users specifically mentioned in the message
	 */
	public User[] mentions;
	/**
	 * Roles specifically mentioned in this message
	 */
	public String[] mention_roles;
	/**
	 * Any attached files
	 */
	public Attachment[] attachments;
	/**
	 * Any embedded content
	 */
	public Embed[] embeds;
	/**
	 * Reactions to this message
	 */
	public Reaction[] reactions;
	/**
	 * Used for validating a message was sent
	 */
	public String nonce;
	/**
	 * Whether this message is pinned
	 */
	public boolean pinned;
	/**
	 * If the message is generated by a webhook, this is the webhook's ID
	 */
	public String webhook_id;
	private Embed embed;
	private String channel_id;

	/**
	 * @param content New content of the message
	 * @return this
	 */
	public Message setContent(String content)
	{
		this.content = content;
		return this;
	}

	/**
	 * @param embed {@link Embed} object to be sent with the message
	 * @return this
	 */
	public Message setEmbed(Embed embed)
	{
		this.embed = embed;
		return this;
	}

	/**
	 * @return {@link Channel} this message was sent in
	 */
	public Channel getChannel()
	{
		return Main.discordAPI.getChannel(channel_id);
	}

	/**
	 * @param emoji Emoji to be added to the message
	 * @return this
	 */
	public Message addReaction(String emoji)
	{
		try
		{
			DiscordAPI.request("PUT", "/channels/" + channel_id + "/messages/" + id + "/reactions/" + URLEncoder.encode(emoji, "UTF-8") + "/@me");
		} catch(Exception e)
		{
			e.printStackTrace();
		}
		return this;
	}

	/**
	 * @param emojis List of emojis to be added to the message
	 * @return this
	 */
	public Message addReactions(final String[] emojis)
	{
		new Thread(()->
		{
			for(String emoji : emojis)
			{
				addReaction(emoji);
				try
				{
					Thread.sleep(500);
				} catch(InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}).run();
		return this;
	}

	/**
	 * @param content New content of this message
	 * @return this
	 */
	public Message edit(String content)
	{
		JsonObject json = new JsonObject();
		json.addProperty("content", content);
		Message res = (Message) DiscordAPI.request("PATCH", "/channels/" + channel_id + "/messages/" + id, json.toString(), new Message());
		if(res != null)
		{
			this.content = res.content;
		}
		return this;
	}

	/**
	 * Deletes this message.
	 * After deletion, this object is rendered almost useless.
	 *
	 * @return this
	 */
	public Message delete()
	{
		DiscordAPI.request("DELETE", "/channels/" + channel_id + "/messages/" + id);
		this.id = null;
		return this;
	}

	/**
	 * Get Time
	 *
	 * @return The UNIX timestamp of the message's creation.
	 */
	public long getTime()
	{
		try
		{
			return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssSSSXXX").parse(this.timestamp).getTime() / 1000L;
		} catch(ParseException e)
		{
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * Get Time Millis
	 *
	 * @return The time millis of the message's creation.
	 */
	public long getMillis()
	{
		try
		{
			return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssSSSXXX").parse(this.timestamp).getTime() / 1000L;
		} catch(ParseException e)
		{
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * Get Edit Time
	 *
	 * @return The UNIX timestamp of the message's last edit.
	 */
	public long getEditTime()
	{
		try
		{
			return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssSSSXXX").parse(this.edited_timestamp).getTime();
		} catch(ParseException e)
		{
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * Get Edit Time Millis
	 *
	 * @return The time millis of the message's last edit.
	 */
	public long getEditMillis()
	{
		try
		{
			return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssSSSXXX").parse(this.edited_timestamp).getTime() / 1000L;
		} catch(ParseException e)
		{
			e.printStackTrace();
		}
		return 0;
	}

	public Message[] getArray(int size)
	{
		return new Message[size];
	}

	public String toString()
	{
		if(author == null)
		{
			return "{Message \"" + content + "\"}";
		} else
		{
			return "{Message #" + id +
					" by " + author.toString() +
					" in " + getChannel().toString() + "}";
		}
	}
}
