package net.madmanmarkau.Silence;

import java.util.Date;

public class IgnoreParams extends SilenceParams {
	String ignoreName;

	// All constructors require a name
	public IgnoreParams(String name) {
		super ();
		this.ignoreName = name;
	}

	public IgnoreParams(String name, boolean silenced) {
		super (silenced);
		this.ignoreName = name;
	}

	public IgnoreParams(String name, Date silenceStart, int silenceTime) {
		super (silenceStart, silenceTime);
		this.ignoreName = name;
	}

	public IgnoreParams(String name, int silenceTime) {
		super (silenceTime);
		this.ignoreName = name;
	}

	public String getName () {
		return this.ignoreName;
	}

	public boolean isIgnored (String username) {
		if (username.compareTo (this.ignoreName) == 0)
			return !this.updateSilenceTimer();
		else
			return false;
	}

	// Since we have a name upon creation, don't need a name in these.
	public void ignorePlayer(int silenceTime) {
		this.silencePlayer (silenceTime);
	}

	public void unignorePlayer() {
		this.unsilencePlayer ();
	}

}

