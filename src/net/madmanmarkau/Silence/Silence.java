package net.madmanmarkau.Silence;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
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
    private String silencePath;
	private final String silenceFile = "silence.txt";

    
    // Store which users have Silence enacted.
    private final HashMap<String, SilenceParams> silenceState = new HashMap<String, SilenceParams>();
    private final SilenceParams globalSilence = new SilenceParams();

	@Override
	public void onDisable() {
		saveSilenceList();
	    log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " unloaded");
	}

	@Override
	public void onEnable() {
		this.pdfFile = this.getDescription();
		this.silencePath = "plugins" + File.separator + pdfFile.getName() + File.separator;

		setupPermissions();
		loadSilenceList();
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
		pm.registerEvent(Event.Type.PLAYER_CHAT, this.playerListener, Event.Priority.Highest, this);
		pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, this.playerListener, Event.Priority.Highest, this);
	}


	public void loadSilenceList() {
		File file = new File(silencePath);
		if ( !(file.exists()) ) {
			file.mkdir();
		}

		// Create homes file if not exist
		file = new File(silencePath + silenceFile);
		if ( !file.exists() ) {
			try {
				FileWriter fstream = new FileWriter(silencePath + silenceFile);
				BufferedWriter out = new BufferedWriter(fstream);

				out.write("# Stores silenced user information.");
				out.newLine();
				out.write("# DO NOT EDIT! File is regularly overwritten.");
				out.newLine();

				out.close();
			} catch (Exception e) {
				log.warning(pdfFile.getName() + " could not write the default silence state file.");
				this.getServer().getPluginManager().disablePlugin(this);
			}
		}

		this.silenceState.clear();
		
		try {
			FileReader fstream = new FileReader(silencePath + silenceFile);
			BufferedReader reader = new BufferedReader(fstream);

			String line = reader.readLine().trim();

			while (line != null) {
				if (!line.startsWith("#")) {

					String[] values = line.split(";");

					try {
						if (values.length == 3)
						{
							String playerName;
							long silenceStart;
							int silenceTime;

							playerName = values[0];
							silenceStart = Long.parseLong(values[1]);
							silenceTime = Integer.parseInt(values[2]);
							
							this.silenceState.put(playerName, new SilenceParams(new Date(silenceStart), silenceTime));
						}
					} catch (Exception e) {
						// This entry failed. Ignore and continue.
					}
				}

				line = reader.readLine();
			}

			reader.close();
		} catch (Exception e) {
			log.severe(pdfFile.getName() + " could not read the silence state file.");
			this.getServer().getPluginManager().disablePlugin(this);
		}
	}

	public void saveSilenceList() {
		File file = new File(silencePath);
		if ( !(file.exists()) ) {
			file.mkdir();
		}

		try {
			FileWriter fstream = new FileWriter(silencePath + silenceFile);
			BufferedWriter writer = new BufferedWriter(fstream);

			writer.write("# Stores silenced user information.");
			writer.newLine();
			writer.write("# DO NOT EDIT! File is regularly overwritten.");
			writer.newLine();
			writer.newLine();

			for (Entry<String, SilenceParams> entry : this.silenceState.entrySet()) {
				SilenceParams silence = entry.getValue();

				if (silence.silenced) {
					writer.write(entry.getKey() + ";" + silence.silenceStart.getTime() + ";" + silence.silenceTime);
					writer.newLine();
				}
			}
			writer.close();
		} catch (Exception e) {
			log.severe(pdfFile.getName() + " could not save the silence state file.");
			this.getServer().getPluginManager().disablePlugin(this);
		}
	}
	
	public SilenceParams getUserParams(Player player) {
		if (this.silenceState.containsKey(player.getName())) {
			return this.silenceState.get(player.getName());
		}
		return new SilenceParams();
	}
	
	public void setUserParams(Player player, SilenceParams params) {
		if (params.getSilenced() == false && getUserParams(player).getSilenced() == true) {
			this.silenceState.remove(player.getName());
		} else {
			if (this.silenceState.containsKey(player.getName())) {
				this.silenceState.remove(player.getName());
			}
			this.silenceState.put(player.getName(), params);
		}
		saveSilenceList();
	}

	public SilenceParams getGlobalParams() {
		return this.globalSilence;
	}

	private int decodeTime(String time) {
		// Parse integer seconds

		try {
			int silenceTime = Integer.parseInt(time);

			if (silenceTime >= 0) return silenceTime;
		} catch (Exception e) {}

		try {
			int dayIndex = time.indexOf("d");
			int hourIndex = time.indexOf("h");
			int minuteIndex = time.indexOf("m");
			int secondIndex = time.indexOf("s");
			int lastIndex = 0;

			if (dayIndex > -1 || hourIndex > -1 || minuteIndex > -1 || secondIndex > -1) {
				int timeInSeconds = 0;
				
				if (dayIndex > -1) {
					timeInSeconds += Integer.parseInt(time.substring(lastIndex, dayIndex)) * 60 * 60 * 24;
					lastIndex = dayIndex + 1;
				}
				
				if (hourIndex > -1) {
					timeInSeconds += Integer.parseInt(time.substring(lastIndex, hourIndex)) * 60 * 60;
					lastIndex = hourIndex + 1;
				}
				
				if (minuteIndex > -1) {
					timeInSeconds += Integer.parseInt(time.substring(lastIndex, minuteIndex)) * 60;
					lastIndex = minuteIndex + 1;
				}
				
				if (secondIndex > -1) {
					timeInSeconds += Integer.parseInt(time.substring(lastIndex, secondIndex));
					lastIndex = secondIndex + 1;
				}
	
				if (timeInSeconds >= 0) return timeInSeconds;
			}
		} catch (Exception e) {}

		try {
			int timeInSeconds = 0;
			int lastIndex = 0;

			int thisIndex;
			
			thisIndex = time.indexOf(":", lastIndex);

			if (thisIndex > -1) {
				timeInSeconds += Integer.parseInt(time.substring(lastIndex, thisIndex));
				lastIndex = thisIndex + 1;
			} else {
				timeInSeconds += Integer.parseInt(time.substring(lastIndex));
				if (timeInSeconds >= 0) return timeInSeconds;
			}

			thisIndex = time.indexOf(":", lastIndex);
			timeInSeconds *= 60;
			
			if (thisIndex > -1) {
				timeInSeconds += Integer.parseInt(time.substring(lastIndex, thisIndex));
				lastIndex = thisIndex + 1;
			} else {
				timeInSeconds += Integer.parseInt(time.substring(lastIndex));
				if (timeInSeconds >= 0) return timeInSeconds;
			}

			thisIndex = time.indexOf(":", lastIndex);
			timeInSeconds *= 60;
			
			if (thisIndex > -1) {
				timeInSeconds += Integer.parseInt(time.substring(lastIndex, thisIndex));
				lastIndex = thisIndex + 1;
			} else {
				timeInSeconds += Integer.parseInt(time.substring(lastIndex));
				if (timeInSeconds >= 0) return timeInSeconds;
			}

			thisIndex = time.indexOf(":", lastIndex);
			timeInSeconds *= 24;
			
			if (thisIndex > -1) {
				timeInSeconds += Integer.parseInt(time.substring(lastIndex, thisIndex));
				lastIndex = thisIndex + 1;
			} else {
				timeInSeconds += Integer.parseInt(time.substring(lastIndex));
				if (timeInSeconds >= 0) return timeInSeconds;
			}
			
		} catch (Exception e) {}

		return -1;
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
				} else {
					int silenceTime = decodeTime(args[1]);
					
					if (silenceTime > 0) {
						userParams.silencePlayer(silenceTime);
						setUserParams(target, userParams);

						sender.sendMessage("Player " + target.getName() + " is silenced for " + silenceTime + " seconds.");
					}
					return true;
				}
			}
		} else if (cmd.getName().compareToIgnoreCase("silence_silenceall") == 0) {
			if (args.length == 0) {
				if (player != null && !this.Permissions.has(player, "silence.queryall")) return false;

				if (globalSilence.getSilenced()) {
					sender.sendMessage("Global silence is on!");
				} else {
					sender.sendMessage("Global silence is off!");
				}

				return true;
			} else if (args.length == 1) {
				if (player != null && !this.Permissions.has(player, "silence.modifyall")) return false;

				if (args[0].compareToIgnoreCase("on") == 0) {
					globalSilence.setSilenced(true);

					sender.sendMessage("Global silence is now on.");
					log.info("Player " + player.getName() + " enabled global silence.");
					return true;
				} else if (args[0].compareToIgnoreCase("off") == 0) {
					globalSilence.setSilenced(false);

					sender.sendMessage("Global silence is now off.");
					log.info("Player " + player.getName() + " disabled global silence.");
					return true;
				} else {
					int silenceTime = decodeTime(args[0]);
					
					if (silenceTime > 0) {
						globalSilence.silencePlayer(silenceTime);

						sender.sendMessage("Global silence is on for " + silenceTime + " seconds.");
						log.info("Player " + player.getName() + " enabled global silence for " + silenceTime + " seconds.");
					}
					return true;
				}
			}
		}
		return false;
    }
}
