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
import org.alfresco.web.forms.Rendition;
import org.alfresco.web.ui.common.Utils;
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
   protected List<SelectItem> createMimeTypes;
   protected String createdPath = null;
   protected List<Rendition> renditions = null;
   protected FormInstanceData formInstanceData = null;
   protected boolean formSelectDisabled = false;
   protected boolean startWorkflow = false;

   /** AVM service bean reference */
   protected AVMService avmService;

   /** AVM sync service bean reference */
   protected AVMSyncService avmSyncService;
   
   /** AVM Browse Bean reference */
   protected AVMBrowseBean avmBrowseBean;

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
      this.renditions = null;
      this.startWorkflow = false;
      
      // check for a form ID being passed in as a parameter
      if (this.parameters.get(UIUserSandboxes.PARAM_FORM_ID) != null)
      {
         // it is used to init the dialog to a specific template
         String webFormId = parameters.get(UIUserSandboxes.PARAM_FORM_ID);
         NodeRef webFormRef = new NodeRef(Repository.getStoreRef(), webFormId);
         String formName = (String)this.nodeService.getProperty(webFormRef, WCMAppModel.PROP_FORMNAME);
         Form form = FormsService.getInstance().getForm(formName);
         if (form != null)
         {
            this.formName = form.getName();
            this.formSelectDisabled = true;
         }
      }
   }

   @Override
   public String next()
   {
      final int step = Application.getWizardManager().getCurrentStep();
      if (step == 3)
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
      return super.next();
   }

   @Override
   public String back()
   {
      final int step = Application.getWizardManager().getCurrentStep();
      if (step == 2)
      {
         LOGGER.debug("clearing form instance data");
         this.formInstanceData = null;
         this.renditions = null;
      }
      return super.back();
   }

   @Override
   public String finish()
   {
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
      final List<AVMDifference> diffList = 
         new ArrayList<AVMDifference>(1 + this.renditions.size());
      diffList.add(new AVMDifference(-1, this.createdPath, 
                                     -1, this.createdPath.replaceFirst(AVMConstants.STORE_PREVIEW, 
                                                                       AVMConstants.STORE_MAIN), 
                                     AVMDifference.NEWER));
      for (Rendition rendition : this.renditions)
      {
         final String path = AVMNodeConverter.ToAVMVersionPath(rendition.getNodeRef()).getSecond();
         diffList.add(new AVMDifference(-1, path, 
                                        -1, path.replaceFirst(AVMConstants.STORE_PREVIEW, 
                                                              AVMConstants.STORE_MAIN), 
                                        AVMDifference.NEWER));
      }
      this.avmSyncService.update(diffList, null, true, true, true, true, null, null);
      
      // reset all paths and structures to the main store
      this.createdPath = this.createdPath.replaceFirst(AVMConstants.STORE_PREVIEW,
                                                       AVMConstants.STORE_MAIN);
      boolean form = (MimetypeMap.MIMETYPE_XML.equals(this.mimeType) && this.formName != null);
      if (form)
      {
         this.formInstanceData = new FormInstanceDataImpl(AVMNodeConverter.ToNodeRef(-1, this.createdPath));
         this.renditions = this.formInstanceData.getRenditions();
      }
      if (this.startWorkflow)
      {
         WorkflowDefinition wd = null;
         Map<QName, Serializable> parameters = null;
         
         // get the workflow definition and parameters
         final Node website = this.avmBrowseBean.getWebsite();
         final List<ChildAssociationRef> webFormRefs = this.nodeService.getChildAssocs(
               website.getNodeRef(), WCMAppModel.ASSOC_WEBFORM, RegexQNamePattern.MATCH_ALL);
         for (ChildAssociationRef ref : webFormRefs)
         {
            final String formName = (String)
            this.nodeService.getProperty(ref.getChildRef(), WCMAppModel.PROP_FORMNAME);
            if (formName.equals(this.getForm().getName()))
            {
               if (LOGGER.isDebugEnabled())
                  LOGGER.debug("loading workflowRefs for " + formName);
               
               final List<ChildAssociationRef> workflowRefs = 
                  this.nodeService.getChildAssocs(ref.getChildRef(),
                        WCMAppModel.ASSOC_WORKFLOWDEFAULTS,
                        RegexQNamePattern.MATCH_ALL);
               if (workflowRefs.size() == 0)
               {
                  throw new AlfrescoRuntimeException("no workflow parameters found for form " + formName);
               }
               if (workflowRefs.size() > 1)
               {
                  throw new AlfrescoRuntimeException("found more than one workflow parameters node for " + formName);
               }
               
               final NodeRef workflowRef = workflowRefs.get(0).getChildRef();
               final String workflowName = (String)this.nodeService.getProperty(workflowRef, WCMAppModel.PROP_WORKFLOW_NAME);
               
               if (LOGGER.isDebugEnabled())
                  LOGGER.debug("using workflow " + workflowName + " for form " + formName);
               wd = this.workflowService.getDefinitionByName(workflowName);
               
               // deserialize the workflow parameters
               parameters = (Map<QName, Serializable>)AVMWorkflowUtil.deserializeWorkflowParams(workflowRef);
               
               break;
            }
         }
         
         if (LOGGER.isDebugEnabled())
            LOGGER.debug("creating workflow package");
         
         // create package paths (layered to user sandbox area as target)
         String stagingPath = AVMConstants.buildAVMStoreRootPath(this.avmBrowseBean.getStagingStore());
         String packagesPath = AVMWorkflowUtil.createAVMLayeredPackage(this.avmService, stagingPath);
         
         List<AVMDifference> diffs = new ArrayList<AVMDifference>(8);
         // construct diffs for selected items for submission
         String webapp = (String)website.getProperties().get(WCMAppModel.PROP_DEFAULTWEBAPP);
         String sandboxPath = AVMConstants.buildAVMStoreRootPath(this.avmBrowseBean.getSandbox());
         if (form)
         {
            // collect diffs for form data instance and all renditions
            for (Rendition rendition : this.getRenditions())
            {
               String renditionPath = AVMNodeConverter.ToAVMVersionPath(rendition.getNodeRef()).getSecond();
               int webappIndex = renditionPath.indexOf('/' + webapp);
               renditionPath = renditionPath.substring(webappIndex);
               String srcPath = sandboxPath + renditionPath;
               String destPath = packagesPath + renditionPath;
               AVMDifference diff = new AVMDifference(-1, srcPath, -1, destPath, AVMDifference.NEWER);
               diffs.add(diff);
            }
            String instancePath = AVMNodeConverter.ToAVMVersionPath(this.formInstanceData.getNodeRef()).getSecond();
            int webappIndex = instancePath.indexOf('/' + webapp);
            instancePath = instancePath.substring(webappIndex);
            String srcPath = sandboxPath + instancePath;
            String destPath = packagesPath + instancePath;
            AVMDifference diff = new AVMDifference(-1, srcPath, -1, destPath, AVMDifference.NEWER);
            diffs.add(diff);
         }
         else
         {
            // diff for txt or html content
            int webappIndex = this.createdPath.indexOf('/' + webapp);
            String itemPath = this.createdPath.substring(webappIndex);
            String srcPath = sandboxPath + itemPath;
            String destPath = packagesPath + itemPath;
            AVMDifference diff = new AVMDifference(-1, srcPath, -1, destPath, AVMDifference.NEWER);
            diffs.add(diff);
         }
         
         // write changes to layer so files are marked as modified
         this.avmSyncService.update(diffs, null, true, true, false, false, null, null);
         
         // convert package to workflow package
         AVMNodeDescriptor packageDesc = this.avmService.lookup(-1, packagesPath);
         NodeRef packageNodeRef = this.workflowService.createPackage(
               AVMNodeConverter.ToNodeRef(-1, packageDesc.getPath()));
         this.nodeService.setProperty(packageNodeRef, WorkflowModel.PROP_IS_SYSTEM_PACKAGE, true);
         parameters.put(WorkflowModel.ASSOC_PACKAGE, packageNodeRef);
         // TODO: capture label and comment?
         parameters.put(AVMWorkflowUtil.PROP_LABEL, form ? this.formInstanceData.getName() : this.fileName);
         parameters.put(AVMWorkflowUtil.PROP_FROM_PATH, AVMConstants.buildAVMStoreRootPath(
               this.avmBrowseBean.getSandbox()));
         
         if (LOGGER.isDebugEnabled())
            LOGGER.debug("starting workflow " + wd + " with parameters " + parameters);
         
         // start the workflow to get access to the start task
         WorkflowPath path = this.workflowService.startWorkflow(wd.id, parameters);
         if (path != null)
         {
            // extract the start task
            List<WorkflowTask> tasks = this.workflowService.getTasksForWorkflowPath(path.id);
            if (tasks.size() == 1)
            {
               WorkflowTask startTask = tasks.get(0);
               
               if (startTask.state == WorkflowTaskState.IN_PROGRESS)
               {
                  // end the start task to trigger the first 'proper' task in the workflow
                  this.workflowService.endTask(startTask.id, null);
               }
            }
         }
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
      final FormsService fs = FormsService.getInstance();
      // get the parent path of the location to save the content
      String fileName = this.fileName;
      if (LOGGER.isDebugEnabled())
         LOGGER.debug("saving file content to " + fileName);

      String path = this.avmBrowseBean.getCurrentPath();
      path = path.replaceFirst(AVMConstants.STORE_MAIN, AVMConstants.STORE_PREVIEW);
      if (MimetypeMap.MIMETYPE_XML.equals(this.mimeType) && this.formName != null)
      {
         final Document formInstanceData = fs.parseXML(this.content);

         path = this.getForm().getOutputPathForFormInstanceData(path, fileName, formInstanceData);
         final String[] sb = AVMNodeConverter.SplitBase(path);
         path = sb[0];
         fileName = sb[1];
      }

      if (LOGGER.isDebugEnabled())
         LOGGER.debug("reseting layer " + path.split(":")[0] + ":/" + AVMConstants.DIR_APPBASE);

      this.avmSyncService.resetLayer(path.split(":")[0] + ":/" + AVMConstants.DIR_APPBASE);

      if (LOGGER.isDebugEnabled())
         LOGGER.debug("creating all directories in path " + path);

      fs.makeAllDirectories(path);

      if (LOGGER.isDebugEnabled())
         LOGGER.debug("creating file " + fileName + " in " + path);

      // put the content of the file into the AVM store
      avmService.createFile(path, 
                            fileName, 
                            new ByteArrayInputStream((this.content == null ? "" : this.content).getBytes()));
      
      // remember the created path
      this.createdPath = path + '/' + fileName;
      
      // add titled aspect for the read/edit properties screens
      final NodeRef formInstanceDataNodeRef = AVMNodeConverter.ToNodeRef(-1, this.createdPath);
      Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>(1, 1.0f);
      titledProps.put(ContentModel.PROP_TITLE, fileName);
      this.nodeService.addAspect(formInstanceDataNodeRef, ContentModel.ASPECT_TITLED, titledProps);

      if (MimetypeMap.MIMETYPE_XML.equals(this.mimeType) && this.formName != null)
      {
         this.formInstanceData = new FormInstanceDataImpl(formInstanceDataNodeRef);
         this.getForm().registerFormInstanceData(formInstanceDataNodeRef);
         this.renditions = FormsService.getInstance().generateRenditions(formInstanceDataNodeRef);
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
      final Node website = this.avmBrowseBean.getWebsite();
      if (website == null)
      {
         throw new IllegalStateException("CreateWebContentWizard must be called within a Web Project context!");
      }
      final List<ChildAssociationRef> webFormRefs = this.nodeService.getChildAssocs(
            website.getNodeRef(), WCMAppModel.ASSOC_WEBFORM, RegexQNamePattern.MATCH_ALL);
      final List<SelectItem> items = new ArrayList<SelectItem>(webFormRefs.size());
      for (ChildAssociationRef ref : webFormRefs)
      {
         final String formName = (String)
            this.nodeService.getProperty(ref.getChildRef(), WCMAppModel.PROP_FORMNAME);
         final Form form = FormsService.getInstance().getForm(formName);
         if (form != null)
         {
            items.add(new SelectItem(formName, form.getTitle()));
         }
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
      final FormsService ts = FormsService.getInstance();
      return ts.getForm(this.getFormName());
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
   public FormProcessor.InstanceData getInstanceData()
   {
      return new FormProcessor.InstanceData()
      {
         private final FormsService ts = FormsService.getInstance();

         public Document getContent()
         { 
            try
            {
               final String content = CreateWebContentWizard.this.getContent();
               return content != null ? this.ts.parseXML(content) : null;
            }
            catch (Exception e)
            {
               e.printStackTrace();
               return null;
            }
         }
    
         public void setContent(final Document d)
         {
            CreateWebContentWizard.this.setContent(ts.writeXMLToString(d));
         }
      };
   }

   public FormInstanceData getFormInstanceData()
   {
      return this.formInstanceData;
   }
   
   public List<Rendition> getRenditions()
   {
      return this.renditions;
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
