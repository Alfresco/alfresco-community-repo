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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.spaces.CreateSpaceWizard;
import org.alfresco.web.bean.wizard.BaseWizardBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Kevin Roast
 */
public class CreateWebsiteWizard extends BaseWizardBean
{
   private static Log logger = LogFactory.getLog(CreateWebsiteWizard.class);
   
   protected String name;
   protected String title;
   protected String description;
   
   private String websitesFolderId = null;
   
   protected AVMService avmService;
   
   
   // ------------------------------------------------------------------------------
   // Wizard implementation
   
   /**
    * Initialises the wizard
    */
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      this.name = null;
      this.title = null;
      this.description = null;
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
            ContentModel.TYPE_AVMWEBFOLDER);
      NodeRef nodeRef = fileInfo.getNodeRef();
      
      if (logger.isDebugEnabled())
         logger.debug("Created website folder node with name: " + this.name);
      
      // apply the uifacets aspect - icon, title and description props
      Map<QName, Serializable> uiFacetsProps = new HashMap<QName, Serializable>(4);
      uiFacetsProps.put(ContentModel.PROP_ICON, CreateSpaceWizard.DEFAULT_SPACE_ICON_NAME);
      uiFacetsProps.put(ContentModel.PROP_TITLE, this.title);
      uiFacetsProps.put(ContentModel.PROP_DESCRIPTION, this.description);
      this.nodeService.addAspect(nodeRef, ContentModel.ASPECT_UIFACETS, uiFacetsProps);
      
      // TODO: create layers for invited users
      // TODO: invite users with appropriate permissions into this folder
      
      // create the AVM store to represent the newly created location website
      this.avmService.createAVMStore(this.name);
      AVMStoreDescriptor avmStore = this.avmService.getAVMStore(this.name);
      this.avmService.createDirectory("/", "appBase");
      this.avmService.createDirectory("/appBase", "avm_webapps");
      this.avmService.createDirectory("/appBase/avm_webapps", this.name);
      
      
      
      // set the property on the node to reference the AVM store
      this.nodeService.setProperty(nodeRef, ContentModel.PROP_AVMSTORE, this.name);
      
      // navigate to the Websites folder so we can see the newly created folder
      this.navigator.setCurrentNodeId(websiteParentId);
      
      return "browse";
   }
   
   /**
    * @param avmService The avmService to set.
    */
   public void setAvmService(AVMService avmService)
   {
      this.avmService = avmService;
   }

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
    * @return summary text for the wizard
    */
   public String getSummary()
   {
      ResourceBundle bundle = Application.getBundle(FacesContext.getCurrentInstance());
      
      return buildSummary(
            new String[] {bundle.getString("name"), 
                          bundle.getString("description")},
            new String[] {this.name, this.description});
   }
   
   
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
   
   private void createStagingStore(String name)
   {
      String stagingStore = name + AVMConstants.STORE_STAGING;
      this.avmService.createAVMStore(stagingStore);
      String path = stagingStore + ":/";
      this.avmService.createDirectory(path, AVMConstants.DIR_APPBASE);
      path += '/' + AVMConstants.DIR_APPBASE;
      this.avmService.createDirectory(path, AVMConstants.DIR_WEBAPPS);
   }
   
   private void createSandboxStores(String name, String username)
   {
      String sandboxStore = name + '-' + username + AVMConstants.STORE_MAIN;
      this.avmService.createAVMStore(sandboxStore);
      String path = sandboxStore + ":/";
      this.avmService.createLayeredDirectory(name + AVMConstants.STORE_STAGING, path, AVMConstants.DIR_APPBASE);
      
      String previewStore = name + AVMConstants.STORE_PREVIEW;
      this.avmService.createAVMStore(previewStore);
      path = previewStore + ":/";
      this.avmService.createLayeredDirectory(name + AVMConstants.STORE_STAGING, path, AVMConstants.DIR_APPBASE);
   }
}
