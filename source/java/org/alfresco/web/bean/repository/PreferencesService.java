/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.web.bean.repository;

import javax.faces.context.FacesContext;

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
      return getPreferences(user);
   }
   
   /**
    * @param user User instance
    * @return The Preferences for the current User instance.
    */
   public static Preferences getPreferences(User user)
   {
      return user.getPreferences();
   }
}
