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
package org.alfresco.web.config;

import java.util.Iterator;

import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigException;
import org.alfresco.config.xml.elementreader.ConfigElementReader;
import org.dom4j.Element;

/**
 * @author Kevin Roast
 */
public class MimeTypesElementReader implements ConfigElementReader
{
   public final static String ELEMENT_MIMETYPES    = "mimetypes";
   public final static String ELEMENT_MIMEMAPPING  = "mime-mapping";
   public final static String ELEMENT_EXTENSION    = "extension";
   public final static String ELEMENT_MIMETYPE     = "mime-type";
   
   /**
    * @see org.alfresco.config.xml.elementreader.ConfigElementReader#parse(org.dom4j.Element)
    */
   public ConfigElement parse(Element element)
   {
      MimeTypeConfigElement configElement = null;
      
      if (element != null)
      {
         String name = element.getName();
         if (name.equals(ELEMENT_MIMETYPES) == false)
         {
            throw new ConfigException("MimeTypesElementReader can only parse " +
                  ELEMENT_MIMETYPES + "elements, the element passed was '" + 
                  name + "'");
         }
         
         configElement = new MimeTypeConfigElement();
         
         // walk the mime-mapping elements
         Iterator<Element> mappings = element.elementIterator(ELEMENT_MIMEMAPPING);
         while (mappings.hasNext())
         {
            Element mapping = mappings.next(); 
            Element extensionElement = mapping.element(ELEMENT_EXTENSION);
            Element mimetypeElement = mapping.element(ELEMENT_MIMETYPE);
            
            if (extensionElement == null || mimetypeElement == null)
            {
               throw new ConfigException("mime-mapping element must specify 'extension' and 'mime-type'"); 
            }
            
            String extension = extensionElement.getTextTrim();
            String mimetype = mimetypeElement.getTextTrim();
            
            if (extension == null || extension.length() == 0)
            {
               throw new ConfigException("mime-mapping extension element value must be specified");
            }
            if (mimetype == null || mimetype.length() == 0)
            {
               throw new ConfigException("mime-mapping mimetype element value must be specified");
            }
            
            // add the mimetype extension to the config element
            configElement.addMapping(extension, mimetype);
         }
      }
      
      return configElement;
   }
}
