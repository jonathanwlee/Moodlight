package edu.cornell.gannett;

import com.galvanic.pipsdk.PIP.PipInfo;

// This class encapsulates all information regarding a specific PIP and
// provides a mapping between PipManager and the UI.
public class PipDataItem
{
	public enum ConnectStatus {
		Disconnected, Connecting, Connected
	};

	public int pipID;
	public String name;
	public String address;
	public String version;
	public int batteryLevel;
	public ConnectStatus connectStatus;
	public boolean streaming;
	public boolean discovered;
	public int stressTrend;
	public boolean registered;

	public PipDataItem(PipInfo pip, boolean discovered, boolean registered)
	{
		this.name = pip.name;
		this.address = pip.btAddress;
		this.pipID = pip.pipID;
		this.stressTrend = -1;
		this.connectStatus = ConnectStatus.Disconnected;
		this.streaming = false;
		this.version = "<>";
		this.batteryLevel = -1;
	}

	@Override
	public String toString()
	{
		return name;
	}
}