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
