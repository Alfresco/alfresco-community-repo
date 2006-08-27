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
package org.alfresco.web.ui.common.component;

import java.io.IOException;
import java.util.Map;

import javax.faces.component.NamingContainer;
import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.FacesEvent;

import org.alfresco.web.ui.common.PanelGenerator;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.WebResources;

/**
 * @author kevinr
 */
public class UIPanel extends UICommand
{
   // ------------------------------------------------------------------------------
   // Component Impl 
   
   /**
    * Default constructor
    */
   public UIPanel()
   {
      setRendererType(null);
   }
   
   /**
    * @see javax.faces.component.UIComponent#getFamily()
    */
   public String getFamily()
   {
      return "org.alfresco.faces.Controls";
   }
   
   /**
    * Return the UI Component to be displayed on the right of the panel title area
    * 
    * @return UIComponent
    */
   public UIComponent getTitleComponent()
   {
      UIComponent titleComponent = null;
      
      // attempt to find a component with the specified ID
      String facetsId = getFacetsId();
      if (facetsId != null)
      {
         UIForm parent = Utils.getParentForm(FacesContext.getCurrentInstance(), this);
         UIComponent facetsComponent = parent.findComponent(facetsId);
         if (facetsComponent != null)
         {
            // get the 'title' facet from the component
            titleComponent = facetsComponent.getFacet("title");
         }
      }
      
      return titleComponent;
   }

   /**
    * @see javax.faces.component.UIComponentBase#encodeBegin(javax.faces.context.FacesContext)
    */
   public void encodeBegin(FacesContext context) throws IOException
   {
      if (isRendered() == false)
      {
         return;
      }
      
      ResponseWriter out = context.getResponseWriter();
      
      // determine if we have a component on the header
      UIComponent titleComponent = getTitleComponent();
      
      // determine whether we have any adornments
      String label = getLabel();
      if (label != null)
      {
         label = Utils.encode(label);
      }
      if (label != null || isProgressive() == true || titleComponent != null)
      {
         this.hasAdornments = true;
      }
      
      // make sure we have a default background color for the content area
      String bgcolor = getBgcolor();
      if (bgcolor == null)
      {
         bgcolor = PanelGenerator.BGCOLOR_WHITE;
      }
      
      // determine if we have a bordered title area, note, we also need to have
      // the content area border defined as well
      if ((getTitleBgcolor() != null) && (getTitleBorder() != null) && 
          (getBorder() != null) && this.hasAdornments)
      {
         this.hasBorderedTitleArea = true;
      }
      
      // output first part of border table
      if (this.hasBorderedTitleArea)
      {
         PanelGenerator.generatePanelStart(
               out,
               context.getExternalContext().getRequestContextPath(),
               getTitleBorder(),
               getTitleBgcolor());
      }
      else if (getBorder() != null)
      {
         PanelGenerator.generatePanelStart(
               out,
               context.getExternalContext().getRequestContextPath(),
               getBorder(),
               bgcolor);
      }

      if (this.hasAdornments)
      {
         // start the containing table if we have any adornments
         out.write("<table border='0' cellspacing='0' cellpadding='0' width='100%'><tr><td>");
      }

      // output progressive disclosure icon in appropriate state
      // TODO: manage state of this icon via component Id!
      if (isProgressive() == true)
      {
         out.write("<a href='#' onclick=\"");
         String value = getClientId(context) + NamingContainer.SEPARATOR_CHAR + Boolean.toString(!isExpanded());
         out.write(Utils.generateFormSubmit(context, this, getHiddenFieldName(), value));
         out.write("\">");
         
         if (isExpanded() == true)
         {
            out.write(Utils.buildImageTag(context, WebResources.IMAGE_EXPANDED, 11, 11, label));
         }
         else
         {
            out.write(Utils.buildImageTag(context, WebResources.IMAGE_COLLAPSED, 11, 11, label));
         }
         
         out.write("</a>&nbsp;&nbsp;");
      }
      
      // output textual label
      if (label != null)
      {
         out.write("<span");
         Utils.outputAttribute(out, getAttributes().get("style"), "style");
         Utils.outputAttribute(out, getAttributes().get("styleClass"), "class");
         out.write('>');
         
         out.write(label);    // already encoded above
         
         out.write("</span>");
      }
      
      if (this.hasAdornments)
      {
         out.write("</td>");
      }
      
      // render the title component if supplied
      if (titleComponent != null)
      {
         out.write("<td align='right'>");
         Utils.encodeRecursive(context, titleComponent);
         out.write("</td>");
      }
      
      if (this.hasAdornments)
      {
         out.write("</tr></table>");
      }
      
      // if we have the titled border area, output the middle section
      if (this.hasBorderedTitleArea && isExpanded())
      {
         PanelGenerator.generateTitledPanelMiddle(
               out,
               context.getExternalContext().getRequestContextPath(),
               getTitleBorder(),
               getBorder(),
               getBgcolor());
      }
   }

   /**
    * @see javax.faces.component.UIComponentBase#encodeEnd(javax.faces.context.FacesContext)
    */
   public void encodeEnd(FacesContext context) throws IOException
   {
      if (isRendered() == false)
      {
         return;
      }
      
      ResponseWriter out = context.getResponseWriter();
      
      // output final part of border table
      if (this.hasBorderedTitleArea && isExpanded() == false)
      {
         PanelGenerator.generatePanelEnd(
               out,
               context.getExternalContext().getRequestContextPath(),
               getTitleBorder());
      }
      else if (getBorder() != null)
      {
         PanelGenerator.generatePanelEnd(
               out,
               context.getExternalContext().getRequestContextPath(),
               getBorder());
      }
   }
   
   /**
    * @see javax.faces.component.UIComponentBase#decode(javax.faces.context.FacesContext)
    */
   public void decode(FacesContext context)
   {
      Map requestMap = context.getExternalContext().getRequestParameterMap();
      String fieldId = getHiddenFieldName();
      String value = (String)requestMap.get(fieldId);
      
      // we encoded the value to start with our Id
      if (value != null && value.startsWith(getClientId(context)))
      {
         // we were clicked, strip out the value
         value = value.substring(getClientId(context).length() + 1);
         
         // the expand/collapse icon was clicked, so toggle the state
         ExpandedEvent event = new ExpandedEvent(this, Boolean.parseBoolean(value));
         queueEvent(event);
         
         //
         // TODO: See http://forums.java.sun.com/thread.jspa?threadID=524925&start=15&tstart=0
         //       Bug/known issue in JSF 1.1 RI
         //       This causes a problem where the View attempts to assign duplicate Ids
         //       to components when createUniqueId() on UIViewRoot is called before the
         //       render phase. This occurs in the Panel tag as it must call getComponent()
         //       early to decide whether to allow the tag to render contents or not.
         //
         // context.getViewRoot().setTransient(true);
         //
         //       The other solution is to explicity give ALL child components of the
         //       panel a unique Id rather than a generated one! 
      }
   }
   
   /**
    * @see javax.faces.component.UICommand#broadcast(javax.faces.event.FacesEvent)
    */
   public void broadcast(FacesEvent event) throws AbortProcessingException
   {
      if (event instanceof ExpandedEvent)
      {
         // expanded event - we handle this
         setExpanded( ((ExpandedEvent)event).State );
         
         if (getExpandedActionListener() != null)
         {
            Utils.processActionMethod(getFacesContext(), getExpandedActionListener(), (ExpandedEvent)event);
         }
      }
      else
      {
         super.broadcast(event);
      }
   }

   /**
    * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext, java.lang.Object)
    */
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
      setExpanded( ((Boolean)values[1]).booleanValue() );
      this.progressive = (Boolean)values[2];
      this.border = (String)values[3];
      this.bgcolor = (String)values[4];
      this.label = (String)values[5];
      this.titleBgcolor = (String)values[6];
      this.titleBorder = (String)values[7];
      this.expandedActionListener = (MethodBinding)values[8];
      this.facetsId = (String)values[9];
   }
   
   /**
    * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
    */
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[10];
      // standard component attributes are saved by the super class
      values[0] = super.saveState(context);
      values[1] = (isExpanded() ? Boolean.TRUE : Boolean.FALSE);
      values[2] = this.progressive;
      values[3] = this.border;
      values[4] = this.bgcolor;
      values[5] = this.label;
      values[6] = this.titleBgcolor;
      values[7] = this.titleBorder;
      values[8] = this.expandedActionListener;
      values[9] = this.facetsId;
      return values;
   }
   
   
   // ------------------------------------------------------------------------------
   // Strongly typed component property accessors 
   
   /** 
    * @param binding    The MethodBinding to call when expand/collapse is performed by the user.
    */
   public void setExpandedActionListener(MethodBinding binding)
   {
      this.expandedActionListener = binding;
   }
   
   /** 
    * @return The MethodBinding to call when expand/collapse is performed by the user.
    */
   public MethodBinding getExpandedActionListener()
   {
      return this.expandedActionListener;
   }
   
   /**
    * @return Returns the bgcolor.
    */
   public String getBgcolor()
   {
      ValueBinding vb = getValueBinding("bgcolor");
      if (vb != null)
      {
         this.bgcolor = (String)vb.getValue(getFacesContext());
      }
      
      return this.bgcolor;
   }
   
   /**
    * @param bgcolor    The bgcolor to set.
    */
   public void setBgcolor(String bgcolor)
   {
      this.bgcolor = bgcolor;
   }

   /**
    * @return Returns the border name.
    */
   public String getBorder()
   {
      ValueBinding vb = getValueBinding("border");
      if (vb != null)
      {
         this.border = (String)vb.getValue(getFacesContext());
      }
      
      return this.border;
   }

   /**
    * @param border  The border name to user.
    */
   public void setBorder(String border)
   {
      this.border = border;
   }
   
   /**
    * @return Returns the bgcolor of the title area
    */
   public String getTitleBgcolor()
   {
      ValueBinding vb = getValueBinding("titleBgcolor");
      if (vb != null)
      {
         this.titleBgcolor = (String)vb.getValue(getFacesContext());
      }
      
      return this.titleBgcolor;
   }

   /**
    * @param titleBgcolor Sets the bgcolor of the title area
    */
   public void setTitleBgcolor(String titleBgcolor)
   {
      this.titleBgcolor = titleBgcolor;
   }

   /**
    * @return Returns the border style of the title area
    */
   public String getTitleBorder()
   {
      ValueBinding vb = getValueBinding("titleBorder");
      if (vb != null)
      {
         this.titleBorder = (String)vb.getValue(getFacesContext());
      }
      
      return this.titleBorder;
   }

   /**
    * @param titleBorder Sets the border style of the title area
    */
   public void setTitleBorder(String titleBorder)
   {
      this.titleBorder = titleBorder;
   }

   /**
    * @return Returns the label.
    */
   public String getLabel()
   {
      ValueBinding vb = getValueBinding("label");
      if (vb != null)
      {
         this.label = (String)vb.getValue(getFacesContext());
      }
      
      return this.label;
   }

   /**
    * @param label The label to set.
    */
   public void setLabel(String label)
   {
      this.label = label;
   }

   /**
    * @return Returns the progressive display setting.
    */
   public boolean isProgressive()
   {
      ValueBinding vb = getValueBinding("progressive");
      if (vb != null)
      {
         this.progressive = (Boolean)vb.getValue(getFacesContext());
      }
      
      if (this.progressive != null)
      {
         return this.progressive.booleanValue();
      }
      else
      {
         // return default
         return false;
      }
   }
   
   /**
    * @param progressive   The progressive display boolean to set.
    */
   public void setProgressive(boolean progressive)
   {
      this.progressive = Boolean.valueOf(progressive);
   }
   
   /**
    * Returns whether the component show allow rendering of its child components.
    */
   public boolean isExpanded()
   {
      ValueBinding vb = getValueBinding("expanded");
      if (vb != null)
      {
         this.expanded = (Boolean)vb.getValue(getFacesContext());
      }
      
      if (this.expanded != null)
      {
         return this.expanded.booleanValue();
      }
      else
      {
         // return default
         return true;
      }
   }
   
   /**
    * Sets whether the component show allow rendering of its child components.
    * For this component we change this value if the user indicates to change the
    * hidden/visible state of the progressive panel.
    */
   public void setExpanded(boolean expanded)
   {
      this.expanded = Boolean.valueOf(expanded);
   }
   
   /**
    * Get the facets component Id to use
    *
    * @return the facets component Id
    */
   public String getFacetsId()
   {
      ValueBinding vb = getValueBinding("facets");
      if (vb != null)
      {
         this.facetsId = (String)vb.getValue(getFacesContext());
      }
      
      return this.facetsId;
   }

   /**
    * Set the facets component Id to use
    *
    * @param facets     the facets component Id
    */
   public void setFacetsId(String facets)
   {
      this.facetsId = facets;
   }
   
   
   // ------------------------------------------------------------------------------
   // Private helpers
   
   /**
    * We use a hidden field name based on the parent form component Id and
    * the string "panel" to give a hidden field name that can be shared by all panels
    * within a single UIForm component.
    * 
    * @return hidden field name
    */
   private String getHiddenFieldName()
   {
      UIForm form = Utils.getParentForm(getFacesContext(), this);
      return form.getClientId(getFacesContext()) + NamingContainer.SEPARATOR_CHAR + "panel";
   }
   
   
   // ------------------------------------------------------------------------------
   // Private members 
   
   // component settings
   private String border = null;
   private String bgcolor = null;
   private String titleBorder = null;
   private String titleBgcolor = null;
   private Boolean progressive = null;
   private String label = null;
   private String facetsId = null;
   private MethodBinding expandedActionListener = null;
   
   // component state
   private boolean hasAdornments = false;
   private boolean hasBorderedTitleArea = false;
   private Boolean expanded = Boolean.TRUE;
   
   
   // ------------------------------------------------------------------------------
   // Inner classes
   
   /**
    * Class representing the an action relevant when the panel is expanded or collapsed.
    */
   public static class ExpandedEvent extends ActionEvent
   {
      public ExpandedEvent(UIComponent component, boolean state)
      {
         super(component);
         State = state;
      }
      
      public boolean State;
   }
}
