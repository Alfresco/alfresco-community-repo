/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.filesys.repo.desk;

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
