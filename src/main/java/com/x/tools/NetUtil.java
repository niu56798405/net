package com.x.tools;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class NetUtil {
	private static String toString(InetAddress addr){                             
        byte[] ipAddr = addr.getAddress();     
        String ipAddrStr = "";     
        for (int i = 0; i < ipAddr.length; i++) {     
            if (i > 0) {     
                ipAddrStr += ".";     
            }     
            ipAddrStr += ipAddr[i] & 0xFF;     
        }                   
        return ipAddrStr;     
	}
	
	public static String getLocalIP(){
	    try {
	        InetAddress candidateAddress = null;
	        // 遍历所有的网络接口
	        for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements(); ) {
	            NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
	            // 在所有的接口下再遍历IP
	            for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
	                InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
	                if (!inetAddr.isLoopbackAddress()) {// 排除loopback类型地址
	                    if (inetAddr.isSiteLocalAddress()) {
	                        // 如果是site-local地址，就是它了
	                        return toString(inetAddr);
	                    } else if (candidateAddress == null) {
	                        // site-local类型的地址未被发现，先记录候选地址
	                        candidateAddress = inetAddr;
	                    }
	                }
	            }
	        }
	        if (candidateAddress != null) {
	            return toString(candidateAddress);
	        }
	        // 如果没有发现 non-loopback地址.只能用最次选的方案
	        InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
	        return toString(jdkSuppliedAddress);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return null;
	}
	public static void main(String[] args) {
		System.out.println(getLocalIP());
	}

	
}
