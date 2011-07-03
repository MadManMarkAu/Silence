package net.madmanmarkau.Silence;

import java.util.Date;

public class SilenceParams extends DataEntry {
	private String silencedPlayer = "";
	
	public SilenceParams(String silencedPlayer) {
		super();
		this.setSilencedPlayer(silencedPlayer);
		super.setSaveRequired(true);
	}
	
	public SilenceParams(String silencedPlayer, boolean active) {
		super(active);
		this.setSilencedPlayer(silencedPlayer);
		super.setSaveRequired(true);
	}
	
	public SilenceParams(String silencedPlayer, Date activeStart, int activeTime) {
		super(activeStart, activeTime);
		this.setSilencedPlayer(silencedPlayer);
		super.setSaveRequired(true);
	}
	
	public SilenceParams(String silencedPlayer, int activeTime) {
		super(activeTime);
		this.setSilencedPlayer(silencedPlayer);
		super.setSaveRequired(true);
	}

	public void setSilencedPlayer(String silencedPlayer) {
		this.silencedPlayer = silencedPlayer;
	}

	public String getSilencedPlayer() {
		return silencedPlayer;
	}
}
