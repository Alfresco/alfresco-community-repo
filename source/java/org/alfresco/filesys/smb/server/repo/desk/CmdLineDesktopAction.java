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
