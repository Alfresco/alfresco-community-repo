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

import java.io.IOException;
import java.io.Serializable;
import java.io.Serializable;
import java.util.*;
import java.util.List;
import javax.faces.context.FacesContext;
import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.web.app.servlet.DownloadContentServlet;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.bean.wcm.AVMConstants;
import org.alfresco.web.forms.*;
import org.alfresco.web.forms.xforms.XFormsProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.*;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Provides configured data for a web project.
 *
 * @author Ariel Backenroth
 */
public class WebProject
   implements Serializable
{

   /////////////////////////////////////////////////////////////////////////////

   /**
    * Wraps a form object to provide overridden values at the web project level
    */
   private class FormWrapper 
      extends FormImpl
   {
      private final NodeRef formNodeRef;
      private Form baseForm;
      private NodeRef defaultWorkflowNodeRef;

      private FormWrapper(final Form form, final NodeRef formNodeRef)
      {
         super(((FormImpl)form).getNodeRef());
         this.formNodeRef = formNodeRef;
      }

      @Override
      public String getTitle()
      {
         final NodeService nodeService = this.getServiceRegistry().getNodeService();
         return (String)nodeService.getProperty(this.formNodeRef,
                                                ContentModel.PROP_TITLE);
      }

      @Override
      public String getDescription()
      {
         final NodeService nodeService = this.getServiceRegistry().getNodeService();
         return (String)nodeService.getProperty(this.formNodeRef,
                                                ContentModel.PROP_DESCRIPTION);
      }

      @Override
      public String getOutputPathPattern()
      {
         final NodeService nodeService = this.getServiceRegistry().getNodeService();
         final String result = (String)
            nodeService.getProperty(this.formNodeRef,
                                    WCMAppModel.PROP_OUTPUT_PATH_PATTERN);
         return (result != null ? result : this.baseForm.getOutputPathPattern());
      }

      @Override
      protected NodeRef getDefaultWorkflowNodeRef()
      {
         if (this.defaultWorkflowNodeRef == null)
         {
            final NodeService nodeService = this.getServiceRegistry().getNodeService();
            final List<ChildAssociationRef> workflowRefs = 
               nodeService.getChildAssocs(this.formNodeRef,
                                          WCMAppModel.ASSOC_WORKFLOWDEFAULTS,
                                          RegexQNamePattern.MATCH_ALL);
            if (workflowRefs.size() == 0)
            {
               return null;
            }
               
            this.defaultWorkflowNodeRef = workflowRefs.get(0).getChildRef();
         }
         return this.defaultWorkflowNodeRef;
      }

      @Override
      protected Map<String, RenderingEngineTemplate> loadRenderingEngineTemplates()
      {
         final Map<String, RenderingEngineTemplate> allRets = super.loadRenderingEngineTemplates();

         final NodeService nodeService = this.getServiceRegistry().getNodeService();
         final List<ChildAssociationRef> retNodeRefs = 
            nodeService.getChildAssocs(this.formNodeRef,
                                       WCMAppModel.ASSOC_WEBFORMTEMPLATE,
                                       RegexQNamePattern.MATCH_ALL);
         final Map<String, RenderingEngineTemplate> result = 
            new HashMap<String, RenderingEngineTemplate>(retNodeRefs.size(), 1.0f);
         for (ChildAssociationRef car : retNodeRefs)
         {
            final String renderingEngineTemplateName = (String)
               nodeService.getProperty(car.getChildRef(), 
                                       WCMAppModel.PROP_BASE_RENDERING_ENGINE_TEMPLATE_NAME);
            final String outputPathPattern = (String)
               nodeService.getProperty(car.getChildRef(), WCMAppModel.PROP_OUTPUT_PATH_PATTERN);
            final RenderingEngineTemplateImpl ret = (RenderingEngineTemplateImpl)
               allRets.get(renderingEngineTemplateName);
            result.put(ret.getName(), 
                       new RenderingEngineTemplateImpl(ret.getNodeRef(),
                                                       ret.getRenditionPropertiesNodeRef())
                       {
                          @Override
                          public String getOutputPathPattern()
                          {
                             return outputPathPattern;
                          }
                       });

         }
         return result;
      }
   }

   /////////////////////////////////////////////////////////////////////////////

   private final NodeRef nodeRef;

   public WebProject(final NodeRef nodeRef)
   {
      this.nodeRef = nodeRef;
   }

   public WebProject(final String avmPath)
   {
      String stagingStore = AVMConstants.buildStagingStoreName(AVMConstants.getStoreId(AVMConstants.getStoreName(avmPath)));
      final AVMService avmService = this.getServiceRegistry().getAVMService();
      this.nodeRef = (NodeRef)
         avmService.getStoreProperty(stagingStore, 
                                     AVMConstants.PROP_WEB_PROJECT_NODE_REF).getValue(DataTypeDefinition.NODE_REF);
   }

   /**
    * Returns the name of the web project.
    *
    * @return the name of the web project.
    */
   public String getName()
   {
      final ServiceRegistry serviceRegistry = this.getServiceRegistry();
      final NodeService nodeService = serviceRegistry.getNodeService();
      return (String)nodeService.getProperty(this.nodeRef, ContentModel.PROP_NAME);
   }

   /**
    * Returns the store id for this web project.
    *
    * @return the store id for this web project.
    */
   public String getStoreId()
   {
      final ServiceRegistry serviceRegistry = this.getServiceRegistry();
      final NodeService nodeService = serviceRegistry.getNodeService();
      return (String)nodeService.getProperty(this.nodeRef, WCMAppModel.PROP_AVMSTORE);
   }
   
   /**
    * Returns the staging store name.
    *
    * @return the staging store name.
    */
   public String getStagingStore()
   {
      return AVMConstants.buildStagingStoreName(this.getStoreId());
   }

   /**
    * Returns the forms configured for this web project.
    *
    * @return the forms configured for this web project.
    */
   public List<Form> getForms()
   {
      return Collections.unmodifiableList(new ArrayList(this.getFormsImpl().values()));
   }

   /**
    * Returns the form with the given name or <tt>null</tt> if not found.
    *
    * @param name the name of the form
    * @return the form or <tt>null</tt> if not found.
    * @exception NullPointerException if the name is <tt>null</tt>.
    */
   public Form getForm(final String name)
   {
      if (name == null)
      {
         throw new NullPointerException();
      }
      return this.getFormsImpl().get(name);
   }

   /**
    * Returns <tt>true</tt> if the user is a manager of this web project.
    *
    * @param user the user
    * @return <tt>true</tt> if the user is a manager, <tt>false</tt> otherwise.
    * @exception NullPointerException if the user is null.
    */
   public boolean isManager(final User user)
   {
      if (user.isAdmin())
      {
         return true;
      }

      final ServiceRegistry serviceRegistry = this.getServiceRegistry();
      final NodeService nodeService = serviceRegistry.getNodeService();
      final String currentUser = user.getUserName();
      final List<ChildAssociationRef> userInfoRefs = 
         nodeService.getChildAssocs(this.nodeRef, 
                                    WCMAppModel.ASSOC_WEBUSER, 
                                    RegexQNamePattern.MATCH_ALL);
      for (ChildAssociationRef ref : userInfoRefs)
      {
         final NodeRef userInfoRef = ref.getChildRef();
         final String username = (String)nodeService.getProperty(userInfoRef, WCMAppModel.PROP_WEBUSERNAME);
         final String userrole = (String)nodeService.getProperty(userInfoRef, WCMAppModel.PROP_WEBUSERROLE);
         if (currentUser.equals(username) && AVMConstants.ROLE_CONTENT_MANAGER.equals(userrole))
         {
            return true;
         }
      }
      return false;
   }

   /**
    * Returns the default webapp for this web project.
    *
    * @return the default webapp for this web project.
    */
   public String getDefaultWebapp()
   {
      final ServiceRegistry serviceRegistry = this.getServiceRegistry();
      final NodeService nodeService = serviceRegistry.getNodeService();
      return (String)
         nodeService.getProperty(this.nodeRef, WCMAppModel.PROP_DEFAULTWEBAPP);
   }

   private Map<String, Form> getFormsImpl()
   {
      final ServiceRegistry serviceRegistry = this.getServiceRegistry();
      final NodeService nodeService = serviceRegistry.getNodeService();
      final List<ChildAssociationRef> formRefs = 
         nodeService.getChildAssocs(this.nodeRef,
                                    WCMAppModel.ASSOC_WEBFORM,
                                    RegexQNamePattern.MATCH_ALL);
      Map<String, Form> result = new HashMap<String, Form>(formRefs.size(), 1.0f);
      for (final ChildAssociationRef ref : formRefs)
      {
         final String formName = (String)
            nodeService.getProperty(ref.getChildRef(), WCMAppModel.PROP_FORMNAME);
         final Form baseForm = FormsService.getInstance().getForm(formName);
         result.put(formName, new FormWrapper(baseForm, ref.getChildRef()));
      }
      return result;
   }

   private ServiceRegistry getServiceRegistry()
   {
      final FacesContext fc = FacesContext.getCurrentInstance();
      return Repository.getServiceRegistry(fc);
   }
}