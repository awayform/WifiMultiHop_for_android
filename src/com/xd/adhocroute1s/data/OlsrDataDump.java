package com.xd.adhocroute1s.data;

import java.util.Collection;

public class OlsrDataDump {
	public Config config;
	public Collection<Gateway> gateways;
	public Collection<HNA> hna;
	public Collection<Interface> interfaces;
	public Collection<Link> links;
	public Collection<MID> mid;
	public Collection<Neighbor> neighbors;
	public Collection<Node> topology;
	public Collection<Plugin> plugins;
	public Collection<Route> routes;
	public int systemTime = 0;
	public int timeSinceStartup = 0;
	public String uuid = "";

	String raw = "";

	public void setRaw(String s) {
		raw = s;
	}
	public String getRaw() {
		return raw;
	}
	public String toString(){
		return raw;
	}
}
