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
package org.alfresco.web.app;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.alfresco.error.AlfrescoRuntimeException;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

/**
 * Wrapper around Alfresco Resource Bundle objects. Used to catch and handle missing
 * resource exception to help identify missing I18N strings in client apps.
 * <p>
 * Also provides a factory method to get/create a shared instance to a named resource
 * bundle for a particular locale.
 * 
 * @author Kevin Roast
 */
public final class ResourceBundleWrapper extends ResourceBundle
{
   private static Logger logger = Logger.getLogger(ResourceBundleWrapper.class);
   
   private ResourceBundle delegate;
   
   private static Map<String, ResourceBundle> cache = new HashMap<String, ResourceBundle>();  
   
   /**
    * Constructor
    * 
    * @param bundle     The ResourceBundle to route calls too
    */
   private ResourceBundleWrapper(ResourceBundle bundle)
   {
      this.delegate = bundle;
   }
   
   /**
    * @see java.util.ResourceBundle#getKeys()
    */
   public Enumeration<String> getKeys()
   {
      return this.delegate.getKeys();
   }
   
   /**
    * @see java.util.ResourceBundle#handleGetObject(java.lang.String)
    */
   protected Object handleGetObject(String key)
   {
      try
      {
         return this.delegate.getObject(key);
      }
      catch (MissingResourceException err)
      {
         if (logger.isEnabledFor(Priority.WARN))
            logger.warn("Failed to find I18N message string key: " + key);
         
         return "$$" + key + "$$";
      }
   }
   
   /**
    * Factory method to get/create a shared instance to a named resource bundle for a
    * particular locale. A static cache of language bundles is used to save memory, as each
    * bundle consumes >300K per user and they are read only objects.
    * 
    * @param name       Bundle name
    * @param locale     Locale to retrieve bundle for
    * 
    * @return Shared ResourceBundle instance for specified locale
    */
   public static ResourceBundle findSharedResourceBundle(String name, Locale locale)
   {
      String key = name + '_' + locale;
      
      ResourceBundle bundle = cache.get(key);
      if (bundle == null)
      {
         // we can safely use a weak synchronization point here - i.e. it doesn't actually matter
         // if more than one thread loads and caches the same bundle if a race condition occurs
         synchronized (cache)
         {
            bundle = ResourceBundle.getBundle(name, locale);
            if (bundle == null)
            {
               throw new AlfrescoRuntimeException("Unable to load Alfresco messages bundle: " + name);
            }
            
            // apply our wrapper to catch MissingResourceException
            bundle = new ResourceBundleWrapper(bundle);
            
            // cache the bundle for later use
            cache.put(key, bundle);
         }
      }
      
      return bundle;
   }
}
