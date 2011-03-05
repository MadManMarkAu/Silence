package net.madmanmarkau.Silence;

import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;
import org.bukkit.event.Event;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

// Permissions:
// bool Permissions.has(player, "foo.bar"))

// Config:
// String Config.getString("foo.bar", "default");


public class Silence extends JavaPlugin {
	public final Logger log = Logger.getLogger("Minecraft");
    public PermissionHandler Permissions;
	public Configuration Config;
    public PluginDescriptionFile pdfFile;
	
    private SilencePlayerListener playerListener = new SilencePlayerListener(this);

    
    // Store which users have Silence enacted.
    private final HashMap<Player, SilenceParams> silenceState = new HashMap<Player, SilenceParams>();

	@Override
	public void onDisable() {
	    log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " unloaded");
	}

	@Override
	public void onEnable() {
		this.pdfFile = this.getDescription();

		setupPermissions();
		registerEvents();
		
		log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " loaded");
	}

	public void setupPermissions() {
		Plugin perm = this.getServer().getPluginManager().getPlugin("Permissions");
			
		if (this.Permissions == null) {
			if (perm!= null) {
				this.getServer().getPluginManager().enablePlugin(perm);
				this.Permissions = ((Permissions) perm).getHandler();
			}
			else {
				log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + "not enabled. Permissions not detected");
				this.getServer().getPluginManager().disablePlugin(this);
			}
		}
	}

	private void registerEvents() {
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_COMMAND, this.playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_CHAT, this.playerListener, Event.Priority.Lowest, this);
	}

	public SilenceParams getUserParams(Player player) {
		if (this.silenceState.containsKey(player)) {
			return this.silenceState.get(player);
		}
		return new SilenceParams();
	}
	
	public void setUserParams(Player player, SilenceParams params) {
		if (params.getSilenced() == false && getUserParams(player).getSilenced() == true) {
			this.silenceState.remove(player);
		} else {
			if (this.silenceState.containsKey(player)) {
				this.silenceState.remove(player);
			}
			this.silenceState.put(player, params);
		}
	}
	
	public boolean onPlayerCommand(Player player, String[] params) {
		if (params.length == 1) { // plugin.Permissions.has(event.getPlayer(), "silence")
			// Print usage
		    Messaging.send(player, "&c/silence usage:");
		    Messaging.send(player, "&c    /silence <player>      - Query player status");
		    Messaging.send(player, "&c    /silence <player> on   - Silence the player");
		    Messaging.send(player, "&c    /silence <player> off  - Allow player to talk");

		    return true;
		} else if (params.length == 2 && this.Permissions.has(player, "silence.query")) {
			Player target = player.getServer().getPlayer(params[1]);
			
			if (target == null) {
				Messaging.send(player, "&ePlayer " + params[1] + " not found!");
				return true;
			}
			
			SilenceParams userParams = getUserParams(player);
			
			if (userParams.getSilenced()) {
				Messaging.send(player, "&ePlayer " + target.getName() + " DOES NOT have a voice.");
			} else {
				Messaging.send(player, "&ePlayer " + target.getName() + " has a voice.");
			}
			
			return true;
		} else if (params.length == 3 && this.Permissions.has(player, "silence.modify")) {
			Player target = player.getServer().getPlayer(params[1]);
			
			if (target == null) {
				Messaging.send(player, "&ePlayer " + params[1] + " not found!");
				return true;
			}
			
			SilenceParams userParams = getUserParams(target);
			
			if (params[2].matches("on")) {
				userParams.setSilenced(true);
				setUserParams(target, userParams);

				Messaging.send(player, "&ePlayer " + target.getName() + " is now silenced");
				return true;
			} else if (params[2].matches("off")) {
				userParams.setSilenced(false);
				setUserParams(target, userParams);

				Messaging.send(player, "&ePlayer " + target.getName() + " is no longer silenced");
				return true;
			}
		}
		
		return false;
	}
}
