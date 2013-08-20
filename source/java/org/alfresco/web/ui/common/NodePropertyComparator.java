/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.web.ui.common;

import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.web.bean.repository.DataDictionary;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.app.Application;

import org.springframework.web.jsf.FacesContextUtils;

import java.util.Comparator;
import java.util.Date;
import java.util.Map;

import javax.faces.context.FacesContext;

/**
 * Comparator to sort the list of nodes according theirs properties and sort order
 *
 * @author vdanilchenko
 * @since 4.1.3
 */
public class NodePropertyComparator implements Comparator<Object>
{
   private String propertyName;
   private boolean isAscending;
   private DataDictionary dataDictionary; 

   /**
     * @param propertyName     the property name to sort
     * @param isAscending     sort order
     */
   public NodePropertyComparator(String propertyName, boolean isAscending)
   {
      super();
      this.propertyName = propertyName;
      this.isAscending = isAscending;         

      FacesContext context = FacesContext.getCurrentInstance();
      dataDictionary = (DataDictionary)FacesContextUtils.getRequiredWebApplicationContext(context).getBean(Application.BEAN_DATA_DICTIONARY);
   }

   @SuppressWarnings({ "unchecked", "rawtypes" })
   public int compare(Object node1, Object node2)
   {
      Map<String, Object> nodeProperties1 = ((Node)node1).getProperties();
      Map<String, Object> nodeProperties2 = ((Node)node2).getProperties();
      PropertyDefinition pd1 = dataDictionary.getPropertyDefinition((Node)node1, propertyName);
      PropertyDefinition pd2 = dataDictionary.getPropertyDefinition((Node)node2, propertyName);
      Comparable propertyValue1, propertyValue2;
      if((pd1 != null) && (pd2 != null))
      {
         String typeName = pd1.getDataType().getName().getLocalName();

         if(typeName.equals("datetime"))
         {
            propertyValue1 = (Date) nodeProperties1.get(propertyName);
            propertyValue2 = (Date) nodeProperties2.get(propertyName);
         }
         else if(typeName.equals("long"))
         {
            propertyValue1 = (Long) nodeProperties1.get(propertyName);
            propertyValue2 = (Long) nodeProperties2.get(propertyName);
         }
         else if(typeName.equals("boolean"))
         {
            propertyValue1 = (Boolean) nodeProperties1.get(propertyName);
            propertyValue2 = (Boolean) nodeProperties2.get(propertyName);
         }
            //string types: text, mltext
            //non comparable types: locale, content
         else
         {
            propertyValue1 = nodeProperties1.get(propertyName).toString();
            propertyValue2 = nodeProperties2.get(propertyName).toString();
         }
      }
      //additional properties doesn't contains in the node properties
      //their type can't be resolved using DataDictionary
      //QNameNodeMap resolves them on first invocation and puts them into the map of node properties
      else
      {
         if(propertyName.equals("size"))
         {
            propertyValue1 = (Long) nodeProperties1.get(propertyName);
            propertyValue2 = (Long) nodeProperties2.get(propertyName);
         }
         else
         {
             propertyValue1 = nodeProperties1.get(propertyName).toString();
             propertyValue2 = nodeProperties2.get(propertyName).toString();
         }
      }

      if(isAscending)
      {
          return propertyValue1.compareTo(propertyValue2);
      }
      return propertyValue2.compareTo(propertyValue1);
   }
}
