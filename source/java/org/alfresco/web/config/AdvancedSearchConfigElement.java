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
package org.alfresco.web.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.extensions.config.ConfigElement;
import org.springframework.extensions.config.ConfigException;
import org.springframework.extensions.config.element.ConfigElementAdapter;

/**
 * Custom config element that represents config values for advanced search
 * 
 * @author Gavin Cornwell
 */
public class AdvancedSearchConfigElement extends ConfigElementAdapter implements Serializable
{
   private static final long serialVersionUID = -6427054671579839728L;

   public static final String CONFIG_ELEMENT_ID = "advanced-search";
   
   private List<String> contentTypes = null;
   private List<String> folderTypes = null;
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
    * @see org.springframework.extensions.config.element.ConfigElementAdapter#getChildren()
    */
   @Override
   public List<ConfigElement> getChildren()
   {
      throw new ConfigException("Reading the advanced search config via the generic interfaces is not supported");
   }
   
   /**
    * @see org.springframework.extensions.config.element.ConfigElementAdapter#combine(org.alfresco.config.ConfigElement)
    */
   public ConfigElement combine(ConfigElement configElement)
   {
      AdvancedSearchConfigElement newElement = (AdvancedSearchConfigElement)configElement;
      AdvancedSearchConfigElement combinedElement = new AdvancedSearchConfigElement();
      
      // just copy the list of types and properties from this instance to the new one
      if (this.contentTypes != null)
      {
         for (String type : this.contentTypes)
         {
            combinedElement.addContentType(type);
         }
      }
      if (this.folderTypes != null)
      {
         for (String type : this.folderTypes)
         {
            combinedElement.addFolderType(type);
         }
      }
      
      if (this.customProps != null)
      {
         for (CustomProperty property : this.customProps)
         {
            combinedElement.addCustomProperty(property);
         }
      }
      
      // now add those types and custom properties from the element to be combined
      if (newElement.getContentTypes() != null)
      {
         for (String type : newElement.getContentTypes())
         {
            combinedElement.addContentType(type);
         }
      }
      
      if (newElement.getFolderTypes() != null)
      {
         for (String type : newElement.getFolderTypes())
         {
            combinedElement.addFolderType(type);
         }
      }
      
      if (newElement.getCustomProperties() != null)
      {
         for (CustomProperty property : newElement.getCustomProperties())
         {
            combinedElement.addCustomProperty(property);
         }
      }
      
      return combinedElement;
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
    * @return Returns the folderTypes.
    */
   public List<String> getFolderTypes()
   {
      return this.folderTypes;
   }

   /**
    * @param folderTypes The folderTypes to set.
    */
   /*package*/ void setFolderTypes(List<String> folderTypes)
   {
      this.folderTypes = folderTypes;
   }
   
   /**
    * @param folderType Adds the given folder type to the list
    */
   /*package*/ void addFolderType(String folderType)
   {
      if (this.folderTypes == null)
      {
         this.folderTypes = new ArrayList<String>(3);
      }
      
      if (this.folderTypes.contains(folderType) == false)
      {
         this.folderTypes.add(folderType);
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
   public static class CustomProperty implements Serializable
   {
      private static final long serialVersionUID = 1457092137913897740L;
      
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
