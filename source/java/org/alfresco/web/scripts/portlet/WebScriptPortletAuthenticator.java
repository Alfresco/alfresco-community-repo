/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.scripts.portlet;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.alfresco.web.scripts.WebScriptDescription.RequiredAuthentication;

/**
 * Web Script Authenticator for the JSR-168 environment
 * 
 * @author davidc
 */
public interface WebScriptPortletAuthenticator
{
   /**
    * Authenticate Web Script execution
    * 
    * @param required  required level of authentication
    * @param isGuest  is Guest accessing the web script
    * @param req  portlet render request
    * @param res  portlet render response
    * 
    * @return true if authorised, false otherwise
    */
   public boolean authenticate(RequiredAuthentication required, boolean isGuest, RenderRequest req, RenderResponse res);
}
