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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;

import org.alfresco.web.scripts.WebScriptMatch;
import org.alfresco.web.scripts.WebScriptRequest;
import org.alfresco.web.scripts.portlet.WebScriptPortletRequest;

/**
 * Implementation of a WebScript Request for the JSF environment.
 * 
 * @author Kevin Roast
 */
public class WebScriptJSFRequest implements WebScriptRequest
{
   private WebScriptMatch match;
   private FacesContext fc;
   private String[] scriptUrlParts;
   private Map<String, String> args = new HashMap<String, String>(4, 1.0f);

   /**
    * Constructor
    * 
    * @param fc         FacesContext
    * @param match      WebScriptMatch that matched this webscript
    * @param scriptUrl  The script URL this request is for
    */
   WebScriptJSFRequest(FacesContext fc, WebScriptMatch match, String scriptUrl)
   {
      this.fc = fc;
      this.match = match;
      this.scriptUrlParts = WebScriptPortletRequest.getScriptUrlParts(scriptUrl);
      if (this.scriptUrlParts[3] != null)
      {
         String[] parts = this.scriptUrlParts[3].split("&");
         for (String argument : parts)
         {
            int sepIndex = argument.indexOf('=');
            if (sepIndex != -1)
            {
               String value = "";
               if (argument.length() > sepIndex + 1)
               {
                  value = argument.substring(sepIndex + 1);
               }
               this.args.put(argument.substring(0, sepIndex), value);
            }
         }
      }
   }

   /**
    * Gets the matching API Service for this request
    * 
    * @return  the service match
    */
   public WebScriptMatch getServiceMatch()
   {
      return this.match;
   }

   /**
    * Get server portion of the request
    *
    * e.g. scheme://host:port
    *  
    * @return  server path
    */
   public String getServerPath()
   {
      // NOTE: not accessable from JSF context - cannot create absolute external urls...
      return "";
   }

   /**
    * @see org.alfresco.web.scripts.WebScriptRequest#getContextPath()
    */
   public String getContextPath()
   {
      return fc.getExternalContext().getRequestContextPath();
   }

   /**
    * Gets the Alfresco Web Script Context Path
    * 
    * @return  service url  e.g. /alfresco/service
    */
   public String getServiceContextPath()
   {
      return fc.getExternalContext().getRequestContextPath() + scriptUrlParts[1];
   }

   /**
    * Gets the Alfresco Service Path
    * 
    * @return  service url  e.g. /alfresco/service/search/keyword
    */
   public String getServicePath()
   {
      return getServiceContextPath() + scriptUrlParts[2];
   }

   /**
    * Gets the full request URL
    * 
    * @return  request url e.g. /alfresco/service/search/keyword?q=term
    */
   public String getURL()
   {
      return getServicePath() + (scriptUrlParts[3] != null ? "?" + scriptUrlParts[3] : "");
   }

   /**
    * @see org.alfresco.web.scripts.WebScriptRequest#getQueryString()
    */
   public String getQueryString()
   {
      return scriptUrlParts[3];
   }

   /**
    * @see org.alfresco.web.scripts.WebScriptRequest#getParameterNames()
    */
   public String[] getParameterNames()
   {
      Set<String> keys = this.args.keySet();
      String[] names = new String[keys.size()];
      keys.toArray(names);
      return names;
   }

   /**
    * @see org.alfresco.web.scripts.WebScriptRequest#getParameter(java.lang.String)
    */
   public String getParameter(String name)
   {
      return this.args.get(name);
   }

   /**
    * Gets the path extension beyond the path registered for this service
    * 
    * e.g.
    * a) service registered path = /search/engine
    * b) request path = /search/engine/external
    * 
    * => /external
    * 
    * @return  extension path
    */
   public String getExtensionPath()
   {
      String servicePath = this.scriptUrlParts[2];
      if (servicePath.indexOf('/') != -1)
      {
         return servicePath.substring(servicePath.indexOf('/'));
      }
      else
      {
         return null;
      }
   }

   /**
    * Determine if Guest User?
    * 
    * @return  true => guest user
    */
   public boolean isGuest()
   {
      return Boolean.valueOf(getParameter("guest"));
   }

   /**
    * Get Requested Format
    * 
    * @return  content type requested
    */
   public String getFormat()
   {
      String format = getParameter("format");
      return (format == null || format.length() == 0) ? "" : format;
   }

   /**
    * Get User Agent
    * 
    * TODO: Expand on known agents
    * 
    * @return  MSIE / Firefox
    */
   public String getAgent()
   {
      // NOTE: unknown in the JSF environment
      return null;
   }
}
