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

import java.io.*;
import java.util.ArrayList;
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
import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.workflow.*;
import org.alfresco.service.namespace.NamespaceService;
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
import org.alfresco.web.forms.RenditionImpl;
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
      final List<AVMDifference> diffList = new ArrayList<AVMDifference>(1 + this.renditions.size());
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

      if (this.startWorkflow)
      {
         WorkflowDefinition wd = null;
         Map<QName, Serializable> parameters = null;

         // get the workflow definition and parameters
         {
            final Node website = this.avmBrowseBean.getWebsite();
            final List<ChildAssociationRef> webFormRefs = this.nodeService.getChildAssocs(
               website.getNodeRef(), WCMAppModel.ASSOC_WEBFORM, RegexQNamePattern.MATCH_ALL);
            for (ChildAssociationRef ref : webFormRefs)
            {
               final String formName = (String)
                  this.nodeService.getProperty(ref.getChildRef(), WCMAppModel.PROP_FORMNAME);
               if (formName.equals(this.getForm().getName()))
               {
                  System.err.println("loading workflowRefs for " + formName);
                  final List<ChildAssociationRef> workflowRefs = 
                     this.nodeService.getChildAssocs(ref.getChildRef(),
                                                     WCMAppModel.ASSOC_WORKFLOWDEFAULTS,
                                                     RegexQNamePattern.MATCH_ALL);
                  if (workflowRefs.size() == 0)
                  {
                     throw new RuntimeException("no workflow parameters found for form " + formName);
                  }
                  if (workflowRefs.size() > 1)
                  {
                     throw new RuntimeException("found more than one workflow parameters node for " + formName);
                  }

                  final NodeRef workflowRef = workflowRefs.get(0).getChildRef();
                  final String workflowName = (String)this.nodeService.getProperty(workflowRef, WCMAppModel.PROP_WORKFLOW_NAME);
                  if (workflowName == null)
                  {
                     throw new RuntimeException("no workflow found for form " + formName);
                  }
                  System.err.println("using workflow " + workflowName + " for form " + formName);
                  wd = this.workflowService.getDefinitionByName("jbpm$" + workflowName);
                  
                  final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                  final ContentReader cr = this.contentService.getReader(workflowRef, WCMAppModel.PROP_WORKFLOWDEFAULTS);
                  if (cr == null)
                  {
                     parameters = new HashMap<QName, Serializable>();
                  }
                  else
                  {
                     cr.getContent(baos);
                     
                     final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                     final ObjectInputStream ois = new ObjectInputStream(bais);
                     parameters = (Map<QName, Serializable>)ois.readObject();
                  }
                  break;
               }
            }
         }

         System.err.println("creating workflow package");
         final NodeRef workflowPackageNodeRef = this.workflowService.createPackage(null);
// doesn't work yet.  need to use ASPECT_REFERENCES_NODE to get it to deal with avm nodes
// and we need some fixes from dave before we can enable.
//         QName qn = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
//                                      QName.createValidLocalName(this.formInstanceData.getName()));
//         String s = AVMNodeConverter.ToAVMVersionPath(this.formInstanceData.getNodeRef()).getSecond();
//         s = s.replaceFirst(AVMConstants.STORE_PREVIEW, AVMConstants.STORE_MAIN);
//         System.err.println("adding  " + s + " to workflow package with qname" + qn);
//         this.nodeService.addChild(workflowPackageNodeRef,
//                                   AVMNodeConverter.ToNodeRef(-1, s),
//                                   ContentModel.ASSOC_CONTAINS,
//                                   qn);
//         for (Rendition rendition : this.getRenditions())
//         {
//            qn = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
//                                   QName.createValidLocalName(rendition.getName()));
//            s = AVMNodeConverter.ToAVMVersionPath(rendition.getNodeRef()).getSecond();
//            s = s.replaceFirst(AVMConstants.STORE_PREVIEW, AVMConstants.STORE_MAIN);
//            System.err.println("adding  " + s + " to workflow package with qname " + qn);
//
//            this.nodeService.addChild(workflowPackageNodeRef,
//                                      AVMNodeConverter.ToNodeRef(-1, s),
//                                      ContentModel.ASSOC_CONTAINS,
//                                      qn);
//         }
         parameters.put(WorkflowModel.ASSOC_PACKAGE, workflowPackageNodeRef);
         System.err.println("starting workflow " + wd + " with parameters " + parameters);
         final WorkflowPath wp = this.workflowService.startWorkflow(wd.getId(), parameters);
      }
      else
      {
         System.err.println("************* not starting workflow");
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
      if (LOGGER.isDebugEnabled())
         LOGGER.debug("saving file content to " + this.fileName);
      // get the parent path of the location to save the content
      String path = this.avmBrowseBean.getCurrentPath();
      path = path.replaceFirst(AVMConstants.STORE_MAIN, AVMConstants.STORE_PREVIEW);
      if (MimetypeMap.MIMETYPE_XML.equals(this.mimeType) && this.formName != null)
      {

         final Document formInstanceData = fs.parseXML(this.content);

         path = this.getForm().getOutputPathForFormInstanceData(path, this.fileName, formInstanceData);
         final String[] sb = AVMNodeConverter.SplitBase(path);
         path = sb[0];
         this.fileName = sb[1];
      }


      if (LOGGER.isDebugEnabled())
         LOGGER.debug("reseting layer " + path.split(":")[0] + ":/" + AVMConstants.DIR_APPBASE);

      this.avmSyncService.resetLayer(path.split(":")[0] + ":/" + AVMConstants.DIR_APPBASE);

      if (LOGGER.isDebugEnabled())
         LOGGER.debug("creating all directories in path " + path);

      fs.makeAllDirectories(path);

      if (LOGGER.isDebugEnabled())
         LOGGER.debug("creating file " + this.fileName + " in " + path);

      // put the content of the file into the AVM store
      avmService.createFile(path, 
                            this.fileName, 
                            new ByteArrayInputStream((this.content == null ? "" : this.content).getBytes()));
      
      // remember the created path
      this.createdPath = path + '/' + this.fileName;
      
      // add titled aspect for the read/edit properties screens
      final NodeRef formInstanceDataNodeRef = AVMNodeConverter.ToNodeRef(-1, this.createdPath);
      Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>(1, 1.0f);
      titledProps.put(ContentModel.PROP_TITLE, this.fileName);
      this.nodeService.addAspect(formInstanceDataNodeRef, ContentModel.ASPECT_TITLED, titledProps);
      this.formInstanceData = new FormInstanceDataImpl(formInstanceDataNodeRef);

      if (MimetypeMap.MIMETYPE_XML.equals(this.mimeType) && this.formName != null)
      {
         this.getForm().registerFormInstanceData(formInstanceDataNodeRef);
         this.renditions = FormsService.getInstance().generateRenditions(formInstanceDataNodeRef);
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
