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
import org.alfresco.web.config.DashboardsConfigElement.DashletDefinition;
import org.alfresco.web.config.DashboardsConfigElement.LayoutDefinition;
import org.dom4j.Element;

/**
 * Reader for the 'dashboards' config element and child elements.
 * 
 * @author Kevin Roast
 */
public class DashboardsElementReader implements ConfigElementReader
{
   public static final String ELEMENT_DASHBOARDS = "dashboards";
   public static final String ELEMENT_LAYOUTS = "layouts";
   public static final String ELEMENT_LAYOUT = "layout";
   public static final String ELEMENT_DASHLETS = "dashlets";
   public static final String ELEMENT_DASHLET = "dashlet";
   public static final String ATTR_ID = "id";
   public static final String ATTR_COLUMNS = "columns";
   public static final String ATTR_COLUMNLENGTH = "column-length";
   public static final String ATTR_IMAGE = "image";
   public static final String ATTR_LABEL = "label";
   public static final String ATTR_DESCRIPTION = "description";
   public static final String ATTR_LABELID = "label-id";
   public static final String ATTR_DESCRIPTIONID = "description-id";
   public static final String ATTR_JSP = "jsp";
   public static final String ATTR_CONFIGJSP = "config-jsp";
   public static final String ATTR_ALLOWNARROW = "allow-narrow";
   
   /**
    * @see org.alfresco.config.xml.elementreader.ConfigElementReader#parse(org.dom4j.Element)
    */
   @SuppressWarnings("unchecked")
   public ConfigElement parse(Element element)
   {
      DashboardsConfigElement configElement = new DashboardsConfigElement();
      
      if (element != null)
      {
         if (DashboardsConfigElement.CONFIG_ELEMENT_ID.equals(element.getName()) == false)
         {
            throw new ConfigException("DashboardsElementReader can only process elements of type 'dashboards'");
         }
         
         Element layoutsElement = element.element(ELEMENT_LAYOUTS);
         if (layoutsElement != null)
         {
            Iterator<Element> layoutsItr = layoutsElement.elementIterator(ELEMENT_LAYOUT);
            while (layoutsItr.hasNext())
            {
               LayoutDefinition layoutDef = parseLayoutDefinition(layoutsItr.next());
               configElement.addLayoutDefinition(layoutDef);
            }
         }
         
         Element dashletsElement = element.element(ELEMENT_DASHLETS);
         if (dashletsElement != null)
         {
            Iterator<Element> dashletsItr = dashletsElement.elementIterator(ELEMENT_DASHLET);
            while (dashletsItr.hasNext())
            {
               DashletDefinition dashletDef = parseDashletDefinition(dashletsItr.next());
               configElement.addDashletDefinition(dashletDef);
            }
         }
      }
      
      return configElement;
   }
   
   /**
    * Parse a single Layout definition from config.
    * 
    * @param config
    * 
    * @return LayoutDefinition for the specified config element.
    */
   private static LayoutDefinition parseLayoutDefinition(Element config)
   {
      String id = getMandatoryLayoutAttributeValue(config, ATTR_ID);
      
      LayoutDefinition def = new LayoutDefinition(id);
      
      String columns = getMandatoryLayoutAttributeValue(config, ATTR_COLUMNS);
      def.Columns = Integer.parseInt(columns);
      String columnLength = getMandatoryLayoutAttributeValue(config, ATTR_COLUMNLENGTH);
      def.ColumnLength = Integer.parseInt(columnLength);
      def.Image = getMandatoryLayoutAttributeValue(config, ATTR_IMAGE);
      def.JSPPage = getMandatoryLayoutAttributeValue(config, ATTR_JSP);
      String label = config.attributeValue(ATTR_LABEL);
      String labelId = config.attributeValue(ATTR_LABELID);
      if ((label == null || label.length() == 0) && (labelId == null || labelId.length() == 0))
      {
         throw new ConfigException("Either 'label' or 'label-id' attribute must be specified for Dashboard 'layout' configuration element.");
      }
      def.Label = label;
      def.LabelId = labelId;
      String description = config.attributeValue(ATTR_DESCRIPTION);
      String descriptionId = config.attributeValue(ATTR_DESCRIPTIONID);
      if ((description == null || description.length() == 0) && (descriptionId == null || descriptionId.length() == 0))
      {
         throw new ConfigException("Either 'description' or 'description-id' attribute must be specified for Dashboard 'layout' configuration element.");
      }
      def.Description = description;
      def.DescriptionId = descriptionId;
      
      return def;
   }
   
   /**
    * Return a mandatory layout attribute layout. Throw an exception if the value is not found.
    * 
    * @param config
    * @param attr
    * 
    * @return String value
    */
   private static String getMandatoryLayoutAttributeValue(Element config, String attr)
   {
      String value = config.attributeValue(attr);
      if (value == null || value.length() == 0)
      {
         throw new ConfigException("Missing mandatory '" + attr + "' attribute for Dashboard 'layout' configuration element.");
      }
      return value;
   }
   
   /**
    * Parse a single Dashlet definition from config.
    * 
    * @param config
    * 
    * @return DashletDefinition for the specified config element.
    */
   private static DashletDefinition parseDashletDefinition(Element config)
   {
      String id = getMandatoryDashletAttributeValue(config, ATTR_ID);
      
      DashletDefinition def = new DashletDefinition(id);
      
      String allowNarrow = config.attributeValue(ATTR_ALLOWNARROW);
      if (allowNarrow != null && allowNarrow.length() != 0)
      {
         def.AllowNarrow = Boolean.parseBoolean(allowNarrow);
      }
      def.JSPPage = getMandatoryDashletAttributeValue(config, ATTR_JSP);
      def.ConfigJSPPage = config.attributeValue(ATTR_CONFIGJSP);
      String label = config.attributeValue(ATTR_LABEL);
      String labelId = config.attributeValue(ATTR_LABELID);
      if ((label == null || label.length() == 0) && (labelId == null || labelId.length() == 0))
      {
         throw new ConfigException("Either 'label' or 'label-id' attribute must be specified for Dashboard 'dashlet' configuration element.");
      }
      def.Label = label;
      def.LabelId = labelId;
      String description = config.attributeValue(ATTR_DESCRIPTION);
      String descriptionId = config.attributeValue(ATTR_DESCRIPTIONID);
      if ((description == null || description.length() == 0) && (descriptionId == null || descriptionId.length() == 0))
      {
         throw new ConfigException("Either 'description' or 'description-id' attribute must be specified for Dashboard 'dashlet' configuration element.");
      }
      def.Description = description;
      def.DescriptionId = descriptionId;
      
      return def;
   }
   
   /**
    * Return a mandatory dashlet attribute layout. Throw an exception if the value is not found.
    * 
    * @param config
    * @param attr
    * 
    * @return String value
    */
   private static String getMandatoryDashletAttributeValue(Element config, String attr)
   {
      String value = config.attributeValue(attr);
      if (value == null || value.length() == 0)
      {
         throw new ConfigException("Missing mandatory '" + attr + "' attribute for Dashboard 'dashlet' configuration element.");
      }
      return value;
   }
}
