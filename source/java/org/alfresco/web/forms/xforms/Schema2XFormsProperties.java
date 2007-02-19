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
 * http://www.alfresco.com/legal/licensing" */
package org.alfresco.web.forms.xforms;

import java.io.InputStream;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.forms.Form;
import org.springframework.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Ariel Backenroth
 */
public class Schema2XFormsProperties
{
   /////////////////////////////////////////////////////////////////////////////

   private class ResourceBundleWrapper
      extends ResourceBundle
   {

      private final ResourceBundle bundle;
      private final ResourceBundle parent;

      public ResourceBundleWrapper(final ResourceBundle bundle, 
                                   final ResourceBundle parent)
      {
         LOGGER.debug("creating bundle " + bundle + 
                      " with parent " + parent);
         this.bundle = bundle;
         this.parent = parent;
      }

      public Enumeration<String> getKeys()
      {
         final Enumeration<String> bk = bundle.getKeys();
         final Enumeration<String> pk = parent.getKeys();
         return new Enumeration<String>()
         {

            public boolean hasMoreElements()
            {
               return bk.hasMoreElements() || pk.hasMoreElements();
            }

            public String nextElement()
            {
               return (bk.hasMoreElements() 
                       ? bk.nextElement() 
                       : pk.nextElement());
            }
         };
      }

      protected Object handleGetObject(final String key)
      {
         try
         {
            return this.bundle.getObject(key);
         }
         catch (MissingResourceException mre1)
         {
            try
            {
               return this.parent.getObject(key);
            }
            catch (MissingResourceException mre2)
            {
               return null;
            }
         }
      }
   }

   /////////////////////////////////////////////////////////////////////////////

   private static final Log LOGGER = LogFactory.getLog(Schema2XFormsProperties.class);

   private final ContentService contentService;
   private final NamespaceService namespaceService;
   private final NodeService nodeService;
   private final SearchService searchService;
   private String[] locations;

   public Schema2XFormsProperties(final ContentService contentService,
                                  final NamespaceService namespaceService,
                                  final NodeService nodeService,
                                  final SearchService searchService)
   {
      this.contentService = contentService;
      this.namespaceService = namespaceService;
      this.nodeService = nodeService;
      this.searchService = searchService;
   }

   public ResourceBundle getResourceBundle(final Form form, final Locale locale)
   {
      final LinkedList<ResourceBundle> bundles = new LinkedList<ResourceBundle>();
      for (String location : this.locations)
      {
         ClassLoader loader = Thread.currentThread().getContextClassLoader();
         location = location.replace("${form.name}", 
                                     (NamespaceService.CONTENT_MODEL_PREFIX + 
                                      ':' + form.getName()));
         if (location.startsWith("alfresco:"))
         {
            location = location.substring("alfresco:".length());
            loader = new ClassLoader(loader)
            {
               public InputStream getResourceAsStream(String name)
               {
                  LOGGER.debug("loading resource " + name);
                  final ResultSet results = 
                     searchService.query(Repository.getStoreRef(),
                                         SearchService.LANGUAGE_LUCENE,
                                         "PATH:\"" + name + "\"");
                  LOGGER.debug("search returned " + results.length() + 
                               " results");
                  if (results.length() == 1)
                  {
                     final NodeRef nr = results.getNodeRef(0);
                     final ContentReader reader =
                        contentService.getReader(nr, ContentModel.PROP_CONTENT);
                     return reader.getContentInputStream();
                  }
                  else
                  {
                     return super.getResourceAsStream(name);
                  }
               }
            };
         }
         else if (location.startsWith("classpath:"))
         {
            location = location.substring("classpath:".length());
         }

         LOGGER.debug("using loader " + loader + " for location " + location);
         try
         {
            final ResourceBundle rb = ResourceBundle.getBundle(location, 
                                                               locale,
                                                               loader);
            LOGGER.debug("found bundle " + rb + " for location " + location);
            bundles.addFirst(rb);
         }
         catch (MissingResourceException mse)
         {
            LOGGER.debug("unable to located bundle at " + location + 
                         ": " + mse.getMessage());
         }
      }

      ResourceBundle result = null;
      for (ResourceBundle rb : bundles)
      {
         result = (result == null ? rb : new ResourceBundleWrapper(rb, result));
      }
      return result;
   }

   public void setLocations(final String[] locations)
   {
      this.locations = locations;
   }

   public String toString()
   {
      return (this.getClass().getName() + "{" +
              StringUtils.arrayToCommaDelimitedString(this.locations) +
              "}");
   }
}