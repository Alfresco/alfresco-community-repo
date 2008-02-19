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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
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
public final class ResourceBundleWrapper extends ResourceBundle implements Serializable
{
   private static final long serialVersionUID = -3230653664902689948L;
   private static Log logger = LogFactory.getLog(ResourceBundleWrapper.class);
   
   /** List of custom bundle names */
   private static List<String> addedBundleNames = new ArrayList<String>(10);

   /** List of delegate resource bundles */
   transient private List<ResourceBundle> delegates;
   
   public static final String BEAN_RESOURCE_MESSAGE_SERVICE = "messageService";      
   public static final String PATH = "app:company_home/app:dictionary/app:webclient_extension";
   
   /**
    * Constructor
    *
    * @param bundles    the resource bundles including the default, custom and any added
    */
   private ResourceBundleWrapper(List<ResourceBundle> bundles)
   {
      this.delegates = bundles;
   }
   
   /**
    * @see java.util.ResourceBundle#getKeys()
    */
   public Enumeration<String> getKeys()
   {
      if (this.delegates.size() == 1)
      {
         return this.delegates.get(0).getKeys();
      }
      else
      {
         Vector<String> allKeys = new Vector<String>(100, 2); 
         for (ResourceBundle delegate : this.delegates) 
         {
             Enumeration<String> keys = delegate.getKeys();
             while(keys.hasMoreElements() == true)
             {
                 allKeys.add(keys.nextElement());
             }
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
      
      for (ResourceBundle delegate : this.delegates) 
      {
         try
         {
            // Try and lookup the key from the resource bundle 
            result = delegate.getObject(key);
            if (result != null)
            {
                break;
            }
         }
         catch (MissingResourceException mre)
         {
            // ignore as this means the key was not present  
         }
      }
   
      // if the key was not found return a default string 
      if (result == null)
      {
         if (logger.isWarnEnabled() == true)
         {
            logger.warn("Failed to find I18N message string key: " + key);
         }
      
         result = "$$" + key + "$$";
      }
      
      return result;
   }
   
   /**
    * Factory method to get a named wrapped resource bundle for a particular locale.
    * 
    * @param servletContext     ServletContext
    * @param name               Bundle name
    * @param locale             Locale to retrieve bundle for
    * 
    * @return Wrapped ResourceBundle instance for specified locale
    */
   public static ResourceBundle getResourceBundle(ServletContext servletContext, String name, Locale locale)
   {
       List<ResourceBundle> bundles = new ArrayList<ResourceBundle>(ResourceBundleWrapper.addedBundleNames.size() + 2);

       // Load the default bundle
       ResourceBundle bundle = ResourceBundle.getBundle(name, locale);
       if (bundle == null)
       {
          throw new AlfrescoRuntimeException("Unable to load Alfresco messages bundle: " + name);
       }
       bundles.add(bundle);
       
       // also look up the custom version of the bundle in the extension package
       ResourceBundle customBundle = null;
       
       if (servletContext != null)
       {
           MessageService messageService = (MessageService)WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext).getBean(BEAN_RESOURCE_MESSAGE_SERVICE);
           
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
               customBundle = messageService.getRepoResourceBundle(Repository.getStoreRef(), path, locale);
           }
           catch (Throwable t)
           {
               // for now ... ignore the error, cannot be found or read from repo
               logger.debug("Custom Web Client properties not found: " + storeRef + path);
           }
       }
       
       if (customBundle == null)
       {
           // also look up the custom version of the bundle in the extension package
           String customName = determineCustomBundleName(name);
           try
           {
              customBundle = ResourceBundle.getBundle(customName, locale);
            
              if (logger.isDebugEnabled()== true)
              {
                 logger.debug("Located and loaded custom bundle: " + customName);
              }
           }
           catch (MissingResourceException mre)
           {
              // ignore the error, just leave custom bundle as null
           }
       }
       
       // Add the custom bundle to the list
       if (customBundle != null)
       {
           bundles.add(customBundle);
       }

       // Add any additional bundles
       for (String bundleName : ResourceBundleWrapper.addedBundleNames) 
       {
           try
           {
              // Load the added bundle 
              ResourceBundle addedBundle = ResourceBundle.getBundle(bundleName, locale);
              bundles.add(addedBundle);
              
              if (logger.isDebugEnabled())
              {
                 logger.debug("Located and loaded added bundle: " + bundleName);
              }
           }
           catch (MissingResourceException mre)
           {
              // ignore the error, just log some debug info
              if (logger.isDebugEnabled())
              {
                 logger.debug("Unable to load added bundle: " + bundleName);
              }
           }
       }
       
       // apply our wrapper to catch MissingResourceException
       return new ResourceBundleWrapper(bundles);
   }
   
   /**
    * Adds a resource bundle to the collection of custom bundles available
    * 
    * @param name   the name of the resource bundle
    */
   public static void addResourceBundle(String name)
   {
       ResourceBundleWrapper.addedBundleNames.add(name);
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
