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
package org.alfresco.web.ui.repo.tag;

import javax.faces.component.UIComponent;

import org.alfresco.web.ui.common.tag.HtmlComponentTag;
import org.alfresco.web.ui.repo.RepoConstants;

/**
 * Base class for all tags that combine the multi value component
 * and renderers
 * 
 * @author gavinc
 */
public abstract class BaseMultiValueTag extends HtmlComponentTag
{
   private String value;
   private String lastItemAdded;
   private String readOnly;
   private String selectItemMsg;
   private String selectedItemsMsg;
   private String noSelectedItemsMsg;
   
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return RepoConstants.ALFRESCO_FACES_MULTIVALUE_EDITOR;
   }
   
   /**
    * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
    */
   protected void setProperties(UIComponent component)
   {
      super.setProperties(component);
      setStringBindingProperty(component, "value", this.value);
      setStringBindingProperty(component, "lastItemAdded", this.lastItemAdded);
      setStringProperty(component, "selectItemMsg", this.selectItemMsg);
      setStringProperty(component, "selectedItemsMsg", this.selectedItemsMsg);
      setStringProperty(component, "noSelectedItemsMsg", this.noSelectedItemsMsg);
      setBooleanProperty(component, "readOnly", this.readOnly);
   }
   
   /**
    * @see javax.servlet.jsp.tagext.Tag#release()
    */
   public void release()
   {
      this.value = null;
      this.lastItemAdded = null;
      this.readOnly = null;
      this.selectedItemsMsg = null;
      this.selectItemMsg = null;
      this.noSelectedItemsMsg = null;
      
      super.release();
   }

   /**
    * @param value The value to set.
    */
   public void setValue(String value)
   {
      this.value = value;
   }

   /**
    * Sets the lastItemAdded value expression binding
    * 
    * @param lastItemAdded lastItemAdded binding
    */
   public void setLastItemAdded(String lastItemAdded)
   {
      this.lastItemAdded = lastItemAdded;
   }

   /**
    * Sets the readOnly flag for the component
    * 
    * @param readOnly true if the component will be read only
    */
   public void setReadOnly(String readOnly)
   {
      this.readOnly = readOnly;
   }

   /**
    * Sets the message to display for the no selected items
    * 
    * @param noSelectedItemsMsg The message
    */
   public void setNoSelectedItemsMsg(String noSelectedItemsMsg)
   {
      this.noSelectedItemsMsg = noSelectedItemsMsg;
   }
   
   /**
    * Sets the message to display for the selected items
    * 
    * @param selectedItemsMsg The message
    */
   public void setSelectedItemsMsg(String selectedItemsMsg)
   {
      this.selectedItemsMsg = selectedItemsMsg;
   }

   /**
    * Sets the message to display for inviting the user to select an item
    * 
    * @param selectItemMsg The message
    */
   public void setSelectItemMsg(String selectItemMsg)
   {
      this.selectItemMsg = selectItemMsg;
   }
}
