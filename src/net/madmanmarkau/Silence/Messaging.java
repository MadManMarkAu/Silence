package net.madmanmarkau.Silence;

import org.bukkit.entity.Player;

public class Messaging
{
	public static String parseColor(String original)
	{
		return original.replaceAll("(&([a-z0-9]))", "§$2").replace("&&", "&");
	}

	public static String colorize(String original)
	{
		return original.replace("<black>", "§0").replace("<navy>", "§1").replace("<green>", "§2").replace("<teal>", "§3").replace("<red>", "§4").replace("<purple>", "§5").replace("<gold>", "§6").replace("<silver>", "§7").replace("<gray>", "§8").replace("<blue>", "§9").replace("<lime>", "§a").replace("<aqua>", "§b").replace("<rose>", "§c").replace("<pink>", "§d").replace("<yellow>", "§e").replace("<white>", "§f");
	}

	public static void send(Player player, String message)
	{
		player.sendMessage(parseColor(message));
	}
}
