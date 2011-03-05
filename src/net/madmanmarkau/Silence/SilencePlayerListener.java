package net.madmanmarkau.Silence;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerListener;

public class SilencePlayerListener extends PlayerListener {
	public static Silence plugin;
	
	public SilencePlayerListener(Silence instance) {
		plugin = instance;
	}
	
	public void onPlayerChat(PlayerChatEvent event) {
		Player player = event.getPlayer();
		SilenceParams params = plugin.getUserParams(player);
		
		if (params.getSilenced()) {
			player.sendMessage(ChatColor.GOLD + "You may not chat!");
			event.setCancelled(true);
		}
	}
}
