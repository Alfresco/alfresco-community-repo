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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
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
      uiFacetsProps.put(ContentModel.PROP_ICON, AVMConstants.SPACE_ICON_WEBSITE);
      uiFacetsProps.put(ContentModel.PROP_TITLE, this.title);
      uiFacetsProps.put(ContentModel.PROP_DESCRIPTION, this.description);
      this.nodeService.addAspect(nodeRef, ContentModel.ASPECT_UIFACETS, uiFacetsProps);
      
      // TODO: invite users with appropriate permissions into this folder
      
      // create the AVM stores (layers) to represent the newly created location website
      createStagingSandbox(this.name);
      
      // create a sandbox for each user - TODO: based on role?
      List<String> invitedUsers = getInvitedUsernames();
      invitedUsers.add(Application.getCurrentUser(context).getUserName());
      for (String username : invitedUsers)
      {
         createUserSandbox(this.name, username);
      }
      
      // save the list of invited users against the store
      this.nodeService.setProperty(nodeRef, ContentModel.PROP_USERSANDBOXES, (Serializable)invitedUsers);
      
      // set the property on the node to reference the AVM store
      this.nodeService.setProperty(nodeRef, ContentModel.PROP_AVMSTORE, this.name);
      
      // navigate to the Websites folder so we can see the newly created folder
      this.navigator.setCurrentNodeId(websiteParentId);
      
      return "browse";
   }
   
   /**
    * @param avmService The AVMService to set.
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
   
   /**
    * Create the staging sandbox for the named store.
    * 
    * A staging sandbox is comprised of two stores, the first named 'storename-staging' with a
    * preview store named 'storename-preview' layered over the staging store.
    * 
    * Various store meta-data properties are set including:
    * Identifier for store-types: .sandbox.staging.main and .sandbox.staging.preview
    * Store-id: .sandbox-id.<guid> (unique across all stores in the sandbox)
    * DNS: .dns.<store> = <path-to-webapps-root>
    * 
    * @param name       The store name to create the sandbox for
    */
   private void createStagingSandbox(String name)
   {
      // create the 'staging' store for the website
      String stagingStore = AVMConstants.buildAVMStagingStoreName(name);
      this.avmService.createAVMStore(stagingStore);
      if (logger.isDebugEnabled())
         logger.debug("Created staging sandbox store: " + stagingStore);
      
      // create the system directories 'appBase' and 'avm_webapps'
      String path = stagingStore + ":/";
      //this.avmService.createDirectory(path, AVMConstants.DIR_APPBASE);
      this.fileFolderService.create(AVMNodeConverter.ToNodeRef(-1, path), AVMConstants.DIR_APPBASE, ContentModel.TYPE_AVM_PLAIN_FOLDER);
      path += AVMConstants.DIR_APPBASE;
      //this.avmService.createDirectory(path, AVMConstants.DIR_WEBAPPS);
      this.fileFolderService.create(AVMNodeConverter.ToNodeRef(-1, path), AVMConstants.DIR_WEBAPPS, ContentModel.TYPE_AVM_PLAIN_FOLDER);
      
      // tag the store with the store type
      this.avmService.setStoreProperty(stagingStore,
            QName.createQName(null, AVMConstants.PROP_SANDBOX_STAGING_MAIN),
            new PropertyValue(DataTypeDefinition.TEXT, null));
      
      // tag the store with the DNS name property
      tagStoreDNSPath(stagingStore);
      
      
      // create the 'preview' store for the website
      String previewStore = AVMConstants.buildAVMStagingPreviewStoreName(name);
      this.avmService.createAVMStore(previewStore);
      if (logger.isDebugEnabled())
         logger.debug("Created staging sandbox store: " + previewStore);
      
      // create a layered directory pointing to 'appBase' in the staging area
      path = previewStore + ":/";
      String targetPath = name + AVMConstants.STORE_STAGING + ":/" + AVMConstants.DIR_APPBASE;
      this.avmService.createLayeredDirectory(targetPath, path, AVMConstants.DIR_APPBASE);
      //this.fileFolderService.create(AVMNodeConverter.ToNodeRef(-1, path), AVMConstants.DIR_APPBASE, ContentModel.TYPE_AVM_PLAIN_FOLDER);
      
      // tag the store with the store type
      this.avmService.setStoreProperty(previewStore,
            QName.createQName(null, AVMConstants.PROP_SANDBOX_STAGING_PREVIEW),
            new PropertyValue(DataTypeDefinition.TEXT, null));
      
      // tag the store with the DNS name property
      tagStoreDNSPath(previewStore);
      
      
      // tag all related stores to indicate that they are part of a single sandbox
      String sandboxIdProp = AVMConstants.PROP_SANDBOXID + GUID.generate();
      this.avmService.setStoreProperty(stagingStore,
            QName.createQName(null, sandboxIdProp),
            new PropertyValue(DataTypeDefinition.TEXT, null));
      this.avmService.setStoreProperty(previewStore,
            QName.createQName(null, sandboxIdProp),
            new PropertyValue(DataTypeDefinition.TEXT, null));
   }
   
   /**
    * Create a user sandbox for the named store.
    * 
    * A user sandbox is comprised of two stores, the first named 'storename-username-main' layered
    * over the staging store with a preview store named 'storename-username-preview' layered over
    * the main store.
    * 
    * Various store meta-data properties are set including:
    * Identifier for store-types: .sandbox.author.main and .sandbox.author.preview
    * Store-id: .sandbox-id.<guid> (unique across all stores in the sandbox)
    * DNS: .dns.<store> = <path-to-webapps-root>
    * 
    * @param name       The store name to create the sandbox for
    * @param username   Username of the user to create the sandbox for
    */
   private void createUserSandbox(String name, String username)
   {
      // create the user 'main' store
      String userStore = AVMConstants.buildAVMUserMainStoreName(name, username);
      this.avmService.createAVMStore(userStore);
      if (logger.isDebugEnabled())
         logger.debug("Created staging sandbox store: " + userStore);
      
      // create a layered directory pointing to 'appBase' in the staging area
      String path = userStore + ":/";
      String targetPath = name + AVMConstants.STORE_STAGING + ":/" + AVMConstants.DIR_APPBASE;
      this.avmService.createLayeredDirectory(targetPath, path, AVMConstants.DIR_APPBASE);
      
      // tag the store with the store type
      this.avmService.setStoreProperty(userStore,
            QName.createQName(null, AVMConstants.PROP_SANDBOX_AUTHOR_MAIN),
            new PropertyValue(DataTypeDefinition.TEXT, null));

      // tag the store with the base name of the website so that corresponding
      // staging areas can be found.
      this.avmService.setStoreProperty(userStore,
            QName.createQName(null, AVMConstants.PROP_WEBSITE_NAME),
            new PropertyValue(DataTypeDefinition.TEXT, name));
      
      // tag the store with the DNS name property
      tagStoreDNSPath(userStore);
      
      
      // create the user 'preview' store
      String previewStore = AVMConstants.buildAVMUserPreviewStoreName(name, username);
      this.avmService.createAVMStore(previewStore);
      if (logger.isDebugEnabled())
         logger.debug("Created staging sandbox store: " + previewStore);
      
      // create a layered directory pointing to 'appBase' in the user 'main' store
      path = previewStore + ":/";
      targetPath = userStore + ":/" + AVMConstants.DIR_APPBASE;
      this.avmService.createLayeredDirectory(targetPath, path, AVMConstants.DIR_APPBASE);
      
      // tag the store with the store type
      this.avmService.setStoreProperty(previewStore,
            QName.createQName(null, AVMConstants.PROP_SANDBOX_AUTHOR_PREVIEW),
            new PropertyValue(DataTypeDefinition.TEXT, null));
      
      // tag the store with the DNS name property
      tagStoreDNSPath(previewStore);
      
      
      // tag all related stores to indicate that they are part of a single sandbox
      String sandboxIdProp = AVMConstants.PROP_SANDBOXID + GUID.generate();
      this.avmService.setStoreProperty(userStore, QName.createQName(null, sandboxIdProp),
            new PropertyValue(DataTypeDefinition.TEXT, null));
      this.avmService.setStoreProperty(previewStore, QName.createQName(null, sandboxIdProp),
            new PropertyValue(DataTypeDefinition.TEXT, null));
   }
   
   /**
    * Tag a named store with a DNS path meta-data attribute.
    * The DNS meta-data attribute is set to the system path 'store:/appBase/avm_webapps'
    * 
    * @param store  Name of the store to tag
    */
   private void tagStoreDNSPath(String store)
   {
      String path = store + ":/" + AVMConstants.DIR_APPBASE + '/' + AVMConstants.DIR_WEBAPPS;
      // TODO: DNS name mangle the property name - can only contain value DNS characters!
      String dnsProp = AVMConstants.PROP_DNS + store;
      this.avmService.setStoreProperty(store, QName.createQName(null, dnsProp),
            new PropertyValue(DataTypeDefinition.TEXT, path));
   }
   
   /**
    * @return The list of invited usernames
    */
   private List<String> getInvitedUsernames()
   {
      // TODO: add the list of invited users here
      return new ArrayList<String>(1);
   }
}
