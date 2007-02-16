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

import java.util.Iterator;

import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigException;
import org.alfresco.config.xml.elementreader.ConfigElementReader;
import org.alfresco.web.action.ActionEvaluator;
import org.alfresco.web.config.ActionsConfigElement.ActionDefinition;
import org.alfresco.web.config.ActionsConfigElement.ActionGroup;
import org.dom4j.Element;

/**
 * Config Element Reader for the "Action" config blocks.
 * 
 * @author Kevin Roast
 */
public class ActionsElementReader implements ConfigElementReader
{
   public static final String ELEMENT_ACTION = "action";
   public static final String ELEMENT_ACTIONGROUP = "action-group";
   public static final String ELEMENT_PERMISSIONS = "permissions";
   public static final String ELEMENT_PERMISSION = "permission";
   public static final String ELEMENT_EVALUATOR = "evaluator";
   public static final String ELEMENT_LABEL = "label";
   public static final String ELEMENT_LABELMSG = "label-id";
   public static final String ELEMENT_TOOLTIP = "tooltip";
   public static final String ELEMENT_TOOLTIPMSG = "tooltip-id";
   public static final String ELEMENT_SHOWLINK = "show-link";
   public static final String ELEMENT_STYLE = "style";
   public static final String ELEMENT_STYLECLASS = "style-class";
   public static final String ELEMENT_IMAGE = "image";
   public static final String ELEMENT_ACTIONLISTENER = "action-listener";
   public static final String ELEMENT_ONCLICK = "onclick";
   public static final String ELEMENT_HREF = "href";
   public static final String ELEMENT_TARGET = "target";
   public static final String ELEMENT_SCRIPT = "script";
   public static final String ELEMENT_PARAMS = "params";
   public static final String ELEMENT_PARAM = "param";
   public static final String ATTRIBUTE_ID = "id";
   public static final String ATTRIBUTE_IDREF = "idref";
   public static final String ATTRIBUTE_NAME = "name";
   public static final String ATTRIBUTE_ALLOW = "allow";
   public static final String ATTRIBUTE_HIDE = "hide";

   /**
    * @see org.alfresco.config.xml.elementreader.ConfigElementReader#parse(org.dom4j.Element)
    */
   @SuppressWarnings("unchecked")
   public ConfigElement parse(Element element)
   {
      ActionsConfigElement configElement = new ActionsConfigElement();
      
      if (element != null)
      {
         if (ActionsConfigElement.CONFIG_ELEMENT_ID.equals(element.getName()) == false)
         {
            throw new ConfigException("ActionsElementReader can only parse config elements of type 'Actions'");
         }
         
         Iterator<Element> actionItr = element.elementIterator(ELEMENT_ACTION);
         while (actionItr.hasNext())
         {
            // work on each 'action' element in turn
            Element actionElement = actionItr.next();
            
            // parse the action definition for the element
            ActionDefinition actionDef = parseActionDefinition(actionElement);
            
            // add our finished action def to the map of all actions
            configElement.addActionDefinition(actionDef);
         }
         
         Iterator<Element> actionGroupItr = element.elementIterator(ELEMENT_ACTIONGROUP);
         while (actionGroupItr.hasNext())
         {
            // work on each 'action-group' element in turn
            Element groupElement = actionGroupItr.next();
            String groupId = groupElement.attributeValue(ATTRIBUTE_ID);
            if (groupId == null || groupId.length() == 0)
            {
               throw new ConfigException("'action-group' config element specified without mandatory 'id' attribute.");
            }
            
            // build a structure to represent the action group
            ActionGroup actionGroup = new ActionGroup(groupId); 
            
            // loop round each action ref and add them to the list for this action group
            Iterator<Element> actionRefItr = groupElement.elementIterator(ELEMENT_ACTION);
            while (actionRefItr.hasNext())
            {
               Element actionRefElement = actionRefItr.next();
               
               // look for an action referred to be Id - this is the common use-case
               String idRef = actionRefElement.attributeValue(ATTRIBUTE_IDREF);
               if (idRef == null || idRef.length() == 0)
               {
                  // look for an action defined directly rather than referenced by Id
                  String id = actionRefElement.attributeValue(ATTRIBUTE_ID);
                  if (id != null && id.length() != 0)
                  {
                     ActionDefinition def = parseActionDefinition(actionRefElement);
                     // override action definition ID based on the group name to avoid conflicts
                     def.id = actionGroup.getId() + '_' + def.getId();
                     configElement.addActionDefinition(def);
                     actionGroup.addAction(def.getId());
                  }
               }
               else
               {
                  // look for the hide attribute
                  String hide = actionRefElement.attributeValue(ATTRIBUTE_HIDE);
                  if (hide != null && Boolean.parseBoolean(hide))
                  {
                     actionGroup.hideAction(idRef);
                  }
                  else
                  {
                     // add the action definition ID to the group
                     actionGroup.addAction(idRef);
                  }
               }
            }
            
            // get simple string properties for the action group
            actionGroup.Style = groupElement.elementTextTrim(ELEMENT_STYLE);
            actionGroup.StyleClass = groupElement.elementTextTrim(ELEMENT_STYLECLASS);
            if (groupElement.element(ELEMENT_SHOWLINK) != null)
            {
               actionGroup.ShowLink = Boolean.parseBoolean(groupElement.element(ELEMENT_SHOWLINK).getTextTrim());
            }
            
            // add the action group to the map of all action groups
            configElement.addActionGroup(actionGroup);
         }
      }
      
      return configElement;
   }

   /**
    * Parse an ActionDefinition from the specific config element.
    * 
    * @param actionElement    The config element containing the action def
    * 
    * @return The populated ActionDefinition
    */
   public ActionDefinition parseActionDefinition(Element actionElement)
   {
      String actionId = actionElement.attributeValue(ATTRIBUTE_ID);
      if (actionId == null || actionId.length() == 0)
      {
         throw new ConfigException("'action' config element specified without mandatory 'id' attribute.");
      }
      
      // build a structure to represent the action definition
      ActionDefinition actionDef = new ActionDefinition(actionId); 
      
      // look for the permissions element - it can contain many permission
      Element permissionsElement = actionElement.element(ELEMENT_PERMISSIONS);
      if (permissionsElement != null)
      {
         // read and process each permission element
         Iterator<Element> permissionItr = permissionsElement.elementIterator(ELEMENT_PERMISSION);
         while (permissionItr.hasNext())
         {
            Element permissionElement = permissionItr.next();
            boolean allow = true;
            if (permissionElement.attributeValue(ATTRIBUTE_ALLOW) != null)
            {
               allow = Boolean.parseBoolean(permissionElement.attributeValue(ATTRIBUTE_ALLOW));
            }
            String permissionValue = permissionElement.getTextTrim();
            if (allow)
            {
               actionDef.addAllowPermission(permissionValue);
            }
            else
            {
               actionDef.addDenyPermission(permissionValue);
            }
         }
      }
      
      // find and construct the specified evaluator class
      Element evaluatorElement = actionElement.element(ELEMENT_EVALUATOR);
      if (evaluatorElement != null)
      {
         Object evaluator;
         String className = evaluatorElement.getTextTrim();
         try
         {
            Class clazz = Class.forName(className);
            evaluator = clazz.newInstance();
         }
         catch (Throwable err)
         {
            throw new ConfigException("Unable to construct action '" + 
                  actionId + "' evaluator classname: " +className);
         }
         if (evaluator instanceof ActionEvaluator == false)
         {
            throw new ConfigException("Action '" + actionId + "' evaluator class '" +
                  className + "' does not implement ActionEvaluator interface.");
         }
         actionDef.Evaluator = (ActionEvaluator)evaluator;
      }
      
      // find any parameter values that the action requires
      Element paramsElement = actionElement.element(ELEMENT_PARAMS);
      if (paramsElement != null)
      {
         Iterator<Element> paramsItr = paramsElement.elementIterator(ELEMENT_PARAM);
         while (paramsItr.hasNext())
         {
            Element paramElement = paramsItr.next();
            String name = paramElement.attributeValue(ATTRIBUTE_NAME);
            if (name == null || name.length() == 0)
            {
               throw new ConfigException("Action '" + actionId +
                     "' param does not have mandatory 'name' attribute.");
            }
            String value = paramElement.getTextTrim();
            if (value == null || value.length() == 0)
            {
               throw new ConfigException("Action '" + actionId + "' param '" + name + "'" +
                     "' does not have a value.");
            }
            actionDef.addParam(name, value);
         }
      }
      
      // get simple string properties for the action
      actionDef.Label = actionElement.elementTextTrim(ELEMENT_LABEL);
      actionDef.LabelMsg = actionElement.elementTextTrim(ELEMENT_LABELMSG);
      actionDef.Tooltip = actionElement.elementTextTrim(ELEMENT_TOOLTIP);
      actionDef.TooltipMsg = actionElement.elementTextTrim(ELEMENT_TOOLTIPMSG);
      actionDef.Href = actionElement.elementTextTrim(ELEMENT_HREF);
      actionDef.Target = actionElement.elementTextTrim(ELEMENT_TARGET);
      actionDef.Script = actionElement.elementTextTrim(ELEMENT_SCRIPT);
      actionDef.Action = actionElement.elementTextTrim(ELEMENT_ACTION);
      actionDef.ActionListener = actionElement.elementTextTrim(ELEMENT_ACTIONLISTENER);
      actionDef.Onclick = actionElement.elementTextTrim(ELEMENT_ONCLICK);
      actionDef.Image = actionElement.elementTextTrim(ELEMENT_IMAGE);
      actionDef.Style = actionElement.elementTextTrim(ELEMENT_STYLE);
      actionDef.StyleClass = actionElement.elementTextTrim(ELEMENT_STYLECLASS);
      if (actionElement.element(ELEMENT_SHOWLINK) != null)
      {
         actionDef.ShowLink = Boolean.parseBoolean(actionElement.element(ELEMENT_SHOWLINK).getTextTrim());
      }
      
      return actionDef;
   }
}
