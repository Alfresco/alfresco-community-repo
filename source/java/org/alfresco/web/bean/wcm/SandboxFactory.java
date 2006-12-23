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
    * @param storeId    The store name to create the sandbox for
    * @param managers   The list of authorities who have ContentManager role in the website 
    */
   public static void createStagingSandbox(final String storeId, final List<String> managers)
   {
      final ServiceRegistry services = Repository.getServiceRegistry(FacesContext.getCurrentInstance());
      final AVMService avmService = services.getAVMService();
      final PermissionService permissionService = services.getPermissionService();
      
      // create the 'staging' store for the website
      final String stagingStoreName = AVMConstants.buildStagingStoreName(storeId);
      avmService.createStore(stagingStoreName);
      if (logger.isDebugEnabled())
         logger.debug("Created staging sandbox store: " + stagingStoreName);
      
      // create the system directories 'appBase' and 'avm_webapps'
      avmService.createDirectory(stagingStoreName + ":/", AVMConstants.DIR_APPBASE);
      NodeRef dirRef = AVMNodeConverter.ToNodeRef(-1, AVMConstants.buildStoreRootPath(stagingStoreName));
      for (String manager : managers)
      {
         permissionService.setPermission(dirRef, manager, ROLE_CONTENT_MANAGER, true);
      }
      avmService.createDirectory(AVMConstants.buildStoreRootPath(stagingStoreName), 
                                 AVMConstants.DIR_WEBAPPS);
      
      // tag the store with the store type
      avmService.setStoreProperty(stagingStoreName,
                                  QName.createQName(null, AVMConstants.PROP_SANDBOX_STAGING_MAIN),
                                  new PropertyValue(DataTypeDefinition.TEXT, null));
      
      // tag the store with the DNS name property
      tagStoreDNSPath(avmService, stagingStoreName, storeId);
      
      // snapshot the store
      avmService.createSnapshot(stagingStoreName, null, null);
      
      
      // create the 'preview' store for the website
      final String previewStoreName = AVMConstants.buildStagingPreviewStoreName(storeId);
      avmService.createStore(previewStoreName);
      if (logger.isDebugEnabled())
         logger.debug("Created staging preview sandbox store: " + previewStoreName +
                      " above " + stagingStoreName);
      
      // create a layered directory pointing to 'appBase' in the staging area

      avmService.createLayeredDirectory(AVMConstants.buildStoreRootPath(stagingStoreName), 
                                        previewStoreName + ":/", 
                                        AVMConstants.DIR_APPBASE);
      dirRef = AVMNodeConverter.ToNodeRef(-1, AVMConstants.buildStoreRootPath(previewStoreName));
      for (String manager : managers)
      {
         permissionService.setPermission(dirRef, manager, ROLE_CONTENT_MANAGER, true);
      }
      
      // tag the store with the store type
      avmService.setStoreProperty(previewStoreName,
                                  QName.createQName(null, AVMConstants.PROP_SANDBOX_STAGING_PREVIEW),
                                  new PropertyValue(DataTypeDefinition.TEXT, null));
      
      // tag the store with the DNS name property
      tagStoreDNSPath(avmService, previewStoreName, storeId, "preview");
      
      // snapshot the store
      avmService.createSnapshot(previewStoreName, null, null);
      
      
      // tag all related stores to indicate that they are part of a single sandbox
      String sandboxIdProp = AVMConstants.PROP_SANDBOXID + GUID.generate();
      avmService.setStoreProperty(stagingStoreName,
                                  QName.createQName(null, sandboxIdProp),
                                  new PropertyValue(DataTypeDefinition.TEXT, null));
      avmService.setStoreProperty(previewStoreName,
                                  QName.createQName(null, sandboxIdProp),
                                  new PropertyValue(DataTypeDefinition.TEXT, null));
      
      if (logger.isDebugEnabled())
      {
         dumpStoreProperties(avmService, stagingStoreName);
         dumpStoreProperties(avmService, previewStoreName);
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
    * @param storeId    The store id to create the sandbox for
    * @param managers   The list of authorities who have ContentManager role in the website
    * @param username   Username of the user to create the sandbox for
    * @param role       Role permission for the user
    */
   public static void createUserSandbox(final String storeId, 
                                        final List<String> managers, 
                                        final String username, 
                                        final String role)
   {
      final ServiceRegistry services = Repository.getServiceRegistry(FacesContext.getCurrentInstance());
      final AVMService avmService = services.getAVMService();
      final PermissionService permissionService = services.getPermissionService();
      
      // create the user 'main' store
      final String userStoreName = AVMConstants.buildUserMainStoreName(storeId, username);
      if (avmService.getStore(userStoreName) != null)
      {
         if (logger.isDebugEnabled())
         {
            logger.debug("Not creating as store already exists: " + userStoreName);
         }
         return;
      }

      avmService.createStore(userStoreName);
      final String stagingStoreName = AVMConstants.buildStagingStoreName(storeId);
      if (logger.isDebugEnabled())
         logger.debug("Created user sandbox store: " + userStoreName +
                      " above staging store " + stagingStoreName);
      
      // create a layered directory pointing to 'appBase' in the staging area
      avmService.createLayeredDirectory(AVMConstants.buildStoreRootPath(stagingStoreName), 
                                        userStoreName + ":/", 
                                        AVMConstants.DIR_APPBASE);
      NodeRef dirRef = AVMNodeConverter.ToNodeRef(-1, AVMConstants.buildStoreRootPath(userStoreName));
      permissionService.setPermission(dirRef, username, role, true);
      for (String manager : managers)
      {
         permissionService.setPermission(dirRef, manager, ROLE_CONTENT_MANAGER, true);
      }
      
      // tag the store with the store type
      avmService.setStoreProperty(userStoreName,
                                  QName.createQName(null, AVMConstants.PROP_SANDBOX_AUTHOR_MAIN),
                                  new PropertyValue(DataTypeDefinition.TEXT, null));
         
      // tag the store with the base name of the website so that corresponding
      // staging areas can be found.
      avmService.setStoreProperty(userStoreName,
                                  QName.createQName(null, AVMConstants.PROP_WEBSITE_NAME),
                                  new PropertyValue(DataTypeDefinition.TEXT, storeId));
         
      // tag the store, oddly enough, with its own store name for querying.
      // when will the madness end.
      avmService.setStoreProperty(userStoreName,
                                  QName.createQName(null, AVMConstants.PROP_SANDBOX_STORE_PREFIX + userStoreName),
                                  new PropertyValue(DataTypeDefinition.TEXT, null));
         
      // tag the store with the DNS name property
      tagStoreDNSPath(avmService, userStoreName, storeId, username);
         
      // snapshot the store
      avmService.createSnapshot(userStoreName, null, null);
         
         
      // create the user 'preview' store
      String previewStoreName = AVMConstants.buildUserPreviewStoreName(storeId, username);
      avmService.createStore(previewStoreName);
      if (logger.isDebugEnabled())
         logger.debug("Created user preview sandbox store: " + previewStoreName +
                      " above " + userStoreName);
         
      // create a layered directory pointing to 'appBase' in the user 'main' store
      avmService.createLayeredDirectory(AVMConstants.buildStoreRootPath(userStoreName), 
                                        previewStoreName + ":/", 
                                        AVMConstants.DIR_APPBASE);
      dirRef = AVMNodeConverter.ToNodeRef(-1, AVMConstants.buildStoreRootPath(previewStoreName));
      permissionService.setPermission(dirRef, username, role, true);
      for (String manager : managers)
      {
         permissionService.setPermission(dirRef, manager, ROLE_CONTENT_MANAGER, true);
      }
         
      // tag the store with the store type
      avmService.setStoreProperty(previewStoreName,
                                  QName.createQName(null, AVMConstants.PROP_SANDBOX_AUTHOR_PREVIEW),
                                  new PropertyValue(DataTypeDefinition.TEXT, null));
         
      // tag the store with its own store name for querying.
      avmService.setStoreProperty(previewStoreName,
                                  QName.createQName(null, AVMConstants.PROP_SANDBOX_STORE_PREFIX + previewStoreName),
                                  new PropertyValue(DataTypeDefinition.TEXT, null));
         
      // tag the store with the DNS name property
      tagStoreDNSPath(avmService, previewStoreName, storeId, username, "preview");
         
      // snapshot the store
      avmService.createSnapshot(previewStoreName, null, null);
         
         
      // tag all related stores to indicate that they are part of a single sandbox
      String sandboxIdProp = AVMConstants.PROP_SANDBOXID + GUID.generate();
      avmService.setStoreProperty(userStoreName, QName.createQName(null, sandboxIdProp),
                                  new PropertyValue(DataTypeDefinition.TEXT, null));
      avmService.setStoreProperty(previewStoreName, QName.createQName(null, sandboxIdProp),
                                  new PropertyValue(DataTypeDefinition.TEXT, null));
         
      if (logger.isDebugEnabled())
      {
         dumpStoreProperties(avmService, userStoreName);
         dumpStoreProperties(avmService, previewStoreName);
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
   public static String createWorkflowSandbox(final String storeId)
   {
      final ServiceRegistry services = Repository.getServiceRegistry(FacesContext.getCurrentInstance());
      final AVMService avmService = services.getAVMService();
      final PermissionService permissionService = services.getPermissionService();
      
      final String stagingStoreName = AVMConstants.buildStagingStoreName(storeId);

      // create the user 'main' store
      final String packageName = AVMConstants.STORE_WORKFLOW + "-" + GUID.generate();
      final String workflowMainStoreName = 
         AVMConstants.buildWorkflowMainStoreName(storeId, packageName);
      
      avmService.createStore(workflowMainStoreName);
      if (logger.isDebugEnabled())
         logger.debug("Created workflow sandbox store: " + workflowMainStoreName);
         
      // create a layered directory pointing to 'appBase' in the staging area
      avmService.createLayeredDirectory(AVMConstants.buildStoreRootPath(stagingStoreName), 
                                        workflowMainStoreName + ":/", 
                                        AVMConstants.DIR_APPBASE);
//      NodeRef dirRef = AVMNodeConverter.ToNodeRef(-1, path + '/' + AVMConstants.DIR_APPBASE);
//      permissionService.setPermission(dirRef, username, role, true);
//      for (String manager : managers)
//         {
//            permissionService.setPermission(dirRef, manager, ROLE_CONTENT_MANAGER, true);
//         }
         
      // tag the store with the store type
      avmService.setStoreProperty(workflowMainStoreName,
                                  QName.createQName(null, AVMConstants.PROP_SANDBOX_WORKFLOW_MAIN),
                                  new PropertyValue(DataTypeDefinition.TEXT, null));
         
      // tag the store with the base name of the website so that corresponding
      // staging areas can be found.
      avmService.setStoreProperty(workflowMainStoreName,
                                  QName.createQName(null, AVMConstants.PROP_WEBSITE_NAME),
                                  new PropertyValue(DataTypeDefinition.TEXT, storeId));
         
      // tag the store, oddly enough, with its own store name for querying.
      // when will the madness end.
      avmService.setStoreProperty(workflowMainStoreName,
                                  QName.createQName(null, AVMConstants.PROP_SANDBOX_STORE_PREFIX + workflowMainStoreName),
                                  new PropertyValue(DataTypeDefinition.TEXT, null));
         
      // tag the store with the DNS name property
      tagStoreDNSPath(avmService, workflowMainStoreName, storeId, packageName);
         
      // snapshot the store
      avmService.createSnapshot(workflowMainStoreName, null, null);
         
      // create the user 'preview' store
      final String workflowPreviewStoreName = 
         AVMConstants.buildWorkflowPreviewStoreName(storeId, packageName);
      avmService.createStore(workflowPreviewStoreName);
      if (logger.isDebugEnabled())
         logger.debug("Created user sandbox preview store: " + workflowPreviewStoreName);
         
      // create a layered directory pointing to 'appBase' in the user 'main' store
      avmService.createLayeredDirectory(AVMConstants.buildStoreRootPath(workflowMainStoreName), 
                                        workflowPreviewStoreName + ":/", 
                                        AVMConstants.DIR_APPBASE);
//      dirRef = AVMNodeConverter.ToNodeRef(-1, path + '/' + AVMConstants.DIR_APPBASE);
//         permissionService.setPermission(dirRef, username, role, true);
//         for (String manager : managers)
//         {
//            permissionService.setPermission(dirRef, manager, ROLE_CONTENT_MANAGER, true);
//         }
         
      // tag the store with the store type
      avmService.setStoreProperty(workflowPreviewStoreName,
                                  QName.createQName(null, AVMConstants.PROP_SANDBOX_WORKFLOW_PREVIEW),
                                  new PropertyValue(DataTypeDefinition.TEXT, null));
      
      // tag the store with its own store name for querying.
      avmService.setStoreProperty(workflowPreviewStoreName,
                                  QName.createQName(null, 
                                                    AVMConstants.PROP_SANDBOX_STORE_PREFIX + workflowPreviewStoreName),
                                  new PropertyValue(DataTypeDefinition.TEXT, null));
         
      // tag the store with the DNS name property
      tagStoreDNSPath(avmService, workflowPreviewStoreName, storeId, packageName, "preview");
      
      // snapshot the store
      avmService.createSnapshot(workflowPreviewStoreName, null, null);
         
         
      // tag all related stores to indicate that they are part of a single sandbox
      String sandboxIdProp = AVMConstants.PROP_SANDBOXID + GUID.generate();
      avmService.setStoreProperty(workflowMainStoreName, QName.createQName(null, sandboxIdProp),
                                  new PropertyValue(DataTypeDefinition.TEXT, null));
      avmService.setStoreProperty(workflowPreviewStoreName, QName.createQName(null, sandboxIdProp),
                                  new PropertyValue(DataTypeDefinition.TEXT, null));
      
      if (logger.isDebugEnabled())
      {
         dumpStoreProperties(avmService, workflowMainStoreName);
         dumpStoreProperties(avmService, workflowPreviewStoreName);
      }
      return packageName;
   }
   
   /**
    * Tag a named store with a DNS path meta-data attribute.
    * The DNS meta-data attribute is set to the system path 'store:/appBase/avm_webapps'
    * 
    * @param store  Name of the store to tag
    */
   private static void tagStoreDNSPath(AVMService avmService, String store, String... components)
   {
      String path = AVMConstants.buildSandboxRootPath(store);
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
      logger.debug("Store " + store);
      Map<QName, PropertyValue> props = avmService.getStoreProperties(store);
      for (QName name : props.keySet())
      {
         logger.debug("   " + name + ": " + props.get(name));
      }
   }
}
