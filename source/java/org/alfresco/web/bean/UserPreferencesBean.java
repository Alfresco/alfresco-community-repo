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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.alfresco.config.Config;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.i18n.I18NUtil;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.context.UIContextService;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.repository.PreferencesService;
import org.alfresco.web.config.LanguagesConfigElement;

/**
 * Simple bean backing the user preferences settings.
 * 
 * @author Kevin Roast
 */
public class UserPreferencesBean
{
   private static final String PREF_STARTLOCATION = "start-location";
   
   private static final String PREF_CONTENTFILTERLANGUAGE = "content-filter-language";
   private static final String MSG_CONTENTALLLANGUAGES = "content_all_languages";

   /** language locale selection */
   private String language = null;
   
   /** content language locale selection */
   private String contentFilterLanguage = null;
   

   /**
    * @return the list of available languages
    */
   public SelectItem[] getLanguages()
   {
       // Get the item selection list
       SelectItem[] items = getLanguageItems(false);
       // Change the current language
       if (this.language == null)
       {
           // first try to get the language that the current user is using
           Locale lastLocale = Application.getLanguage(FacesContext.getCurrentInstance());
           if (lastLocale != null)
           {
              this.language = lastLocale.toString();
           }
           // else we default to the first item in the list
           else if (items.length > 0)
           {
              this.language = (String) items[0].getValue();
           }
           else
           {
               throw new AlfrescoRuntimeException("The language list is empty");
           }
       }
       return items;
   }

   /**
    * @return Returns the language selection.
    */
   public String getLanguage()
   {
      return this.language;
   }

   /**
    * @param language       The language selection to set.
    */
   public void setLanguage(String language)
   {
      this.language = language;
      Application.setLanguage(FacesContext.getCurrentInstance(), this.language);
   }
   
   /**
    * @return current content filter language, or <tt>null</tt> if all languages was selected
    */
   public String getContentFilterLanguage()
   {
      if (this.contentFilterLanguage == null)
      {
          Locale locale = (Locale) PreferencesService.getPreferences().getValue(PREF_CONTENTFILTERLANGUAGE);
          // Null means All Languages
          if (locale == null)
          {
              this.contentFilterLanguage = MSG_CONTENTALLLANGUAGES;
          }
          else
          {
              this.contentFilterLanguage = locale.toString();
          }
      }
      return (contentFilterLanguage.equals(MSG_CONTENTALLLANGUAGES)) ? null : contentFilterLanguage;
   }
   
   /**
    * @param languageStr   A valid locale string or {@link #MSG_CONTENTALLLANGUAGES}
    */
   public void setContentFilterLanguage(String languageStr)
   {
       this.contentFilterLanguage = languageStr;
       Locale language = null;
       if (languageStr.equals(MSG_CONTENTALLLANGUAGES))
       {
           // The generic "All Languages" was selected - persist this as a null
       }
       else
       {
           // It should be a proper locale string
           language = I18NUtil.parseLocale(languageStr);
       }
       PreferencesService.getPreferences().setValue(PREF_CONTENTFILTERLANGUAGE, language);
       
       // Ensure a refresh
       UIContextService.getInstance(FacesContext.getCurrentInstance()).notifyBeans();
   }
   
   /**
    * @return list of items for the content filtering language selection
    */
   public SelectItem[] getContentFilterLanguages()
   {
       // Get the item selection list
       SelectItem[] items = getLanguageItems(true);

       return items;
   }
   
   /**
    * Helper to return the available language items
    * 
    * @param includeAllLanguages    True to include a marker item for "All Languages"
    * @return
    */
   private static SelectItem[] getLanguageItems(boolean includeAllLanguages)
   {
      FacesContext fc = FacesContext.getCurrentInstance();
      Config config = Application.getConfigService(fc).getConfig("Languages");
      LanguagesConfigElement langConfig = (LanguagesConfigElement)config.getConfigElement(
            LanguagesConfigElement.CONFIG_ELEMENT_ID);
      
      List<String> languages = langConfig.getLanguages();
      List<SelectItem> items = new ArrayList<SelectItem>(10);
      if (includeAllLanguages)
      {
         String allLanguagesStr = Application.getMessage(fc, MSG_CONTENTALLLANGUAGES);
         items.add(new SelectItem(MSG_CONTENTALLLANGUAGES, allLanguagesStr));
      }
      for (String locale : languages)
      {
         // get label associated to the locale
         String label = langConfig.getLabelForLanguage(locale);
         items.add(new SelectItem(locale, label));
      }
      
      return items.toArray(new SelectItem[items.size()]);
   }
   
   /**
    * @return the start location for this user (@see NavigationBean)
    */
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
   
   /**
    * @param location   The current start location for this user (@see NavigationBean)
    */
   public void setStartLocation(String location)
   {
      PreferencesService.getPreferences().setValue(PREF_STARTLOCATION, location);
   }
   
   /**
    * @return the list of available start locations
    */
   public SelectItem[] getStartLocations()
   {
      FacesContext fc = FacesContext.getCurrentInstance();
      NavigationBean navigator = (NavigationBean)FacesHelper.getManagedBean(fc, "NavigationBean");
      ResourceBundle msg = Application.getBundle(fc);
      
      List<SelectItem> locations = new ArrayList<SelectItem>(4);
      
      // add My Alfresco location
      locations.add(new SelectItem(
            NavigationBean.LOCATION_MYALFRESCO, msg.getString(NavigationBean.MSG_MYALFRESCO)));
      
      // add My Home location
      locations.add(new SelectItem(
            NavigationBean.LOCATION_HOME, msg.getString(NavigationBean.MSG_MYHOME)));
      
      // add Company Home location if visible
      if (navigator.getCompanyHomeVisible())
      {
         locations.add(new SelectItem(
               NavigationBean.LOCATION_COMPANY, msg.getString(NavigationBean.MSG_COMPANYHOME)));
      }
      
      // add Guest Home location if visible
      if (navigator.getGuestHomeVisible())
      {
         locations.add(new SelectItem(
               NavigationBean.LOCATION_GUEST, msg.getString(NavigationBean.MSG_GUESTHOME)));
      }
      
      return locations.toArray(new SelectItem[locations.size()]);
   }
   
   /**
    * @return true if the Guest user is allowed to configure the user preferences
    */
   public boolean getAllowGuestConfig()
   {
      return Application.getClientConfig(FacesContext.getCurrentInstance()).getAllowGuestConfig();
   }
}
