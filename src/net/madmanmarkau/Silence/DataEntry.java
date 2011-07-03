package net.madmanmarkau.Silence;

import java.util.Date;

public class DataEntry {
	private boolean active = false;
	private Date activeStart = new Date();
	private int activeTime = 0;
	private boolean saveRequired = false;
	
	public DataEntry() {
		this.active = false;
		this.activeStart = new Date();
		this.activeTime = 0;
		this.saveRequired = false;
	}
	
	public DataEntry(boolean active) {
		this.active = active;
		this.activeStart = new Date();
		if (active) {
			this.activeTime = -1;
			this.saveRequired = true;
		} else {
			this.activeTime = 0;
			this.saveRequired = false;
		}
	}
	
	public DataEntry(Date activeStart, int activeTime) {
		this.active = true;
		this.activeStart = activeStart;
		this.activeTime = activeTime;
		this.saveRequired = true;
	}
	
	public DataEntry(int activeTime) {
		this.active = true;
		this.activeStart = new Date();
		this.activeTime = activeTime;
		this.saveRequired = true;
	}

	public void setActive(boolean value) {
		this.active = value;
	}
	
	public boolean getActive() {
		return this.active;
	}
	
	public void setActiveStart(Date value) {
		this.activeStart = value;
	}
	
	public Date getActiveStart() {
		return this.activeStart;
	}
	
	public void setActiveTime(int value) {
		this.activeTime = value;
	}
	
	public int getActiveTime() {
		return this.activeTime;
	}
	
	public void activateEntry() {
		this.active = true;
		this.activeStart = new Date();
		this.activeTime = -1;
		this.saveRequired = true;
	}
	
	public void activateEntry(int activeTime) {
		this.active = true;
		this.activeStart = new Date();
		this.activeTime = activeTime;
		this.saveRequired = true;
	}
	
	public void deactivateEntry() {
		this.active = false;
		this.activeStart = new Date();
		this.activeTime = 0;
		this.saveRequired = true;
	}
	
	public boolean updateActiveTimer() {
		if (this.active) {
			if (this.activeTime > 0) {
				Date now = new Date();
				long timeDiff = (now.getTime() - this.activeStart.getTime());

				if (timeDiff > this.activeTime * 1000) {
					this.active = false;
					this.activeStart = new Date();
					this.activeTime = 0;
					this.saveRequired = true;
					return false;
				}
			}
			return true;
		}
		return false;
	}

	protected void setSaveRequired(boolean value) {
		this.saveRequired = value;
	}
	
	public boolean getSaveRequired() {
		return this.saveRequired;
	}
}
