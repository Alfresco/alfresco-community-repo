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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
