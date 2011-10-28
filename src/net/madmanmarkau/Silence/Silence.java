package net.madmanmarkau.Silence;

import java.util.ArrayList;
import java.util.Date;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import com.nijiko.permissions.PermissionHandler;

// Permissions:
// bool Permissions.has(player, "foo.bar"))

// Config:
// String Config.getString("foo.bar", "default");


/**
 * @author MadManMarkAu, PSW(filbert66)
 */
public class Silence extends JavaPlugin {
	public PermissionHandler Permissions;
	public YamlConfiguration Config;

	private boolean usePermissions;

	private SilencePlayerListener playerListener = new SilencePlayerListener(this);

	@Override
	public void onDisable() {
		DataManager.saveSilenceList();
		DataManager.saveIgnoreList();
		Messaging.logInfo(this.getDescription().getName() + " version " + this.getDescription().getVersion() + " unloaded", this);
	}

	@Override
	public void onEnable() {
		DataManager.initialize(this, this.getDataFolder().getAbsolutePath());

		SilencePermissions.initialize(this);

		if (DataManager.loadSilenceList() && DataManager.loadIgnoreList()) {
			registerEvents(); 
			registerEvents();
			Messaging.logInfo(this.getDescription().getName() + " version " + this.getDescription().getVersion() + " loaded", this);
		} else {
			this.getServer().getPluginManager().disablePlugin(this);
		}
	}

	private void registerEvents() {
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_CHAT, this.playerListener, Event.Priority.Highest, this);
		pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, this.playerListener, Event.Priority.Highest, this);
	}

	/**
	 * Determines if Permissions system is active, or if permissions should be based on isOp();
	 * @return True if Permissions is enabled, otherwise false.
	 */
	public boolean ifUsePermissions () {
		return this.usePermissions;
	}

	/**
	 * Check if a given permission is allowed. If Permissions system is not active, default to isOp()
	 * @param player Player to query permission for.
	 * @param permission Permission to query for.
	 * @param onlyOps If Permissions system is not active, only operators are allowed to perform this function.
	 * @param allowConsole True to allow NULL players (console) permission.
	 * @return True if operation is allowed, otherwise false.
	 */
	public boolean isOpAllowed(Player player, String permission, boolean onlyOps, boolean allowConsole) {
		if (player != null) {
			if (this.usePermissions) {
				return this.Permissions.has(player, permission);
			} else {
				if (onlyOps) {
					if (player.isOp()) {
						return true;
					} else {
						return false;
					}
				} else {
					return true;
				}
			}
		} else {
			return allowConsole;
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
				if (!isOpAllowed(player, "silence.query", true, true)) return true;

				Player target = sender.getServer().getPlayer(args[0]);

				if (target == null) {
					Messaging.sendError(sender, "Player " + args[0] + " not found!");
					return true;
				}

				SilenceParams userParams = DataManager.getSilenceParams(player);

				if (userParams.getActive()) {
					Messaging.sendSuccess(sender, "Player " + target.getName() + " DOES NOT have a voice.");
				} else {
					Messaging.sendSuccess(sender, "Player " + target.getName() + " has a voice.");
				}

				return true;
			} else if (args.length == 2) {
				if (!isOpAllowed(player, "silence.modify", true, true)) return true;

				Player target = sender.getServer().getPlayer(args[0]);

				if (target == null) {
					Messaging.sendError(sender, "Player " + args[0] + " not found!");
					return true;
				}

				SilenceParams userParams = DataManager.getSilenceParams(target);

				if (args[1].compareToIgnoreCase("on") == 0) {
					userParams.setActive(true);
					DataManager.setSilenceParams(target, userParams);

					Messaging.sendSuccess(sender, "Player " + target.getName() + " is now silenced");
					return true;
				} else if (args[1].compareToIgnoreCase("off") == 0) {
					userParams.setActive(false);
					DataManager.setSilenceParams(target, userParams);

					Messaging.sendSuccess(sender, "Player " + target.getName() + " is no longer silenced");
					return true;
				} else {
					int silenceTime = Util.decodeTime(args[1]);

					if (silenceTime > 0) {
						userParams.setActive(true);
						userParams.setActiveStart(new Date());
						userParams.setActiveTime(silenceTime);
						DataManager.setSilenceParams(target, userParams);

						Messaging.sendSuccess(sender, "Player " + target.getName() + " is silenced for " + silenceTime + " seconds.");
					}
					return true;
				}
			}
		} else if (cmd.getName().compareToIgnoreCase("silence_silenceall") == 0) {
			if (args.length == 0) {
				if (!isOpAllowed(player, "silence.queryall", true, true)) return true;

				if (DataManager.getGlobalSilenceParams().getActive()) {
					Messaging.sendSuccess(sender, "Global silence is on!");
				} else {
					Messaging.sendSuccess(sender, "Global silence is off!");
				}

				return true;
			} else if (args.length == 1) {
				if (!isOpAllowed(player, "silence.modifyall", true, true)) return true;

				if (args[0].compareToIgnoreCase("on") == 0) {
					DataManager.getGlobalSilenceParams().setActive(true);

					Messaging.sendSuccess(sender, "Global silence is now on.");
					Messaging.logInfo((player == null ? "CONSOLE" : "Player " + player.getName()) + " enabled global silence.", this);
					return true;
				} else if (args[0].compareToIgnoreCase("off") == 0) {
					DataManager.getGlobalSilenceParams().setActive(false);

					Messaging.sendSuccess(sender, "Global silence is now off.");
					Messaging.logInfo((player == null ? "CONSOLE" : "Player " + player.getName()) + " disabled global silence.", this);
					return true;
				} else {
					int silenceTime = Util.decodeTime(args[0]);

					if (silenceTime > 0) {
						DataManager.getGlobalSilenceParams().setActive(true);
						DataManager.getGlobalSilenceParams().setActiveStart(new Date());
						DataManager.getGlobalSilenceParams().setActiveTime(silenceTime);

						Messaging.sendSuccess(sender, "Global silence is on for " + silenceTime + " seconds.");
						Messaging.logInfo((player == null ? "CONSOLE" : "Player " + player.getName()) + " enabled global silence for " + silenceTime + " seconds.", this);
					}
					return true;
				}
			}
		} else if (cmd.getName().compareToIgnoreCase("silence_ignore") == 0) {
			if (!isOpAllowed(player, "silence.ignore", false, false)) return true;

			if (args.length == 0) { // Retrieve a list of ignored players
				ArrayList<IgnoreParams> targets = DataManager.getIgnoreParams(player);
				ArrayList<String> ignoredPlayers = new ArrayList<String>();
				
				// Obtain a list of ignored players.
				if (targets != null) {
					for (int i=0; i < targets.size(); i++) {
						ignoredPlayers.add(targets.get(i).getIgnoredPlayer());
					}
				}

				Messaging.sendSuccess(sender, "You are ignoring: " + Util.joinString((String[]) ignoredPlayers.toArray(), ", "));

				return true;
			} else if (args.length == 1) { // Query ignore state for a specific player
				Player target = sender.getServer().getPlayer(args[0]);

				if (target == null) {
					Messaging.sendError(sender, "Player " + args[0] + " not found!");
					return true;
				}

				if (DataManager.isUserIgnoring(player.getName(), target.getName()) != 0) {
					Messaging.sendSuccess(sender, "Player " + target.getName() + " being ignored by you.");
				} else {
					Messaging.sendSuccess(sender, "Player " + target.getName() + " NOT being ignored by you.");
				}

				return true;
			} else if (args.length == 2) { // Set ignore state for a user
				Player target = sender.getServer().getPlayer(args[0]);

				if (target == null) {
					Messaging.sendError(sender, "Player " + args[0] + " not found!");
					return true;
				}

				IgnoreParams userParams = new IgnoreParams(player.getName(), target.getName());

				if (args[1].compareToIgnoreCase("on") == 0) {
					userParams.setActive(true);
					DataManager.setIgnoreParams(player, target, userParams);

					Messaging.sendSuccess(sender, "Player " + target.getName() + " is now ignored.");
					Messaging.sendSuccess(target, "Player " + player.getName() + " is now ignoring you.");

					return true;
				} else if (args[1].compareToIgnoreCase("off") == 0) {
					userParams.setActive(false);
					DataManager.setIgnoreParams(player, target, userParams);

					Messaging.sendSuccess(sender, "Player " + target.getName() + " is no longer ignored.");
					Messaging.sendSuccess(target, "Player " + player.getName() + " is now no longer ignoring you.");

					return true;
				} else {
					int silenceTime = Util.decodeTime(args[1]);

					if (silenceTime > 0) {
						userParams.setActive(true);
						userParams.setActiveStart(new Date());
						userParams.setActiveTime(silenceTime);
						DataManager.setIgnoreParams(player, target, userParams);

						Messaging.sendSuccess(sender, "Player " + target.getName() + " is now ignored for " + silenceTime + "seconds.");
						Messaging.sendSuccess(target, "Player " + player.getName() + " is now ignoring you for " + silenceTime + " seconds.");

					}
					return true;
				}
			}
		}
		return false;
	}
}
