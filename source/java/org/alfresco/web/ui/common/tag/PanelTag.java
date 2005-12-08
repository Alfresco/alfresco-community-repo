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
import javax.servlet.jsp.JspException;

import org.alfresco.web.ui.common.component.UIPanel;

/**
 * @author kevinr
 */
public class PanelTag extends HtmlComponentTag
{
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.Panel";
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#getRendererType()
    */
   public String getRendererType()
   {
      // the component is self renderering
      return null;
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
    */
   protected void setProperties(UIComponent component)
   {
      super.setProperties(component);
      
      setStringProperty(component, "label", this.label);
      setStringProperty(component, "border", this.border);
      setBooleanProperty(component, "progressive", this.progressive);
      setStringProperty(component, "bgcolor", this.bgcolor);
      setStringProperty(component, "titleBorder", this.titleBorder);
      setStringProperty(component, "titleBgcolor", this.titleBgcolor);
      setBooleanProperty(component, "expanded", this.expanded);
      setStringProperty(component, "facetsId", this.facetsId);
      if (expandedActionListener != null)
      {
         if (isValueReference(expandedActionListener))
         {
            MethodBinding vb = getFacesContext().getApplication().createMethodBinding(expandedActionListener, ACTION_CLASS_ARGS);
            ((UIPanel)component).setExpandedActionListener(vb);
         }
         else
         {
            throw new FacesException("Expanded Action listener method binding incorrectly specified: " + expandedActionListener);
         }
      }
   }
   
   /**
    * @see javax.servlet.jsp.tagext.Tag#release()
    */
   public void release()
   {
      super.release();
      this.label = null;
      this.border = null;
      this.progressive = null;
      this.bgcolor = null;
      this.expanded = null;
      this.expandedActionListener = null;
      this.facetsId = null;
   }
   
   /**
    * Override this to allow the panel component to control whether child components
    * are rendered by the JSP tag framework. This is a nasty solution as it requires
    * a reference to the UIPanel instance and also specific knowledge of the component
    * type that is created by the framework for this tag.
    * 
    * The reason for this solution is to allow any child content (including HTML tags)
    * to be displayed inside the UIPanel component without having to resort to the
    * awful JSF Component getRendersChildren() mechanism - as this would force the use
    * of the verbatim tags for ALL non-JSF child content!
    */
   protected int getDoStartValue() throws JspException
   {
      UIComponent component = getComponentInstance();
      if (component instanceof UIPanel)
      {
         if (((UIPanel)component).isExpanded() == true && component.isRendered() == true)
         {
            return EVAL_BODY_INCLUDE;
         }
         else
         {
            return SKIP_BODY;
         }
      }
      return EVAL_BODY_INCLUDE;
   }

   /**
    * Set the border
    *
    * @param border     the border
    */
   public void setBorder(String border)
   {
      this.border = border;
   }

   /**
    * Set the progressive
    *
    * @param progressive     the progressive
    */
   public void setProgressive(String progressive)
   {
      this.progressive = progressive;
   }

   /**
    * Set the label
    *
    * @param label     the label
    */
   public void setLabel(String label)
   {
      this.label = label;
   }

   /**
    * Set the bgcolor
    *
    * @param bgcolor     the bgcolor
    */
   public void setBgcolor(String bgcolor)
   {
      this.bgcolor = bgcolor;
   }
   
   /**
    * @param titleBgcolor The title area background color
    */
   public void setTitleBgcolor(String titleBgcolor)
   {
      this.titleBgcolor = titleBgcolor;
   }

   /**
    * @param titleBorder The title area border style
    */
   public void setTitleBorder(String titleBorder)
   {
      this.titleBorder = titleBorder;
   }

   /**
    * Set whether the panel is expanded, default is true.
    *
    * @param expanded     the expanded flag
    */
   public void setExpanded(String expanded)
   {
      this.expanded = expanded;
   }

   /**
    * Set the expandedActionListener
    *
    * @param expandedActionListener     the expandedActionListener
    */
   public void setExpandedActionListener(String expandedActionListener)
   {
      this.expandedActionListener = expandedActionListener;
   }

   /**
    * Set the facetsId
    *
    * @param facets     the facetsId
    */
   public void setFacetsId(String facetsId)
   {
      this.facetsId = facetsId;
   }


   /** the facets component Id */
   private String facetsId;

   /** the expandedActionListener */
   private String expandedActionListener;
   
   /** the expanded flag */
   private String expanded;

   /** the border */
   private String border;

   /** the progressive */
   private String progressive;

   /** the label */
   private String label;

   /** the bgcolor */
   private String bgcolor;

   /** the title border style */
   private String titleBorder;
   
   /** the title bgcolor */
   private String titleBgcolor;
}
