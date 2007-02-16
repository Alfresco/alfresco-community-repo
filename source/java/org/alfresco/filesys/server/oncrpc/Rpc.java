/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * and Open Source Software ("FLOSS") applications as described in Alfresco's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing" */
package org.alfresco.filesys.server.oncrpc;

/**
 * ONC/RPC Constants Class
 * 
 * @author GKSpencer
 */
public class Rpc {

	//	RPC length/flags

	public static final int LastFragment 	= 0x80000000;
	public static final int LengthMask 		= 0x7FFFFFFF;

	//	RPC message types

	public static final int Call 			= 0;
	public static final int Reply 			= 1;

	//	Call status

	public static final int CallAccepted 	= 0;
	public static final int CallDenied 		= 1;

	//	Required RPC version

	public static final int RpcVersion 		= 2;

	//	Call accepted status codes

	public static final int StsSuccess 			= 0; //	RPC executed successfully
	public static final int StsProgUnavail 		= 1; //	program not available
	public static final int StsProgMismatch 	= 2; //	program version mismatch
	public static final int StsProcUnavail 		= 3; //	program does not support procedure
	public static final int StsBadArgs 			= 4; //	bad arguments in request

	//	Call rejected status codes

	public static final int StsRpcMismatch 		= 0; //	RPC version number does not equal 2
	public static final int StsAuthError 		= 1; //	authentication error

	//	Authentication failure status codes

	public static final int AuthBadCred 		= 1; //	bad credentials
	public static final int AuthRejectCred 		= 2; //	client must begin new session
	public static final int AuthBadVerf 		= 3; //	bad verifier
	public static final int AuthRejectedVerf 	= 4; //	verifier rejected or replayed
	public static final int AuthTooWeak 		= 5; //	rejected for security reasons

	//	True/false values

	public static final int True 			= 1;
	public static final int False 			= 0;

	//	Protocol ids

	public static final int TCP 			= 6;
	public static final int UDP 			= 17;

	/**
	 * Return a program id as a service name
	 * 
	 * @param progId int
	 * @return String
	 */
	public final static String getServiceName(int progId)
	{
		String svcName = null;

		switch (progId)
		{
		case 100005:
			svcName = "Mount";
			break;
		case 100003:
			svcName = "NFS";
			break;
		case 100000:
			svcName = "Portmap";
			break;
		default:
			svcName = "" + progId;
			break;
		}

		return svcName;
	}
}
