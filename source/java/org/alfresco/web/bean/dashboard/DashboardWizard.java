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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

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
   /** List of icons items to display as selectable Layout definitions */
   private List<UIListItem> layoutIcons = null;
   
   /** List of descriptions of the layouts */
   private List<UIDescription> layoutDescriptions= null;
   
   /** Currently selected layout */
   private String layout = DashboardManager.LAYOUT_DEFAULT;
   
   
   // ------------------------------------------------------------------------------
   // Wizard implementation
   
   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#init(java.util.Map)
    */
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      
   }

   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#finishImpl(javax.faces.context.FacesContext, java.lang.String)
    */
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      return null;
   }
   
   /**
    * @return Returns the summary data for the wizard.
    */
   public String getSummary()
   {
      LayoutDefinition def = DashboardManager.getDashboardConfig().getLayoutDefinition(this.layout);
      String label = def.Label;
      if (label == null || label.length() == 0)
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
      // TODO: implement - need current PageConfig from DashboardManager
      return this.layout;
   }
   
   /**
    * Set the currently selected layout ID
    */
   public void setLayout(String layout)
   {
      this.layout = layout;
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
         if (label == null || label.length() == 0)
         {
            label = Application.getMessage(context, layoutDef.LabelId);
         }
         String desc = layoutDef.Description;
         if (desc == null || desc.length() == 0)
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
}
