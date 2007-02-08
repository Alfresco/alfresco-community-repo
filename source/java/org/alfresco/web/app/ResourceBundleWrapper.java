/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.web.app;

import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;

import org.alfresco.error.AlfrescoRuntimeException;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

/**
 * Wrapper around Alfresco Resource Bundle objects. Used to catch and handle missing
 * resource exception to help identify missing I18N strings in client apps.
 * Also used to look for the requested string in a custom resource bundle.
 * 
 * @author Kevin Roast
 */
public final class ResourceBundleWrapper extends ResourceBundle
{
   private static Logger logger = Logger.getLogger(ResourceBundleWrapper.class);
   
   private ResourceBundle delegate;
   private ResourceBundle delegateCustom;
   
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
            if (logger.isEnabledFor(Priority.WARN))
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
   public static ResourceBundle getResourceBundle(String name, Locale locale)
   {
      ResourceBundle bundle = ResourceBundle.getBundle(name, locale);
      if (bundle == null)
      {
         throw new AlfrescoRuntimeException("Unable to load Alfresco messages bundle: " + name);
      }
      
      // also look up the custom version of the bundle in the extension package
      String customName = determineCustomBundleName(name);
      ResourceBundle customBundle = null;
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
