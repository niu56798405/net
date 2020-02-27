package com.x.rpc;

public class Node {
	private ServerType serverType;
	private int id;//服务id
	private String host;
	private int port;
	
	public Node() {
		super();
	}
	
	public Node(ServerType serverType, int id, String host, int port) {
		super();
		this.serverType = serverType;
		this.id = id;
		this.host = host;
		this.port = port;
	}
	
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public ServerType getServerType() {
		return serverType;
	}
	public void setServerType(ServerType serverType) {
		this.serverType = serverType;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((serverType == null) ? 0 : serverType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (id != other.id)
			return false;
		if (serverType != other.serverType)
			return false;
		return true;
	}
	
	
	
}
