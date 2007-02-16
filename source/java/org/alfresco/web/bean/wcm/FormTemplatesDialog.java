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
package org.alfresco.web.bean.wcm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.wcm.CreateWebsiteWizard.FormWrapper;
import org.alfresco.web.bean.wcm.CreateWebsiteWizard.PresentationTemplate;
import org.alfresco.web.forms.Form;
import org.alfresco.web.forms.RenderingEngineTemplate;
import org.alfresco.web.ui.common.component.UIListItem;
import org.alfresco.web.ui.common.component.UISelectList;
import org.alfresco.web.ui.wcm.WebResources;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Backing bean for the Website Project Form Templates dialog.
 * Launched from the Select Templates button on the Define Web Content Forms page.
 * 
 * @author Kevin Roast
 */
public class FormTemplatesDialog extends BaseDialogBean
{
   private static final String COMPONENT_TEMPLATELIST = "template-list";

   private static final Log logger = LogFactory.getLog(FormTemplatesDialog.class);
   
   protected AVMService avmService;
   protected CreateWebsiteWizard websiteWizard;
   
   /** datamodel for table of selected presentation templates */
   private DataModel templatesDataModel = null;
   
   /** list of objects describing the selected presentation templates*/
   private List<PresentationTemplate> templates = null;
   
   /** transient list of template UIListItem objects */
   private List<UIListItem> templateList = null;
   
   /**
    * @param avmService    The avmService to set.
    */
   public void setAvmService(AVMService avmService)
   {
      this.avmService = avmService;
   }
   
   /**
    * @param wizard        The Create Website Wizard to set.
    */
   public void setCreateWebsiteWizard(CreateWebsiteWizard wizard)
   {
      this.websiteWizard = wizard;
   }
   
   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#init(java.util.Map)
    */
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      this.templatesDataModel = null;
      this.templates = new ArrayList<PresentationTemplate>(getActionForm().getTemplates().size());
      this.templates.addAll(getActionForm().getTemplates());
   }
   
   /**
    * @return an object representing the form for the current action
    */
   public FormWrapper getActionForm()
   {
      return this.websiteWizard.getActionForm();
   }
   
   /**
    * @return JSF data model wrapping the templates selected by the user
    */
   public DataModel getTemplatesDataModel()
   {
      if (this.templatesDataModel == null)
      {
         this.templatesDataModel = new ListDataModel();
      }
      
      this.templatesDataModel.setWrappedData(this.templates);
      
      return this.templatesDataModel;
   }

   /**
    * @param templatesDataModel  JSF data model wrapping the templates
    */
   public void setTemplatesDataModel(DataModel templatesDataModel)
   {
      this.templatesDataModel = templatesDataModel;
   }
   
   /**
    * @return List of UIListItem objects representing the available presentation templates for selection
    */
   public List<UIListItem> getTemplatesList()
   {
      Form form = getActionForm().getForm();
      List<RenderingEngineTemplate> engines = form.getRenderingEngineTemplates();
      List<UIListItem> items = new ArrayList<UIListItem>(engines.size());
      for (RenderingEngineTemplate engine : engines)
      {
         PresentationTemplate wrapper = new PresentationTemplate(engine);
         UIListItem item = new UIListItem();
         item.setValue(wrapper);
         item.setLabel(wrapper.getTitle() + " (" + engine.getMimetypeForRendition() + ")");
         item.setDescription(wrapper.getDescription());
         item.setImage(WebResources.IMAGE_TEMPLATE_32);
         items.add(item);
      }
      this.templateList = items;
      return items;
   }
   
   /**
    * Action handler to add a template to the list for this form
    */
   public void addTemplate(ActionEvent event)
   {
      UISelectList selectList = (UISelectList)event.getComponent().findComponent(COMPONENT_TEMPLATELIST);
      int index = selectList.getRowIndex();
      if (index != -1)
      {
         PresentationTemplate template = (PresentationTemplate)this.templateList.get(index).getValue();
         // clone the PresentationTemplate into one the user can modify
         this.templates.add(new PresentationTemplate(template.getRenderingEngineTemplate(), template.getOutputPathPattern()));
      }
   }
   
   /**
    * Remove a presentation template from the selected list
    */
   public void removeTemplate(ActionEvent event)
   {
      PresentationTemplate wrapper = (PresentationTemplate)this.templatesDataModel.getRowData();
      if (wrapper != null)
      {
         this.templates.remove(wrapper);
      }
   }
   
   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#finishImpl(javax.faces.context.FacesContext, java.lang.String)
    */
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      List<PresentationTemplate> list = getActionForm().getTemplates();
      list.clear();
      for (PresentationTemplate wrapper : this.templates)
      {
         list.add(wrapper);
      }
      return outcome;
   }
   
   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#getFinishButtonDisabled()
    */
   @Override
   public boolean getFinishButtonDisabled()
   {
      return false;
   }
}
