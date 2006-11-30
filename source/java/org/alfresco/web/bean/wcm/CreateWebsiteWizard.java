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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wizard.BaseWizardBean;
import org.alfresco.web.bean.wizard.InviteUsersWizard.UserGroupRole;
import org.alfresco.web.forms.Form;
import org.alfresco.web.forms.FormsService;
import org.alfresco.web.forms.RenderingEngineTemplate;
import org.alfresco.web.ui.common.component.UIListItem;
import org.alfresco.web.ui.common.component.UISelectList;
import org.alfresco.web.ui.wcm.WebResources;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Backing bean for the Create Web Project wizard.
 * 
 * @author Kevin Roast
 */
public class CreateWebsiteWizard extends BaseWizardBean
{
   private static final String COMPONENT_FORMLIST = "form-list";
   private static final String COMPONENT_WORKFLOWLIST = "workflow-list";
   private static final String MSG_DESCRIPTION = "description";
   private static final String MSG_NAME = "name";
   private static final String MSG_USERROLES = "create_website_summary_users";
   private static final String MSG_FORM_SUMMARY = "website_form_summary";
   private static final String MSG_NONE = "value_not_set";

   private static final String WEBAPP_DEFAULT = "ROOT";

   private static Log logger = LogFactory.getLog(CreateWebsiteWizard.class);
   
   protected String dnsName;
   protected String title;
   protected String name;
   protected String description;
   protected String webapp = WEBAPP_DEFAULT;
   
   private String websitesFolderId = null;
   
   protected AVMService avmService;
   protected WorkflowService workflowService;
   
   /** datamodel for table of selected forms */
   private DataModel formsDataModel = null;
   
   /** transient list of form UIListItem objects */
   private List<UIListItem> formsList = null;
   
   /** list of form wrapper objects */
   private List<FormWrapper> forms = null;
   
   /** Current form for dialog context */
   private FormWrapper actionForm = null;
   
   /** datamodel for table of selected workflows */
   private DataModel workflowsDataModel = null;
   
   /** transient list of workflow UIListItem objects */
   private List<UIListItem> workflowsList = null;
   
   /** list of workflow wrapper objects */
   private List<WorkflowWrapper> workflows = null;
   
   /** Current workflow for dialog context */
   private WorkflowWrapper actionWorkflow = null;
   
   
   // ------------------------------------------------------------------------------
   // Wizard implementation
   
   /**
    * Initialises the wizard
    */
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      this.name = null;
      this.dnsName = null;
      this.title = null;
      this.description = null;
      this.formsDataModel = null;
      this.forms = new ArrayList<FormWrapper>(8);
      this.workflowsDataModel = null;
      this.workflows = new ArrayList<WorkflowWrapper>(4);
      
      // init the dependant bean we are using for the invite users pages
      InviteWebsiteUsersWizard wiz = getInviteUsersWizard();
      wiz.init(null);
   }
   
   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#finishImpl(javax.faces.context.FacesContext, java.lang.String)
    */
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      // create the website space in the correct parent folder
      String websiteParentId = getWebsitesFolderId();
      
      FileInfo fileInfo = this.fileFolderService.create(
            new NodeRef(Repository.getStoreRef(), websiteParentId),
            this.name,
            WCMAppModel.TYPE_AVMWEBFOLDER);
      NodeRef nodeRef = fileInfo.getNodeRef();
      
      if (logger.isDebugEnabled())
         logger.debug("Created website folder node with name: " + this.name);
      
      String avmStore = DNSNameMangler.MakeDNSName(this.dnsName);
      
      // apply the uifacets aspect - icon, title and description props
      Map<QName, Serializable> uiFacetsProps = new HashMap<QName, Serializable>(4);
      uiFacetsProps.put(ApplicationModel.PROP_ICON, AVMConstants.SPACE_ICON_WEBSITE);
      uiFacetsProps.put(ContentModel.PROP_TITLE, this.title);
      uiFacetsProps.put(ContentModel.PROP_DESCRIPTION, this.description);
      this.nodeService.addAspect(nodeRef, ApplicationModel.ASPECT_UIFACETS, uiFacetsProps);
      
      // set the default webapp name for the project
      String webapp = (this.webapp != null && this.webapp.length() != 0) ? this.webapp : WEBAPP_DEFAULT;
      this.nodeService.setProperty(nodeRef, WCMAppModel.PROP_DEFAULTWEBAPP, webapp);
      
      // call a delegate wizard bean to provide invite user functionality
      InviteWebsiteUsersWizard wiz = getInviteUsersWizard();
      wiz.setNode(new Node(nodeRef));
      wiz.setAvmStore(avmStore);
      wiz.setStandalone(false);
      // the wizard is responsible for notifying the invited users, setting the appropriate
      // node permissions and also for creating user sandboxes and associations to the web folder node
      outcome = wiz.finish();
      if (outcome != null)
      {
         // create the AVM staging store to represent the newly created location website
         SandboxFactory.createStagingSandbox(avmStore, wiz.getManagers());
         
         // create the default webapp folder under the hidden system folders
         String stagingStore = AVMConstants.buildAVMStagingStoreName(avmStore);
         this.avmService.createDirectory(AVMConstants.buildAVMStoreRootPath(stagingStore), webapp);
         
         // set the property on the node to reference the root AVM store
         this.nodeService.setProperty(nodeRef, WCMAppModel.PROP_AVMSTORE, avmStore);
         
         // persist the forms, templates, workflows and workflow defaults to the model for this web project
         saveWebProjectModel(nodeRef);
         
         // navigate to the Websites folder so we can see the newly created folder
         this.navigator.setCurrentNodeId(websiteParentId);
         
         outcome = AlfrescoNavigationHandler.CLOSE_WIZARD_OUTCOME;
      }
      return outcome;
   }
   
   /**
    * Persist the forms, templates, workflows and workflow defaults to the model for this web project
    * 
    * @param nodeRef        NodeRef to the web project
    */
   private void saveWebProjectModel(NodeRef nodeRef)
   {
      Map<QName, Serializable> props = new HashMap<QName, Serializable>(4, 1.0f);
      
      // first walk each form object, saving templates and workflow defaults for each
      for (FormWrapper form : this.forms)
      {
         // create web form with name as per the name of the form object in the DD
         props.put(WCMAppModel.PROP_FORMNAME, form.getName());
         NodeRef formRef = this.nodeService.createNode(nodeRef,
                  WCMAppModel.ASSOC_WEBFORM,
                  WCMAppModel.ASSOC_WEBFORM,
                  WCMAppModel.TYPE_WEBFORM,
                  props).getChildRef();
         
         // add title aspect for user defined title and description labels
         props.clear();
         props.put(ContentModel.PROP_TITLE, form.getTitle());
         props.put(ContentModel.PROP_DESCRIPTION, form.getDescription());
         this.nodeService.addAspect(formRef, ContentModel.ASPECT_TITLED, props);
         
         // add filename pattern aspect if a filename pattern has been applied
         if (form.getFilenamePattern() != null)
         {
            props.clear();
            props.put(WCMAppModel.PROP_FILENAMEPATTERN, form.getFilenamePattern());
            this.nodeService.addAspect(formRef, WCMAppModel.ASPECT_FILENAMEPATTERN, props);
         }
         
         // associate to workflow defaults if any are present
         if (form.getWorkflow() != null)
         {
            WorkflowWrapper workflow = form.getWorkflow();
            props.clear();
            props.put(WCMAppModel.PROP_WORKFLOW_NAME, workflow.getName());
            NodeRef workflowRef = this.nodeService.createNode(formRef,
                  WCMAppModel.ASSOC_WORKFLOWDEFAULTS,
                  WCMAppModel.ASSOC_WORKFLOWDEFAULTS,
                  WCMAppModel.TYPE_WORKFLOWDEFAULTS,
                  props).getChildRef();
            
            // persist workflow default params
            if (workflow.getParams() != null)
            {
               serializeWorkflowParams((Serializable)workflow.getParams(), workflowRef);
            }
         }
         
         // associate to a web form template for each template applied to the form
         for (PresentationTemplate template : form.getTemplates())
         {
            props.clear();
            props.put(WCMAppModel.PROP_ENGINE, template.getRenderingEngineTemplate().getNodeRef());
            NodeRef templateRef = this.nodeService.createNode(formRef,
                  WCMAppModel.ASSOC_WEBFORMTEMPLATE,
                  WCMAppModel.ASSOC_WEBFORMTEMPLATE,
                  WCMAppModel.TYPE_WEBFORMTEMPLATE,
                  props).getChildRef();
            
            // add filename pattern aspect if a filename pattern has been applied
            if (template.getFilenamePattern() != null)
            {
               props.clear();
               props.put(WCMAppModel.PROP_FILENAMEPATTERN, template.getFilenamePattern());
               this.nodeService.addAspect(templateRef, WCMAppModel.ASPECT_FILENAMEPATTERN, props);
            }
         }
      }
      
      // finally walk each web project workflow definition and save defaults for each
      for (WorkflowWrapper workflow : this.workflows)
      {
         props.clear();
         props.put(ContentModel.PROP_NAME, workflow.getName());
         NodeRef workflowRef = this.nodeService.createNode(nodeRef,
               WCMAppModel.ASSOC_WEBWORKFLOWDEFAULTS,
               WCMAppModel.ASSOC_WEBWORKFLOWDEFAULTS,
               WCMAppModel.TYPE_WEBWORKFLOWDEFAULTS,
               props).getChildRef();
         
         // persist workflow default params
         if (workflow.getParams() != null)
         {
            serializeWorkflowParams((Serializable)workflow.getParams(), workflowRef);
         }
         
         // add filename pattern aspect if a filename pattern has been applied
         if (workflow.getFilenamePattern() != null)
         {
            props.clear();
            props.put(WCMAppModel.PROP_FILENAMEPATTERN, workflow.getFilenamePattern());
            this.nodeService.addAspect(workflowRef, WCMAppModel.ASPECT_FILENAMEPATTERN, props);
         }
      }
   }

   /**
    * Serialize the workflow params to a content stream
    * 
    * @param params             Serializable workflow params
    * @param workflowRef        The noderef to write the property too
    */
   private void serializeWorkflowParams(Serializable params, NodeRef workflowRef)
   {
      try
      {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(params);
         oos.close();
         // write the serialized Map as a binary content stream - like database blob!
         ContentService cs = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getContentService();
         ContentWriter writer = cs.getWriter(workflowRef, WCMAppModel.PROP_WORKFLOWDEFAULTS, true);
         writer.setMimetype(MimetypeMap.MIMETYPE_BINARY);
         writer.putContent(new ByteArrayInputStream(baos.toByteArray()));
      }
      catch (IOException ioerr)
      {
         throw new AlfrescoRuntimeException("Unable to serialize workflow default parameters: " + ioerr.getMessage());
      }
   }
   
   
   // ------------------------------------------------------------------------------
   // Service setters
   
   /**
    * @param avmService The AVMService to set.
    */
   public void setAvmService(AVMService avmService)
   {
      this.avmService = avmService;
   }
   
   /**
    * @param workflowService  The WorkflowService to set.
    */
   public void setWorkflowService(WorkflowService workflowService)
   {
      this.workflowService = workflowService;
   }

   
   // ------------------------------------------------------------------------------
   // Bean getters and setters
   
   /**
    * @return Returns the name.
    */
   public String getName()
   {
      return name;
   }
   
   /**
    * @param name The name to set.
    */
   public void setName(String name)
   {
      this.name = name;
   }

   /**
    * @return DNS name
    */
   public String getDnsName()
   {
      return this.dnsName;
   }

   /**
    * @param DNS name
    */
   public void setDnsName(String dnsName)
   {
      this.dnsName = dnsName;
   }

   /**
    * @return Returns the title.
    */
   public String getTitle()
   {
      return title;
   }
   
   /**
    * @param title The title to set.
    */
   public void setTitle(String title)
   {
      this.title = title;
   }
   
   /**
    * @return Returns the description.
    */
   public String getDescription()
   {
      return description;
   }
   
   /**
    * @param description The description to set.
    */
   public void setDescription(String description)
   {
      this.description = description;
   }
   
   /**
    * @return the default webapp name for the project
    */
   public String getWebapp()
   {
      return this.webapp;
   }

   /**
    * @param webapp  The default webapp name for the project
    */
   public void setWebapp(String webapp)
   {
      this.webapp = webapp;
   }

   /**
    * @return summary text for the wizard
    */
   public String getSummary()
   {
      FacesContext fc = FacesContext.getCurrentInstance();
      
      // build a summary section to list the invited users and there roles
      StringBuilder buf = new StringBuilder(128);
      List<UserGroupRole> invitedUserRoles =
         (List<UserGroupRole>)getInviteUsersWizard().getUserRolesDataModel().getWrappedData();
      String currentUser = Application.getCurrentUser(fc).getUserName();
      boolean foundCurrentUser = false;
      for (UserGroupRole userRole : invitedUserRoles)
      {
         if (currentUser.equals(userRole.getAuthority()))
         {
            foundCurrentUser = true;
         }
         buf.append(userRole.getLabel());
         buf.append("<br>");
      }
      if (foundCurrentUser == false)
      {
         buf.append(getInviteUsersWizard().buildLabelForUserAuthorityRole(
               currentUser, SandboxFactory.ROLE_CONTENT_MANAGER));
      }
      
      return buildSummary(
            new String[] {Application.getMessage(fc, MSG_USERROLES)},
            new String[] {buf.toString()});
   }
   
   
   // ------------------------------------------------------------------------------
   // Define Web Content Workflows page
   
   /**
    * @return JSF data model for the Form templates
    */
   public DataModel getFormsDataModel()
   {
      if (this.formsDataModel == null)
      {
         this.formsDataModel = new ListDataModel();
      }
      
      this.formsDataModel.setWrappedData(this.forms);
      
      return this.formsDataModel;
   }
   
   /**
    * @return the List of selected and configured Form objects (for summary screen) 
    */
   public List<FormWrapper> getForms()
   {
      return this.forms;
   }

   /**
    * @param formsDataModel   JSF data model for the Form templates
    */
   public void setFormsDataModel(DataModel formsDataModel)
   {
      this.formsDataModel = formsDataModel;
   }
   
   /**
    * @return List of UI items to represent the available Workflows for all websites
    */
   public List<UIListItem> getFormsList()
   {
      Collection<Form> forms = FormsService.getInstance().getForms();
      List<UIListItem> items = new ArrayList<UIListItem>(forms.size());
      for (Form form : forms)
      {
         UIListItem item = new UIListItem();
         item.setValue(form);
         item.setLabel(form.getName());
         item.setDescription(form.getDescription());
         item.setImage(WebResources.IMAGE_WEBFORM_32);
         items.add(item);
      }
      this.formsList = items;
      return items;
   }
   
   /**
    * Action handler called when the Add to List button is pressed for a form template
    */
   public void addForm(ActionEvent event)
   {
      UISelectList selectList = (UISelectList)event.getComponent().findComponent(COMPONENT_FORMLIST);
      int index = selectList.getRowIndex();
      if (index != -1)
      {
         Form form = (Form)this.formsList.get(index).getValue();
         this.forms.add(this.new FormWrapper(form));
      }
   }
   
   /**
    * Remove a template form from the selected list
    */
   public void removeForm(ActionEvent event)
   {
      FormWrapper wrapper = (FormWrapper)this.formsDataModel.getRowData();
      if (wrapper != null)
      {
         this.forms.remove(wrapper);
      }
   }
   
   /**
    * Action method to setup a form for dialog context for the current row
    */
   public void setupFormAction(ActionEvent event)
   {
      setActionForm( (FormWrapper)this.formsDataModel.getRowData() );
   }
   
   /**
    * @return the current action form for dialog context
    */
   public FormWrapper getActionForm()
   {
      return this.actionForm;
   }

   /**
    * @param actionForm    For dialog context
    */
   public void setActionForm(FormWrapper actionForm)
   {
      this.actionForm = actionForm;
      if (actionForm != null)
      {
         setActionWorkflow(actionForm.getWorkflow());
      }
      else
      {
         setActionWorkflow(null);
      }
   }
   
   /**
    * Action method to setup a workflow for dialog context for the current row
    */
   public void setupWorkflowAction(ActionEvent event)
   {
      setActionWorkflow( (WorkflowWrapper)this.workflowsDataModel.getRowData() );
   }
   
   /**
    * @return Returns the action Workflow for dialog context
    */
   public WorkflowWrapper getActionWorkflow()
   {
      return this.actionWorkflow;
   }

   /**
    * @param actionWorkflow   The action Workflow to set for dialog context
    */
   public void setActionWorkflow(WorkflowWrapper actionWorkflow)
   {
      this.actionWorkflow = actionWorkflow;
   }
   
   
   // ------------------------------------------------------------------------------
   // Specify Settings (ah-hoc Workflows) page
   
   /**
    * @return JSF data model for the Workflow templates
    */
   public DataModel getWorkflowsDataModel()
   {
      if (this.workflowsDataModel == null)
      {
         this.workflowsDataModel = new ListDataModel();
      }
      
      this.workflowsDataModel.setWrappedData(this.workflows);
      
      return this.workflowsDataModel;
   }

   /**
    * @param workflowsDataModel   JSF data model for the Workflow templates
    */
   public void setWorkflowsDataModel(DataModel workflowsDataModel)
   {
      this.workflowsDataModel = workflowsDataModel;
   }
   
   /**
    * @return the list of workflows (for the summary screen)
    */
   public List<WorkflowWrapper> getWorkflows()
   {
      return this.workflows;
   }
   
   /**
    * @return List of UI items to represent the available Workflows for all websites
    */
   public List<UIListItem> getWorkflowList()
   {
      // TODO: add list of workflows from config
      // @see org.alfresco.web.wcm.FormDetailsDialog#getWorkflowList()
      List<WorkflowDefinition> workflowDefs =  this.workflowService.getDefinitions();
      List<UIListItem> items = new ArrayList<UIListItem>(workflowDefs.size());
      for (WorkflowDefinition workflowDef : workflowDefs)
      {
         UIListItem item = new UIListItem();
         item.setValue(workflowDef);
         item.setLabel(workflowDef.title);
         item.setDescription(workflowDef.description);
         item.setImage(WebResources.IMAGE_WORKFLOW_32);
         items.add(item);
      }
      this.workflowsList = items;
      return items;
   }
   
   /**
    * Action handler called when the Add to List button is pressed for a workflow
    */
   public void addWorkflow(ActionEvent event)
   {
      UISelectList selectList = (UISelectList)event.getComponent().findComponent(COMPONENT_WORKFLOWLIST);
      int index = selectList.getRowIndex();
      if (index != -1)
      {
         WorkflowDefinition workflow = (WorkflowDefinition)this.workflowsList.get(index).getValue();
         this.workflows.add(new WorkflowWrapper(workflow.getName(), workflow.getTitle()));
      }
   }
   
   /**
    * Remove a workflow from the selected list
    */
   public void removeWorkflow(ActionEvent event)
   {
      WorkflowWrapper wrapper = (WorkflowWrapper)this.workflowsDataModel.getRowData();
      if (wrapper != null)
      {
         this.workflows.remove(wrapper);
      }
   }

   
   // ------------------------------------------------------------------------------
   // Invite users page
   
   /**
    * @return the InviteWebsiteUsersWizard delegate bean
    */
   private InviteWebsiteUsersWizard getInviteUsersWizard()
   {
      return (InviteWebsiteUsersWizard)FacesHelper.getManagedBean(
            FacesContext.getCurrentInstance(), "InviteWebsiteUsersWizard");
   }
   
   
   // ------------------------------------------------------------------------------
   // Helper methods
   
   /**
    * Helper to get the ID of the 'Websites' system folder
    * 
    * @return ID of the 'Websites' system folder
    * 
    * @throws AlfrescoRuntimeException if unable to find the required folder
    */
   private String getWebsitesFolderId()
   {
      if (this.websitesFolderId == null)
      {
         // get the template from the special Content Templates folder
         FacesContext fc = FacesContext.getCurrentInstance();
         String xpath = Application.getRootPath(fc) + "/" + Application.getWebsitesFolderName(fc);
         
         NodeRef rootNodeRef = this.nodeService.getRootNode(Repository.getStoreRef());
         NamespaceService resolver = Repository.getServiceRegistry(fc).getNamespaceService();
         List<NodeRef> results = this.searchService.selectNodes(rootNodeRef, xpath, null, resolver, false);
         if (results.size() == 1)
         {
            this.websitesFolderId = results.get(0).getId();
         }
         else
         {
            throw new AlfrescoRuntimeException("Unable to find 'Websites' system folder at: " + xpath);
         }
      }
      
      return this.websitesFolderId;
   }
   
   
   // ------------------------------------------------------------------------------
   // Inner classes
   
   /**
    * Wrapper class for a configurable template Form instance
    */
   public class FormWrapper
   {
      private Form form;
      private String title;
      private String description;
      private WorkflowWrapper workflow;
      private String filenamePattern;
      private List<PresentationTemplate> templates = null;
      
      FormWrapper(Form form)
      {
         this.form = form;
         this.title = form.getName();
      }
      
      public Form getForm()
      {
         return this.form;
      }
      
      public String getName()
      {
         return this.form.getName();
      }
      
      public String getTitle()
      {
         return this.title;
      }
      
      public void setTitle(String title)
      {
         this.title = title;
      }
      
      public String getDescription()
      {
         return this.description;
      }

      public void setDescription(String description)
      {
         this.description = description;
      }

      /**
       * @return Returns the workflow.
       */
      public WorkflowWrapper getWorkflow()
      {
         WorkflowDefinition wf = this.form.getDefaultWorkflow();
         if (this.workflow == null && wf != null)
         {
            this.workflow = new WorkflowWrapper(wf.name, wf.getTitle());
         }
         return this.workflow;
      }

      /**
       * @param workflow The workflow to set.
       */
      public void setWorkflow(WorkflowWrapper workflow)
      {
         this.workflow = workflow;
      }

      /**
       * @return Returns the filename pattern.
       */
      public String getFilenamePattern()
      {
         if (this.filenamePattern == null)
         {
            this.filenamePattern = this.form.getOutputPathPattern();
         }
         return this.filenamePattern;
      }

      /**
       * @param filenamePattern The filename pattern to set.
       */
      public void setFilenamePattern(String filenamePattern)
      {
         this.filenamePattern = filenamePattern;
      }
      
      /**
       * @return Returns the presentation templates.
       */
      public List<PresentationTemplate> getTemplates()
      {
         if (this.templates == null)
         {
            List<RenderingEngineTemplate> templates = this.form.getRenderingEngineTemplates();
            this.templates = new ArrayList<PresentationTemplate>(templates.size());
            for (RenderingEngineTemplate template : templates)
            {
               this.templates.add(new PresentationTemplate(template));
            }
         }
         return this.templates;
      }

      /**
       * @param templates The presentation templates to set.
       */
      public void setTemplates(List<PresentationTemplate> templates)
      {
         this.templates = templates;
      }

      /**
       * @return Human readable summary of the configuration for this form
       */
      public String getDetails()
      {
         String none = '<' + Application.getMessage(FacesContext.getCurrentInstance(), MSG_NONE) + '>';
         return MessageFormat.format(Application.getMessage(FacesContext.getCurrentInstance(), MSG_FORM_SUMMARY),
               getWorkflow() != null ? this.workflow.title : none,
               getFilenamePattern() != null ? this.filenamePattern : none,
               getTemplates() != null ? this.templates.size() : 0);
      }
   }
   
   /**
    * Class to represent a single configured Presentation Template instance
    */
   public static class PresentationTemplate
   {
      private RenderingEngineTemplate ret;
      private String title;
      private String description;
      private String filenamePattern;
      
      public PresentationTemplate(RenderingEngineTemplate ret)
      {
         this(ret, null);
      }
      
      public PresentationTemplate(RenderingEngineTemplate ret, String filenamePattern)
      {
         this.ret = ret;
         this.filenamePattern = filenamePattern;
      }
      
      public RenderingEngineTemplate getRenderingEngineTemplate()
      {
         return this.ret;
      }
      
      public String getTitle()
      {
         if (this.title == null)
         {
            this.title = ret.getName();
         }
         return this.title;
      }
      
      public String getDescription()
      {
         if (this.description == null)
         {
            this.description = ret.getDescription();
         }
         return this.description;
      }
      
      /**
       * @return Returns the filename pattern.
       */
      public String getFilenamePattern()
      {
         if (this.filenamePattern == null)
         {
            this.filenamePattern = ret.getOutputPathPattern();
         }
         return this.filenamePattern;
      }

      /**
       * @param filenamePattern The filename pattern to set.
       */
      public void setFilenamePattern(String filenamePattern)
      {
         this.filenamePattern = filenamePattern;
      }
   }
   
   /**
    * Class to represent a single configured Workflow instance
    */
   public static class WorkflowWrapper
   {
      private String name;
      private String title;
      private String filenamePattern;
      private QName type;
      private Map<QName, Serializable> params;
      
      public WorkflowWrapper(String name, String title)
      {
         this.name = name;
         this.title = title;
         this.filenamePattern = filenamePattern;
      }
      
      /**
       * @return Returns the name key of the workflow.
       */
      public String getName()
      {
         return this.name;
      }

      /**
       * @return the display label of the workflow.
       */
      public String getTitle()
      {
         return this.title;
      }

      /**
       * @return Returns the filename pattern.
       */
      public String getFilenamePattern()
      {
         return this.filenamePattern;
      }

      /**
       * @param filenamePattern The filename pattern to set.
       */
      public void setFilenamePattern(String filenamePattern)
      {
         this.filenamePattern = filenamePattern;
      }

      /**
       * @return Returns the workflow params.
       */
      public Map<QName, Serializable> getParams()
      {
         return this.params;
      }

      /**
       * @param params The workflow params to set.
       */
      public void setParams(Map<QName, Serializable> params)
      {
         this.params = params;
      }

      /**
       * @return Returns the type.
       */
      public QName getType()
      {
         return this.type;
      }

      /**
       * @param type The type to set.
       */
      public void setType(QName type)
      {
         this.type = type;
      }
   }
}
