// YubicoClient.java
//
// Yubico Java client class that calls Yubico authentication server to
// validate an OTP (One-Time Password) generated by a Yubikey.
// For specification see <http://yubico.com/developers/api/>.

// Copyright (c) 2008, Yubico AB.  All rights reserved.

// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:

// * Redistributions of source code must retain the above copyright
//   notice, this list of conditions and the following disclaimer.

// * Redistributions in binary form must reproduce the above copyright
//   notice, this list of conditions and the following
//   disclaimer in the documentation and/or other materials provided
//   with the distribution.

//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
//  CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
//  INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
//  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
//  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
//  BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
//  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
//  TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
//  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
//  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
//  TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
//  THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
//  SUCH DAMAGE.

// Written by Paul Chen <paul@yubico.com>, March 2008.

package com.yubico;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 Your app instantiate an object of this class, then call verify(OTP) to validate the 
 one-time password (OTP) generated by Yubikey
 */

public class YubicoClient {
	
	static final String YUBICO_AUTH_SRV_URL = "http://api.yubico.com/wsapi/verify?id=";
	
	private int _clientId;
	private String _response;
	
	/**
	 * Initializes the Yubico object.
	 *
	 * @param initId	The Client ID you wish to verify against or operate within.
	 */
	public YubicoClient( int initId ) {
		_clientId = initId;
	}
	
	/**
	 * Returns the ID passed to the initialized Yubico object.
	 * 
	 * @return id	The Client ID passed to the initializing class.
	 */
	public int getId() {
		return _clientId;
	}
	

	public String getLastResponse() {
		return _response;
	}
	
	/**
	 * Verify otp against online verification service.
	 *
	 * @param otp	The OTP to verify, in modhex format.
	 * 
	 * @return bool		Verified OK or not?
	 */
	public boolean verify( String otp ) {

		boolean result = false;
		
		_response = "";
		
		try {			
	        URL srv = new URL(YUBICO_AUTH_SRV_URL + _clientId + "&otp=" + otp);
	        URLConnection conn = srv.openConnection(); 
	        BufferedReader in = new BufferedReader(new InputStreamReader(
	                                conn.getInputStream()));
	        String inputLine;
	        while ((inputLine = in.readLine()) != null) { 
	        	_response += inputLine + "\n";
	            if (inputLine.startsWith("status=")) {
	            	if (inputLine.equals("status=OK")) {
	            		result = true;
	            	}
	            }
	        }
	        in.close();
		} catch (Exception e) {
			System.err.println("Error! " + e.getMessage());
		}

		return result;
		
	} // End of verify
	
	/**
	 * Extract the public ID of a Yubikey from an OTP it generated.
	 *
	 * @param otp	The OTP to extract ID from, in modhex format.
	 * 
	 * @return string	Public ID of Yubikey that generated otp. Between 0 and 12 characters.
	 */
	public String getPublicId(String otp) {
		Integer len = otp.length();
		
		/* The OTP part is always the last 32 bytes of otp. Whatever is before that
		 * (if anything) is the public ID of the Yubikey. The ID can be set to ''
		 * through personalization.
		 */
		return otp.substring(0, len - 32);
	} // End of getPublicId
	
	public static void main (String args []) throws Exception
	{
		if (args.length != 2) {
            System.err.println("\n*** Test your Yubikey against Yubico OTP validation server ***");
            System.err.println("\nUsage: java com.yubico.YubicoClient Auth_ID OTP");
            System.err.println("\nEg. java com.yubico.YubicoClient 28 vvfucnlcrrnejlbuthlktguhclhvegbungldcrefbnku");
            System.err.println("\nTouch Yubikey to generate the OTP. Visit Yubico.com for more details.");
            return;
		}
		
		int authId = Integer.parseInt(args[0]);
		String otp = args[1];
		
		YubicoClient yc = new YubicoClient(authId);
		if (yc.verify(otp)) {
			System.out.println("\n* OTP verified OK");
		} else {
			System.out.println("\n* Failed to verify OTP");
		}
		
		System.out.println("\n* Last response:\n" + yc.getLastResponse());

	} // End of main
	
} // End of class
