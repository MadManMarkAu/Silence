package net.madmanmarkau.Silence;

import java.util.Set;

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
			SilenceParams params = DataManager.getSilenceParams(player);
			
			// if (!params.updateSilenceTimer() || (!plugin.getGlobalParams().updateSilenceTimer() && !plugin.Permissions.has(event.getPlayer(), "silence.silenceall.ignore"))) {
			if (!params.updateActiveTimer() || (DataManager.getGlobalSilenceParams().updateActiveTimer() && !plugin.isOpAllowed(player, "silence.silenceall.ignore", true, true))) {
				Messaging.sendError(player, "You may not chat!");
				event.setCancelled(true);
			} else {
				// Send message to players who are not ignoring you.

				// Start: This copies and modifies part of Bukkit's internal code and is subject to change.
				Set<Player> players = event.getRecipients();
				String message = String.format(event.getFormat(), player.getDisplayName(), event.getMessage());

				if (DataManager.isUserIgnoring("CONSOLE", player.getName()) == 0)
					Messaging.logInfoRaw(message);

				for (Player dest : players) {
					if (DataManager.isUserIgnoring (dest.getName(), player.getName()) == 0) {
						dest.sendMessage(message);
					}
				}
				// End 
				
				if (params.getSaveRequired()) {
					DataManager.saveSilenceList();
				}
				event.setCancelled (true);
			}
		}	
	}
	
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if (!event.isCancelled() && event.getMessage().toLowerCase().startsWith("/me ") && event.getPlayer() != null) {
			Player player = event.getPlayer();
			SilenceParams params = DataManager.getSilenceParams(player);
			
			if (!params.updateActiveTimer() || (DataManager.getGlobalSilenceParams().updateActiveTimer() && !plugin.isOpAllowed(player, "silence.silenceall.ignore", true, true))) {
				Messaging.sendError(player, "You may not chat!");
				event.setCancelled(true);
			} else {
				// Send message to players who are not ignoring you.

				Set<Player> players = event.getRecipients();
				String message = "* " + event.getPlayer().getName() + " " + event.getMessage().substring(event.getMessage().indexOf(" ")).trim(); // Copied from Bukkit source and modified accordingly.

				if (DataManager.isUserIgnoring("CONSOLE", player.getName()) == 0)
					Messaging.logInfoRaw(message);

				for (Player dest : players) {
					if (DataManager.isUserIgnoring (dest.getName(), player.getName()) == 0) {
						dest.sendMessage(message);
					}
				}
				
				event.setCancelled (true);
			}
			
			if (params.getSaveRequired()) {
				DataManager.saveSilenceList();
			}
		}
	}
}
