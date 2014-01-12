package com.bbpos;


public class SimpleMain {
	
	public static void main(String[] args) {
		String bdk = "0123456789ABCDEFFEDCBA9876543210";
		String ksn = "00000232100117e00027";
		String tk1 = "de8bfe769dca885cf3cc312135fe2cccfacf176235f4bdee773d1865334315ed2aefcab613f1884b5d63051703d5a0e2bd5d1988eeabe641bd5d1988eeabe641";
		
		String key = DUKPTServer.GetDataKeyVar(ksn, bdk);
		String decryptedTLV = TripleDES.decrypt(tk1, key);
		System.out.println(key);

		}

}
