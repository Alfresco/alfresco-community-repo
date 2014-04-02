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
package org.alfresco.web.bean.users;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.alfresco.service.cmr.ml.ContentFilterLanguagesService;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.context.UIContextService;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.NavigationBean;
import org.alfresco.web.bean.repository.PreferencesService;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.config.LanguagesConfigElement;
import org.springframework.extensions.config.Config;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Simple bean backing the user preferences settings.
 * 
 * @author Kevin Roast
 */
public class UserPreferencesBean implements Serializable
{
   private static final long serialVersionUID = -1262481849503163054L;

   public static final String PREF_INTERFACELANGUAGE = "interface-language";
   
   private static final String PREF_STARTLOCATION = "start-location";
   private static final String PREF_CONTENTFILTERLANGUAGE = "content-filter-language";
   private static final String PREF_DOWNLOADAUTOMATICALLY = "download-automatically";

   /** 
    * Remplacement message for set the filter at 'all languages'. 
    * Must be considered as a null value.
    */
   public static final String MSG_CONTENTALLLANGUAGES = "content_all_languages";

   /** content language locale selection */
   private String contentFilterLanguage = null;

   /** the injected MultilingualContentService */
   transient private MultilingualContentService multilingualContentService;

   /** the injected ContentFilterLanguagesService */
   transient private ContentFilterLanguagesService contentFilterLanguagesService;

   /** the injected NodeService */
   transient private NodeService nodeService;

   /**
    * @return the list of available languages
    */
   public SelectItem[] getLanguages()
   {
      // Get the item selection list for the list of languages
      return getLanguageItems();
   }

   /**
    * @return Returns the language selection for the current user session.
    */
   public String getLanguage()
   {
      return Application.getLanguage(FacesContext.getCurrentInstance()).toString();
   }
   
   /**
    * @param language       The language selection to set.
    */
   public void setLanguage(String language)
   {
      // set the language for the current session
      Application.setLanguage(FacesContext.getCurrentInstance(), language);

      // save user preference
      if (Application.getCurrentUser(FacesContext.getCurrentInstance()) != null)
      {
         PreferencesService.getPreferences().setValue(PREF_INTERFACELANGUAGE, language);
      }
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
      List<String> languages = getContentFilterLanguagesService().getFilterLanguages();

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
         String label = getContentFilterLanguagesService().getLabelByCode(lang);
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
      List<Locale> missingLocales = getMultilingualContentService().getMissingTranslations(translation, returnTranslationLanguage);

      //    set the item selection list      
      SelectItem[] items = new SelectItem[missingLocales.size()];
      int idx = 0;

      for(Locale locale : missingLocales)
      {
         String label = getContentFilterLanguagesService().getLabelByCode(locale.getLanguage());

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
      if (multilingualContentService == null)
      {
         multilingualContentService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getMultilingualContentService();
      }
      
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
      if (nodeService == null)
      {
         nodeService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getNodeService();
      }
      
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
   
   /**
    * @return the contentFilterLanguagesService
    */
   ContentFilterLanguagesService getContentFilterLanguagesService()
   {
      if (contentFilterLanguagesService == null)
      {
         contentFilterLanguagesService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getContentFilterLanguagesService();
      }
      return contentFilterLanguagesService;
   }

   /**
    * @return download files automatically for offline editing or not
    */
   public boolean isDownloadAutomatically()
   {
      Boolean downloadAutomatically = (Boolean) PreferencesService.getPreferences().getValue(PREF_DOWNLOADAUTOMATICALLY);
      if (downloadAutomatically == null)
      {
         return true;
     }
     else
     {
         return downloadAutomatically.booleanValue();
     }
   }

   /** 
    * @param downloadAutomatically the boolean value to set
    */
   public void setDownloadAutomatically(boolean downloadAutomatically)
   {
      PreferencesService.getPreferences().setValue(PREF_DOWNLOADAUTOMATICALLY, downloadAutomatically);
   }
}
