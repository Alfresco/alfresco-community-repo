/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
