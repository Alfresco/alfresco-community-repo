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
package org.alfresco.web.bean;

import java.util.ResourceBundle;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.PreferencesService;

/**
 * Simple bean backing the user preferences settings.
 * 
 * @author Kevin Roast
 */
public class UserPreferencesBean
{
   private static final String PREF_STARTLOCATION = "start-location";
   private static final String MSG_MYALFRESCO = "my_alfresco";
   private static final String MSG_MYHOME = "my_home";
   private static final String MSG_COMPANYHOME = "company_home";
   private static final String MSG_GUESTHOME = "guest_home";
   
   
   public String getStartLocation()
   {
      String location = (String)PreferencesService.getPreferences().getValue(PREF_STARTLOCATION);
      if (location == null)
      {
         // default to value from client config
         location = Application.getClientConfig(FacesContext.getCurrentInstance()).getInitialLocation();
      }
      return location;
   }
   
   public void setStartLocation(String location)
   {
      PreferencesService.getPreferences().setValue(PREF_STARTLOCATION, location);
   }
   
   public SelectItem[] getStartLocations()
   {
      ResourceBundle msg = Application.getBundle(FacesContext.getCurrentInstance());
      return new SelectItem[] {
            new SelectItem(NavigationBean.LOCATION_MYALFRESCO, msg.getString(MSG_MYALFRESCO)),
            new SelectItem(NavigationBean.LOCATION_HOME, msg.getString(MSG_MYHOME)),
            new SelectItem(NavigationBean.LOCATION_COMPANY, msg.getString(MSG_COMPANYHOME)),
            new SelectItem(NavigationBean.LOCATION_GUEST, msg.getString(MSG_GUESTHOME))};
   }
}
