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
package org.alfresco.web.app;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.servlet.ServletContext;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.web.bean.repository.Repository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.support.WebApplicationContextUtils;


/**
 * Wrapper around Alfresco Resource Bundle objects. Used to catch and handle missing
 * resource exception to help identify missing I18N strings in client apps.
 * Also used to look for the requested string in a custom resource bundle.
 * 
 * @author Kevin Roast
 */
public final class ResourceBundleWrapper extends ResourceBundle
{
   private static Log    logger = LogFactory.getLog(ResourceBundleWrapper.class);
   
   private ResourceBundle delegate;
   private ResourceBundle delegateCustom;
   
   private MessageService messageService;
   
   public static final String BEAN_RESOURCE_BUNDLE_WRAPPER = "resourceBundleWrapper";
      
   public static final String PATH = "app:company_home/app:dictionary/app:webclient_extension";

   public ResourceBundleWrapper(MessageService messageService)
   {
      this.messageService = messageService;
   }
   
   // Helper to get the ResourceBundleWrapper instance with access to the repository
   public static ResourceBundleWrapper getResourceBundleWrapper(ServletContext context)
   {
      return (ResourceBundleWrapper)WebApplicationContextUtils.getRequiredWebApplicationContext(context).getBean(
            BEAN_RESOURCE_BUNDLE_WRAPPER);
   }
   
   /**
    * Constructor
    * 
    * @param bundle       The ResourceBundle to route calls too
    * @param customBundle A custom version of bundle to look in if the string is
    *                     not found in bundle
    */
   private ResourceBundleWrapper(ResourceBundle bundle, ResourceBundle customBundle)
   {
      this.delegate = bundle;
      this.delegateCustom = customBundle;
   }
   
   /**
    * @see java.util.ResourceBundle#getKeys()
    */
   public Enumeration<String> getKeys()
   {
      if (this.delegateCustom == null)
      {
         return this.delegate.getKeys();
      }
      else
      {
         // get existing keys
         Enumeration<String> keys = this.delegate.getKeys();
         Enumeration<String> customKeys = this.delegateCustom.getKeys();
         
         // combine keys into one list
         Vector<String> allKeys = new Vector<String>(100, 2);
         while (keys.hasMoreElements())
         {
            allKeys.add(keys.nextElement());
         }
         while (customKeys.hasMoreElements())
         {
            allKeys.add(customKeys.nextElement());
         }
         
         return allKeys.elements();
      }
   }
   
   /**
    * @see java.util.ResourceBundle#handleGetObject(java.lang.String)
    */
   protected Object handleGetObject(String key)
   {
      Object result = null;
      
      try
      {
         result =  this.delegate.getObject(key);
      }
      catch (MissingResourceException err)
      {
         // if the string wasn't found in the normal bundle
         // try the custom bundle if there is one
         try
         {
            if (this.delegateCustom != null)
            {
               result = this.delegateCustom.getObject(key);
            }
         }
         catch (MissingResourceException mre)
         {
            // don't do anything here, dealt with below
         }
         
         // if the key was not found return a default string 
         if (result == null)
         {
            if (logger.isWarnEnabled())
               logger.warn("Failed to find I18N message string key: " + key);
         
            result = "$$" + key + "$$";
         }
      }
      
      return result;
   }
   
   /**
    * Factory method to get a named wrapped resource bundle for a particular locale.
    * 
    * @param name       Bundle name
    * @param locale     Locale to retrieve bundle for
    * 
    * @return Wrapped ResourceBundle instance for specified locale
    */
   public ResourceBundle getResourceBundle(String name, Locale locale)
   {
      ResourceBundle bundle = ResourceBundle.getBundle(name, locale);
      if (bundle == null)
      {
         throw new AlfrescoRuntimeException("Unable to load Alfresco messages bundle: " + name);
      }
      
      // also look up the custom version of the bundle in the extension package
      ResourceBundle customBundle = null;
      
      // first try in the repo otherwise try the classpath
      StoreRef storeRef = null;
      String path = null;
      
      try
      {
          String customName = null;
          int idx = name.lastIndexOf(".");
          if (idx != -1)
          {
              customName = name.substring(idx+1, name.length());
          }
          else
          {
              customName = name;
          }

          storeRef = Repository.getStoreRef();
          
          // TODO - make path configurable in one place ... 
          // Note: path here is XPath for selectNodes query
          path = PATH + "/cm:" + customName;
          InputStream is = messageService.getRepoResourceBundle(Repository.getStoreRef(), path, locale);
          customBundle = new PropertyResourceBundle(is);
          is.close();
      }
      catch (Throwable t)
      {
          // for now ... ignore the error, cannot be found or read from repo
          logger.debug("Custom Web Client properties not found: " + storeRef + path);
      }

      if (customBundle == null)
      {
          // classpath
          String customName = determineCustomBundleName(name);
          try
          {
             customBundle = ResourceBundle.getBundle(customName, locale);
             
             if (logger.isDebugEnabled())
                logger.debug("Located and loaded custom bundle: " + customName);
          }
          catch (MissingResourceException mre)
          {
             // ignore the error, just leave custom bundle as null
          }
      }
      
      // apply our wrapper to catch MissingResourceException
      return new ResourceBundleWrapper(bundle, customBundle);
   }
   
   /**
    * Determines the name for the custom bundle to lookup based on the given
    * bundle name
    * 
    * @param bundleName The standard bundle
    * @return The name of the custom bundle (in the alfresco.extension package)
    */
   protected static String determineCustomBundleName(String bundleName)
   {
      String extensionPackage = "alfresco.extension.";
      String customBundleName = null;
      
      int idx = bundleName.lastIndexOf(".");
      if (idx == -1)
      {
         customBundleName = extensionPackage + bundleName;
      }
      else
      {
         customBundleName = extensionPackage + 
               bundleName.substring(idx+1, bundleName.length());
      }
      
      return customBundleName;
   }
}
