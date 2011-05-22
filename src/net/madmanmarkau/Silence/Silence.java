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
import java.util.ArrayList;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerListener;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

// Permissions:
// bool Permissions.has(player, "foo.bar"))

// Config:
// String Config.getString("foo.bar", "default");

/* 
 * Silence mods by PSW
 *   a. fail silently (don't echo command usage) if user doesn't have permission to execute command 
 *   b. if Permissions is not loaded, check isOp() instead
 */

public class Silence extends JavaPlugin {
	public final Logger log = Logger.getLogger("Minecraft");

	private boolean UsePermissions;
	public static PermissionHandler Permissions;
	
	public Configuration Config;
	public PluginDescriptionFile pdfFile;
    
    private SilencePlayerListener playerListener = new SilencePlayerListener(this);
    private String silencePath;
	private final String silenceFile = "silence.txt";
	private final String ignoreFile = "ignore.txt";
	final String ServerName = "SERVER";

    
    // Store which users have Silence enacted.
    private final HashMap<String, SilenceParams> silenceState = new HashMap<String, SilenceParams>();
    private final SilenceParams globalSilence = new SilenceParams();
	
	// Store which users and whom they are ignoring. Key is the ignoring user.
	private final HashMap<String, ArrayList> ignoreState = new HashMap<String, ArrayList>();

	@Override
	public void onDisable() {
		saveSilenceList();
		saveIgnoreList();
	    log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " unloaded");
	}

	@Override
	public void onEnable() {
		this.pdfFile = this.getDescription();
		this.silencePath = "plugins" + File.separator + pdfFile.getName() + File.separator;

		setupPermissions();
		if (loadSilenceList() && loadIgnoreList()) {
			registerEvents();	
		
			log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " loaded");
		}
	}

	public void setupPermissions() {
		Plugin perm = this.getServer().getPluginManager().getPlugin("Permissions");
			
		//PSW: copied code from SimpleAdmin plugin to dynamically query for Permissions, and operate without.
		if (this.Permissions == null) {
			if (perm!= null) {
				UsePermissions = true;
				this.getServer().getPluginManager().enablePlugin(perm);
				this.Permissions = ((Permissions) perm).getHandler();
				System.out.println("[Annoy] Permissions system detected!");
			}
			else {
				log.info("Permission system not detected, defaulting to OP");
				UsePermissions = false;	
			}
		}
	}

	private void registerEvents() {
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_CHAT, this.playerListener, Event.Priority.Highest, this);
		pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, this.playerListener, Event.Priority.Highest, this);
	}

	public boolean ifUsePermissions () {
		return UsePermissions;
	}
	
	// Return true if success reading/writting silence list.
	public boolean loadSilenceList() {
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
				return false;
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
			return false;
		}
		
		return true; //success!
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
			this.getServer().getPluginManager().disablePlugin(this); // See onDisable. Is this potentially recursive??
		}
	}
	
	public SilenceParams getUserParams(Player player) {
		if (player != null && this.silenceState.containsKey(player.getName())) {
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
	
	// Begin Ignore Fuctions
	// Return true if success reading/writting silence list.
	public boolean loadIgnoreList() {
		File file = new File(silencePath);
		if ( !(file.exists()) ) {
			file.mkdir();
		}
		
		// Create homes file if not exist
		file = new File(silencePath + ignoreFile);
		if ( !file.exists() ) {
			try {
				FileWriter fstream = new FileWriter(silencePath + ignoreFile);
				BufferedWriter out = new BufferedWriter(fstream);
				
				out.write("# Stores ignored user information.");
				out.newLine();
				out.write("# DO NOT EDIT! File is regularly overwritten.");
				out.newLine();
				
				out.close();
			} catch (Exception e) {
				log.warning(pdfFile.getName() + " could not write a default ignored state file. Working only in RAM.");
				// this.getServer().getPluginManager().disablePlugin(this);
				// return false;
			}
		}
		
		this.ignoreState.clear();
		
		try {
			FileReader fstream = new FileReader(silencePath + ignoreFile);
			BufferedReader reader = new BufferedReader(fstream);
			
			String line = reader.readLine().trim();
			String ignorer = null;

			while (line != null) {
				if (!line.startsWith("#")) {
					
					if (line.endsWith(":")) {
						String[] names = line.split(":");
					
						if (names.length != 1)
							log.warning (pdfFile.getName() + " in " + ignoreFile + " line malformatted: " + line);
						ignorer = names [0];
					} else {
						String[] values = line.split(";");
						
						try { 
							if (values.length > 1 && ignorer == null)
								log.warning (pdfFile.getName() + " in " + ignoreFile + " expected '<name>:' at " + line);
							else if (values.length == 3)
							{
								String playerName;
								long silenceStart;
								int silenceTime;
								
								playerName = values[0];
								silenceStart = Long.parseLong(values[1]);
								silenceTime = Integer.parseInt(values[2]);
								
								IgnoreParams ignoring = new IgnoreParams(playerName, new Date(silenceStart), silenceTime);
								
								if ( !this.ignoreState.containsKey (ignorer)) {  // haven't ignored anyone up to now
									ArrayList<IgnoreParams> ignoreList = new ArrayList<IgnoreParams>(1);
									
									this.ignoreState.put(ignorer, ignoreList);
								}
								this.ignoreState.get(ignorer).add(ignoring); // add this IgnoreParams
							}
							else if (values.length != 1)
								log.warning (pdfFile.getName() + " in " + ignoreFile + " line malformatted: " + line);

						} catch (Exception e) {
							// This entry failed. Ignore and continue.
						}
					}
				}
				
				line = reader.readLine();
			}
			
			reader.close();
		} catch (Exception e) {
			log.severe(pdfFile.getName() + " could not read the ignore state file. Working from blank RAM.");
			//this.getServer().getPluginManager().disablePlugin(this);
			//return false;
		}
		
		return true; //success!
	}
	
	public void saveIgnoreList() {
		File file = new File(silencePath);
		if ( !(file.exists()) ) {
			file.mkdir();
		}
		
		try {
			FileWriter fstream = new FileWriter(silencePath + ignoreFile);
			BufferedWriter writer = new BufferedWriter(fstream);
			
			writer.write("# Stores ignored user information.");
			writer.newLine();
			writer.write("# DO NOT EDIT! File is regularly overwritten.");
			writer.newLine();
			writer.newLine();
			
			for (Entry<String, ArrayList> entry : this.ignoreState.entrySet()) {
				ArrayList<IgnoreParams> ignoreList = entry.getValue();
				
				writer.write (entry.getKey() + ":");
				writer.newLine();
				
				for (IgnoreParams ignore : ignoreList) {
					if (ignore.silenced) {
						writer.write(ignore.ignoreName + ";" + ignore.silenceStart.getTime() + ";" + ignore.silenceTime);
						writer.newLine();
					}
				}
				writer.newLine(); // finished this ignorer
			}
			writer.close();
		} catch (Exception e) {
			log.severe(pdfFile.getName() + " could not save the ignore state file. Running from RAM only");
			//this.getServer().getPluginManager().disablePlugin(this); // See onDisable. Is this potentially recursive??
		}
	}
	
	/* 
	 * returns 0 if not ignoring (to keep boolean syntax)
	 * returns 1-n, index+1 of entry in ArrayList of ignored users
	 */
	public int isUserIgnoring (String destName, String chatter) {		
		if (this.ignoreState.containsKey(destName)) {
			ArrayList<IgnoreParams> ignoreList = this.ignoreState.get(destName);
			
			for (int i=0; i< ignoreList.size(); i++) {
				if (ignoreList.get(i).isIgnored (chatter))
					return i+1;
			}
		}
		return 0;
	}
	
	public void setIgnoreParams(Player ignorer, Player ignored, IgnoreParams params) {
		String ignorerName;
		
		if (ignorer != null) 
			ignorerName = ignorer.getName();
		else	
			ignorerName = ServerName;
		int index = isUserIgnoring (ignorerName, ignored.getName()) - 1;

		if (params.getSilenced() == false && (index >= 0)) {
			this.ignoreState.get(ignorerName).remove(index);
		} else { // being silenced or not already being ignored by this user
			if (index >= 0) {
				this.ignoreState.get(ignorerName).remove(index); // remove existing entry
			}
			else if ( !this.ignoreState.containsKey (ignorerName)) {  // haven't ignored anyone up to now
				ArrayList<IgnoreParams> ignoreList = new ArrayList<IgnoreParams>(1);
				
				this.ignoreState.put(ignorerName, ignoreList);
			}
			this.ignoreState.get(ignorerName).add(params); // add this IgnoreParams
		}
		saveIgnoreList();
	}
	// End Ignore Fuctions
	
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
		String playerName;
    	
		if (sender instanceof Player) {
			player = (Player) sender;
			playerName = player.getName();
		}
		else {
			playerName = ServerName;
		}

    	
		if (cmd.getName().compareToIgnoreCase("silence_silence") == 0) {
			if (args.length == 1) {
				if (player != null && UsePermissions && !this.Permissions.has(player, "silence.query")) return false;
				// PSW added UsePermissions to query; if not everyone can query.
				Player target = sender.getServer().getPlayer(args[0]);

				if (target == null) {
					sender.sendMessage("Player " + args[0] + " not found!");
					return true;
				}

				SilenceParams userParams = getUserParams(target);

				if (userParams.getSilenced()) {
					sender.sendMessage("Player " + target.getName() + " DOES NOT have a voice.");
				} else {
					sender.sendMessage("Player " + target.getName() + " has a voice.");
				}

				return true;
			} else if (args.length == 2) {
				if (player != null) {
					if ((UsePermissions && !this.Permissions.has(player, "silence.modify")) ||
						!player.isOp()) {
						return true;  // fail silently without spouting usage for the command
					}
				}
				// If there is no player, is it console operator? Yes,and player==null

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
				if (player != null && UsePermissions && !this.Permissions.has(player, "silence.queryall")) return false;
				// PSW if no Permissions, anyone can query

				if (globalSilence.getSilenced()) {
					sender.sendMessage("Global silence is on!");
				} else {
					sender.sendMessage("Global silence is off!");
				}

				return true;
			} else if (args.length == 1) {
				if (player != null) {
					if ((UsePermissions && !this.Permissions.has(player, "silence.modifyall")) ||
						!player.isOp()) {
						return true;  // fail silently without spouting usage for the command
					}
				}
				// code edited here.

				if (args[0].compareToIgnoreCase("on") == 0) {
					globalSilence.setSilenced(true);

					sender.sendMessage("Global silence is now on.");
					log.info((player == null ? ServerName : "Player " + playerName) + " enabled global silence.");
					return true;
				} else if (args[0].compareToIgnoreCase("off") == 0) {
					globalSilence.setSilenced(false);

					sender.sendMessage("Global silence is now off.");
					log.info((player == null ? ServerName : "Player " + playerName) + " disabled global silence.");
					return true;
				} else {
					int silenceTime = decodeTime(args[0]);
					
					if (silenceTime > 0) {
						globalSilence.silencePlayer(silenceTime);

						sender.sendMessage("Global silence is on for " + silenceTime + " seconds.");
						log.info((player == null ? ServerName : "Player " + playerName ) + " enabled global silence for " + silenceTime + " seconds.");
					}
					return true;
				}
			}
		} else if (cmd.getName().compareToIgnoreCase("silence_ignore") == 0) {
			// PSW if no Permissions, anyone can ignore.
			if (player != null && UsePermissions && !this.Permissions.has(player, "silence.ignore")) return true;
			// If there is no player, is it console operator?

			if (args.length == 0) {
				ArrayList<IgnoreParams> targets = ignoreState.get (playerName);
				
				if (targets == null) {
					sender.sendMessage("You are ignoring no one. Why'd you ask?");
				} else {
					for (int i=0; i < targets.size(); i++) {
						sender.sendMessage("Player " + targets.get(i).getName() + " being ignored by you.");
					}
				}

				return true;
			} else if (args.length == 1) { // query a specific target 				
	
				Player target = sender.getServer().getPlayer(args[0]);

				if (target == null) {
					sender.sendMessage("Player " + args[0] + " not found!");
					return true;
				}

				if (isUserIgnoring (playerName, target.getName()) != 0) {
					sender.sendMessage("Player " + target.getName() + " being ignored by you.");
				} else {
					sender.sendMessage("Player " + target.getName() + " NOT being ignored by you.");
				}

				return true;
			} else if (args.length == 2) { // setting ignore state for a user

				Player target = sender.getServer().getPlayer(args[0]);

				if (target == null) {
					sender.sendMessage("Player " + args[0] + " not found!");
					return true;
				}

				IgnoreParams userParams = new IgnoreParams (target.getName());
				
				if (args[1].compareToIgnoreCase("on") == 0) {
					userParams.setSilenced(true);
					setIgnoreParams(player, target, userParams);

					sender.sendMessage("Player " + target.getName() + " is now ignored");
					target.sendMessage(playerName + " is now ignoring your chats.");
					
					return true;
				} else if (args[1].compareToIgnoreCase("off") == 0) {
					userParams.setSilenced(false);
					setIgnoreParams(player, target, userParams);

					sender.sendMessage("Player " + target.getName() + " is no longer ignored");
					target.sendMessage(playerName + " is again listening to your chats.");

					return true;
				} else {
					int silenceTime = decodeTime(args[1]);
					
					if (silenceTime > 0) {
						userParams.ignorePlayer(silenceTime);
						setIgnoreParams(player, target, userParams);

						sender.sendMessage("Player " + target.getName() + " is ignored for " + silenceTime + " seconds.");
						target.sendMessage(playerName + " is NOT listening to your chats for " + silenceTime + " seconds.");

					}
					return true;
				}
			}
		}
		return false;
    }
}
