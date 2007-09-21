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
 * http://www.alfresco.com/legal/licensing"
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
   public static final String ATTR_ICON = "icon";
   public static final String ATTR_TITLE = "title";
   public static final String ATTR_TITLE_ID = "title-id";
   public static final String ATTR_SUBTITLE = "subtitle";
   public static final String ATTR_SUBTITLE_ID = "subtitle-id";
   public static final String ATTR_DESCRIPTION = "description";
   public static final String ATTR_DESCRIPTION_ID = "description-id";
   public static final String ATTR_ERROR_MSG_ID = "error-message-id";
   public static final String ATTR_SHOW_OK_BUTTON = "show-ok-button";
   
   // action related attributes
   public static final String ATTR_ACTIONS_CONFIG_ID = "actions-config-id";
   public static final String ATTR_ACTIONS_AS_MENU = "actions-as-menu";
   public static final String ATTR_ACTIONS_MENU_LABEL = "actions-menu-label";
   public static final String ATTR_ACTIONS_MENU_LABEL_ID = "actions-menu-label-id";
   public static final String ATTR_MORE_ACTIONS_CONFIG_ID = "more-actions-config-id";
   public static final String ATTR_MORE_ACTIONS_MENU_LABEL = "more-actions-menu-label";
   public static final String ATTR_MORE_ACTIONS_MENU_LABEL_ID = "more-actions-menu-label-id";
   
   // button related attributes
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
            String icon = item.attributeValue(ATTR_ICON);
            String title = item.attributeValue(ATTR_TITLE);
            String titleId = item.attributeValue(ATTR_TITLE_ID);
            String subTitle = item.attributeValue(ATTR_SUBTITLE);
            String subTitleId = item.attributeValue(ATTR_SUBTITLE_ID);
            String description = item.attributeValue(ATTR_DESCRIPTION);
            String descriptionId = item.attributeValue(ATTR_DESCRIPTION_ID);
            String errorMsgId = item.attributeValue(ATTR_ERROR_MSG_ID);
            String showOK = item.attributeValue(ATTR_SHOW_OK_BUTTON);
            
            boolean isOKButtonVisible = true;
            if (showOK != null)
            {
               isOKButtonVisible = Boolean.parseBoolean(showOK);
            }
            
            // action related config
            String actionsConfigId = item.attributeValue(ATTR_ACTIONS_CONFIG_ID);
            boolean useMenuForActions = false;
            String asMenu = item.attributeValue(ATTR_ACTIONS_AS_MENU);
            if (asMenu != null)
            {
               useMenuForActions = Boolean.parseBoolean(asMenu);
            }
            String actionsMenuLabel = item.attributeValue(ATTR_ACTIONS_MENU_LABEL);
            String actionsMenuLabelId = item.attributeValue(ATTR_ACTIONS_MENU_LABEL_ID);
            String moreActionsConfigId = item.attributeValue(ATTR_MORE_ACTIONS_CONFIG_ID);
            String moreActionsMenuLabel = item.attributeValue(ATTR_MORE_ACTIONS_MENU_LABEL);
            String moreActionsMenuLabelId = item.attributeValue(ATTR_MORE_ACTIONS_MENU_LABEL_ID);
            
            // parse any buttons that may be present
            List<DialogButtonConfig> buttons = parseButtons(item);
            
            // setup the attrbiutes object
            DialogsConfigElement.DialogAttributes attrs = 
                  new DialogsConfigElement.DialogAttributes(name, page, bean);
            attrs.setIcon(icon);
            attrs.setTitle(title);
            attrs.setTitleId(titleId);
            attrs.setSubTitle(subTitle);
            attrs.setSubTitleId(subTitleId);
            attrs.setDescription(description);
            attrs.setDescriptionId(descriptionId);
            attrs.setErrorMessageId(errorMsgId);
            attrs.setOKButtonVisible(isOKButtonVisible);
            attrs.setButtons(buttons);
            attrs.setActionsConfigId(actionsConfigId);
            attrs.setActionsAsMenu(useMenuForActions);
            attrs.setActionsMenuLabel(actionsMenuLabel);
            attrs.setActionsMenuLabelId(actionsMenuLabelId);
            attrs.setMoreActionsConfigId(moreActionsConfigId);
            attrs.setMoreActionsMenuLabel(moreActionsMenuLabel);
            attrs.setMoreActionsMenuLabelId(moreActionsMenuLabelId);
            
            // create and add the dialog config object
            DialogsConfigElement.DialogConfig cfg = new DialogsConfigElement.DialogConfig(attrs);
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
