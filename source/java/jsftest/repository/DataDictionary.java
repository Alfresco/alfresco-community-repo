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
package jsftest.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Class to represent a basic data dictionary service
 * 
 * @author gavinc
 */
public class DataDictionary
{
   private Map types;
   
   public DataDictionary()
   {
      this.types = new HashMap();
      
      // setup the dictionary
      Property name = new Property("name", "string", "Name", false);
      Property desc = new Property("description", "string", "Description" , false);
      Property created = new Property("created", "datetime", "Created Date", true);
      Property modified = new Property("modified", "datetime", "Modified Date", false);
      Property keywords = new Property("keywords", "string[]", "Keywords", false);
      
      MetaData base = new MetaData("base");
      base.addProperty(name);
      base.addProperty(desc);
      base.addProperty(created);
      base.addProperty(modified);
      base.addProperty(keywords);
      
      Property sopid = new Property("sopId", "string", "SOP ID", true);
      Property effective = new Property("effective", "datetime", "Effective Date", false);
      Property approved = new Property("approved", "boolean", "Approved", false);
      Property latestVersion = new Property("latestversion", "string", "Latest Version", true);
      
      MetaData sop = new MetaData("SOP");
      sop.setProperties(base.getProperties());
      sop.addProperty(sopid);
      sop.addProperty(effective);
      sop.addProperty(approved);
      // add an aspect and the associated property
      sop.addAspect("versionable");
      sop.addProperty(latestVersion);
      
      this.types.put(base.getTypeName(), base);
      this.types.put(sop.getTypeName(), sop);
   }
   
   public MetaData getMetaData(String type)
   {
      return (MetaData)this.types.get(type);
   }
   
   /**
    * @return Returns the types.
    */
   public Map getTypes()
   {
      return this.types;
   }
   
   
   // *********************
   // *** Inner classes ***
   // *********************
   
   /**
    * Represents the meta data of an object
    * @author gavinc
    */
	public class MetaData
	{
      private Map propertiesMap;
	   private List properties;
	   private String typeName;
      private List aspects;
	   
	   public MetaData(String typeName)
	   {
	      this.properties = new ArrayList();
         this.propertiesMap = new HashMap();
         this.aspects = new ArrayList();
	      this.typeName = typeName;
	   }
	   
	   /**
	    * Adds a property to the meta data object
	    * 
	    * @author gavinc
	    */
	   public void addProperty(Property property)
	   {
	      this.properties.add(property);
         this.propertiesMap.put(property.getName(), property);
	   }
	   
      /**
       * @return Returns the properties.
       */
      public List getProperties()
      {
         return this.properties;
      }
      
      /**
       * @param properties The properties to set.
       */
      public void setProperties(List properties)
      {
         this.properties.clear();
         this.propertiesMap.clear();
         
         Iterator iter = properties.iterator();
         while (iter.hasNext())
         {
            Property prop = (Property)iter.next();
            this.properties.add(prop);
            this.propertiesMap.put(prop.getName(), prop);
         }
      }
      
      public Map getPropertiesMap()
      {
         return this.propertiesMap;
      }
      
      public List getAspects()
      {
         return this.aspects;
      }
      
      public void addAspect(String aspect)
      {
         this.aspects.add(aspect);
      }
      
      /**
       * @return Returns the typeName.
       */
      public String getTypeName()
      {
         return this.typeName;
      }
	}
	
	/**
	 * Represents a property on an object
	 * @author gavinc
	 */
	public class Property
	{
	   private String name;
	   private String type;
	   private String displayName;
	   private boolean readOnly;
	   
      /**
       * @param name
       * @param type
       * @param readOnly
       */
      public Property(String name, String type, String displayName, boolean readOnly)
      {
         this.name = name;
         this.type = type;
         this.displayName = displayName;
         this.readOnly = readOnly;
      }
      
	   /**
	    * @return Returns the name.
	    */
	   public String getName()
	   {
	      return this.name;
	   }
	   
	   /**
	    * @param name The name to set.
	    */
	   public void setName(String name)
	   {
	      this.name = name;
	   }
	   
	   /**
	    * @return Returns the type.
	    */
	   public String getType()
	   {
	      return this.type;
	   }
	   
	   /**
	    * @param type The type to set.
	    */
	   public void setType(String type)
	   {
	      this.type = type;
	   }
	   
      /**
       * @return Returns the displayName.
       */
      public String getDisplayName()
      {
         return this.displayName;
      }
      
      /**
       * @param displayName The displayName to set.
       */
      public void setDisplayName(String displayName)
      {
         this.displayName = displayName;
      }
      
	   /**
	    * @return Returns the readOnly.
	    */
	   public boolean isReadOnly()
	   {
	      return this.readOnly;
	   }
	   
	   /**
	    * @param readOnly The readOnly to set.
	    */
	   public void setReadOnly(boolean readOnly)
	   {
	      this.readOnly = readOnly;
	   }
	}
	
}