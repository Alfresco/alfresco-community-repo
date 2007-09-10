/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.web.bean.wcm;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.alfresco.config.Config;
import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigService;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.model.WCMWorkflowModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.avm.AVMExistsException;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.content.BaseContentWizard;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.data.QuickSort;
import org.alfresco.web.forms.Form;
import org.alfresco.web.forms.FormInstanceData;
import org.alfresco.web.forms.FormNotFoundException;
import org.alfresco.web.forms.FormProcessor;
import org.alfresco.web.forms.FormsService;
import org.alfresco.web.forms.RenderingEngineTemplate;
import org.alfresco.web.forms.Rendition;
import org.alfresco.web.forms.XMLUtil;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIListItem;
import org.alfresco.web.ui.wcm.component.UIUserSandboxes;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

/**
 * Bean implementation for the "Edit Web Content Wizard" dialog
 */
public class EditWebContentWizard extends CreateWebContentWizard
{
   private static final Log LOGGER = LogFactory.getLog(EditWebContentWizard.class);
   
   private AVMNode avmNode;
   private Form form;

   // ------------------------------------------------------------------------------
   // Wizard implementation
   
   @Override
   public void init(final Map<String, String> parameters)
   {
      super.init(parameters);
      this.avmNode = this.avmBrowseBean.getAvmActionNode();
      if (this.avmNode == null)
      {
         throw new IllegalArgumentException("Edit Form wizard requires action node context.");
      }
      LOGGER.debug("path is " + this.avmNode.getPath());
      this.createdPath = AVMUtil.getCorrespondingPathInPreviewStore(this.avmNode.getPath());
      this.formInstanceData = this.formsService.getFormInstanceData(-1, this.createdPath);
      final WebProject webProject = new WebProject(this.createdPath);
      try
      {
         this.formName = this.formInstanceData.getForm().getName();
         this.form = webProject.getForm(this.formName);
      }
      catch (FormNotFoundException fnfe)
      {
         Utils.addErrorMessage(fnfe.getMessage(), fnfe);
      }
      this.content = this.avmService.getContentReader(-1, this.createdPath).getContentString();
      this.fileName = this.formInstanceData.getName();
      this.mimeType = MimetypeMap.MIMETYPE_XML;
   }

   @Override
   public String back()
   {
      if ("content".equals(Application.getWizardManager().getCurrentStepName()))
      {
         //override in order not to delete these items
         this.formInstanceData = null;
         this.renditions = null;
      }
      return super.back();
   }

   @Override
   protected void saveContent()
      throws Exception
   {
      if (LOGGER.isDebugEnabled())
      {
         LOGGER.debug("saving " + this.createdPath);
      }
      final ContentWriter writer = this.avmService.getContentWriter(this.createdPath);
      this.content = XMLUtil.toString(this.instanceDataDocument, false);
      writer.putContent(this.content);
      // XXXarielb might not need to do this reload
      this.formInstanceData = this.formsService.getFormInstanceData(-1, this.createdPath);
      final List<FormInstanceData.RegenerateResult> result = this.formInstanceData.regenerateRenditions();
      this.renditions = new LinkedList<Rendition>();
      for (FormInstanceData.RegenerateResult rr : result)
      {
         if (rr.getException() != null)
         {
            Utils.addErrorMessage("error regenerating rendition using " + rr.getRenderingEngineTemplate().getName() + 
                                  ": " + rr.getException().getMessage(),
                                  rr.getException());
         }
         else
         {
            this.renditions.add(rr.getRendition());
         }
      }
   }

   /** Indicates whether or not the wizard is currently in edit mode */
   @Override
   public boolean getEditMode()
   {
      return true;
   }

   @Override
   public boolean getSubmittable()
   {
      return !AVMUtil.isWorkflowStore(AVMUtil.getStoreName(this.createdPath));
   }

   /** 
    * Overridden to avoid calling getWebProject since potentially there is no web project 
    * context in workflow scenario.
    */
   @Override 
   public Form getForm()
   {
      return this.form;
   }
}
