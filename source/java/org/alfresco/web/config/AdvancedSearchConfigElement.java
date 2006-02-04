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

import java.util.ArrayList;
import java.util.List;

import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigException;
import org.alfresco.config.element.ConfigElementAdapter;

/**
 * Custom config element that represents config values for advanced search
 * 
 * @author Gavin Cornwell
 */
public class AdvancedSearchConfigElement extends ConfigElementAdapter
{
   public static final String CONFIG_ELEMENT_ID = "advanced-search";
   
   private List<String> contentTypes = null;
   private List<CustomProperty> customProps = null;
   
   /**
    * Default Constructor
    */
   public AdvancedSearchConfigElement()
   {
      super(CONFIG_ELEMENT_ID);
   }
   
   /**
    * Constructor
    * 
    * @param name Name of the element this config element represents
    */
   public AdvancedSearchConfigElement(String name)
   {
      super(name);
   }

   /**
    * @see org.alfresco.config.element.ConfigElementAdapter#getChildren()
    */
   @Override
   public List<ConfigElement> getChildren()
   {
      throw new ConfigException("Reading the advanced search config via the generic interfaces is not supported");
   }
   
   /**
    * @see org.alfresco.config.element.ConfigElementAdapter#combine(org.alfresco.config.ConfigElement)
    */
   public ConfigElement combine(ConfigElement configElement)
   {
      AdvancedSearchConfigElement existingElement = (AdvancedSearchConfigElement)configElement;
      AdvancedSearchConfigElement newElement = new AdvancedSearchConfigElement();
      
      // just copy the list of types and properties from this instance to the new one
      if (this.contentTypes != null)
      {
         for (String type : this.contentTypes)
         {
            newElement.addContentType(type);
         }
      }
      
      if (this.customProps != null)
      {
         for (CustomProperty property : this.customProps)
         {
            newElement.addCustomProperty(property);
         }
      }
      
      // now add those types and custom properties from the element to be combined
      if (existingElement.getContentTypes() != null)
      {
         for (String type : existingElement.getContentTypes())
         {
            newElement.addContentType(type);
         }
      }
      
      if (existingElement.getCustomProperties() != null)
      {
         for (CustomProperty property : existingElement.getCustomProperties())
         {
            newElement.addCustomProperty(property);
         }
      }
      
      return newElement;
   }

   /** 
    * @return Returns the contentTypes.
    */
   public List<String> getContentTypes()
   {
      return this.contentTypes;
   }

   /**
    * @param contentTypes The contentTypes to set.
    */
   /*package*/ void setContentTypes(List<String> contentTypes)
   {
      this.contentTypes = contentTypes;
   }
   
   /**
    * @param contentType Adds the given content type to the list
    */
   /*package*/ void addContentType(String contentType)
   {
      if (this.contentTypes == null)
      {
         this.contentTypes = new ArrayList<String>(3);
      }
      
      if (this.contentTypes.contains(contentType) == false)
      {
         this.contentTypes.add(contentType);
      }
   }
   
   /**
    * @return Returns the customProps.
    */
   public List<CustomProperty> getCustomProperties()
   {
      return this.customProps;
   }

   /**
    * @param customProps The customProps to set.
    */
   /*package*/ void setCustomProperties(List<CustomProperty> customProps)
   {
      this.customProps = customProps;
   }
   
   /**
    * @param property Adds the given custom property to the list
    */
   /*package*/ void addCustomProperty(CustomProperty property)
   {
      if (this.customProps == null)
      {
         this.customProps = new ArrayList<CustomProperty>(3);
      }
      
      // TODO: Determine if the CustomProperty being added is already
      //       in the list
      
      this.customProps.add(property);
   }
   
   /**
    * Simple wrapper class for custom advanced search property
    * @author Kevin Roast
    */
   public static class CustomProperty
   {
      CustomProperty(String type, String aspect, String property, String labelId)
      {
         Type = type;
         Aspect = aspect;
         Property = property;
         LabelId = labelId;
      }
      
      public String Type;
      public String Aspect;
      public String Property;
      public String LabelId;
   }
}
