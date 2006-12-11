package org.alfresco.web.ui.repo.component;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.faces.event.ActionEvent;

import org.alfresco.web.bean.SidebarBean;
import org.alfresco.web.config.SidebarConfigElement;
import org.alfresco.web.config.SidebarConfigElement.SidebarPluginConfig;
import org.alfresco.web.ui.common.PanelGenerator;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.SelfRenderingComponent;
import org.alfresco.web.ui.common.component.UIListItems;
import org.alfresco.web.ui.common.component.UIModeList;

/**
 * Component that represents the sidebar.
 * <p>
 * A sidebar consists of multiple plugins, of which only
 * one is active at one time. All registered plugins are
 * displayed in a drop down allowing the user to
 * change the active plugin. An action group can also be
 * associated with a plugin, which get rendered in the 
 * sidebar header.
 * </p>
 * 
 * @author gavinc
 */
public class UISidebar extends SelfRenderingComponent
{
   public static final String COMPONENT_TYPE = "org.alfresco.faces.Sidebar";
   
   protected String activePlugin;
   
   @Override
   public String getFamily()
   {
      return COMPONENT_TYPE;
   }

   @Override
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
      this.activePlugin = (String)values[1];
   }
   
   @Override
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[8];
      // standard component attributes are saved by the super class
      values[0] = super.saveState(context);
      values[1] = this.activePlugin;
      return values;
   }
   
   @SuppressWarnings("unchecked")
   @Override
   public void encodeBegin(FacesContext context) throws IOException
   {
      if (!isRendered()) return;
      
      ResponseWriter out = context.getResponseWriter();
      
      out.write("<div id=\"sidebar\">");
      
      // render the start of the header panel
      PanelGenerator.generatePanelStart(out, 
            context.getExternalContext().getRequestContextPath(),
            "blue", "#D3E6FE");
         
      // generate the required child components if not present
      if (this.getChildCount() == 1)
      {
         // create the mode list component
         UIModeList modeList = (UIModeList)context.getApplication().
               createComponent("org.alfresco.faces.ModeList");
         modeList.setId("sidebarPluginList");
         modeList.setValue(this.getActivePlugin());
         modeList.setIconColumnWidth(2);
         modeList.setMenu(true);
         modeList.setMenuImage("/images/icons/menu.gif");
         modeList.getAttributes().put("itemSpacing", 4);
         modeList.getAttributes().put("styleClass", "moreActionsMenu");
         modeList.getAttributes().put("selectedStyleClass", "statusListHighlight");
         MethodBinding listener = context.getApplication().createMethodBinding(
               "#{SidebarBean.pluginChanged}", new Class[] {ActionEvent.class});
         modeList.setActionListener(listener);
            
         // create the child list items component
         UIListItems items = (UIListItems)context.getApplication().
               createComponent("org.alfresco.faces.ListItems");
         ValueBinding binding = context.getApplication().createValueBinding(
               "#{SidebarBean.plugins}");
         items.setValueBinding("value", binding);
         
         // add the list items to the mode list component
         modeList.getChildren().add(items);
         
         // create the actions component
         UIActions actions = (UIActions)context.getApplication().
               createComponent("org.alfresco.faces.Actions");
         actions.setId("sidebarActions");
         actions.setShowLink(false);
         setupActionGroupId(context, actions);
         
         // add components to the sidebar
         this.getChildren().add(0, modeList);
         this.getChildren().add(1, actions);
      }
      else
      {
         // update the child UIActions component with the correct 
         // action group id and clear it's current children
         UIActions actions = (UIActions)this.getChildren().get(1);
         actions.getChildren().clear();
         setupActionGroupId(context, actions);
      }
   }

   @Override
   public void encodeChildren(FacesContext context) throws IOException
   {
      if (!isRendered()) return;
      
      // there should be 3 children, the modelist, the actions
      // and the plugin, get them individually and render

      if (getChildren().size() == 3)
      {
         ResponseWriter out = context.getResponseWriter();

         out.write("<table border='0' cellpadding='0' cellspacing='0' width='100%'><tr><td>");
         
         // render the list
         UIModeList modeList = (UIModeList)getChildren().get(0);
         Utils.encodeRecursive(context, modeList);
         
         out.write("</td><td align='right'>");
         
         // render the actions
         UIActions actions = (UIActions)getChildren().get(1);
         Utils.encodeRecursive(context, actions);
         
         out.write("</td></tr></table>");
         
         // render the end of the header panel
         PanelGenerator.generateTitledPanelMiddle(out,
            context.getExternalContext().getRequestContextPath(),
            "blue", "white", "white");
      
         // render the plugin
         UIComponent plugin = (UIComponent)getChildren().get(2);
         Utils.encodeRecursive(context, plugin);
      }
   }

   @Override
   public void encodeEnd(FacesContext context) throws IOException
   {
      if (!isRendered()) return;
      
      // render the end of the panel
      ResponseWriter out = context.getResponseWriter();
      PanelGenerator.generatePanelEnd(out,
            context.getExternalContext().getRequestContextPath(),
            "white");
      out.write("</div>");
   }

   @Override
   public boolean getRendersChildren()
   {
      return true;
   }
   
   // ------------------------------------------------------------------------------
   // Strongly typed component property accessors 
   
   /**
    * Returns the id of the plugin that is currently active
    * 
    * @return The currently active plugin
    */
   public String getActivePlugin()
   {
      ValueBinding vb = getValueBinding("activePlugin");
      if (vb != null)
      {
         this.activePlugin = (String)vb.getValue(getFacesContext());
      }
      
      return this.activePlugin;
   }
   
   /**
    * Sets the active plugin the sidebar should show
    * 
    * @param activePlugin Id of the plugin to make active
    */
   public void setActivePlugin(String activePlugin)
   {
      this.activePlugin = activePlugin;
   }
   
   /**
    * Sets up the corrent actions config group id on the given actions
    * component.
    * 
    * @param context Faces context
    * @param actionsComponent The actions component to set the group id for
    */
   protected void setupActionGroupId(FacesContext context, UIActions actionsComponent)
   {
      String actionsGroupId = null;
      SidebarConfigElement config = SidebarBean.getSidebarConfig(context);
      if (config != null)
      {
         SidebarPluginConfig plugin = config.getPlugin(getActivePlugin());
         if (plugin != null)
         {
            actionsGroupId = plugin.getActionsConfigId();
         }
      }
      actionsComponent.setValue(actionsGroupId);
   }
}


