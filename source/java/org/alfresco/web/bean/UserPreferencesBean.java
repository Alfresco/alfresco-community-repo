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
import org.alfresco.service.cmr.ml.ContentFilterLanguagesService;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
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

   /** 
    * Remplacement message for set the filter at 'all languages'. 
    * Must be considered as a null value.
    */
   public static final String MSG_CONTENTALLLANGUAGES = "content_all_languages";

   /** language locale selection */
   private String language = null;

   /** content language locale selection */
   private String contentFilterLanguage = null;

   /** the injected MultilingualContentService */
   MultilingualContentService multilingualContentService;

   /** the injected ContentFilterLanguagesService */
   ContentFilterLanguagesService contentFilterLanguagesService;

   /** the injected NodeService */
   NodeService nodeService;

   /**
    * @return the list of available languages
    */
   public SelectItem[] getLanguages()
   {
      // Get the item selection list
      SelectItem[] items = getLanguageItems();
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
      Application.setLanguage(FacesContext.getCurrentInstance(), language);

      // Set the current locale in the server
      I18NUtil.setLocale(I18NUtil.parseLocale(language));
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
            this.contentFilterLanguage = null;
         }
         else
         {
            this.contentFilterLanguage = locale.toString();
         }
      }

      // set the content filter locale on the core                 
      I18NUtil.setContentLocale(I18NUtil.parseLocale(this.contentFilterLanguage));

      return this.contentFilterLanguage;
   }

   /**
    * @param languageStr   A valid locale string or {@link #MSG_CONTENTALLLANGUAGES}
    */
   public void setContentFilterLanguage(String contentFilterLanguage)
   {
      this.contentFilterLanguage = contentFilterLanguage;
      Locale language = null;
      if (contentFilterLanguage.equals(MSG_CONTENTALLLANGUAGES))
      {
         // The generic "All Languages" was selected - persist this as a null
         this.contentFilterLanguage = null;
      }
      else
      {
         // It should be a proper locale string
         language = I18NUtil.parseLocale(contentFilterLanguage);
      }
      PreferencesService.getPreferences().setValue(PREF_CONTENTFILTERLANGUAGE, language);

      // set the content filter locale on the core
      I18NUtil.setContentLocale(language);

      // Ensure a refresh
      UIContextService.getInstance(FacesContext.getCurrentInstance()).notifyBeans();
   }

   /**
    * @return list of items for the content filtering language selection include the label 'all langaguages'
    */
   public SelectItem[] getContentFilterLanguages()
   {
      // Get the item selection list
      return getContentFilterLanguages(true);
   }

   /**
    * @param includeAllLanguages if true, the list must include the label 'all languages'
    * @return list of items for the content filtering language selection
    */
   public SelectItem[] getContentFilterLanguages(boolean includeAllLanguages)
   {
      FacesContext fc = FacesContext.getCurrentInstance();
      ResourceBundle msg = Application.getBundle(fc);

      // get the list of filter languages 
      List<String> languages = contentFilterLanguagesService.getFilterLanguages();

      // set the item selection list      
      SelectItem[] items = new SelectItem[(includeAllLanguages) ? languages.size() + 1 : languages.size()];
      int idx = 0;

      // include the <All Languages> item if needed
      if (includeAllLanguages)
      {
         String allLanguagesStr = msg.getString(MSG_CONTENTALLLANGUAGES);
         items[idx] = new SelectItem(MSG_CONTENTALLLANGUAGES, allLanguagesStr);
         idx++;
      }

      for (String lang : languages)
      {
         String label = contentFilterLanguagesService.getLabelByCode(lang);
         items[idx] = new SelectItem(lang, label);
         idx++;
      }

      return items;
   }

   /**
    * return the list of languages in which the given node hasn't be translated yet.  
    * 
    * @param translation the translatable node ref
    * @param returnTranslationLanguage if true, return the language of the given translation.
    * @return the list of languages
    */
   public SelectItem[] getAvailablesContentFilterLanguages(NodeRef translation, boolean returnTranslationLanguage)
   {
      // get the list of missing translation of this node
      List<Locale> missingLocales = multilingualContentService.getMissingTranslations(translation, returnTranslationLanguage);

      //    set the item selection list      
      SelectItem[] items = new SelectItem[missingLocales.size()];
      int idx = 0;

      for(Locale locale : missingLocales)
      {
         String label = contentFilterLanguagesService.getLabelByCode(locale.getLanguage());

         items[idx] = new SelectItem(
               locale.toString(),
               label);
         idx++;
      }

      return items;
   }

   /**
    * Helper to return the available language items
    * 
    * @param includeAllLanguages    True to include a marker item for "All Languages"
    * @return Array of SelectItem objects
    */
   private static SelectItem[] getLanguageItems()
   {
      FacesContext fc = FacesContext.getCurrentInstance();
      Config config = Application.getConfigService(fc).getConfig("Languages");
      LanguagesConfigElement langConfig = (LanguagesConfigElement)config.getConfigElement(
            LanguagesConfigElement.CONFIG_ELEMENT_ID);

      List<String> languages = langConfig.getLanguages();
      List<SelectItem> items = new ArrayList<SelectItem>(10);

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

   /**
    * @return the multilingualContentService
    */
   public MultilingualContentService getMultilingualContentService() 
   {
      return multilingualContentService;
   }

   /**
    * @param multilingualContentService the multilingualContentService to set
    */
   public void setMultilingualContentService(
         MultilingualContentService multilingualContentService) 
   {
      this.multilingualContentService = multilingualContentService;
   }

   /**
    * @return the nodeService
    */
   public NodeService getNodeService() 
   {
      return nodeService;
   }

   /**
    * @param nodeService the nodeService to set
    */
   public void setNodeService(NodeService nodeService) 
   {
      this.nodeService = nodeService;
   }

   /**
    * @param contentFilterLanguagesService the contentFilterLanguagesService to set
    */
   public void setContentFilterLanguagesService(
         ContentFilterLanguagesService contentFilterLanguagesService) 
   {
      this.contentFilterLanguagesService = contentFilterLanguagesService;
   }
}
