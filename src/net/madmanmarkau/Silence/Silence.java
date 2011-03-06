package net.madmanmarkau.Silence;

import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
    {
    	Player player = null;
    	
		if (sender instanceof Player) {
			player = (Player) sender;
		}
    	
		if (cmd.getName().compareToIgnoreCase("silence_silence") == 0) {
			if (args.length == 1) {
				if (player != null && !this.Permissions.has(player, "silence.query")) return false;

				Player target = sender.getServer().getPlayer(args[0]);

				if (target == null) {
					sender.sendMessage("Player " + args[0] + " not found!");
					return true;
				}

				SilenceParams userParams = getUserParams(player);

				if (userParams.getSilenced()) {
					sender.sendMessage("Player " + target.getName() + " DOES NOT have a voice.");
				} else {
					sender.sendMessage("Player " + target.getName() + " has a voice.");
				}

				return true;
			} else if (args.length == 2) {
				if (player != null && !this.Permissions.has(player, "silence.modify")) return false;

				Player target = sender.getServer().getPlayer(args[0]);

				if (target == null) {
					sender.sendMessage("Player " + args[0] + " not found!");
					return true;
				}

				SilenceParams userParams = getUserParams(target);

				if (args[1].compareToIgnoreCase("on") == 0) {
					userParams.setSilenced(true);
					setUserParams(target, userParams);

					sender.sendMessage("Player " + target.getName() + " is now silenced");
					return true;
				} else if (args[1].compareToIgnoreCase("off") == 0) {
					userParams.setSilenced(false);
					setUserParams(target, userParams);

					sender.sendMessage("Player " + target.getName() + " is no longer silenced");
					return true;
				}
			}
		}
		return false;
    }
}
