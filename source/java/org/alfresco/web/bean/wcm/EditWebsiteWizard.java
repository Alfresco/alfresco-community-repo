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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.forms.Form;
import org.alfresco.web.forms.FormsService;
import org.alfresco.web.forms.RenderingEngineTemplate;

/**
 * Backing bean for the Edit Web Project wizard.
 * 
 * @author Kevin Roast
 */
public class EditWebsiteWizard extends CreateWebsiteWizard
{
   // ------------------------------------------------------------------------------
   // Wizard implementation
   
   /**
    * Initialises the wizard
    */
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      // the editMode flag is used to disabled some wizard fields
      this.editMode = true;
      
      NodeRef websiteRef = this.browseBean.getActionSpace().getNodeRef();
      if (websiteRef == null)
      {
         throw new IllegalArgumentException("Edit Web Project wizard requires action node context.");
      }
      loadWebProjectModel(websiteRef);
   }
   
   /**
    * Restore the forms, templates and workflows from the model for this web project
    * 
    * @param nodeRef        NodeRef to the web project
    */
   private void loadWebProjectModel(NodeRef nodeRef)
   {
      // simple properties
      this.name = (String)this.nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
      this.title = (String)this.nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE);
      this.description = (String)this.nodeService.getProperty(nodeRef, ContentModel.PROP_DESCRIPTION);
      this.dnsName = (String)this.nodeService.getProperty(nodeRef, WCMAppModel.PROP_AVMSTORE);
      this.webapp = (String)this.nodeService.getProperty(nodeRef, WCMAppModel.PROP_DEFAULTWEBAPP);
      
      // load the form templates
      List<ChildAssociationRef> webFormRefs = this.nodeService.getChildAssocs(
            nodeRef, WCMAppModel.ASSOC_WEBFORM, RegexQNamePattern.MATCH_ALL);
      for (ChildAssociationRef ref : webFormRefs)
      {
         NodeRef formRef = ref.getChildRef();
         
         String name = (String)this.nodeService.getProperty(formRef, WCMAppModel.PROP_FORMNAME);
         Form formImpl = FormsService.getInstance().getForm(name);
         if (formImpl != null)
         {
            FormWrapper form = new FormWrapper(formImpl);
            form.setTitle((String)this.nodeService.getProperty(formRef, ContentModel.PROP_TITLE));
            form.setDescription((String)this.nodeService.getProperty(formRef, ContentModel.PROP_DESCRIPTION));
            form.setFilenamePattern((String)this.nodeService.getProperty(formRef, WCMAppModel.PROP_FILENAMEPATTERN));
            
            // the single workflow attached to the form 
            List<ChildAssociationRef> workflowRefs = this.nodeService.getChildAssocs(
               formRef, WCMAppModel.ASSOC_WORKFLOWDEFAULTS, RegexQNamePattern.MATCH_ALL);
            if (workflowRefs.size() == 1)
            {
               NodeRef wfRef = workflowRefs.get(0).getChildRef();
               String wfName = (String)this.nodeService.getProperty(wfRef, WCMAppModel.PROP_WORKFLOW_NAME);
               WorkflowDefinition wfDef = this.workflowService.getDefinitionByName(wfName);
               if (wfDef != null)
               {
                  WorkflowWrapper wfWrapper = new WorkflowWrapper(wfName, wfDef.getTitle(), wfDef.getDescription());
                  wfWrapper.setParams((Map<QName, Serializable>)AVMWorkflowUtil.deserializeWorkflowParams(wfRef));
                  if (wfDef.startTaskDefinition != null)
                  {
                     wfWrapper.setType(wfDef.startTaskDefinition.metadata.getName());
                  }
                  form.setWorkflow(wfWrapper);
               }
            }
            
            // the templates attached to the form
            List<RenderingEngineTemplate> engineTemplates = formImpl.getRenderingEngineTemplates();
            List<ChildAssociationRef> templateRefs = this.nodeService.getChildAssocs(
               formRef, WCMAppModel.ASSOC_WEBFORMTEMPLATE, RegexQNamePattern.MATCH_ALL);
            for (ChildAssociationRef tChildRef : templateRefs)
            {
               NodeRef templateRef = tChildRef.getChildRef();
               NodeRef engineRef = (NodeRef)this.nodeService.getProperty(templateRef, WCMAppModel.PROP_ENGINE);
               for (RenderingEngineTemplate ret : engineTemplates)
               {
                  if (engineRef.equals(ret.getNodeRef()))
                  {
                     String filenamePattern = (String)this.nodeService.getProperty(templateRef,
                           WCMAppModel.PROP_FILENAMEPATTERN);
                     PresentationTemplate template = new PresentationTemplate(ret, filenamePattern);
                     form.addTemplate(template);
                     break;
                  }
               }
            }
            
            this.forms.add(form);
         }
      }
      
      // load the workflows associated with the website
      List<ChildAssociationRef> workflowRefs = this.nodeService.getChildAssocs(
            nodeRef, WCMAppModel.ASSOC_WEBWORKFLOWDEFAULTS, RegexQNamePattern.MATCH_ALL);
      for (ChildAssociationRef wChildRef : workflowRefs)
      {
         NodeRef wfRef = wChildRef.getChildRef();
         String wfName = (String)this.nodeService.getProperty(wfRef, WCMAppModel.PROP_WORKFLOW_NAME);
         WorkflowDefinition wfDef = this.workflowService.getDefinitionByName(wfName);
         if (wfDef != null)
         {
            WorkflowWrapper wfWrapper = new WorkflowWrapper(wfName, wfDef.getTitle(), wfDef.getDescription());
            wfWrapper.setParams((Map<QName, Serializable>)AVMWorkflowUtil.deserializeWorkflowParams(wfRef));
            wfWrapper.setFilenamePattern((String)this.nodeService.getProperty(
                  wfRef, WCMAppModel.PROP_FILENAMEPATTERN));
            if (wfDef.startTaskDefinition != null)
            {
               wfWrapper.setType(wfDef.startTaskDefinition.metadata.getName());
            }
            this.workflows.add(wfWrapper);
         }
      }
   }
   
   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#finishImpl(javax.faces.context.FacesContext, java.lang.String)
    */
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      NodeRef nodeRef = this.browseBean.getActionSpace().getNodeRef();
      
      // apply the name, title and description props
      this.nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, this.name);
      this.nodeService.setProperty(nodeRef, ContentModel.PROP_TITLE, this.title);
      this.nodeService.setProperty(nodeRef, ContentModel.PROP_DESCRIPTION, this.description);
      
      // clear the existing settings for forms, template and workflows - then the existing methods
      // can be used to apply the modified and previous settings from scratch
      clearWebProjectModel(nodeRef);
      
      // TODO: add the ability to change/rename the root webapp and DNS name for the website
      
      // persist the forms, templates, workflows and workflow defaults to the model for this web project
      saveWebProjectModel(nodeRef);
      
      return AlfrescoNavigationHandler.CLOSE_WIZARD_OUTCOME;
   }
   
   /**
    * Cascade delete the existing Form and Workflow defs attached to the specified Web Project node
    * 
    * @param nodeRef    Web project node
    */
   private void clearWebProjectModel(NodeRef nodeRef)
   {
      List<ChildAssociationRef> webFormRefs = nodeService.getChildAssocs(
               nodeRef, WCMAppModel.ASSOC_WEBFORM, RegexQNamePattern.MATCH_ALL);
      for (ChildAssociationRef ref : webFormRefs)
      {
         // cascade delete will take case of child-child relationships
         this.nodeService.removeChild(nodeRef, ref.getChildRef());
      }
      
      List<ChildAssociationRef> wfRefs = nodeService.getChildAssocs(
               nodeRef, WCMAppModel.ASSOC_WEBWORKFLOWDEFAULTS, RegexQNamePattern.MATCH_ALL);
      for (ChildAssociationRef ref : wfRefs)
      {
         this.nodeService.removeChild(nodeRef, ref.getChildRef());
      }
   }
}
