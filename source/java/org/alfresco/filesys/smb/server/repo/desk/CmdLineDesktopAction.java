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

import org.alfresco.filesys.smb.server.repo.DesktopAction;
import org.alfresco.filesys.smb.server.repo.DesktopParams;
import org.alfresco.filesys.smb.server.repo.DesktopResponse;

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
