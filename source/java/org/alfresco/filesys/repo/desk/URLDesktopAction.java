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
