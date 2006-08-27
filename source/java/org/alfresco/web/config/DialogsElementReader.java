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
import java.util.Iterator;
import java.util.List;

import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigException;
import org.alfresco.config.xml.elementreader.ConfigElementReader;
import org.alfresco.web.config.DialogsConfigElement.DialogButtonConfig;
import org.dom4j.Element;

/**
 * Custom element reader to parse config for dialogs
 * 
 * @author gavinc
 */
public class DialogsElementReader implements ConfigElementReader
{
   public static final String ELEMENT_DIALOGS = "dialogs";
   public static final String ELEMENT_DIALOG = "dialog";
   public static final String ELEMENT_BUTTONS = "buttons";
   public static final String ELEMENT_BUTTON = "button";
   public static final String ATTR_NAME = "name";
   public static final String ATTR_PAGE = "page";
   public static final String ATTR_MANAGED_BEAN = "managed-bean";
   public static final String ATTR_ACTIONS_CONFIG_ID = "actions-config-id";
   public static final String ATTR_ICON = "icon";
   public static final String ATTR_TITLE = "title";
   public static final String ATTR_TITLE_ID = "title-id";
   public static final String ATTR_DESCRIPTION = "description";
   public static final String ATTR_DESCRIPTION_ID = "description-id";
   public static final String ATTR_ERROR_MSG_ID = "error-message-id";
   public static final String ATTR_SHOW_OK_BUTTON = "show-ok-button";
   public static final String ATTR_ID = "id";
   public static final String ATTR_LABEL = "label";
   public static final String ATTR_LABEL_ID = "label-id";
   public static final String ATTR_ACTION = "action";
   public static final String ATTR_DISABLED = "disabled";
   public static final String ATTR_ONCLICK = "onclick";
   
   /**
    * @see org.alfresco.config.xml.elementreader.ConfigElementReader#parse(org.dom4j.Element)
    */
   @SuppressWarnings("unchecked")
   public ConfigElement parse(Element element)
   {
      DialogsConfigElement configElement = null;
      
      if (element != null)
      {
         String elementName = element.getName();
         if (elementName.equals(ELEMENT_DIALOGS) == false)
         {
            throw new ConfigException("DialogsElementReader can only parse " +
                  ELEMENT_DIALOGS + "elements, the element passed was '" + 
                  elementName + "'");
         }
         
         configElement = new DialogsConfigElement();
         
         // go through the dialogs
         Iterator<Element> items = element.elementIterator(ELEMENT_DIALOG);
         while (items.hasNext())
         {
            Element item = items.next();
            
            String name = item.attributeValue(ATTR_NAME);
            String page = item.attributeValue(ATTR_PAGE);
            String bean = item.attributeValue(ATTR_MANAGED_BEAN);
            String actions = item.attributeValue(ATTR_ACTIONS_CONFIG_ID);
            String icon = item.attributeValue(ATTR_ICON);
            String title = item.attributeValue(ATTR_TITLE);
            String titleId = item.attributeValue(ATTR_TITLE_ID);
            String description = item.attributeValue(ATTR_DESCRIPTION);
            String descriptionId = item.attributeValue(ATTR_DESCRIPTION_ID);
            String errorMsgId = item.attributeValue(ATTR_ERROR_MSG_ID);
            String showOK = item.attributeValue(ATTR_SHOW_OK_BUTTON);
            
            boolean isOKButtonVisible = true;
            if (showOK != null)
            {
               isOKButtonVisible = Boolean.parseBoolean(showOK);
            }
            
            // parse any buttons that may be present
            List<DialogButtonConfig> buttons = parseButtons(item);
            
            DialogsConfigElement.DialogConfig cfg = new DialogsConfigElement.DialogConfig(
                  name, page, bean, actions, icon, title, titleId, description, 
                  descriptionId, errorMsgId, isOKButtonVisible, buttons);
            
            configElement.addDialog(cfg);
         }
      }
      
      return configElement;
   }

   /**
    * Retrieve the configuration for additional buttons.
    * 
    * @param dialog The dialog XML element
    * @return List of configured buttons
    */
   @SuppressWarnings("unchecked")
   protected List<DialogButtonConfig> parseButtons(Element dialog)
   {
      List<DialogButtonConfig> buttons = null;
      
      // iterate over any configured buttons
      Element buttonsConfig = dialog.element(ELEMENT_BUTTONS);
      if (buttonsConfig != null)
      {
         buttons = new ArrayList<DialogButtonConfig>(4);
         
         Iterator<Element> children = buttonsConfig.elementIterator(ELEMENT_BUTTON);
         while (children.hasNext())
         {
            Element button = children.next();
            
            String id = button.attributeValue(ATTR_ID);
            String label = button.attributeValue(ATTR_LABEL);
            String labelId = button.attributeValue(ATTR_LABEL_ID);
            String action = button.attributeValue(ATTR_ACTION);
            String disabled = button.attributeValue(ATTR_DISABLED);
            String onclick = button.attributeValue(ATTR_ONCLICK);
            
            // create the button config object
            DialogButtonConfig btnCfg = new DialogButtonConfig(id, label, 
                  labelId, action, disabled, onclick);
            
            // add the button to the list
            buttons.add(btnCfg);
         }
      }
      
      return buttons;
   }
}
