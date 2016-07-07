package com.xd.adhocroute1s.data;

import java.util.Collection;

public class Neighbor {
	public String ipv4Address;
	public boolean symmetric;
	public boolean multiPointRelay;
	public boolean multiPointRelaySelector;
	public int willingness;
	public int twoHopNeighborCount;
	public Collection<String> twoHopNeighbors;
}
