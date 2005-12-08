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
package org.alfresco.web.ui.repo.tag.shelf;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.el.MethodBinding;

import org.alfresco.web.ui.common.tag.BaseComponentTag;
import org.alfresco.web.ui.repo.component.shelf.UIRecentSpacesShelfItem;

/**
 * @author Kevin Roast
 */
public class RecentSpacesShelfItemTag extends BaseComponentTag
{
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.RecentSpacesShelfItem";
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#getRendererType()
    */
   public String getRendererType()
   {
      // self rendering component
      return null;
   }
   
   /**
    * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
    */
   protected void setProperties(UIComponent component)
   {
      super.setProperties(component);
      setStringBindingProperty(component, "value", this.value);
      if (isValueReference(this.navigateActionListener))
      {
         MethodBinding vb = getFacesContext().getApplication().createMethodBinding(this.navigateActionListener, ACTION_CLASS_ARGS);
         ((UIRecentSpacesShelfItem)component).setNavigateActionListener(vb);
      }
      else
      {
         throw new FacesException("Navigate Action listener method binding incorrectly specified: " + this.navigateActionListener);
      }
   }
   
   /**
    * @see javax.servlet.jsp.tagext.Tag#release()
    */
   public void release()
   {
      super.release();
      
      this.value = null;
      this.navigateActionListener = null;
   }
   
   /**
    * Set the value used to bind the recent spaces list to the component
    *
    * @param value     the value
    */
   public void setValue(String value)
   {
      this.value = value;
   }
   
   /**
    * Set the navigateActionListener
    *
    * @param navigateActionListener     the navigateActionListener
    */
   public void setNavigateActionListener(String navigateActionListener)
   {
      this.navigateActionListener = navigateActionListener;
   }


   /** the navigateActionListener */
   private String navigateActionListener;
   
   /** the value */
   private String value;
}
