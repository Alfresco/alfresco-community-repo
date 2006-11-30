/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
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
package org.alfresco.filesys.smb.server.repo.desk;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.alfresco.filesys.alfresco.DesktopAction;
import org.alfresco.filesys.alfresco.DesktopParams;
import org.alfresco.filesys.alfresco.DesktopResponse;


/**
 * URL Desktop Action Class
 * 
 * <p>Simple desktop action that returns a test URL.
 * 
 * @author gkspencer
 */
public class URLDesktopAction extends DesktopAction {

	/**
	 * Class constructor
	 */
	public URLDesktopAction()
	{
		super( 0, PreConfirmAction);
	}
	
	@Override
	public String getConfirmationString() {
		return "Run URL action";
	}

	@Override
	public DesktopResponse runAction(DesktopParams params) {

		// Get the local IP address
		
		String ipAddr = null;
		
		try
		{
			ipAddr = InetAddress.getLocalHost().getHostAddress();
		}
		catch (UnknownHostException ex)
		{
		}
		
		// Return a URL in the status message to browse to the folder node
		
		StringBuilder urlStr = new StringBuilder();
		
		urlStr.append( "http://");
		urlStr.append(ipAddr);
		urlStr.append(":8080/alfresco/navigate/browse/workspace/SpacesStore/");
		urlStr.append( params.getFolderNode().getId());
		urlStr.append("?ticket=");
		urlStr.append(params.getTicket());
		
		return new DesktopResponse(StsLaunchURL, urlStr.toString()); 
	}
}
