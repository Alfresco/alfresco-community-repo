/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.wcm.sandbox;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.config.JNDIConstants;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.mbeans.VirtServerRegistry;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.cmr.avm.locking.AVMLockingService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.DNSNameMangler;
import org.alfresco.util.GUID;
import org.alfresco.util.ParameterCheck;
import org.alfresco.wcm.util.WCMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Helper factory to create WCM sandbox structures
 * 
 * @author Kevin Roast
 * @author janv
 */
public final class SandboxFactory extends WCMUtil
{
   private static Log logger = LogFactory.getLog(SandboxFactory.class);
   
   /** Services */
   private NodeService nodeService;
   private PermissionService permissionService;
   private AVMService avmService;
   private AVMLockingService avmLockingService;
   private VirtServerRegistry virtServerRegistry;
   
   public void setNodeService(NodeService nodeService)
   {
       this.nodeService = nodeService;
   }
   
   public void setPermissionService(PermissionService permissionService)
   {
       this.permissionService = permissionService;
   }
   
   public void setAvmService(AVMService avmService)
   {
       this.avmService = avmService;
   }
   
   public void setAvmLockingService(AVMLockingService avmLockingService)
   {
       this.avmLockingService = avmLockingService;
   }
   
   public void setVirtServerRegistry(VirtServerRegistry virtServerRegistry)
   {
       this.virtServerRegistry = virtServerRegistry;
   }
   
   
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
    * @param storeId             The store name to create the sandbox for.
    * @param webProjectNodeRef   The noderef for the webproject.
    * @param branchStoreId       The ID of the store to branch this staging store from.
    */
   public SandboxInfo createStagingSandbox(String storeId, 
                                           NodeRef webProjectNodeRef,
                                           String branchStoreId)
   {
      // create the 'staging' store for the website
      String stagingStoreName = WCMUtil.buildStagingStoreName(storeId);
      avmService.createStore(stagingStoreName);
      
      if (logger.isDebugEnabled())
      {
         logger.debug("Created staging sandbox: " + stagingStoreName);
      }
      
      // we can either branch from an existing staging store or create a new structure
      if (branchStoreId != null)
      {
         String branchStorePath = WCMUtil.buildStagingStoreName(branchStoreId) + ":/" +
                                  JNDIConstants.DIR_DEFAULT_WWW;
         avmService.createBranch(-1, branchStorePath,
                                 stagingStoreName + ":/", JNDIConstants.DIR_DEFAULT_WWW);
      }
      else
      {
         // create the system directories 'www' and 'avm_webapps'
         avmService.createDirectory(stagingStoreName + ":/", JNDIConstants.DIR_DEFAULT_WWW);
         avmService.createDirectory(WCMUtil.buildStoreRootPath(stagingStoreName), 
                                    JNDIConstants.DIR_DEFAULT_APPBASE);
      }
      
      
      // set staging area permissions
      setStagingPermissions(storeId, webProjectNodeRef);
      
      // Add permissions for layers
      
      // tag the store with the store type
      avmService.setStoreProperty(stagingStoreName,
                                  SandboxConstants.PROP_SANDBOX_STAGING_MAIN,
                                  new PropertyValue(DataTypeDefinition.TEXT, null));
      avmService.setStoreProperty(stagingStoreName,
                                  SandboxConstants.PROP_WEB_PROJECT_NODE_REF,
                                  new PropertyValue(DataTypeDefinition.NODE_REF, webProjectNodeRef));
      
      // tag the store with the DNS name property
      tagStoreDNSPath(avmService, stagingStoreName, storeId);
      
      // snapshot the store
      avmService.createSnapshot(stagingStoreName, null, null);
      
      
      // create the 'preview' store for the website
      String previewStoreName = WCMUtil.buildStagingPreviewStoreName(storeId);
      avmService.createStore(previewStoreName);
      
      if (logger.isDebugEnabled())
      {
         logger.debug("Created staging preview sandbox store: " + previewStoreName +
                      " above " + stagingStoreName);
      }
      
      // create a layered directory pointing to 'www' in the staging area
      avmService.createLayeredDirectory(WCMUtil.buildStoreRootPath(stagingStoreName), 
                                        previewStoreName + ":/", 
                                        JNDIConstants.DIR_DEFAULT_WWW);
      
    
      // apply READ permissions for all users
      //dirRef = AVMNodeConverter.ToNodeRef(-1, WCMUtil.buildStoreRootPath(previewStoreName));
      //permissionService.setPermission(dirRef, PermissionService.ALL_AUTHORITIES, PermissionService.READ, true);
      
      // tag the store with the store type
      avmService.setStoreProperty(previewStoreName,
                                  SandboxConstants.PROP_SANDBOX_STAGING_PREVIEW,
                                  new PropertyValue(DataTypeDefinition.TEXT, null));
      
      // tag the store with the DNS name property
      tagStoreDNSPath(avmService, previewStoreName, storeId, "preview");

      // The preview store depends on the main staging store (dist=1)
      tagStoreBackgroundLayer(avmService,previewStoreName,stagingStoreName,1);
      
      // snapshot the store
      avmService.createSnapshot(previewStoreName, null, null);
      
      
      // tag all related stores to indicate that they are part of a single sandbox
      final QName sandboxIdProp = QName.createQName(SandboxConstants.PROP_SANDBOXID + GUID.generate());
      
      avmService.setStoreProperty(stagingStoreName,
                                  sandboxIdProp,
                                  new PropertyValue(DataTypeDefinition.TEXT, null));
      avmService.setStoreProperty(previewStoreName,
                                  sandboxIdProp,
                                  new PropertyValue(DataTypeDefinition.TEXT, null));
      
      if (logger.isDebugEnabled())
      {
         dumpStoreProperties(avmService, stagingStoreName);
         dumpStoreProperties(avmService, previewStoreName);
      }

      return getSandbox(stagingStoreName);
   }
   
   /**
    * Get sandbox info for given sandbox store id
    * 
    * @param sandboxId
    * @return SandboxInfo returns sandbox info or null if sandbox does not exist or is not visible
    */
   public SandboxInfo getSandbox(String sandboxId)
   {
       AVMStoreDescriptor storeDesc = avmService.getStore(sandboxId);
       if (storeDesc == null)
       {
           return null;
       }
       
       String wpStoreId = WCMUtil.getWebProjectStoreId(sandboxId);
       
       String[] storeNames = null;
       
       // Check sandbox type
       Map<QName, PropertyValue> props = avmService.getStoreProperties(sandboxId);
       QName sandboxType = null;
       
       // derive name for now
       String name = null;
       
       if (props.containsKey(SandboxConstants.PROP_SANDBOX_STAGING_MAIN))
       {
           sandboxType = SandboxConstants.PROP_SANDBOX_STAGING_MAIN;
           name = sandboxId;
           storeNames = new String[] {sandboxId, WCMUtil.getCorrespondingPreviewStoreName(sandboxId)};
       }
       else if ( props.containsKey( SandboxConstants.PROP_SANDBOX_STAGING_PREVIEW))
       {
           sandboxType = SandboxConstants.PROP_SANDBOX_STAGING_PREVIEW;
           storeNames = new String[] {WCMUtil.getCorrespondingMainStoreName(sandboxId), sandboxId};
       }
       else if (props.containsKey(SandboxConstants.PROP_SANDBOX_AUTHOR_MAIN))
       {
           sandboxType = SandboxConstants.PROP_SANDBOX_AUTHOR_MAIN;
           name = WCMUtil.getUserName(sandboxId);
           storeNames = new String[] {sandboxId, WCMUtil.getCorrespondingPreviewStoreName(sandboxId)};
       } 
       else if (props.containsKey(SandboxConstants.PROP_SANDBOX_AUTHOR_PREVIEW))
       {
           sandboxType = SandboxConstants.PROP_SANDBOX_AUTHOR_PREVIEW;
           name = WCMUtil.getUserName(sandboxId);
           storeNames = new String[] {WCMUtil.getCorrespondingMainStoreName(sandboxId), sandboxId};
       }
       else if (props.containsKey(SandboxConstants.PROP_SANDBOX_WORKFLOW_MAIN))
       {
           sandboxType = SandboxConstants.PROP_SANDBOX_WORKFLOW_MAIN;
           name = WCMUtil.getWorkflowId(sandboxId);
           storeNames = new String[] {sandboxId, WCMUtil.getCorrespondingPreviewStoreName(sandboxId)};
       }
       else if (props.containsKey(SandboxConstants.PROP_SANDBOX_WORKFLOW_PREVIEW))
       {
           sandboxType = SandboxConstants.PROP_SANDBOX_WORKFLOW_PREVIEW;
           name = WCMUtil.getWorkflowId(sandboxId);
           storeNames = new String[] {WCMUtil.getCorrespondingMainStoreName(sandboxId), sandboxId};
       }
       
       if ((storeNames == null) || (storeNames.length == 0))
       {
           throw new AlfrescoRuntimeException("Must have at least one store");
       }
       
       if ((storeNames.length == 1) && (! sandboxType.equals(SandboxConstants.PROP_SANDBOX_STAGING_MAIN)))
       {
           throw new AlfrescoRuntimeException("Main store must be of type: " + SandboxConstants.PROP_SANDBOX_STAGING_MAIN);
       }

       return new SandboxInfoImpl(wpStoreId, sandboxId, sandboxType, name, storeNames, new Date(storeDesc.getCreateDate()), storeDesc.getCreator());
   }

   protected void setStagingPermissions(String storeId, NodeRef wpNodeRef)
   {
       String storeName = WCMUtil.buildStagingStoreName(storeId);
       NodeRef dirRef = AVMNodeConverter.ToNodeRef(-1, WCMUtil.buildStoreRootPath(storeName));
      
       // Apply specific user permissions as set on the web project
       // All these will be masked out
      
       Map<String, String> userRoles = WCMUtil.listWebUsers(nodeService, wpNodeRef);

       for (Map.Entry<String, String> userRole : userRoles.entrySet())
       {
           String username = userRole.getKey();
           String userrole = userRole.getValue();
      
           permissionService.setPermission(dirRef, username, userrole, true);
       }
   }
   
   public void setStagingPermissionMasks(String storeId)
   {
      String storeName = WCMUtil.buildStagingStoreName(storeId);
      NodeRef dirRef = AVMNodeConverter.ToNodeRef(-1, WCMUtil.buildStoreRootPath(storeName));
      
      // Set store permission masks
      String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
      permissionService.setPermission(dirRef.getStoreRef(), currentUser, PermissionService.CHANGE_PERMISSIONS, true);
      permissionService.setPermission(dirRef.getStoreRef(), currentUser, PermissionService.READ_PERMISSIONS, true);
      permissionService.setPermission(dirRef.getStoreRef(), PermissionService.ALL_AUTHORITIES, PermissionService.READ, true);
      
      // apply READ permissions for all users
      permissionService.setPermission(dirRef, PermissionService.ALL_AUTHORITIES, PermissionService.READ, true);
   }
   
   private void updateStagingAreaManagers(String storeId, NodeRef webProjectNodeRef, final List<String> managers)
   {
       // The stores have the mask set in updateSandboxManagers
       String storeName = WCMUtil.buildStagingStoreName(storeId);
    
       NodeRef dirRef = AVMNodeConverter.ToNodeRef(-1, WCMUtil.buildStoreRootPath(storeName));
       for (String manager : managers)
       {
           permissionService.setPermission(dirRef, manager, WCMUtil.ROLE_CONTENT_MANAGER, true);
           
           // give the manager change permissions permission in the staging area store
           permissionService.setPermission(dirRef.getStoreRef(), manager, 
                    PermissionService.CHANGE_PERMISSIONS, true);
           permissionService.setPermission(dirRef.getStoreRef(), manager, 
                   PermissionService.READ_PERMISSIONS, true);
       }
   }
   
   public void addStagingAreaUser(String wpStoreId, String authority, String role)
   {
       // The stores have the mask set in updateSandboxManagers
       String storeName = WCMUtil.buildStagingStoreName(wpStoreId);
    
       NodeRef dirRef = AVMNodeConverter.ToNodeRef(-1, WCMUtil.buildStoreRootPath(storeName));
       permissionService.setPermission(dirRef, authority, role, true);
   }
   
   /**
    * Create a user sandbox for the named store.
    * 
    * A user sandbox is comprised of two stores, the first 
    * named 'storename--username' layered over the staging store with a preview store 
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
    * @return           Summary information regarding the sandbox
    */
   public SandboxInfo createUserSandbox(String storeId, 
                                        List<String> managers,
                                        String username, 
                                        String role)
   {
      // create the user 'main' store
      String userStoreName    = WCMUtil.buildUserMainStoreName(storeId, username);
      String previewStoreName = WCMUtil.buildUserPreviewStoreName(storeId, username);
      
      SandboxInfo userSandboxInfo = getSandbox(userStoreName);
      if (userSandboxInfo != null)
      {
          if (logger.isDebugEnabled())
          {
             logger.debug("Not creating author sandbox as it already exists: " + userStoreName);
          }
          return userSandboxInfo;
      }
      
      avmService.createStore(userStoreName);
      String stagingStoreName = WCMUtil.buildStagingStoreName(storeId);
      
      if (logger.isDebugEnabled())
      {
         logger.debug("Created user sandbox: " + userStoreName + " above staging store " + stagingStoreName);
      }
      
      // create a layered directory pointing to 'www' in the staging area
      avmService.createLayeredDirectory(WCMUtil.buildStoreRootPath(stagingStoreName), 
                                        userStoreName + ":/", 
                                        JNDIConstants.DIR_DEFAULT_WWW);
      NodeRef dirRef = AVMNodeConverter.ToNodeRef(-1, WCMUtil.buildStoreRootPath(userStoreName));
      
      // Apply access mask to the store (ACls are applie to the staging area)
      
      // apply the user role permissions to the sandbox
      String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
      permissionService.setPermission(dirRef.getStoreRef(), currentUser, WCMUtil.ROLE_CONTENT_MANAGER, true);
      permissionService.setPermission(dirRef.getStoreRef(), username, PermissionService.ALL_PERMISSIONS, true);
      permissionService.setPermission(dirRef.getStoreRef(), PermissionService.ALL_AUTHORITIES, PermissionService.READ, true);
      // apply the manager role permission for each manager in the web project
      for (String manager : managers)
      {
         permissionService.setPermission(dirRef.getStoreRef(), manager, WCMUtil.ROLE_CONTENT_MANAGER, true);
      }
      
      // tag the store with the store type
      avmService.setStoreProperty(userStoreName,
                                  SandboxConstants.PROP_SANDBOX_AUTHOR_MAIN,
                                  new PropertyValue(DataTypeDefinition.TEXT, null));
      
      // tag the store with the base name of the website so that corresponding
      // staging areas can be found.
      avmService.setStoreProperty(userStoreName,
                                  SandboxConstants.PROP_WEBSITE_NAME,
                                  new PropertyValue(DataTypeDefinition.TEXT, storeId));
      
      // tag the store, oddly enough, with its own store name for querying.
      avmService.setStoreProperty(userStoreName,
                                  QName.createQName(null, SandboxConstants.PROP_SANDBOX_STORE_PREFIX + userStoreName),
                                  new PropertyValue(DataTypeDefinition.TEXT, null));
         
      // tag the store with the DNS name property
      tagStoreDNSPath(avmService, userStoreName, storeId, username);
      
      // The user store depends on the main staging store (dist=1)
      tagStoreBackgroundLayer(avmService,userStoreName,stagingStoreName,1);

      // snapshot the store
      avmService.createSnapshot(userStoreName, null, null);
      
      // create the user 'preview' store
      avmService.createStore(previewStoreName);
      
      if (logger.isDebugEnabled())
      {
         logger.debug("Created user preview sandbox store: " + previewStoreName +
                      " above " + userStoreName);
      }
      
      // create a layered directory pointing to 'www' in the user 'main' store
      avmService.createLayeredDirectory(WCMUtil.buildStoreRootPath(userStoreName), 
                                        previewStoreName + ":/", 
                                        JNDIConstants.DIR_DEFAULT_WWW);
      dirRef = AVMNodeConverter.ToNodeRef(-1, WCMUtil.buildStoreRootPath(previewStoreName));
      
      // Apply access mask to the store (ACls are applied to the staging area)
      
      // apply the user role permissions to the sandbox
      permissionService.setPermission(dirRef.getStoreRef(), currentUser, WCMUtil.ROLE_CONTENT_MANAGER, true);
      permissionService.setPermission(dirRef.getStoreRef(), username, PermissionService.ALL_PERMISSIONS, true);
      permissionService.setPermission(dirRef.getStoreRef(), PermissionService.ALL_AUTHORITIES, PermissionService.READ, true);
      // apply the manager role permission for each manager in the web project
      for (String manager : managers)
      {
         permissionService.setPermission(dirRef.getStoreRef(), manager, WCMUtil.ROLE_CONTENT_MANAGER, true);
      }
      
      // tag the store with the store type
      avmService.setStoreProperty(previewStoreName,
                                  SandboxConstants.PROP_SANDBOX_AUTHOR_PREVIEW,
                                  new PropertyValue(DataTypeDefinition.TEXT, null));
      
      // tag the store with its own store name for querying.
      avmService.setStoreProperty(previewStoreName,
                                  QName.createQName(null, SandboxConstants.PROP_SANDBOX_STORE_PREFIX + previewStoreName),
                                  new PropertyValue(DataTypeDefinition.TEXT, null));
         
      // tag the store with the DNS name property
      tagStoreDNSPath(avmService, previewStoreName, storeId, username, "preview");
      
      // The preview user store depends on the main user store (dist=1)
      tagStoreBackgroundLayer(avmService,previewStoreName, userStoreName,1);

      // The preview user store depends on the main staging store (dist=2)
      tagStoreBackgroundLayer(avmService,previewStoreName, stagingStoreName,2);

         
      // snapshot the store
      avmService.createSnapshot(previewStoreName, null, null);
      
      
      // tag all related stores to indicate that they are part of a single sandbox
      QName sandboxIdProp = QName.createQName(null, SandboxConstants.PROP_SANDBOXID + GUID.generate());
      avmService.setStoreProperty(userStoreName, 
                                  sandboxIdProp,
                                  new PropertyValue(DataTypeDefinition.TEXT, null));
      avmService.setStoreProperty(previewStoreName,
                                  sandboxIdProp,
                                  new PropertyValue(DataTypeDefinition.TEXT, null));
      
      if (logger.isDebugEnabled())
      {
         dumpStoreProperties(avmService, userStoreName);
         dumpStoreProperties(avmService, previewStoreName);
      }
      
      return getSandbox(userStoreName);
   }
   
   /**
    * Create a workflow sandbox for the named store.
    * 
    * Various store meta-data properties are set including:
    * Identifier for store-types: .sandbox.workflow.main and .sandbox.workflow.preview
    * Store-id: .sandbox-id.<guid> (unique across all stores in the sandbox)
    * DNS: .dns.<store> = <path-to-webapps-root>
    * Website Name: .website.name = website name
    * 
    * @param storeId The id of the store to create a sandbox for
    * @return Information about the sandbox
    */
   public SandboxInfo createWorkflowSandbox(final String storeId)
   {
      final String stagingStoreName = WCMUtil.buildStagingStoreName(storeId);

      // create the workflow 'main' store
      final String packageName = WCMUtil.STORE_WORKFLOW + "-" + GUID.generate();
      final String mainStoreName = WCMUtil.buildWorkflowMainStoreName(storeId, packageName);
      
      avmService.createStore(mainStoreName);
      
      if (logger.isDebugEnabled())
      {
         logger.debug("Created workflow sandbox store: " + mainStoreName);
      }
         
      // create a layered directory pointing to 'www' in the staging area
      avmService.createLayeredDirectory(WCMUtil.buildStoreRootPath(stagingStoreName), 
                                        mainStoreName + ":/", 
                                        JNDIConstants.DIR_DEFAULT_WWW);
         
      // tag the store with the store type
      avmService.setStoreProperty(mainStoreName,
                                  SandboxConstants.PROP_SANDBOX_WORKFLOW_MAIN,
                                  new PropertyValue(DataTypeDefinition.TEXT, null));
         
      // tag the store with the base name of the website so that corresponding
      // staging areas can be found.
      avmService.setStoreProperty(mainStoreName,
                                  SandboxConstants.PROP_WEBSITE_NAME,
                                  new PropertyValue(DataTypeDefinition.TEXT, storeId));
         
      // tag the store, oddly enough, with its own store name for querying.
      avmService.setStoreProperty(mainStoreName,
                                  QName.createQName(null, SandboxConstants.PROP_SANDBOX_STORE_PREFIX + mainStoreName),
                                  new PropertyValue(DataTypeDefinition.TEXT, null));
         
      // tag the store with the DNS name property
      tagStoreDNSPath(avmService, mainStoreName, storeId, packageName);
         

      // The main workflow store depends on the main staging store (dist=1)
      tagStoreBackgroundLayer(avmService,mainStoreName, stagingStoreName ,1);

      // snapshot the store
      avmService.createSnapshot(mainStoreName, null, null);
         
      // create the workflow 'preview' store
      final String previewStoreName = WCMUtil.buildWorkflowPreviewStoreName(storeId, packageName);
      
      avmService.createStore(previewStoreName);
      
      if (logger.isDebugEnabled())
      {
         logger.debug("Created workflow sandbox preview store: " + previewStoreName);
      }
         
      // create a layered directory pointing to 'www' in the workflow 'main' store
      avmService.createLayeredDirectory(WCMUtil.buildStoreRootPath(mainStoreName), 
                                        previewStoreName + ":/", 
                                        JNDIConstants.DIR_DEFAULT_WWW);
         
      // tag the store with the store type
      avmService.setStoreProperty(previewStoreName,
                                  SandboxConstants.PROP_SANDBOX_WORKFLOW_PREVIEW,
                                  new PropertyValue(DataTypeDefinition.TEXT, null));
      
      // tag the store with its own store name for querying.
      avmService.setStoreProperty(previewStoreName,
                                  QName.createQName(null, 
                                                    SandboxConstants.PROP_SANDBOX_STORE_PREFIX + previewStoreName),
                                  new PropertyValue(DataTypeDefinition.TEXT, null));
         
      // tag the store with the DNS name property
      tagStoreDNSPath(avmService, previewStoreName, storeId, packageName, "preview");


      // The preview worfkflow store depends on the main workflow store (dist=1)
      tagStoreBackgroundLayer(avmService,previewStoreName, mainStoreName,1);

      // The preview workflow store depends on the main staging store (dist=2)
      tagStoreBackgroundLayer(avmService,previewStoreName, stagingStoreName,2);

      
      // snapshot the store
      avmService.createSnapshot(previewStoreName, null, null);
         
         
      // tag all related stores to indicate that they are part of a single sandbox
      final QName sandboxIdProp = QName.createQName(SandboxConstants.PROP_SANDBOXID + GUID.generate());
      avmService.setStoreProperty(mainStoreName, 
                                  sandboxIdProp,
                                  new PropertyValue(DataTypeDefinition.TEXT, null));
      avmService.setStoreProperty(previewStoreName, 
                                  sandboxIdProp,
                                  new PropertyValue(DataTypeDefinition.TEXT, null));
      
      if (logger.isDebugEnabled())
      {
         dumpStoreProperties(avmService, mainStoreName);
         dumpStoreProperties(avmService, previewStoreName);
      }
      
      return getSandbox(mainStoreName);
   }
   
   /**
    * Creates a workflow sandbox for the given user store. This will create a
    * workflow sandbox layered over the user's main store.
    * 
    * @param stagingStore The name of the staging store the user sandbox is layered over
    * @param userStore The name of the user store to create the workflow for
    * @return The store name of the main store in the workflow sandbox
    */
   // TODO refactor AVMExpiredContentProcessor ...
   public String createUserWorkflowSandbox(String stagingStore, String userStore)
   {
       // create the workflow 'main' store
       String packageName = "workflow-" + GUID.generate();
       String workflowStoreName = userStore + STORE_SEPARATOR + packageName;
     
       this.avmService.createStore(workflowStoreName);
       
       if (logger.isDebugEnabled())
           logger.debug("Created user workflow sandbox store: " + workflowStoreName);
        
       // create a layered directory pointing to 'www' in the users store
       this.avmService.createLayeredDirectory(
                userStore + ":/" + JNDIConstants.DIR_DEFAULT_WWW, 
                workflowStoreName + ":/", JNDIConstants.DIR_DEFAULT_WWW);
        
       // tag the store with the store type
       this.avmService.setStoreProperty(workflowStoreName, 
                SandboxConstants.PROP_SANDBOX_AUTHOR_WORKFLOW_MAIN,
                new PropertyValue(DataTypeDefinition.TEXT, null));
        
       // tag the store with the name of the author's store this one is layered over
       this.avmService.setStoreProperty(workflowStoreName, 
                SandboxConstants.PROP_AUTHOR_NAME,
                new PropertyValue(DataTypeDefinition.TEXT, userStore));
        
       // tag the store, oddly enough, with its own store name for querying.
       this.avmService.setStoreProperty(workflowStoreName,
                QName.createQName(null, SandboxConstants.PROP_SANDBOX_STORE_PREFIX + workflowStoreName),
                new PropertyValue(DataTypeDefinition.TEXT, null));
        
       // tag the store with the DNS name property
       String path = workflowStoreName + ":/" + JNDIConstants.DIR_DEFAULT_WWW + 
                "/" + JNDIConstants.DIR_DEFAULT_APPBASE;
       // DNS name mangle the property name - can only contain value DNS characters!
       String dnsProp = SandboxConstants.PROP_DNS + DNSNameMangler.MakeDNSName(stagingStore, packageName);
       this.avmService.setStoreProperty(workflowStoreName, QName.createQName(null, dnsProp),
                new PropertyValue(DataTypeDefinition.TEXT, path));
       
       // the main workflow store depends on the main user store (dist=1)
       String prop_key = SandboxConstants.PROP_BACKGROUND_LAYER + userStore;
       this.avmService.setStoreProperty(workflowStoreName, QName.createQName(null, prop_key),
                new PropertyValue(DataTypeDefinition.INT, 1));
       
       // The main workflow store depends on the main staging store (dist=2)
       prop_key = SandboxConstants.PROP_BACKGROUND_LAYER + stagingStore;
       this.avmService.setStoreProperty(workflowStoreName, QName.createQName(null, prop_key),
                new PropertyValue(DataTypeDefinition.INT, 2));
     
       // snapshot the store
       this.avmService.createSnapshot(workflowStoreName, null, null);
        
       // create the workflow 'preview' store
       String previewStoreName = workflowStoreName + STORE_SEPARATOR + "preview";
       this.avmService.createStore(previewStoreName);
     
       if (logger.isDebugEnabled())
           logger.debug("Created user workflow sandbox preview store: " + previewStoreName);
        
       // create a layered directory pointing to 'www' in the workflow 'main' store
       this.avmService.createLayeredDirectory(
                workflowStoreName + ":/" + JNDIConstants.DIR_DEFAULT_WWW, 
                previewStoreName + ":/", JNDIConstants.DIR_DEFAULT_WWW);
        
       // tag the store with the store type
       this.avmService.setStoreProperty(previewStoreName, SandboxConstants.PROP_SANDBOX_WORKFLOW_PREVIEW,
                new PropertyValue(DataTypeDefinition.TEXT, null));
     
       // tag the store with its own store name for querying.
       avmService.setStoreProperty(previewStoreName,
                QName.createQName(null, SandboxConstants.PROP_SANDBOX_STORE_PREFIX + previewStoreName),
                new PropertyValue(DataTypeDefinition.TEXT, null));
        
       // tag the store with the DNS name property
       path = previewStoreName + ":/" + JNDIConstants.DIR_DEFAULT_WWW + 
                "/" + JNDIConstants.DIR_DEFAULT_APPBASE;
       // DNS name mangle the property name - can only contain value DNS characters!
       dnsProp = SandboxConstants.PROP_DNS + DNSNameMangler.MakeDNSName(userStore, packageName, "preview");
       this.avmService.setStoreProperty(previewStoreName, QName.createQName(null, dnsProp),
                new PropertyValue(DataTypeDefinition.TEXT, path));

       // The preview worfkflow store depends on the main workflow store (dist=1)
       prop_key = SandboxConstants.PROP_BACKGROUND_LAYER + workflowStoreName;
       this.avmService.setStoreProperty(previewStoreName, QName.createQName(null, prop_key),
                new PropertyValue(DataTypeDefinition.INT, 1));

       // The preview workflow store depends on the main user store (dist=2)
       prop_key = SandboxConstants.PROP_BACKGROUND_LAYER + userStore;
       this.avmService.setStoreProperty(previewStoreName, QName.createQName(null, prop_key),
                new PropertyValue(DataTypeDefinition.INT, 2));
       
       // The preview workflow store depends on the main staging store (dist=3)
       prop_key = SandboxConstants.PROP_BACKGROUND_LAYER + stagingStore;
       this.avmService.setStoreProperty(previewStoreName, QName.createQName(null, prop_key),
                new PropertyValue(DataTypeDefinition.INT, 3));
     
       // snapshot the store
       this.avmService.createSnapshot(previewStoreName, null, null);
        
       // tag all related stores to indicate that they are part of a single sandbox
       QName sandboxIdProp = QName.createQName(SandboxConstants.PROP_SANDBOXID + GUID.generate());
       this.avmService.setStoreProperty(workflowStoreName, sandboxIdProp,
                new PropertyValue(DataTypeDefinition.TEXT, null));
       this.avmService.setStoreProperty(previewStoreName, sandboxIdProp,
                new PropertyValue(DataTypeDefinition.TEXT, null));
     
       // return the main workflow store name
       return workflowStoreName;
   }
   
   public List<SandboxInfo> listSandboxes(final String wpStoreId, String userName)
   {
       ParameterCheck.mandatoryString("wpStoreId", wpStoreId);
       ParameterCheck.mandatoryString("userName", userName);
       
       return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<List<SandboxInfo>>()
       {
           public List<SandboxInfo> doWork() throws Exception
           {
               List<AVMStoreDescriptor> stores = avmService.getStores();
               
               List<SandboxInfo> sbInfos = new ArrayList<SandboxInfo>();
               for (AVMStoreDescriptor store : stores)
               {
                   String storeName = store.getName();
                   
                   // list main stores - not preview stores or workflow stores
                   if ((storeName.startsWith(wpStoreId)) && 
                       (! WCMUtil.isPreviewStore(storeName)) &&
                       (! WCMUtil.isWorkflowStore(storeName)))
                   {
                       sbInfos.add(getSandbox(storeName));
                   }
               }
               
               return sbInfos;
           }
       }, userName);
   }

   public void deleteSandbox(String sbStoreId)
   {
       SandboxInfo sbInfo = getSandbox(sbStoreId);
       
       if (sbInfo != null)
       {
           String mainSandboxStore = sbInfo.getMainStoreName();
           
           // found the sandbox to remove - remove the main store (eg. user main store, staging main store, workflow main store)
           String path = WCMUtil.buildSandboxRootPath(mainSandboxStore);
    
            // Notify virtualisation server about removing this sandbox.
            //
            // Implementation note:
            //
            //     Because the removal of virtual webapps in the 
            //     virtualization server is recursive,  it only
            //     needs to be given the name of the main store.  
            //
            //     This notification must occur *prior* to purging content
            //     within the AVM because the virtualization server must list
            //     the avm_webapps dir in each store to discover which 
            //     virtual webapps must be unloaded.  The virtualization 
            //     server traverses the sandbox's stores in most-to-least 
            //     dependent order, so clients don't have to worry about
            //     accessing a preview layer whose main layer has been torn
            //     out from under it.
            
            WCMUtil.removeAllVServerWebapps(virtServerRegistry, path, true);
            
            // TODO: Use the .sandbox-id.  property to delete all sandboxes,
            //       rather than assume a sandbox always had a single preview
            //       layer attached.
            
            // purge stores, eg. main store followed by preview store
            String[] avmStoreNames = sbInfo.getStoreNames();
            
            for (String avmStoreName : avmStoreNames)
            {
                // check it exists before we try to remove it
                if (avmService.getStore(avmStoreName) != null)
                {
                    // purge store from the system
                    avmService.purgeStore(avmStoreName);
                }
                
                // remove any locks this user may have
                avmLockingService.removeStoreLocks(avmStoreName);
            }
       }
   }
   
   public void updateSandboxManagers(final String wpStoreId, NodeRef wpNodeRef, List<String> managers)
   {
       // walk existing user sandboxes and reapply manager permissions to include new manager user
       List<SandboxInfo> sbInfos = AuthenticationUtil.runAs(new RunAsWork<List<SandboxInfo>>()
       {
           public List<SandboxInfo> doWork() throws Exception
           {
               return listSandboxes(wpStoreId, AuthenticationUtil.getSystemUserName());
           }
       }, AuthenticationUtil.getSystemUserName());
       
       for (SandboxInfo sbInfo : sbInfos)
       {
           if (sbInfo.getSandboxType().equals(SandboxConstants.PROP_SANDBOX_AUTHOR_MAIN))
           {
               String username = sbInfo.getName();
               updateUserSandboxManagers(wpStoreId, managers, username);
           }
       }
       
       updateStagingAreaManagers(wpStoreId, wpNodeRef, managers);
   }
   
   /**
    * Update the permissions for the list of sandbox managers applied to a user sandbox.
    * <p>
    * Ensures that all managers in the list have full WRITE access to the specified user stores.
    * 
    * @param storeId    The store id of the sandbox to update
    * @param managers   The list of authorities who have ContentManager role in the web project
    * @param username   Username of the user sandbox to update
    */
   private void updateUserSandboxManagers(final String storeId, final List<String> managers, final String username)
   {
      final String userStoreName    = WCMUtil.buildUserMainStoreName(storeId, username);
      final String previewStoreName = WCMUtil.buildUserPreviewStoreName(storeId, username);
      
      // Apply masks to the stores
      
      // apply the manager role permission to the user main sandbox for each manager
      NodeRef dirRef = AVMNodeConverter.ToNodeRef(-1, WCMUtil.buildStoreRootPath(userStoreName));
      for (String manager : managers)
      {
         permissionService.setPermission(dirRef.getStoreRef(), manager, WCMUtil.ROLE_CONTENT_MANAGER, true);
      }
      
      // apply the manager role permission to the user preview sandbox for each manager
      dirRef = AVMNodeConverter.ToNodeRef(-1, WCMUtil.buildStoreRootPath(previewStoreName));
      for (String manager : managers)
      {
         permissionService.setPermission(dirRef.getStoreRef(), manager, WCMUtil.ROLE_CONTENT_MANAGER, true);
      }
   }
   
   /**
    * Tag a named store with a DNS path meta-data attribute.
    * The DNS meta-data attribute is set to the system path 'store:/www/avm_webapps'
    * 
    * @param store  Name of the store to tag
    */
   private static void tagStoreDNSPath(AVMService avmService, String store, String... components)
   {
      String path = WCMUtil.buildSandboxRootPath(store);
      // DNS name mangle the property name - can only contain value DNS characters!
      String dnsProp = SandboxConstants.PROP_DNS + DNSNameMangler.MakeDNSName(components);
      avmService.setStoreProperty(store, QName.createQName(null, dnsProp),
            new PropertyValue(DataTypeDefinition.TEXT, path));
   }

   /**
    *   Tags a store with a property that indicates one of its 
    *   backgroundStore layers, and the distance of that layer. 
    *   This function must be called separately for each background 
    *   store;  for example the "mysite--alice--preview" store had 
    *   as its immediate background "mysite--alice", which itself had
    *   as its background store "mysite", you'd make a sequence of
    *   calls like this:
    *
    *   <pre>
    *    tagStoreBackgroundLayer("mysite--alice",          "mysite",        1);
    *    tagStoreBackgroundLayer("mysite--alice--preview", "mysite--alice", 1);
    *    tagStoreBackgroundLayer("mysite--alice--preview", "mysite",        2);
    *   </pre>
    *
    *   This make it easy for other parts of the system to determine
    *   which stores depend on others directly or indirectly (which is
    *   useful for reloading virtualized webapps).
    *
    * @param store            Name of the store to tag
    * @param backgroundStore  Name of store's background store
    * @param distance         Distance from store.
    *                         The backgroundStore 'mysite' is 1 away from the store 'mysite--alice'
    *                         but 2 away from the store 'mysite--alice--preview'.
    */
   private static void tagStoreBackgroundLayer(AVMService  avmService, 
                                               String      store, 
                                               String      backgroundStore, 
                                               int         distance)
   {
      String prop_key = SandboxConstants.PROP_BACKGROUND_LAYER + backgroundStore;
      avmService.setStoreProperty(store, QName.createQName(null, prop_key),
            new PropertyValue(DataTypeDefinition.INT, distance));
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
