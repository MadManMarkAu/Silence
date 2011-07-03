package net.madmanmarkau.Silence;

import java.util.Date;

public class IgnoreParams extends DataEntry {
	private String ignoredPlayer;
	private String ignoringPlayer;

	public IgnoreParams(String ignoredPlayer, String ignoringPlayer) {
		super();
		this.setIgnoredPlayer(ignoredPlayer);
		this.setIgnoringPlayer(ignoringPlayer);
		super.setSaveRequired(true);
	}
	
	public IgnoreParams(String ignoredPlayer, String ignoringPlayer, boolean active) {
		super(active);
		this.setIgnoredPlayer(ignoredPlayer);
		this.setIgnoringPlayer(ignoringPlayer);
		super.setSaveRequired(true);
	}
	
	public IgnoreParams(String ignoredPlayer, String ignoringPlayer, Date activeStart, int activeTime) {
		super(activeStart, activeTime);
		this.setIgnoredPlayer(ignoredPlayer);
		this.setIgnoringPlayer(ignoringPlayer);
		super.setSaveRequired(true);
	}
	
	public IgnoreParams(String ignoredPlayer, String ignoringPlayer, int activeTime) {
		super(activeTime);
		this.setIgnoredPlayer(ignoredPlayer);
		this.setIgnoringPlayer(ignoringPlayer);
		super.setSaveRequired(true);
	}

	public void setIgnoredPlayer(String ignoredPlayer) {
		this.ignoredPlayer = ignoredPlayer;
	}

	public String getIgnoredPlayer() {
		return ignoredPlayer;
	}

	public void setIgnoringPlayer(String ignoringPlayer) {
		this.ignoringPlayer = ignoringPlayer;
	}

	public String getIgnoringPlayer() {
		return ignoringPlayer;
	}
}

