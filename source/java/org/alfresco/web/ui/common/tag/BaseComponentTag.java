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
package org.alfresco.web.ui.common.tag;

import javax.faces.FacesException;
import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;

import org.alfresco.web.ui.common.ConstantMethodBinding;

/**
 * @author Kevin Roast
 */
public abstract class BaseComponentTag extends UIComponentTag
{
   /**
    * Helper to set an action property into a command component
    * 
    * @param command    Command component
    * @param action     The action method binding or outcome to set
    */
   protected void setActionProperty(UICommand command, String action)
   {
      if (action != null)
      {
         if (isValueReference(action))
         {
            MethodBinding vb = getFacesContext().getApplication().createMethodBinding(action, null);
            command.setAction(vb);
         }
         else
         {
            MethodBinding vb = new ConstantMethodBinding(action);
            command.setAction(vb);
         }
      }
   }
   
   /**
    * Helper to set an action listener property into a command component
    * 
    * @param command          Command component
    * @param actionListener   Action listener method binding
    */
   protected void setActionListenerProperty(UICommand command, String actionListener)
   {
      if (actionListener != null)
      {
         if (isValueReference(actionListener))
         {
            MethodBinding vb = getFacesContext().getApplication().createMethodBinding(actionListener, ACTION_CLASS_ARGS);
            command.setActionListener(vb);
         }
         else
         {
            throw new FacesException("Action listener method binding incorrectly specified: " + actionListener);
         }
      }
   }
   
   /**
    * Helper method to set a String property value into the component.
    * Respects the possibility that the property value is a Value Binding.
    * 
    * @param component  UIComponent
    * @param name       property string name
    * @param value      property string value
    */
   protected void setStringProperty(UIComponent component, String name, String value)
   {
      if (value != null)
      {
         if (isValueReference(value))
         {
            ValueBinding vb = getFacesContext().getApplication().createValueBinding(value);
            component.setValueBinding(name, vb);
         }
         else
         {
            component.getAttributes().put(name, value);
         }
      }
   }
   
   /**
    * Helper method to set a String value property into the component.
    * Assumes the that the property value can only be a Value Binding.
    * 
    * @param component  UIComponent
    * @param name       property string name
    * @param value      property string value binding
    */
   protected void setStringBindingProperty(UIComponent component, String name, String value)
   {
      if (value != null)
      {
         if (isValueReference(value))
         {
            ValueBinding vb = getFacesContext().getApplication().createValueBinding(value);
            component.setValueBinding(name, vb);
         }
         else
         {
            throw new IllegalArgumentException("Property: '" + name + "' must be a value binding expression.");
         }
      }
   }
   
   /**
    * Helper method to set a static String property into the component.
    * Assumes the that the property value can only be a static string value.
    * 
    * @param component  UIComponent
    * @param name       property string name
    * @param value      property string static value
    */
   protected void setStringStaticProperty(UIComponent component, String name, String value)
   {
      if (value != null)
      {
         component.getAttributes().put(name, value);
      }
   }
   
   /**
    * Helper method to set a String property as an Integer value into the component.
    * Respects the possibility that the property value is a Value Binding.
    * 
    * @param component  UIComponent
    * @param name       property string name
    * @param value      property string value (an Integer will be created)
    */
   protected void setIntProperty(UIComponent component, String name, String value)
   {
      if (value != null)
      {
         if (isValueReference(value))
         {
            ValueBinding vb = getFacesContext().getApplication().createValueBinding(value);
            component.setValueBinding(name, vb);
         }
         else
         {
            try
            {
               component.getAttributes().put(name, Integer.valueOf(value));
            }
            catch (NumberFormatException ne)
            {
               throw new RuntimeException("Was expecting Int value for property '" + name + "' but passed value: " + value); 
            }
         }
      }
   }
   
   protected void setIntStaticProperty(UIComponent component, String name, String value)
   {
      if (value != null)
      {
         try
         {
            component.getAttributes().put(name, Integer.valueOf(value));
         }
         catch (NumberFormatException ne)
         {
            throw new RuntimeException("Was expecting Int value for property '" + name + "' but passed value: " + value); 
         }
      }
   }
   
   /**
    * Helper method to set a String property as an Boolean value into the component.
    * Respects the possibility that the property value is a Value Binding.
    * 
    * @param component  UIComponent
    * @param name       property string name
    * @param value      property string value (a Boolean will be created)
    */
   protected void setBooleanProperty(UIComponent component, String name, String value)
   {
      if (value != null)
      {
         if (isValueReference(value))
         {
            ValueBinding vb = getFacesContext().getApplication().createValueBinding(value);
            component.setValueBinding(name, vb);
         }
         else
         {
            component.getAttributes().put(name, Boolean.valueOf(value));
         }
      }
   }
   
   protected void setBooleanStaticProperty(UIComponent component, String name, String value)
   {
      if (value != null)
      {
         component.getAttributes().put(name, Boolean.valueOf(value));
      }
   }
   
   protected final static Class ACTION_CLASS_ARGS[] = {javax.faces.event.ActionEvent.class};
}
