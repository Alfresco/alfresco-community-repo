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

import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.alfresco.web.bean.repository.Repository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Helper factory to create AVM sandbox structures.
 * 
 * @author Kevin Roast
 */
public final class SandboxFactory
{
   private static Log logger = LogFactory.getLog(SandboxFactory.class);
   
   public static final String ROLE_CONTENT_MANAGER = "ContentManager";
   
   /**
    * Private constructor
    */
   private SandboxFactory()
   {
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
    * Website Name: .website.name = website name
    * 
    * @param name       The store name to create the sandbox for
    * @param managers   The list of authorities who have ContentManager role in the website 
    */
   public static void createStagingSandbox(String name, List<String> managers)
   {
      ServiceRegistry services = Repository.getServiceRegistry(FacesContext.getCurrentInstance());
      AVMService avmService = services.getAVMService();
      PermissionService permissionService = services.getPermissionService();
      
      // create the 'staging' store for the website
      String stagingStore = AVMConstants.buildAVMStagingStoreName(name);
      avmService.createStore(stagingStore);
      if (logger.isDebugEnabled())
         logger.debug("Created staging sandbox store: " + stagingStore);
      
      // create the system directories 'appBase' and 'avm_webapps'
      String path = stagingStore + ":/";
      //this.fileFolderService.create(AVMNodeConverter.ToNodeRef(-1, path), AVMConstants.DIR_APPBASE, ContentModel.TYPE_AVM_PLAIN_FOLDER);
      avmService.createDirectory(path, AVMConstants.DIR_APPBASE);
      NodeRef dirRef = AVMNodeConverter.ToNodeRef(-1, path + '/' + AVMConstants.DIR_APPBASE);
      for (String manager : managers)
      {
         permissionService.setPermission(dirRef, manager, ROLE_CONTENT_MANAGER, true);
      }
      path += AVMConstants.DIR_APPBASE;
      //this.fileFolderService.create(AVMNodeConverter.ToNodeRef(-1, path), AVMConstants.DIR_WEBAPPS, ContentModel.TYPE_AVM_PLAIN_FOLDER);
      avmService.createDirectory(path, AVMConstants.DIR_WEBAPPS);
      
      // tag the store with the store type
      avmService.setStoreProperty(stagingStore,
            QName.createQName(null, AVMConstants.PROP_SANDBOX_STAGING_MAIN),
            new PropertyValue(DataTypeDefinition.TEXT, null));
      
      // tag the store with the DNS name property
      tagStoreDNSPath(avmService, stagingStore, name, "staging");
      
      // snapshot the store
      avmService.createSnapshot(stagingStore, null, null);
      
      
      // create the 'preview' store for the website
      String previewStore = AVMConstants.buildAVMStagingPreviewStoreName(name);
      avmService.createStore(previewStore);
      if (logger.isDebugEnabled())
         logger.debug("Created staging sandbox store: " + previewStore);
      
      // create a layered directory pointing to 'appBase' in the staging area
      path = previewStore + ":/";
      String targetPath = name + AVMConstants.STORE_STAGING + ":/" + AVMConstants.DIR_APPBASE;
      //this.fileFolderService.create(AVMNodeConverter.ToNodeRef(-1, path), AVMConstants.DIR_APPBASE, ContentModel.TYPE_AVM_PLAIN_FOLDER);
      avmService.createLayeredDirectory(targetPath, path, AVMConstants.DIR_APPBASE);
      dirRef = AVMNodeConverter.ToNodeRef(-1, path + '/' + AVMConstants.DIR_APPBASE);
      for (String manager : managers)
      {
         permissionService.setPermission(dirRef, manager, ROLE_CONTENT_MANAGER, true);
      }
      
      // tag the store with the store type
      avmService.setStoreProperty(previewStore,
            QName.createQName(null, AVMConstants.PROP_SANDBOX_STAGING_PREVIEW),
            new PropertyValue(DataTypeDefinition.TEXT, null));
      
      // tag the store with the DNS name property
      tagStoreDNSPath(avmService, previewStore, name, "preview");
      
      // snapshot the store
      avmService.createSnapshot(previewStore, null, null);
      
      
      // tag all related stores to indicate that they are part of a single sandbox
      String sandboxIdProp = AVMConstants.PROP_SANDBOXID + GUID.generate();
      avmService.setStoreProperty(stagingStore,
            QName.createQName(null, sandboxIdProp),
            new PropertyValue(DataTypeDefinition.TEXT, null));
      avmService.setStoreProperty(previewStore,
            QName.createQName(null, sandboxIdProp),
            new PropertyValue(DataTypeDefinition.TEXT, null));
      
      if (logger.isDebugEnabled())
      {
         dumpStoreProperties(avmService, stagingStore);
         dumpStoreProperties(avmService, previewStore);
      }
   }
   
   /**
    * Create a user sandbox for the named store.
    * 
    * A user sandbox is comprised of two stores, the first 
    * named 'storename---username' layered over the staging store with a preview store 
    * named 'storename--username--preview' layered over the main store.
    * 
    * Various store meta-data properties are set including:
    * Identifier for store-types: .sandbox.author.main and .sandbox.author.preview
    * Store-id: .sandbox-id.<guid> (unique across all stores in the sandbox)
    * DNS: .dns.<store> = <path-to-webapps-root>
    * Website Name: .website.name = website name
    * 
    * @param name       The store name to create the sandbox for
    * @param managers   The list of authorities who have ContentManager role in the website
    * @param username   Username of the user to create the sandbox for
    * @param role       Role permission for the user
    */
   public static void createUserSandbox(String name, List<String> managers, String username, String role)
   {
      ServiceRegistry services = Repository.getServiceRegistry(FacesContext.getCurrentInstance());
      AVMService avmService = services.getAVMService();
      PermissionService permissionService = services.getPermissionService();
      
      // create the user 'main' store
      String userStore = AVMConstants.buildAVMUserMainStoreName(name, username);
      if (avmService.getStore(userStore) == null)
      {
         avmService.createStore(userStore);
         if (logger.isDebugEnabled())
            logger.debug("Created user sandbox store: " + userStore);
         
         // create a layered directory pointing to 'appBase' in the staging area
         String path = userStore + ":/";
         String targetPath = name + AVMConstants.STORE_STAGING + ":/" + AVMConstants.DIR_APPBASE;
         avmService.createLayeredDirectory(targetPath, path, AVMConstants.DIR_APPBASE);
         NodeRef dirRef = AVMNodeConverter.ToNodeRef(-1, path + '/' + AVMConstants.DIR_APPBASE);
         permissionService.setPermission(dirRef, username, role, true);
         for (String manager : managers)
         {
            permissionService.setPermission(dirRef, manager, ROLE_CONTENT_MANAGER, true);
         }
         
         // tag the store with the store type
         avmService.setStoreProperty(userStore,
               QName.createQName(null, AVMConstants.PROP_SANDBOX_AUTHOR_MAIN),
               new PropertyValue(DataTypeDefinition.TEXT, null));
         
         // tag the store with the base name of the website so that corresponding
         // staging areas can be found.
         avmService.setStoreProperty(userStore,
               QName.createQName(null, AVMConstants.PROP_WEBSITE_NAME),
               new PropertyValue(DataTypeDefinition.TEXT, name));
         
         // tag the store, oddly enough, with its own store name for querying.
         // when will the madness end.
         avmService.setStoreProperty(userStore,
               QName.createQName(null, AVMConstants.PROP_SANDBOX_STORE_PREFIX + userStore),
               new PropertyValue(DataTypeDefinition.TEXT, null));
         
         // tag the store with the DNS name property
         tagStoreDNSPath(avmService, userStore, name, username);
         
         // snapshot the store
         avmService.createSnapshot(userStore, null, null);
         
         
         // create the user 'preview' store
         String previewStore = AVMConstants.buildAVMUserPreviewStoreName(name, username);
         avmService.createStore(previewStore);
         if (logger.isDebugEnabled())
            logger.debug("Created user sandbox store: " + previewStore);
         
         // create a layered directory pointing to 'appBase' in the user 'main' store
         path = previewStore + ":/";
         targetPath = userStore + ":/" + AVMConstants.DIR_APPBASE;
         avmService.createLayeredDirectory(targetPath, path, AVMConstants.DIR_APPBASE);
         dirRef = AVMNodeConverter.ToNodeRef(-1, path + '/' + AVMConstants.DIR_APPBASE);
         permissionService.setPermission(dirRef, username, role, true);
         for (String manager : managers)
         {
            permissionService.setPermission(dirRef, manager, ROLE_CONTENT_MANAGER, true);
         }
         
         // tag the store with the store type
         avmService.setStoreProperty(previewStore,
               QName.createQName(null, AVMConstants.PROP_SANDBOX_AUTHOR_PREVIEW),
               new PropertyValue(DataTypeDefinition.TEXT, null));
         
         // tag the store with its own store name for querying.
         avmService.setStoreProperty(previewStore,
               QName.createQName(null, AVMConstants.PROP_SANDBOX_STORE_PREFIX + previewStore),
               new PropertyValue(DataTypeDefinition.TEXT, null));
         
         // tag the store with the DNS name property
         tagStoreDNSPath(avmService, previewStore, name, username, "preview");
         
         // snapshot the store
         avmService.createSnapshot(previewStore, null, null);
         
         
         // tag all related stores to indicate that they are part of a single sandbox
         String sandboxIdProp = AVMConstants.PROP_SANDBOXID + GUID.generate();
         avmService.setStoreProperty(userStore, QName.createQName(null, sandboxIdProp),
               new PropertyValue(DataTypeDefinition.TEXT, null));
         avmService.setStoreProperty(previewStore, QName.createQName(null, sandboxIdProp),
               new PropertyValue(DataTypeDefinition.TEXT, null));
         
         if (logger.isDebugEnabled())
         {
            dumpStoreProperties(avmService, userStore);
            dumpStoreProperties(avmService, previewStore);
         }
      }
      else if (logger.isDebugEnabled())
      {
         logger.debug("Not creating as store already exists: " + userStore);
      }
   }
   
   /**
    * Tag a named store with a DNS path meta-data attribute.
    * The DNS meta-data attribute is set to the system path 'store:/appBase/avm_webapps'
    * 
    * @param store  Name of the store to tag
    */
   private static void tagStoreDNSPath(AVMService avmService, String store, String... components)
   {
      String path = AVMConstants.buildAVMStoreRootPath(store);
      // DNS name mangle the property name - can only contain value DNS characters!
      String dnsProp = AVMConstants.PROP_DNS + DNSNameMangler.MakeDNSName(components);
      avmService.setStoreProperty(store, QName.createQName(null, dnsProp),
            new PropertyValue(DataTypeDefinition.TEXT, path));
   }
   
   /**
    * Debug helper method to dump the properties of a store
    *  
    * @param store   Store name to dump properties for
    */
   private static void dumpStoreProperties(AVMService avmService, String store)
   {
      Map<QName, PropertyValue> props = avmService.getStoreProperties(store);
      for (QName name : props.keySet())
      {
         logger.debug("   " + name + ": " + props.get(name));
      }
   }
}
