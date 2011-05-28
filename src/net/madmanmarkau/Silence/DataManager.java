package net.madmanmarkau.Silence;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class DataManager {
	private static JavaPlugin plugin;
	private static String dataPath;
	private static final String silenceFile = "silence.txt";
	private static final String ignoreFile = "ignore.txt";

	// Store which users have Silence enacted.
	private static final HashMap<String, SilenceParams> silenceState = new HashMap<String, SilenceParams>();
	private static final SilenceParams globalSilence = new SilenceParams();
	private static final HashMap<String, ArrayList<IgnoreParams>> ignoreState = new HashMap<String, ArrayList<IgnoreParams>>(); // Store which users and whom they are ignoring. Key is the ignoring user.

	public static void initialize(Plugin plugin, String dataPath) {
		if (!dataPath.endsWith(File.separator)) {
			DataManager.dataPath = dataPath + File.separator;
		} else {
			DataManager.dataPath = dataPath;
		}
	}

	/**
	 * Load the silence list from disk.
	 * @return True on success, false on failure.
	 */
	public static boolean loadSilenceList() {
		File file = new File(dataPath);
		if ( !(file.exists()) ) {
			file.mkdir();
		}

		// Create homes file if not exist
		file = new File(dataPath + silenceFile);
		if ( !file.exists() ) {
			try {
				FileWriter fstream = new FileWriter(dataPath + silenceFile);
				BufferedWriter out = new BufferedWriter(fstream);

				out.write("# Stores silenced user information.");
				out.newLine();
				out.write("# DO NOT EDIT! File is regularly overwritten.");
				out.newLine();

				out.close();
			} catch (Exception e) {
				Messaging.logWarning("Could not write the default silence state file.", plugin);
				return false;
			}
		}

		silenceState.clear();

		try {
			FileReader fstream = new FileReader(dataPath + silenceFile);
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

							silenceState.put(playerName, new SilenceParams(new Date(silenceStart), silenceTime));
						}
					} catch (Exception e) {
						// This entry failed. Ignore and continue.
					}
				}

				line = reader.readLine();
			}

			reader.close();
		} catch (Exception e) {
			Messaging.logSevere("Could not read the silence state file.", plugin);
			return false;
		}

		return true; // success
	}

	/**
	 * Save the silence list to disk.
	 * @return True on success, false on error.
	 */
	public static boolean saveSilenceList() {
		File file = new File(dataPath);
		if ( !(file.exists()) ) {
			file.mkdir();
		}

		try {
			FileWriter fstream = new FileWriter(dataPath + silenceFile);
			BufferedWriter writer = new BufferedWriter(fstream);

			writer.write("# Stores silenced user information.");
			writer.newLine();
			writer.write("# DO NOT EDIT! File is regularly overwritten.");
			writer.newLine();
			writer.newLine();

			for (Entry<String, SilenceParams> entry : silenceState.entrySet()) {
				SilenceParams silence = entry.getValue();

				if (silence.silenced) {
					writer.write(entry.getKey() + ";" + silence.silenceStart.getTime() + ";" + silence.silenceTime);
					writer.newLine();
				}
			}
			writer.close();
		} catch (Exception e) {
			Messaging.logSevere("Could not save the silence state file.", plugin);
			return false;
		}

		return true; // success
	}
	
	/**
	 * Load the ignore data from disk.
	 * @return True if success, otherwise false.
	 */
	public static boolean loadIgnoreList() {
		File file = new File(dataPath);
		if ( !(file.exists()) ) {
			file.mkdir();
		}

		// Create homes file if not exist
		file = new File(dataPath + ignoreFile);
		if ( !file.exists() ) {
			try {
				FileWriter fstream = new FileWriter(dataPath + ignoreFile);
				BufferedWriter out = new BufferedWriter(fstream);

				out.write("# Stores ignored user information.");
				out.newLine();
				out.write("# DO NOT EDIT! File is regularly overwritten.");
				out.newLine();

				out.close();
			} catch (Exception e) {
				Messaging.logWarning("Could not write a default ignored state file. Working only in RAM.", plugin);
			}
		}

		ignoreState.clear();

		try {
			FileReader fstream = new FileReader(dataPath + ignoreFile);
			BufferedReader reader = new BufferedReader(fstream);

			String line = reader.readLine().trim();
			String ignorer = null;

			while (line != null) {
				if (!line.startsWith("#")) {

					if (line.endsWith(":")) {
						String[] names = line.split(":");

						if (names.length != 1)
							Messaging.logWarning("Malformed line loading ignores: " + line, plugin);
						ignorer = names[0];
					} else {
						String[] values = line.split(";");

						try {
							if (values.length > 1 && ignorer == null)
								Messaging.logWarning("Expected '<name>:' loading ignores: " + line, plugin);
							else if (values.length == 3)
							{
								String playerName;
								long silenceStart;
								int silenceTime;

								playerName = values[0];
								silenceStart = Long.parseLong(values[1]);
								silenceTime = Integer.parseInt(values[2]);

								IgnoreParams ignoring = new IgnoreParams(playerName, new Date(silenceStart), silenceTime);

								if ( !ignoreState.containsKey (ignorer)) { // haven't ignored anyone up to now
									ArrayList<IgnoreParams> ignoreList = new ArrayList<IgnoreParams>(1);

									ignoreState.put(ignorer, ignoreList);
								}
								ignoreState.get(ignorer).add(ignoring); // add this IgnoreParams
							}
							else if (values.length != 1)
								Messaging.logWarning("Malformed line loading ignores: " + line, plugin);

						} catch (Exception e) {
							// This entry failed. Ignore and continue.
						}
					}
				}

				line = reader.readLine();
			}

			reader.close();
		} catch (Exception e) {
			Messaging.logSevere("Could not read the ignore state file. Working from blank RAM.", plugin);
		}

		return true; // success
	}

	/**
	 * Saves the ignore data to disk.
	 */
	public static void saveIgnoreList() {
		File file = new File(dataPath);
		if ( !(file.exists()) ) {
			file.mkdir();
		}

		try {
			FileWriter fstream = new FileWriter(dataPath + ignoreFile);
			BufferedWriter writer = new BufferedWriter(fstream);

			writer.write("# Stores ignored user information.");
			writer.newLine();
			writer.write("# DO NOT EDIT! File is regularly overwritten.");
			writer.newLine();
			writer.newLine();

			for (Entry<String, ArrayList<IgnoreParams>> entry : ignoreState.entrySet()) {
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
			Messaging.logSevere("Could not save the ignore state file.", plugin);
		}
	}

	/**
	 * Retrieves an IgnoreParams object for the passed player, or creates one if one doesn't exist.
	 * @param player Player to retrieve ignore parameters from.
	 * @return IgnoreParams object for specified player.
	 */
	public static ArrayList<IgnoreParams> getIgnoreParams(Player player) {
		if (player != null && ignoreState.containsKey(player.getName())) {
			return ignoreState.get(player.getName());
		}
		return new ArrayList<IgnoreParams>();
	}

	/**
	 * Tests to see if a player is being ignored.
	 * @param destName Target of this chat event.
	 * @param chatter Source of this chat event.
	 * @return 0 if no ignore present, otherwise entry number of ignore + 1
	 */
	public static int isUserIgnoring (String destName, String chatter) {
		if (ignoreState.containsKey(destName)) {
			ArrayList<IgnoreParams> ignoreList = ignoreState.get(destName);

			for (int i=0; i< ignoreList.size(); i++) {
				if (ignoreList.get(i).isIgnored (chatter))
					return i+1;
			}
		}
		return 0;
	}

	/**
	 * Sets ignore parameters for a player.
	 * @param ignorer The person submitting the ignore request.
	 * @param ignored The person to be ignored.
	 * @param params IgnoreParams object.
	 */
	public static void setIgnoreParams(Player ignorer, Player ignored, IgnoreParams params) {
		String ignorerName;

		if (ignorer != null)
			ignorerName = ignorer.getName();
		else
			ignorerName = "CONSOLE";
		int index = isUserIgnoring(ignorerName, ignored.getName()) - 1;

		if (params.getSilenced() == false && (index >= 0)) {
			// Ignore is being reset/removed. Delete it from memory.
			ignoreState.get(ignorerName).remove(index);
		} else {
			// Ignore is being updated/added.
			if (index >= 0) {
				// Ignore parameters already exist. Remove them to start afresh.
				ignoreState.get(ignorerName).remove(index);
			}
			else if (!ignoreState.containsKey(ignorerName)) {
				// Create entry to hold this player's ignore list.
				ArrayList<IgnoreParams> ignoreList = new ArrayList<IgnoreParams>(1);

				ignoreState.put(ignorerName, ignoreList);
			}
			// Store the new IgnoreParams object in this player's ignore list.
			ignoreState.get(ignorerName).add(params);
		}
		saveIgnoreList();
	}

	/**
	 * Retrieves a SilenceParams object for the passed player, or creates one if one doesn't exist.
	 * @param player Player to retrieve ignore parameters from.
	 * @return SilenceParams object for specified player.
	 */
	public static SilenceParams getSilenceParams(Player player) {
		if (player != null && silenceState.containsKey(player.getName())) {
			return silenceState.get(player.getName());
		}
		return new SilenceParams();
	}

	/**
	 * Set silence parameters for specified player.
	 * @param player Player to set silence parameters on.
	 * @param params Silence parameters to apply to specified player.
	 */
	public static void setSilenceParams(Player player, SilenceParams params) {
		if (player != null && params.getSilenced() == false && getSilenceParams(player).getSilenced() == true) {
			// Player silence is being removed.
			silenceState.remove(player.getName());
		} else {
			if (silenceState.containsKey(player.getName())) {
				// Remove old entry. This is not actually required, as put() replaces any existing entries.
				silenceState.remove(player.getName());
			}
			// Store the new silence parameters
			silenceState.put(player.getName(), params);
		}
		saveSilenceList();
	}

	/**
	 * Get overriding silence parameters for all players.
	 * @return Silence parameters to apply to all players.
	 */
	public static SilenceParams getGlobalSilenceParams() {
		return globalSilence;
	}
}
