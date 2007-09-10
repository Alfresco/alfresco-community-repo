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
 * http://www.alfresco.com/legal/licensing" 
 */
package org.alfresco.web.bean.wcm;

import java.io.File;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

import org.alfresco.web.ui.common.component.UIListItems;
import org.alfresco.web.ui.common.component.data.UIRichList;
import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.*;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wizard.BaseWizardBean;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.data.QuickSort;
import org.alfresco.web.forms.*;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIListItem;
import org.alfresco.web.ui.wcm.WebResources;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

/**
 * @author arielb
 */
public class RegenerateRenditionsWizard
   extends BaseWizardBean
{

   public final String REGENERATE_SCOPE_ALL = "all";
   public final String REGENERATE_SCOPE_FORM = "form";
   public final String REGENERATE_SCOPE_RENDERING_ENGINE_TEMPLATE = "rendering_engine_template";

   private final static Log LOGGER = LogFactory.getLog(RegenerateRenditionsWizard.class); 

   private AVMService avmService;
   private AVMSyncService avmSyncService;
   private ContentService contentService;
   private SearchService searchService;
   private FormsService formsService;
   private WebProject selectedWebProject;
   private String[] selectedForms;
   private String[] selectedRenderingEngineTemplates;
   private UIRichList renditionChoicesRichList;
   private List<Rendition> regeneratedRenditions;
   private String regenerateScope;

   // ------------------------------------------------------------------------------
   // Wizard implementation
   
   @Override
   protected String finishImpl(final FacesContext context, final String outcome)
      throws Exception
   {
      if (this.regeneratedRenditions != null)
      {
         final List<AVMDifference> diffList = new ArrayList<AVMDifference>(this.regeneratedRenditions.size());
         for (final Rendition r : this.regeneratedRenditions)
         {
            diffList.add(new AVMDifference(-1, r.getPath(), 
                                           -1, AVMUtil.getCorrespondingPathInMainStore(r.getPath()), 
                                           AVMDifference.NEWER));
         }
         LOGGER.debug("updating " + diffList.size() + " renditions in staging");
         this.avmSyncService.update(diffList, null, true, true, true, true, null, null);
         String description = null;
         final ResourceBundle bundle = Application.getBundle(FacesContext.getCurrentInstance());
         if (this.regenerateScope.equals(REGENERATE_SCOPE_FORM))
         {
            description = MessageFormat.format("regenerate_renditions_snapshot_description_scope_form",
                                               StringUtils.join(this.selectedForms, ", "));
         }
         else if (this.regenerateScope.equals(REGENERATE_SCOPE_RENDERING_ENGINE_TEMPLATE))
         {
            description = MessageFormat.format("regenerate_renditions_snapshot_description_scope_rendering_engine_template",
                                               StringUtils.join(this.selectedRenderingEngineTemplates, ", "));
         }
         else
         {
            description = MessageFormat.format("regenerate_renditions_snapshot_description_scope_web_project",
                                               this.selectedWebProject.getName());
         }
         this.avmService.createSnapshot(this.selectedWebProject.getStoreId(),
                                        MessageFormat.format("regenerate_renditions_snapshot_short_description", diffList.size()),
                                        description);
      }
      return outcome;
   }

   @Override
   public void init(final Map<String, String> parameters)
   {
      super.init(parameters);
      this.selectedWebProject = null;
      this.selectedForms = null;
      this.selectedRenderingEngineTemplates = null;
      this.renditionChoicesRichList = null;
      this.regeneratedRenditions = null;
      this.regenerateScope = REGENERATE_SCOPE_ALL;
      if (this.browseBean.getDocument() != null)
      {
         if (this.browseBean.getDocument().hasAspect(WCMAppModel.ASPECT_FORM))
         {
            this.selectedForms = new String[] { this.browseBean.getDocument().getName() };
         }
         else if (this.browseBean.getDocument().hasAspect(WCMAppModel.ASPECT_RENDERING_ENGINE_TEMPLATE))
         {
//            System.err.println("how to handle  ? " + this.browseBean.getDocument());
//            this.selectedRenderingEngineTemplates = new String[] { "*:
         }
      }
      else if (this.browseBean.getActionSpace() != null)
      {
         this.selectedForms = new String[] { this.browseBean.getActionSpace().getName() };
      }
   }
   
   @Override
   public String next()
   {
      final int step = Application.getWizardManager().getCurrentStep();
      if (step == 2)
      {
         try
         {
            this.regeneratedRenditions = this.regenerateRenditions();
         }
         catch (Exception e)
         {
            Application.getWizardManager().getState().setCurrentStep(step - 1);
            Utils.addErrorMessage(e.getMessage(), e);
         }
      }
      return super.next();
   }

   @Override
   public String cancel()
   {
      if (this.selectedWebProject != null)
      {
         final String stagingStoreName = this.selectedWebProject.getStoreId();
         final String previewStoreName = AVMUtil.getCorrespondingPreviewStoreName(stagingStoreName);
         this.avmSyncService.resetLayer(AVMUtil.buildStoreRootPath(previewStoreName));
      }
      return super.cancel();
   }
   
   @Override
   public boolean getNextButtonDisabled()
   {
      return false;
   }

   @Override
   public String getStepDescription()
   {
      final ResourceBundle bundle = Application.getBundle(FacesContext.getCurrentInstance());
      final String stepName = Application.getWizardManager().getCurrentStepName();
      if ("summary".equals(stepName))
      {
         final String s = this.selectedWebProject.getTitle();
         return MessageFormat.format(bundle.getString("regenerate_renditions_summary_desc"), 
                                     this.regeneratedRenditions.size(),
                                     s != null && s.length() != 0 ? s : this.selectedWebProject.getName());
      }
      else
      {
         return super.getContainerDescription();
      }
   }

   // ------------------------------------------------------------------------------
   // Bean Getters and Setters

   public String getRegenerateScope()
   {
      return this.regenerateScope;
   }

   public void setRegenerateScope(final String regenerateScope)
   {
      this.regenerateScope = regenerateScope;
   }

   public List<SelectItem> getWebProjectChoices()
   {
      final List<WebProject> webProjects = WebProject.getWebProjects();
      final List<SelectItem> result = new ArrayList<SelectItem>(webProjects.size());
      for (final WebProject wp : webProjects)
      {
         final String s = wp.getTitle();
         if (this.selectedWebProject == null)
         {
            this.selectedWebProject = wp;
         }
         result.add(new SelectItem(wp.getNodeRef().toString(), s != null && s.length() != 0 ? s : wp.getName()));
      }
      return result;
   }

   public String getSelectedWebProject()
   {
      return this.selectedWebProject == null ? null : this.selectedWebProject.getNodeRef().toString();
   }
   
   public void setSelectedWebProject(final String webProject)
   {
      this.selectedWebProject = (webProject == null || webProject.length() == 0 
                                 ? null 
                                 : new WebProject(new NodeRef(webProject)));

      final UIViewRoot c = FacesContext.getCurrentInstance().getViewRoot();
      ((UIListItems)c.findComponent("wizard:wizard-body:select_list_form_choices:list_items_form_choices")).setValue(null);
      ((UIListItems)c.findComponent("wizard:wizard-body:select_list_rendering_engine_template_choices:list_items_rendering_engine_template_choices")).setValue(null);
      this.renditionChoicesRichList = null;
   }

   public List<UIListItem> getFormChoices()
   {
      final List<UIListItem> result = new LinkedList<UIListItem>();
      final ResourceBundle bundle = Application.getBundle(FacesContext.getCurrentInstance());
      if (this.selectedWebProject != null)
      {
         for (final Form f : this.selectedWebProject.getForms())
         {
            final UIListItem item = new UIListItem();
            item.setValue(f.getName());
            item.setLabel(f.getTitle());
            final List<FormInstanceData> fids = this.getRelatedFormInstanceData(this.selectedWebProject, f);

            item.setDescription(MessageFormat.format(bundle.getString("regenerate_renditions_select_renditions_select_item_desc"), 
                                                     fids.size() * f.getRenderingEngineTemplates().size(),
                                                     this.selectedWebProject.getName()));
            item.setImage("/images/icons/webform_large.gif");
            result.add(item);
         }
      }
      return result;
   }

   public String[] getSelectedForms()
   {
      return this.selectedForms;
   }

   public void setSelectedForms(final String[] forms)
   {
      this.selectedForms = forms == null || forms.length == 0 ? null : forms;
   }

   public String getSelectedForm()
   {
      return this.selectedForms != null && this.selectedForms.length != 0 ? this.selectedForms[0] : null;
   }

   public void setSelectedForm(final String form)
   {
      this.selectedForms = form == null || form.length() == 0 ? null : new String[] { form };
      this.renditionChoicesRichList = null;
   }

   public List<UIListItem> getRenderingEngineTemplateChoices()
   {
      final List<UIListItem> result = new LinkedList<UIListItem>();      
      final ResourceBundle bundle = Application.getBundle(FacesContext.getCurrentInstance());
      for (final Form f : this.selectedWebProject.getForms())
      {
         for (final RenderingEngineTemplate ret : f.getRenderingEngineTemplates())
         {
            final UIListItem item = new UIListItem();
            item.setValue(f.getName() + ":" + ret.getName());
            item.setLabel(ret.getTitle() + "(" + ret.getMimetypeForRendition() + ")");
            final List<Rendition> rs = this.getRelatedRenditions(this.selectedWebProject, ret);
            item.setDescription(MessageFormat.format(bundle.getString("regenerate_renditions_select_renditions_select_item_desc"), 
                                                     rs.size(),
                                                     this.selectedWebProject.getName()));
            item.setImage(Utils.getFileTypeImage(ret.getName(), false));
            result.add(item);
         }
      }
      return result;
   }

   public String[] getSelectedRenderingEngineTemplates()
   {
      return this.selectedRenderingEngineTemplates;
   }

   public void setSelectedRenderingEngineTemplates(final String[] renderingEngineTemplates)
   {
      this.selectedRenderingEngineTemplates = renderingEngineTemplates;
      this.renditionChoicesRichList = null;
   }


   public List<Rendition> getRegeneratedRenditions()
   {
      return this.regeneratedRenditions;
   }

   public void setRegeneratedRenditionsRichList(UIRichList richList)
   {
      this.renditionChoicesRichList = richList;
   }
   
   public UIRichList getRegeneratedRenditionsRichList()
   {
      return this.renditionChoicesRichList;
   }

   // ------------------------------------------------------------------------------
   // Service Injection
   
   /**
    * @param contentService The contentService to set.
    */
   public void setContentService(final ContentService contentService)
   {
      this.contentService = contentService;
   }
   
   /**
    * @param avmService       The AVMService to set.
    */
   public void setAvmService(final AVMService avmService)
   {
      this.avmService = avmService;
   }

   /**
    * @param avmSyncService       The AVMSyncService to set.
    */
   public void setAvmSyncService(final AVMSyncService avmSyncService)
   {
      this.avmSyncService = avmSyncService;
   }

   /**
    * @param searchService       The SearchService to set.
    */
   public void setSearchService(final SearchService searchService)
   {
      this.searchService = searchService;
   }

   /**
    * @param formsService    The FormsService to set.
    */
   public void setFormsService(final FormsService formsService)
   {
      this.formsService = formsService;
   }
   
   // ------------------------------------------------------------------------------
   // Helper Methods

   private List<FormInstanceData> getRelatedFormInstanceData(final WebProject webProject, final Form f)
   {
      final SearchParameters sp = new SearchParameters();
      final StoreRef storeRef = AVMNodeConverter.ToStoreRef(webProject.getStagingStore());
      sp.addStore(storeRef);
      sp.setLanguage(SearchService.LANGUAGE_LUCENE);
      StringBuilder query = new StringBuilder();
      query.append("+ASPECT:\"" + WCMAppModel.ASPECT_FORM_INSTANCE_DATA + "\"");
      query.append("-ASPECT:\"" + WCMAppModel.ASPECT_RENDITION + "\"");
      query.append(" +@" + Repository.escapeQName(WCMAppModel.PROP_PARENT_FORM_NAME) + 
                   ":\"" + f.getName() + "\"");

      LOGGER.debug("running query " + query);
      sp.setQuery(query.toString());
      final ResultSet rs = this.searchService.query(sp);
      final List<FormInstanceData> result = new ArrayList<FormInstanceData>(rs.length());
      for (final ResultSetRow row : rs)
      {
         final String avmPath = AVMNodeConverter.ToAVMVersionPath(row.getNodeRef()).getSecond();
         final String previewAvmPath = AVMUtil.getCorrespondingPathInPreviewStore(avmPath);
         result.add(this.formsService.getFormInstanceData(-1, previewAvmPath));
      }
      return result;
   }

   private List<Rendition> getRelatedRenditions(final WebProject webProject, final RenderingEngineTemplate ret)
   {
      final SearchParameters sp = new SearchParameters();
      final StoreRef storeRef = AVMNodeConverter.ToStoreRef(webProject.getStagingStore());
      sp.addStore(storeRef);
      sp.setLanguage(SearchService.LANGUAGE_LUCENE);
      StringBuilder query = new StringBuilder();
      query.append("+ASPECT:\"" + WCMAppModel.ASPECT_RENDITION + "\"");
      query.append("+@" + Repository.escapeQName(WCMAppModel.PROP_PARENT_RENDERING_ENGINE_TEMPLATE) + 
                   ":\"" + ((RenderingEngineTemplateImpl)ret).getNodeRef() + "\"");

      LOGGER.debug("running query " + query);
      sp.setQuery(query.toString());
      final ResultSet rs = this.searchService.query(sp);
      final List<Rendition> result = new ArrayList<Rendition>(rs.length()); 
      for (final ResultSetRow row : rs)
      {
         final String avmPath = AVMNodeConverter.ToAVMVersionPath(row.getNodeRef()).getSecond();
         final String previewAvmPath = AVMUtil.getCorrespondingPathInPreviewStore(avmPath);
         result.add(this.formsService.getRendition(-1, previewAvmPath));
      }
      return result;
   }

   private List<Rendition> regenerateRenditions()
   {
      final String stagingStoreName = this.selectedWebProject.getStoreId();
      final String previewStoreName = AVMUtil.getCorrespondingPreviewStoreName(stagingStoreName);
      this.avmSyncService.resetLayer(AVMUtil.buildStoreRootPath(previewStoreName));

      final SearchParameters sp = new SearchParameters();
      final StoreRef storeRef = AVMNodeConverter.ToStoreRef(this.selectedWebProject.getStagingStore());
      sp.addStore(storeRef);
      sp.setLanguage(SearchService.LANGUAGE_LUCENE);
      StringBuilder query = new StringBuilder();
      if (this.regenerateScope.equals(REGENERATE_SCOPE_ALL) ||
          this.regenerateScope.equals(REGENERATE_SCOPE_FORM))
      {
         query.append("+ASPECT:\"" + WCMAppModel.ASPECT_FORM_INSTANCE_DATA + "\"");
         query.append("-ASPECT:\"" + WCMAppModel.ASPECT_RENDITION + "\"");
      }
      else
      {
         query.append("+ASPECT:\"" + WCMAppModel.ASPECT_RENDITION + "\"");
      }

      if (this.regenerateScope.equals(REGENERATE_SCOPE_FORM))
      {
         query.append(" +(");
         for (int i = 0; i < this.selectedForms.length; i++)
         {
            query.append("@" + Repository.escapeQName(WCMAppModel.PROP_PARENT_FORM_NAME) + 
                         ":\"" + this.selectedForms[i] + "\"");
            if (i != this.selectedForms.length - 1)
            {
               query.append(" OR ");
            }
         }
         query.append(") ");
      }
      if (this.regenerateScope.equals(REGENERATE_SCOPE_RENDERING_ENGINE_TEMPLATE))
      {
         query.append(" +(");
         for (int i = 0; i < this.selectedRenderingEngineTemplates.length; i++)
         {
            try
            {
               final String formName = this.selectedRenderingEngineTemplates[i].split(":")[0];
               final Form f = this.selectedWebProject.getForm(formName);
               final RenderingEngineTemplate ret = f.getRenderingEngineTemplate((String)this.selectedRenderingEngineTemplates[i].split(":")[1]);
               query.append("@" + Repository.escapeQName(WCMAppModel.PROP_PARENT_RENDERING_ENGINE_TEMPLATE) + 
                            ":\"" + ((RenderingEngineTemplateImpl)ret).getNodeRef() + "\"");
               if (i != this.selectedRenderingEngineTemplates.length - 1)
               {
                  query.append(" OR ");
               }
            }
            catch (FormNotFoundException fnfe)
            {
               LOGGER.debug(fnfe);
            }
         }
         query.append(") ");
      }

      LOGGER.debug("running query " + query);
      sp.setQuery(query.toString());
      final ResultSet rs = this.searchService.query(sp);
      if (LOGGER.isDebugEnabled())
         LOGGER.debug("received " + rs.length() + " results");
    
      final List<Rendition> result = new ArrayList<Rendition>(rs.length());
      for (final ResultSetRow row : rs)
      {
         final String avmPath = AVMNodeConverter.ToAVMVersionPath(row.getNodeRef()).getSecond();
         final String previewAvmPath = AVMUtil.getCorrespondingPathInPreviewStore(avmPath);
         if (this.regenerateScope.equals(REGENERATE_SCOPE_ALL) ||
             this.regenerateScope.equals(REGENERATE_SCOPE_FORM))
         {
            final FormInstanceData fid = this.formsService.getFormInstanceData(-1, previewAvmPath);
            try
            {
               final List<FormInstanceData.RegenerateResult> regenResults = fid.regenerateRenditions();
               for (final FormInstanceData.RegenerateResult rr : regenResults)
               {
                  if (rr.getException() != null)
                  {
                     Utils.addErrorMessage("error regenerating rendition using " + rr.getRenderingEngineTemplate().getName() + 
                                           ": " + rr.getException().getMessage(),
                                           rr.getException());
                  }
                  else
                  {
                     result.add(rr.getRendition());
                  }
               }
            }
            catch (FormNotFoundException fnfe)
            {
               Utils.addErrorMessage("error regenerating renditions of " + fid.getPath() + ": " + fnfe.getMessage(), fnfe);
            }
         }
         else
         {
            final Rendition r = this.formsService.getRendition(-1, previewAvmPath);
            try
            {
               r.regenerate();
               result.add(r);
            }
            catch (Exception e)
            {
               Utils.addErrorMessage("error regenerating rendition using " + r.getRenderingEngineTemplate().getName() + 
                                     ": " + e.getMessage(),
                                     e);
            }
         }
      }
      return result;
   }
}
