/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
