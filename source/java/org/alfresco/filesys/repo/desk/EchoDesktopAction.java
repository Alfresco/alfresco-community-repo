package org.alfresco.filesys.repo.desk;

import java.util.Date;

import org.alfresco.filesys.alfresco.DesktopAction;
import org.alfresco.filesys.alfresco.DesktopParams;
import org.alfresco.filesys.alfresco.DesktopResponse;

/**
 * Echo Desktop Action Class
 * 
 * <p>Simple desktop action that echoes back the received string.
 * 
 * @author gkspencer
 */
public class EchoDesktopAction extends DesktopAction {

	/**
	 * Class constructor
	 */
	public EchoDesktopAction()
	{
		super( 0, PreConfirmAction);
	}
	
	@Override
	public String getConfirmationString() {
		return "Run echo action";
	}

	@Override
	public DesktopResponse runAction(DesktopParams params) {

		// Return a text message
		
		return new DesktopResponse(StsSuccess, "Test message from echo action at " + new Date()); 
	}
}
