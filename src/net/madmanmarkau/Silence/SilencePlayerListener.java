package net.madmanmarkau.Silence;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerListener;

public class SilencePlayerListener extends PlayerListener {
	public static Silence plugin;
	
	public SilencePlayerListener(Silence instance) {
		plugin = instance;
	}
	
	public void onPlayerChat(PlayerChatEvent event) {
		if (!event.isCancelled()) {
			Player player = event.getPlayer();
			SilenceParams params = plugin.getUserParams(player);
			
			if (!params.updateSilenceTimer() || (!plugin.getGlobalParams().updateSilenceTimer() && !plugin.Permissions.has(event.getPlayer(), "silence.silenceall.ignore"))) {
				player.sendMessage(ChatColor.GOLD + "You may not chat!");
				event.setCancelled(true);
			}
			
			if (params.getSaveRequired()) {
				plugin.saveSilenceList();
			}
		}
	}
	
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if (!event.isCancelled() && event.getMessage().toLowerCase().startsWith("/me ")) {
			Player player = event.getPlayer();
			SilenceParams params = plugin.getUserParams(player);
			
			if (!params.updateSilenceTimer() || (!plugin.getGlobalParams().updateSilenceTimer() && !plugin.Permissions.has(event.getPlayer(), "silence.silenceall.ignore"))) {
				player.sendMessage(ChatColor.GOLD + "You may not chat!");
				event.setCancelled(true);
			}
			
			if (params.getSaveRequired()) {
				plugin.saveSilenceList();
			}
		}
	}
}
