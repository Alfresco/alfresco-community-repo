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
package org.alfresco.web.bean.repository;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.alfresco.web.app.Application;

/**
 * Simple client service to retrieve the Preferences object for the current User.
 * 
 * @author Kevin Roast
 */
public final class PreferencesService
{
   /**
    * Private constructor
    */
   private PreferencesService()
   {
   }
   
   /**
    * @return The Preferences for the current User instance.
    */
   public static Preferences getPreferences()
   {
      return getPreferences(FacesContext.getCurrentInstance());
   }
   
   /**
    * @param fc   FacesContext
    * @return The Preferences for the current User instance.
    */
   public static Preferences getPreferences(FacesContext fc)
   {
      User user = Application.getCurrentUser(fc);
      return user != null ? user.getPreferences(fc) : null;
   }
   
   /**
    * @param user User instance
    * @return The Preferences for the current User instance.
    */
   public static Preferences getPreferences(HttpSession session)
   {
      User user = Application.getCurrentUser(session);
      return user != null ? user.getPreferences(session.getServletContext()) : null;
   }
}
