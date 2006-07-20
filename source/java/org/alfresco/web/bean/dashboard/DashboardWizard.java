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
package org.alfresco.web.bean.dashboard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.component.UISelectMany;
import javax.faces.component.UISelectOne;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.alfresco.web.app.Application;
import org.alfresco.web.bean.wizard.BaseWizardBean;
import org.alfresco.web.config.DashboardsConfigElement;
import org.alfresco.web.config.DashboardsConfigElement.DashletDefinition;
import org.alfresco.web.config.DashboardsConfigElement.LayoutDefinition;
import org.alfresco.web.ui.common.component.UIListItem;
import org.alfresco.web.ui.common.component.description.UIDescription;

/**
 * @author Kevin Roast
 */
public class DashboardWizard extends BaseWizardBean
{
   private static final String COMPONENT_COLUMNDASHLETS = "column-dashlets";

   private static final String COMPONENT_ALLDASHLETS = "all-dashlets";

   private static final String MSG_COLUMN = "dashboard_column";
   
   /** List of icons items to display as selectable Layout definitions */
   private List<UIListItem> layoutIcons = null;
   
   /** List of descriptions of the layouts */
   private List<UIDescription> layoutDescriptions = null;
   
   /** List of SelectItem objects representing the available dashlets */
   private List<SelectItem> dashlets = null;
   
   /** Currently selected layout */
   private String layout;
   
   /** Currently selected column to edit */
   private int column;
   
   /** The PageConfig holding the columns/dashlets during editing */
   private PageConfig editConfig;
   
   /** The DashboardManager instance */
   private DashboardManager dashboardManager;
   
   
   // ------------------------------------------------------------------------------
   // Bean setters 
   
   /**
    * @param dashboardManager The dashboardManager to set.
    */
   public void setDashboardManager(DashboardManager dashboardManager)
   {
      this.dashboardManager = dashboardManager;
   }
   
   
   // ------------------------------------------------------------------------------
   // Wizard implementation
   
   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#init(java.util.Map)
    */
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      this.editConfig = new PageConfig(this.dashboardManager.getPageConfig());
      this.layout = this.editConfig.getCurrentPage().getLayoutDefinition().Id;
      this.column = 0;
   }
   
   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#finishImpl(javax.faces.context.FacesContext, java.lang.String)
    */
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      this.dashboardManager.savePageConfig(this.editConfig);
      return outcome;
   }
   
   /**
    * @return Returns the summary data for the wizard.
    */
   public String getSummary()
   {
      LayoutDefinition def = DashboardManager.getDashboardConfig().getLayoutDefinition(this.layout);
      String label = def.Label;
      if (label == null)
      {
         label = Application.getMessage(FacesContext.getCurrentInstance(), def.LabelId);
      }
      return buildSummary(
            new String[]{"Layout"},
            new String[]{label});
   }
   
   
   // ------------------------------------------------------------------------------
   // Dashboard Wizard bean getters
   
   /**
    * @return The currently selected layout ID - used by the Dynamic Description component
    */
   public String getLayout()
   {
      return this.layout;
   }
   
   /**
    * Set the currently selected layout ID
    */
   public void setLayout(String layout)
   {
      this.layout = layout;
      LayoutDefinition def = DashboardManager.getDashboardConfig().getLayoutDefinition(layout);
      this.editConfig.getCurrentPage().setLayoutDefinition(def);
      if (this.column >= def.Columns)
      {
         this.column = def.Columns - 1;
      }
   }
   
   /**
    * @return the number of columns in the selected page layout
    */
   public int getColumnCount()
   {
      return DashboardManager.getDashboardConfig().getLayoutDefinition(getLayout()).Columns;
   }
   
   /**
    * @return the array of UI select items representing the columns that can be configured
    */
   public SelectItem[] getColumns()
   {
      FacesContext fc = FacesContext.getCurrentInstance();
      LayoutDefinition layoutDef = DashboardManager.getDashboardConfig().getLayoutDefinition(getLayout());
      SelectItem[] columns = new SelectItem[layoutDef.Columns];
      for (int i=0; i<layoutDef.Columns; i++)
      {
         String label = Application.getMessage(fc, MSG_COLUMN) + " " + Integer.toString(i + 1);
         columns[i] = new SelectItem(i, label);
      }
      return columns;
   }
   
   public int getColumn()
   {
      return this.column;
   }
   
   public void setColumn(int column)
   {
      if (column != this.column)
      {
         // setting this value will cause various List getters to return
         // different values on the next page refresh 
         this.column = column;
      }
   }
   
   public List<SelectItem> getAllDashlets()
   {
      if (this.dashlets == null)
      {
         FacesContext fc = FacesContext.getCurrentInstance();
         DashboardsConfigElement config = DashboardManager.getDashboardConfig();
         Collection<DashletDefinition> dashletDefs = config.getDashlets();
         List<SelectItem> dashlets = new ArrayList<SelectItem>(dashletDefs.size());
         for (DashletDefinition dashletDef : dashletDefs)
         {
            String label = dashletDef.Label;
            if (label == null)
            {
               label = Application.getMessage(fc, dashletDef.LabelId);
            }
            String description = dashletDef.Description;
            if (description == null)
            {
               description = Application.getMessage(fc, dashletDef.DescriptionId);
            }
            if (description != null)
            {
               // append description of the dashlet if set
               label = label + " (" + description + ')';
            }
            SelectItem item = new SelectItem(dashletDef.Id, label);
            dashlets.add(item);
         }
         this.dashlets = dashlets;
      }
      return this.dashlets;
   }
   
   /**
    * @return the List of SelectItem objects representing the dashlets displayed in the
    *         currently selected column.
    */
   public List<SelectItem> getColumnDashlets()
   {
      FacesContext fc = FacesContext.getCurrentInstance();
      
      Column column = this.editConfig.getCurrentPage().getColumns().get(this.column);
      List<SelectItem> dashlets = new ArrayList<SelectItem>(column.getDashlets().size());
      for (DashletDefinition dashletDef : column.getDashlets())
      {
         String label = dashletDef.Label;
         if (label == null)
         {
            label = Application.getMessage(fc, dashletDef.LabelId);
         }
         dashlets.add(new SelectItem(dashletDef.Id, label));
      }
      return dashlets;
   }
   
   /**
    * @return List of UIDescription objects for the available layouts
    */
   public List<UIDescription> getLayoutDescriptions()
   {
      if (this.layoutDescriptions == null)
      {
         buildLayoutValueLists();
      }
      return this.layoutDescriptions;
   }
   
   /**
    * @return the List of UIListItem objects representing the Layout icons 
    */
   public List<UIListItem> getLayoutIcons()
   {
      if (this.layoutIcons == null)
      {
         buildLayoutValueLists();
      }
      return this.layoutIcons;
   }
   
   private void buildLayoutValueLists()
   {
      List<UIListItem> icons = new ArrayList<UIListItem>(4);
      List<UIDescription> descriptions = new ArrayList<UIDescription>(4);
         
      FacesContext context = FacesContext.getCurrentInstance();
      
      DashboardsConfigElement config = DashboardManager.getDashboardConfig();
      Iterator<LayoutDefinition> layoutItr = config.getLayouts().iterator();
      while (layoutItr.hasNext())
      {
         LayoutDefinition layoutDef = layoutItr.next();
         
         // build UIListItem to represent the layout image
         String label = layoutDef.Label;
         if (label == null)
         {
            label = Application.getMessage(context, layoutDef.LabelId);
         }
         String desc = layoutDef.Description;
         if (desc == null)
         {
            desc = Application.getMessage(context, layoutDef.DescriptionId);
         }
         UIListItem item = new UIListItem();
         item.setLabel(label);
         item.setTooltip(desc);
         item.setValue(layoutDef.Id);
         // set the special attribute used by the imageRadioPicker component
         item.getAttributes().put("image", layoutDef.Image);
         icons.add(item);
         
         // build UIDescription to represent the layout description text
         UIDescription description = new UIDescription();
         description.setControlValue(layoutDef.Id);
         description.setText(desc);
         descriptions.add(description);
      }
      
      this.layoutIcons = icons;
      this.layoutDescriptions = descriptions;
   }
   
   /**
    * Action event handler called to Add dashlets to the selection for a column
    */
   public void addDashlets(ActionEvent event)
   {
      UISelectMany dashletPicker = (UISelectMany)event.getComponent().findComponent(COMPONENT_ALLDASHLETS);
      UISelectOne dashletColumn = (UISelectOne)event.getComponent().findComponent(COMPONENT_COLUMNDASHLETS);
      
      // get the IDs of the selected Dashlet definitions
      Object[] selected = dashletPicker.getSelectedValues();
      
      // get the column to add the dashlets too
      DashboardsConfigElement config = DashboardManager.getDashboardConfig();
      LayoutDefinition layoutDef = this.editConfig.getCurrentPage().getLayoutDefinition();
      Column column = this.editConfig.getCurrentPage().getColumns().get(this.column);
      // add each selected dashlet to the column
      for (int i=0; i<selected.length && column.getDashlets().size() < layoutDef.ColumnLength; i++)
      {
         column.addDashlet(config.getDashletDefinition((String)selected[i]));
      }
   }
   
   /**
    * Action handler called to Remove a dashlet from the selection for a column
    */
   public void removeDashlet(ActionEvent event)
   {
      UISelectOne dashletColumn = (UISelectOne)event.getComponent().findComponent(COMPONENT_COLUMNDASHLETS);
      
      // get the ID of the selected Dashlet definition
      String dashletId = (String)dashletColumn.getValue();
      Column column = this.editConfig.getCurrentPage().getColumns().get(this.column);
      
      // remove the selected dashlet from the column
      for (int i=0; i<column.getDashlets().size(); i++)
      {
         if (column.getDashlets().get(i).Id.equals(dashletId))
         {
            column.getDashlets().remove(i);
            break;
         }
      }
   }
}
