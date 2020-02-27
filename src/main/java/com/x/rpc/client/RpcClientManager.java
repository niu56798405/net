package com.x.rpc.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.experimental.theories.Theories;

import com.x.rpc.Node;
import com.x.rpc.ServerType;

public class RpcClientManager {
	private static ThreadLocal<RpcClient> threadLocal = new ThreadLocal<>();
	private static Map<ServerType, Map<Integer, RpcClient>> clients = new ConcurrentHashMap<>();  
	static RpcClient client_;
	public static void set(RpcClient client){
		client_ = client;
	}
	
	public static RpcClient get(){
		return client_;
	}
	
	
	public static void registeNode(Node node){
		ServerType type = node.getServerType();
		int id = node.getId();
		Map<Integer, RpcClient> map = clients.get(type);
		if (map == null) {
			map = new ConcurrentHashMap<>();
		}
		RpcClient rpcClient = map.get(id);
		if (rpcClient != null) {
			rpcClient.close();
		}						
		rpcClient = new RpcClient(node.getHost(), node.getPort(), id, type);		
		map.put(id, rpcClient);
		clients.put(type, map);		
	}
	
	public static int setGameServer(long playerId){
		Map<Integer, RpcClient> map = clients.get(ServerType.GAME);
		int serverId = (int) (playerId / 1000000);
		RpcClient rpcClient = map.get(serverId);
		set(rpcClient);
		return serverId;
	}
	
	public static void setManager(){
		Map<Integer, RpcClient> map = clients.get(ServerType.MANAGER);
		threadLocal.set(map.get(1));
	}
}
