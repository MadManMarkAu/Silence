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
			
			if (!params.updateSilenceTimer() || // user silenced
				(!plugin.getGlobalParams().updateSilenceTimer() &&  // all silenced
				 ((plugin.ifUsePermissions() && !plugin.Permissions.has(event.getPlayer(), "silence.silenceall.ignore")) ||
				  !player.isOp() ) ) )
			{ // PSW check permissions or must be Op
				player.sendMessage(ChatColor.GOLD + "You may not chat!");
				event.setCancelled(true);
			} else {
				// Only forward to those not ignoring the chatting player
				Player[] players = player.getServer().getOnlinePlayers();

				for (int i = 0; i < players.length; i++) {
					Player dest = players[i];
					 
					if (plugin.isUserIgnoring (dest.getName(), player.getName()) == 0) {
						dest.sendMessage(String.format(event.getFormat(), player.getDisplayName(), event.getMessage() ));
					}
				}
				if (plugin.isUserIgnoring (plugin.ServerName, player.getName()) == 0) 
					plugin.log.info (String.format(event.getFormat(), player.getDisplayName(), event.getMessage()));
				
				if (params.getSaveRequired()) {
					plugin.saveSilenceList();
				}
				event.setCancelled (true);
			}
			// never allows chat to proceed, since we silence them or send discrete messages.
		}	
	}
	
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if (!event.isCancelled() && event.getMessage().toLowerCase().startsWith("/me ")) {
			Player player = event.getPlayer();
			SilenceParams params = plugin.getUserParams(player);
			
			if (!params.updateSilenceTimer() || // user silenced
				(!plugin.getGlobalParams().updateSilenceTimer() &&  // all silenced
				 ((plugin.ifUsePermissions() && !plugin.Permissions.has(event.getPlayer(), "silence.silenceall.ignore")) ||
				  !player.isOp() ) ) )
			{ // PSW check permissions or must be Op
				player.sendMessage(ChatColor.GOLD + "You may not chat!");
				event.setCancelled(true);
			} else {
				// Only forward to those not ignoring the chatting player
				Player[] players = player.getServer().getOnlinePlayers();
				String message = event.getMessage().substring(4); //skip the "/me "
				String format = "* %s %s";

				for (int i = 0; i < players.length; i++) {
					Player dest = players[i];
					 
					if (plugin.isUserIgnoring (dest.getName(), player.getName()) == 0) {
						dest.sendMessage(String.format(format, player.getDisplayName(), message ));
					}
				}
				if (plugin.isUserIgnoring (plugin.ServerName, player.getName()) == 0) 
					plugin.log.info (String.format(format, player.getDisplayName(), message));
				
				event.setCancelled (true);
			}
			
			if (params.getSaveRequired()) {
				plugin.saveSilenceList();
			}
		}
	}
}
