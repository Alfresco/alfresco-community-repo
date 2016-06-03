package org.alfresco.filesys.repo.desk;

import org.alfresco.filesys.alfresco.DesktopAction;
import org.alfresco.filesys.alfresco.DesktopParams;
import org.alfresco.filesys.alfresco.DesktopResponse;

/**
 * Command Line Desktop Action Class
 * 
 * <p>Simple desktop action that returns a test command line.
 * 
 * @author gkspencer
 */
public class CmdLineDesktopAction extends DesktopAction {

	/**
	 * Class constructor
	 */
	public CmdLineDesktopAction()
	{
		super( 0, PreConfirmAction);
	}
	
	@Override
	public String getConfirmationString() {
		return "Run commandline action";
	}

	@Override
	public DesktopResponse runAction(DesktopParams params) {

		// Return a URL in the status message
		
		return new DesktopResponse(StsCommandLine, "%SystemRoot%\\notepad.exe"); 
	}
}
