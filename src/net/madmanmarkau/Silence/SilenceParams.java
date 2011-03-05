package net.madmanmarkau.Silence;

import java.util.Date;

public class SilenceParams {
	boolean silenced = false;
	Date silenceStart = new Date();;
	int silenceTime = 0;
	
	public void setSilenced(boolean value) {
		this.silenced = value;
	}
	
	public boolean getSilenced() {
		return this.silenced;
	}
	
	public void setSilenceStart(Date value) {
		this.silenceStart = value;
	}
	
	public Date getSilenceStart() {
		return this.silenceStart;
	}
	
	public void setSilenceTime(int value) {
		this.silenceTime = value;
	}
	
	public int getSilenceTime() {
		return this.silenceTime;
	}
	
	public SilenceParams() {
		this.silenced = false;
		this.silenceStart = new Date();
		this.silenceTime = 0;
	}
	
	public SilenceParams(boolean silenced) {
		this.silenced = silenced;
		this.silenceStart = new Date();
		this.silenceTime = -1;
	}
	
	public SilenceParams(int silenceTime) {
		this.silencePlayer(silenceTime);
	}
	
	public void silencePlayer(int silenceTime) {
		this.silenced = true;
		this.silenceStart = new Date();
		this.silenceTime = silenceTime;
	}
	
	public void unsilencePlayer() {
		this.silenced = false;
		this.silenceStart = new Date();
		this.silenceTime = 0;
	}
}
