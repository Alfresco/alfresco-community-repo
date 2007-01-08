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
   private static final String MSG_MYALFRESCO = "my_alfresco";
   private static final String MSG_MYHOME = "my_home";
   private static final String MSG_COMPANYHOME = "company_home";
   private static final String MSG_GUESTHOME = "guest_home";
   
   private static final String PREF_CONTENTFILTERLANGUAGE = "content-filter-language";
   private static final String MSG_CONTENTALLLANGUAGES = "content_all_languages";

   /** language locale selection */
   private String language = null;
   
   /** content language locale selection */
   private String contentFilterLanguage = null;
   
   private SelectItem[] getLanguageItems(boolean includeAllLanguages)
   {
       Config config = Application.getConfigService(FacesContext.getCurrentInstance()).getConfig("Languages");
       LanguagesConfigElement langConfig = (LanguagesConfigElement)config.getConfigElement(
             LanguagesConfigElement.CONFIG_ELEMENT_ID);
       
       List<String> languages = langConfig.getLanguages();
       List<SelectItem> items = new ArrayList<SelectItem>(20);
       if (includeAllLanguages)
       {
           ResourceBundle msg = Application.getBundle(FacesContext.getCurrentInstance());
           String allLanguagesStr = msg.getString(MSG_CONTENTALLLANGUAGES);
           items.add(new SelectItem(MSG_CONTENTALLLANGUAGES, allLanguagesStr));
       }
       for (String locale : languages)
       {
          // get label associated to the locale
          String label = langConfig.getLabelForLanguage(locale);
          items.add(new SelectItem(locale, label));
       }
       
       SelectItem[] result = new SelectItem[items.size()];
       return items.toArray(result);
   }

   /**
    * @return the available languages
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
      return contentFilterLanguage;
   }
   
   /**
    * @param languageStr A valid locale string or {@link #MSG_CONTENTALLLANGUAGES}
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
   }
   
   public SelectItem[] getContentFilterLanguages()
   {
       // Get the item selection list
       SelectItem[] items = getLanguageItems(true);

       return items;
   }
   
   
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
