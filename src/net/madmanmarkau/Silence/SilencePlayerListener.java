package net.madmanmarkau.Silence;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerListener;

public class SilencePlayerListener extends PlayerListener {
	public static Silence plugin;
	
	public SilencePlayerListener(Silence instance) {
		plugin = instance;
	}
	
	// TODO: This will need to be updated to the latest Bukkit command method.
	public void onPlayerCommand(PlayerChatEvent event) {
		if (event.getEventName().equals("PLAYER_COMMAND")) {
			String[] params = event.getMessage().split(" ");
			
			if (params[0].equals("/silence") && plugin.Permissions.has(event.getPlayer(), "silence")) {
				event.setCancelled(plugin.onPlayerCommand(event.getPlayer(), params));
			}
		}
	}
	
	public void onPlayerChat(PlayerChatEvent event) {
		Player player = event.getPlayer();
		SilenceParams params = plugin.getUserParams(player);
		
		if (params.getSilenced()) {
			Messaging.send(player, "&eYou may not chat!");
			event.setCancelled(true);
		}
	}
}
