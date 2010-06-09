/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.bean;

import java.io.Serializable;

/**
 * A request-scoped bean that is used to propagate the URL to redirect to after successful login. This is now done with
 * request parameters rather than session-scoped beans so that tools such as MS Office, which do not propagate cookies
 * to hyperlinks, can be used to open hyperlinks to protected documents (ALF-206).
 * 
 * @author dward
 */
public class LoginOutcomeBean implements Serializable
{
   /** The name of the request parameter that provides the initial value of the {@link #redirectURL} property. */
   public static final String PARAM_REDIRECT_URL = "_alfRedirect";

   private static final long serialVersionUID = -2575348340143674698L;

   /** The URL to redirect to after successful login. */
   private String redirectURL;

   /**
    * Gets the URL to redirect to after successful login.
    * 
    * @return the URL to redirect to after successful login
    */
   public String getRedirectURL()
   {
      return redirectURL;
   }

   /**
    * Sets the URL to redirect to after successful login.
    * 
    * @param redirectURL
    *           the URL to redirect to after successful login
    */
   public void setRedirectURL(String redirectURL)
   {
      this.redirectURL = redirectURL;
   }

}
