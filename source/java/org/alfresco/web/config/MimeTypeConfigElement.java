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

import java.util.HashMap;
import java.util.Map;

import org.alfresco.config.ConfigElement;
import org.alfresco.config.element.ConfigElementAdapter;

/**
 * @author Kevin Roast
 */
public final class MimeTypeConfigElement extends ConfigElementAdapter
{
   /**
    * Default Constructor
    */
   public MimeTypeConfigElement()
   {
      super(MimeTypesElementReader.ELEMENT_MIMETYPES);
   }
   
   /**
    * Constructor
    * 
    * @param mappings      Map of mimetype elements to use
    */
   public MimeTypeConfigElement(Map<String, String> mappings)
   {
      super(MimeTypesElementReader.ELEMENT_MIMETYPES);
      this.mimetypes = mappings;
   }

   /**
    * @see org.alfresco.config.element.ConfigElementAdapter#combine(org.alfresco.config.ConfigElement)
    */
   public ConfigElement combine(ConfigElement configElement)
   {
      MimeTypeConfigElement combined = new MimeTypeConfigElement(this.mimetypes);
      
      if (configElement instanceof MimeTypeConfigElement)
      {
         combined.mimetypes.putAll( ((MimeTypeConfigElement)configElement).mimetypes );
      }
      
      return combined;
   }
   
   /**
    * Add a mimetype extension mapping to the config element
    * 
    * @param ext        extension to map against
    * @param mimetype   mimetype content type for the specified extension
    */
   public void addMapping(String ext, String mimetype)
   {
      this.mimetypes.put(ext, mimetype);
   }
   
   /**
    * Return the mimetype for the specified extension
    * 
    * @param ext     File
    * 
    * @return mimetype content type or null if not found
    */
   public String getMimeType(String ext)
   {
      return this.mimetypes.get(ext);
   }
   
   private Map<String, String> mimetypes = new HashMap<String, String>(89, 1.0f);
}
