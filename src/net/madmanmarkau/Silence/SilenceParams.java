package net.madmanmarkau.Silence;

import java.util.Date;
import java.util.logging.Logger;

public class SilenceParams {
	public final Logger log = Logger.getLogger("Minecraft");
	
	boolean silenced = false;
	Date silenceStart = new Date();
	int silenceTime = 0;
	boolean saveRequired = false;
	
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
		this.saveRequired = false;
	}
	
	public SilenceParams(boolean silenced) {
		this.silenced = silenced;
		this.silenceStart = new Date();
		if (silenced) {
			this.silenceTime = -1;
			this.saveRequired = true;
		} else {
			this.silenceTime = 0;
			this.saveRequired = false;
		}
	}
	
	public SilenceParams(Date silenceStart, int silenceTime) {
		this.silenced = true;
		this.silenceStart = silenceStart;
		this.silenceTime = silenceTime;
		this.saveRequired = true;
	}
	
	public SilenceParams(int silenceTime) {
		this.silencePlayer(silenceTime);
	}
	
	public void silencePlayer(int silenceTime) {
		this.silenced = true;
		this.silenceStart = new Date();
		this.silenceTime = silenceTime;
		this.saveRequired = true;
	}
	
	public void unsilencePlayer() {
		this.silenced = false;
		this.silenceStart = new Date();
		this.silenceTime = 0;
		this.saveRequired = true;
	}
	
	public boolean updateSilenceTimer() {
		if (this.silenced) {
			//log.info("Silenced!");
			if (this.silenceTime > 0) {
				Date now = new Date();
				long timeDiff = (now.getTime() - this.silenceStart.getTime()) / 1000;

				/*log.info("Silence time: " + this.silenceTime);
				log.info("Silence start: " + this.silenceStart.getTime());
				log.info("Now: " + now.getTime());
				log.info("Diff: " + timeDiff);*/

				if (timeDiff > this.silenceTime) {
					this.unsilencePlayer();
					return true;
				}
			}
			return false;
		}
		//log.info("Not silenced");
		return true;
	}

	public boolean getSaveRequired() {
		return this.saveRequired;
	}
}
