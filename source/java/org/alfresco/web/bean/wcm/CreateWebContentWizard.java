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
package org.alfresco.web.bean.wcm;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
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
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.avm.wf.AVMSubmittedAspect;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
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
import org.alfresco.web.forms.FormInstanceDataImpl;
import org.alfresco.web.forms.FormProcessor;
import org.alfresco.web.forms.FormsService;
import org.alfresco.web.forms.RenderingEngineTemplate;
import org.alfresco.web.forms.Rendition;
import org.alfresco.web.forms.XMLUtil;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIListItem;
import org.alfresco.web.ui.wcm.component.UIUserSandboxes;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

/**
 * Bean implementation for the "Create Web Content Wizard" dialog
 */
public class CreateWebContentWizard extends BaseContentWizard
{
   private static final Log LOGGER = LogFactory.getLog(CreateWebContentWizard.class);
   
   protected String content = null;
   protected String formName;
   protected transient List<SelectItem> createMimeTypes;
   protected String createdPath = null;
   protected List<Rendition> renditions = null;
   protected FormInstanceData formInstanceData = null;
   protected FormProcessor.Session formProcessorSession = null;
   private Document instanceDataDocument = null;
   protected boolean formSelectDisabled = false;
   protected boolean startWorkflow = false;

   /** AVM service bean reference */
   protected AVMService avmService;

   /** AVM sync service bean reference */
   protected AVMSyncService avmSyncService;
   
   /** AVM Browse Bean reference */
   protected AVMBrowseBean avmBrowseBean;

   /** AVM Submitted Aspect reference */
   protected AVMSubmittedAspect avmSubmittedAspect;

   /** Workflow service bean reference */
   protected WorkflowService workflowService;
   
   /**
    * @param avmService       The AVMService to set.
    */
   public void setAvmService(AVMService avmService)
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
    * @param avmSubmittedAspect  The AVM Submitted Aspect to set.
    */
   public void setAvmSubmittedAspect(AVMSubmittedAspect avmSubmittedAspect)
   {
      this.avmSubmittedAspect = avmSubmittedAspect;
   }
   
   /**
    * @param workflowService  The WorkflowService to set.
    */
   public void setWorkflowService(WorkflowService workflowService)
   {
      this.workflowService = workflowService;
   }

   
   /**
    * @param avmBrowseBean    The AVMBrowseBean to set.
    */
   public void setAvmBrowseBean(AVMBrowseBean avmBrowseBean)
   {
      this.avmBrowseBean = avmBrowseBean;
   }
   
   
   // ------------------------------------------------------------------------------
   // Wizard implementation
   
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      this.content = null;
      this.inlineEdit = true;
      this.formName = null;
      this.mimeType = MimetypeMap.MIMETYPE_XML;
      this.formInstanceData = null;
      if (this.formProcessorSession != null)
      {
         this.formProcessorSession.destroy();
      }
      this.formProcessorSession = null;
      this.instanceDataDocument = null;
      this.renditions = null;
      this.startWorkflow = false;
      this.formSelectDisabled = false;
      
      // check for a form ID being passed in as a parameter
      if (this.parameters.get(UIUserSandboxes.PARAM_FORM_NAME) != null)
      {
         // it is used to init the dialog to a specific template
         final String formName = parameters.get(UIUserSandboxes.PARAM_FORM_NAME);
         final Form form = this.avmBrowseBean.getWebProject().getForm(formName);
         if (form != null)
         {
            this.formName = form.getName();
            this.formSelectDisabled = true;
         }
      }
      
      // reset the preview layer
      String storeName = AVMConstants.getStoreName(this.avmBrowseBean.getCurrentPath());
      storeName = AVMConstants.getCorrespondingPreviewStoreName(storeName);
      final String path = AVMConstants.buildStoreRootPath(storeName);
      if (LOGGER.isDebugEnabled())
         LOGGER.debug("reseting layer " + path);
      this.avmSyncService.resetLayer(path);
   }

   @Override
   public String next()
   {
      final int step = Application.getWizardManager().getCurrentStep();
      if (step == 3)
      {
         // if rendering a form, then save the content now to generate the renditions
         if (MimetypeMap.MIMETYPE_XML.equals(this.mimeType))
         {
            try
            {
               this.saveContent();
            }
            catch (Exception e)
            {
               Application.getWizardManager().getState().setCurrentStep(step - 1);
               Utils.addErrorMessage(e.getMessage(), e);
            }
         }
      }
      return super.next();
   }

   @Override
   public String back()
   {
      final int step = Application.getWizardManager().getCurrentStep();
      if (step == 2)
      {
         LOGGER.debug("clearing form instance data");
         if (this.formInstanceData != null)
         {
            this.avmService.removeNode(this.formInstanceData.getPath());
         }
         if (this.renditions != null)
         {
            for (Rendition r : this.renditions)
            {
               this.avmService.removeNode(r.getPath());
            }
         }
         this.formInstanceData = null;
         this.renditions = null;
      }
      return super.back();
   }

   @Override
   public String finish()
   {
      // if a form is not being entered, then save just html/text content
      if (this.formInstanceData == null || this.renditions == null)
      {
         try
         {
            this.saveContent();
         }
         catch (Exception e)
         {
            Utils.addErrorMessage(e.getMessage(), e);
            return super.getErrorOutcome(e);
         }
      }
      return super.finish();
   }
   
   @Override
   protected String finishImpl(final FacesContext context, final String outcome)
      throws Exception
   {
      final NodeRef[] uploadedFiles = (this.formProcessorSession != null
                                       ? this.formProcessorSession.getUploadedFiles()
                                       : new NodeRef[0]);
      final List<AVMDifference> diffList = 
         new ArrayList<AVMDifference>(1 + this.renditions.size() + uploadedFiles.length);
      diffList.add(new AVMDifference(-1, this.createdPath, 
                                     -1, AVMConstants.getCorrespondingPathInMainStore(this.createdPath),
                                     AVMDifference.NEWER));
      for (Rendition rendition : this.renditions)
      {
         final String path = rendition.getPath();
         diffList.add(new AVMDifference(-1, path, 
                                        -1, AVMConstants.getCorrespondingPathInMainStore(path),
                                        AVMDifference.NEWER));
      }

      for (NodeRef uploadedFile : uploadedFiles)
      {
         final String path = AVMNodeConverter.ToAVMVersionPath(uploadedFile).getSecond();
         diffList.add(new AVMDifference(-1, path,
                                        -1, AVMConstants.getCorrespondingPathInMainStore(path),
                                        AVMDifference.NEWER));
      }

      if (LOGGER.isDebugEnabled())
      {
         for (AVMDifference diff : diffList)
         {
            LOGGER.debug("updating main store with " + diff.getSourcePath());
         }
      }
      this.avmSyncService.update(diffList, null, true, true, true, true, null, null);
      
      // reset all paths and structures to the main store
      this.createdPath = AVMConstants.getCorrespondingPathInMainStore(this.createdPath);
      LOGGER.debug("reset path " + this.createdPath + " to main store");


      boolean form = (MimetypeMap.MIMETYPE_XML.equals(this.mimeType) && this.formName != null);
      if (form)
      {
         this.formInstanceData = new FormInstanceDataImpl(AVMNodeConverter.ToNodeRef(-1, this.createdPath));
         this.renditions = this.formInstanceData.getRenditions();
         LOGGER.debug("reset form instance data " + this.formInstanceData.getName() + 
                      " and " + this.renditions.size() + " to main store");
      }
      if (this.startWorkflow)
      {
         final WorkflowDefinition wd = this.getForm().getDefaultWorkflow();
         final Map<QName, Serializable> parameters = this.getForm().getDefaultWorkflowParameters();
         
         if (LOGGER.isDebugEnabled())
            LOGGER.debug("starting workflow " + wd + " with parameters " + parameters);

         if (parameters == null)
         {
            Utils.addErrorMessage(Application.getMessage(context, "submit_workflow_config_error"));
            return null;
         }
         
         // start the workflow to get access to the start task
         WorkflowPath path = this.workflowService.startWorkflow(wd.id, null);
         if (path != null)
         {
            // extract the start task
            List<WorkflowTask> tasks = this.workflowService.getTasksForWorkflowPath(path.id);
            if (tasks.size() == 1)
            {
               WorkflowTask startTask = tasks.get(0);
               
               if (startTask.state == WorkflowTaskState.IN_PROGRESS)
               {
                  if (LOGGER.isDebugEnabled())
                       LOGGER.debug("creating workflow package");
                  // create package paths (layered to user sandbox area as target)
                  final String storeId = this.avmBrowseBean.getStagingStore();
                  final List<String> srcPaths = new ArrayList<String>();
                  // construct diffs for selected items for submission
                  final String sandboxName = this.avmBrowseBean.getSandbox();
                  if (form)
                  {
                     // collect diffs for form data instance and all renditions
                     for (Rendition rendition : this.getRenditions())
                     {
                        srcPaths.add(AVMConstants.getCorrespondingPath(rendition.getPath(), sandboxName));
                     }
                     for (NodeRef uploadedFile : uploadedFiles)
                     {
                        final String uploadPath = AVMNodeConverter.ToAVMVersionPath(uploadedFile).getSecond();
                        srcPaths.add(AVMConstants.getCorrespondingPath(uploadPath, sandboxName));
                     }

                     srcPaths.add(AVMConstants.getCorrespondingPath(this.formInstanceData.getPath(), sandboxName));
                  }
                  else
                  {
                     // diff for txt or html content
                     srcPaths.add(AVMConstants.getCorrespondingPath(this.createdPath, sandboxName));
                  }

                  LOGGER.debug("creating workflow package with " + srcPaths.size() + " files");
                  final NodeRef packageNodeRef = 
                     AVMWorkflowUtil.createWorkflowPackage(srcPaths,
                                                           storeId,
                                                           path,
                                                           avmSubmittedAspect,
                                                           this.avmSyncService,
                                                           this.avmService,
                                                           this.workflowService,
                                                           this.nodeService);

                  parameters.put(WorkflowModel.ASSOC_PACKAGE, packageNodeRef);
                  // TODO: capture label and comment?
                  parameters.put(AVMWorkflowUtil.PROP_LABEL, form ? this.formInstanceData.getName() : this.fileName);
                  parameters.put(AVMWorkflowUtil.PROP_FROM_PATH, AVMConstants.buildStoreRootPath(sandboxName));
                    
                  // update start task with submit parameters
                  this.workflowService.updateTask(startTask.id, parameters, null, null);
                   
                  // end the start task to trigger the first 'proper' task in the workflow
                  this.workflowService.endTask(startTask.id, null);
               }
            }
         }
      }

      if (this.formProcessorSession != null)
      {
         this.formProcessorSession.destroy();
      }
      
      // return the default outcome
      return outcome;
   }
   
   @Override
   public boolean getNextButtonDisabled()
   {
      // TODO: Allow the next button state to be configured so that
      //       wizard implementations don't have to worry about 
      //       checking step numbers
      
      boolean disabled = false;
      int step = Application.getWizardManager().getCurrentStep();
      if (step == 1)
      {
         disabled = (this.fileName == null || this.fileName.length() == 0);
      }
      
      return disabled;
   }
   
   /**
    * Save the specified content using the currently set wizard attributes
    */
   protected void saveContent() 
      throws Exception
   {
      // get the parent path of the location to save the content
      String fileName = this.fileName;
      if (LOGGER.isDebugEnabled())
         LOGGER.debug("saving file content to " + fileName);

      String path = this.avmBrowseBean.getCurrentPath();
      path = AVMConstants.getCorrespondingPathInPreviewStore(path);
      final Form form = (MimetypeMap.MIMETYPE_XML.equals(this.mimeType) 
                         ? this.getForm()
                         : null);
      if (form != null)
      {
         path = form.getOutputPathForFormInstanceData(this.instanceDataDocument,
                                                      fileName,
                                                      path, 
                                                      this.avmBrowseBean.getWebapp());
         this.content = XMLUtil.toString(this.instanceDataDocument);
         final String[] sb = AVMNodeConverter.SplitBase(path);
         path = sb[0];
         fileName = sb[1];
      }

      if (LOGGER.isDebugEnabled())
         LOGGER.debug("creating all directories in path " + path);

      AVMConstants.makeAllDirectories(path);

      if (LOGGER.isDebugEnabled())
         LOGGER.debug("creating file " + fileName + " in " + path);

      // put the content of the file into the AVM store
      avmService.createFile(path, 
                            fileName, 
                            new ByteArrayInputStream((this.content == null ? "" : this.content).getBytes()));
      
      // remember the created path
      this.createdPath = AVMNodeConverter.ExtendAVMPath(path, fileName);
      
      // add titled aspect for the read/edit properties screens
      final NodeRef formInstanceDataNodeRef = AVMNodeConverter.ToNodeRef(-1, this.createdPath);
      final Map<QName, Serializable> props = new HashMap<QName, Serializable>(1, 1.0f);
      props.put(ContentModel.PROP_TITLE, fileName);
      this.nodeService.addAspect(formInstanceDataNodeRef, ContentModel.ASPECT_TITLED, props);

      if (form != null)
      {
         this.formInstanceData = new FormInstanceDataImpl(formInstanceDataNodeRef)
         {
            @Override
            public Form getForm() { return form; }
         };
         props.clear();
         props.put(WCMAppModel.PROP_PARENT_FORM_NAME, form.getName());
         this.nodeService.addAspect(formInstanceDataNodeRef, WCMAppModel.ASPECT_FORM_INSTANCE_DATA, props);

         this.renditions = new LinkedList<Rendition>();
         for (RenderingEngineTemplate ret : form.getRenderingEngineTemplates())
         {
            this.renditions.add(ret.render(this.formInstanceData));
         }
      }
      else
      {
         this.renditions = Collections.EMPTY_LIST;
      }
   }
   
   
   // ------------------------------------------------------------------------------
   // Bean Getters and Setters
   
   /**
    * @return Returns the content from the edited form.
    */
   public String getContent()
   {
      return this.content;
   }
   
   /**
    * @param content The content to edit (should be clear initially)
    */
   public void setContent(String content)
   {
      this.content = content;
   }

   /**
    * @return the available forms from this web project that can be created.
    */
   public List<SelectItem> getFormChoices()
   {
      final List<Form> forms = this.avmBrowseBean.getWebProject().getForms();
      final List<SelectItem> items = new ArrayList<SelectItem>(forms.size());
      for (final Form f : forms)
      {
         items.add(new SelectItem(f.getName(), f.getTitle()));
      }
      
      final QuickSort sorter = new QuickSort(items, "label", true, IDataContainer.SORT_CASEINSENSITIVE);
      sorter.sort();
      
      return items;
   }
   
   /**
    * @return Returns a list of mime types to allow the user to select from
    */
   public List<SelectItem> getCreateMimeTypes()
   {
      if (this.createMimeTypes == null)
      {
         FacesContext context = FacesContext.getCurrentInstance();
         
         // add the well known object type to start with
         this.createMimeTypes = new ArrayList<SelectItem>(5);
         
         // add the configured create mime types to the list
         ConfigService svc = Application.getConfigService(context);
         Config wizardCfg = svc.getConfig("Content Wizards");
         if (wizardCfg == null)
         {
            LOGGER.warn("Could not find 'Content Wizards' configuration section");
         }
         else
         {
            ConfigElement typesCfg = wizardCfg.getConfigElement("create-mime-types");
            if (typesCfg == null)
            {
               LOGGER.warn("Could not find 'create-mime-types' configuration element");
            }
            else
            {
               for (ConfigElement child : typesCfg.getChildren())
               {
                  String currentMimeType = child.getAttribute("name");
                  if (currentMimeType != null)
                  {
                     String label = getSummaryMimeType(currentMimeType);
                     this.createMimeTypes.add(new SelectItem(currentMimeType, label));
                  }
               }
               
               // make sure the list is sorted by the label
               QuickSort sorter = new QuickSort(this.objectTypes, "label", true, IDataContainer.SORT_CASEINSENSITIVE);
               sorter.sort();
            }
         }
      }
      
      return this.createMimeTypes;
   }
   
   public String getFormName()
   {
      return this.formName;
   }
   
   public Form getForm()
   {
      return (this.getFormName() != null 
              ? this.avmBrowseBean.getWebProject().getForm(this.getFormName())
              : null);
   }
   
   /**
    * @param form Sets the currently selected form
    */
   public void setFormName(final String formName)
   {
      this.formName = formName;
   }

   /**
    * @return Returns the wrapper instance data for feeding the xml
    * content to the form processor.
    */
   public Document getInstanceDataDocument()
   {
      if (this.instanceDataDocument == null)
      {
         final String content = this.getContent();
         try
         {
            this.instanceDataDocument = (content != null 
                                         ? XMLUtil.parse(content) 
                                         : XMLUtil.newDocument());
         }
         catch (Exception e)
         {
            Utils.addErrorMessage("error parsing document", e);
            this.instanceDataDocument = XMLUtil.newDocument();
         }
      }
      return this.instanceDataDocument;
   }

   /**
    * Returns the form processor session.
    */
   public FormProcessor.Session getFormProcessorSession()
   {
      return this.formProcessorSession;
   }

   /**
    * Sets the form processor session.
    */
   public void setFormProcessorSession(final FormProcessor.Session formProcessorSession)
   {
      this.formProcessorSession = formProcessorSession;
   }
      
   /**
    * Returns the generated form instance data.
    */
   public FormInstanceData getFormInstanceData()
   {
      return this.formInstanceData;
   }
   
   /**
    * Returns the generated renditions
    */
   public List<Rendition> getRenditions()
   {
      return this.renditions;
   }
   
   /**
    * Returns the files uploaded using the form
    */
   public List<UIListItem> getUploadedFiles()
   {
      if (this.formProcessorSession == null)
      {
         return Collections.EMPTY_LIST;
      }

      NodeRef[] uploadedFiles = this.formProcessorSession.getUploadedFiles();
      final List<UIListItem> result = 
         new ArrayList<UIListItem>(uploadedFiles.length);

      for (NodeRef nodeRef : uploadedFiles)
      {
         final UIListItem item = new UIListItem();
         final String name = (String)
            this.nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
         item.setValue(name);
         item.setLabel((String)this.nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE));
         item.setDescription((String)this.nodeService.getProperty(nodeRef, ContentModel.PROP_DESCRIPTION));
         item.setImage(Utils.getFileTypeImage(name, false));
         result.add(item);
      }
      return result;
   }

   /**
    * Returns the number of submittable files which is the total number of
    * uploaded files, renditions, and the form instance data.
    */
   public int getNumberOfSubmittableFiles()
   {
      return 1 + this.getUploadedFiles().size() + this.getRenditions().size();
   }

   public boolean getFormSelectDisabled()
   {
      return this.formSelectDisabled;
   }

   public void setFormSelectDisabled(boolean formSelectDisabled)
   {
      this.formSelectDisabled = formSelectDisabled;
   }

   public void setStartWorkflow(final boolean startWorkflow)
   {
      this.startWorkflow = startWorkflow;
   }

   public boolean getStartWorkflow()
   {
      return this.startWorkflow;
   }
   
   public String getSummary()
   {
      final ResourceBundle bundle = Application.getBundle(FacesContext.getCurrentInstance());
      
      // TODO: show first few lines of content here?
      return this.buildSummary(
            new String[] 
            {
               bundle.getString("file_name"), 
               bundle.getString("type"), 
               bundle.getString("content_type")
            },
            new String[] 
            {
               this.fileName, 
               this.getSummaryObjectType(), 
               this.getSummaryMimeType(this.mimeType)
            });
   }
   
   // ------------------------------------------------------------------------------
   // Action event handlers
   
   /**
    * Create content type value changed by the user
    */
   public void createContentChanged(ValueChangeEvent event)
   {
      // clear the content as HTML is not compatible with the plain text box etc.
      this.content = null;
   }
}
