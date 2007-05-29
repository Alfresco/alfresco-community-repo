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
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.web.scripts.jsf;

import org.alfresco.web.scripts.WebScriptMatch;
import org.alfresco.web.scripts.WebScriptURLRequest;

/**
 * Implementation of a WebScript Request for the JSF environment.
 * 
 * @author Kevin Roast
 */
public class WebScriptJSFRequest extends WebScriptURLRequest
{

   /**
    * Constructor
    * 
    * @param fc         FacesContext
    * @param match      WebScriptMatch that matched this webscript
    * @param scriptUrl  The script URL this request is for
    */   
   public WebScriptJSFRequest(String scriptUrl, WebScriptMatch match)
   {
       this(splitURL(scriptUrl), match);
   }
   
   public WebScriptJSFRequest(String[] scriptUrlParts, WebScriptMatch match)
   {
       super(scriptUrlParts, match);
   }

   /* (non-Javadoc)
    * @see org.alfresco.web.scripts.WebScriptRequest#getServerPath()
    */
   public String getServerPath()
   {
      // NOTE: not accessable from JSF context - cannot create absolute external urls...
      return "";
   }

   /* (non-Javadoc)
    * @see org.alfresco.web.scripts.WebScriptRequest#getAgent()
    */
   public String getAgent()
   {
      // NOTE: unknown in the JSF environment
      return null;
   }

}
