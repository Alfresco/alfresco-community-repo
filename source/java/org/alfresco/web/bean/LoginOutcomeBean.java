/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
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
