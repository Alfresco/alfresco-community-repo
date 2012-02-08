/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.wcm.sandbox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.config.JNDIConstants;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.mbeans.VirtServerRegistry;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.avm.actions.AVMDeployWebsiteAction;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.cmr.avm.locking.AVMLockingService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.DNSNameMangler;
import org.alfresco.util.GUID;
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
   private static final Set<String> ZONES; 
    
   public static final String[] PERMISSIONS = new String[] {
        PermissionService.WCM_CONTENT_MANAGER, 
        PermissionService.WCM_CONTENT_PUBLISHER,
        PermissionService.WCM_CONTENT_CONTRIBUTOR, 
        PermissionService.WCM_CONTENT_REVIEWER };
    
   private static Log logger = LogFactory.getLog(SandboxFactory.class);
   
   /** Services */
   private NodeService nodeService;
   private PermissionService permissionService;
   private AVMService avmService;
   private VirtServerRegistry virtServerRegistry;
   private AuthorityService authorityService;
   private AVMLockingService avmLockingService;
   
   static
   {
       HashSet<String> zones = new HashSet<String>(2, 1.0f);
       zones.add(AuthorityService.ZONE_APP_WCM);
       zones.add(AuthorityService.ZONE_AUTH_ALFRESCO);
       ZONES = Collections.unmodifiableSet(zones);
   }
   
   private final static QName PROP_SANDBOX_LOCALHOST_DEPLOYED = QName.createQName(null, ".sandbox.localhost."+AVMDeployWebsiteAction.LIVE_SUFFIX);
   
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
   
   public void setVirtServerRegistry(VirtServerRegistry virtServerRegistry)
   {
       this.virtServerRegistry = virtServerRegistry;
   }
   
   public void setAuthorityService(AuthorityService authorityService)
   {
       this.authorityService = authorityService;
   }
   
   public void setAvmLockingService(AVMLockingService avmLockingService)
   {
       this.avmLockingService = avmLockingService;
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
      long start = System.currentTimeMillis();
      
      // create the 'staging' store for the website
      String stagingStoreName = WCMUtil.buildStagingStoreName(storeId);
      
      // tag store with properties
      Map<QName, PropertyValue> props = new HashMap<QName, PropertyValue>(3);
      // tag the store with the store type
      props.put(SandboxConstants.PROP_SANDBOX_STAGING_MAIN, new PropertyValue(DataTypeDefinition.TEXT, null));
      props.put(SandboxConstants.PROP_WEB_PROJECT_NODE_REF, new PropertyValue(DataTypeDefinition.NODE_REF, webProjectNodeRef));
      // tag the store with the DNS name property
      addStoreDNSPath(stagingStoreName, props, storeId);
      
      avmService.createStore(stagingStoreName, props);
      
      if (logger.isTraceEnabled())
      {
         logger.trace("Created staging sandbox: " + stagingStoreName);
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
      
      // snapshot the store
      avmService.createSnapshot(stagingStoreName, null, null);
      
      
      // create the 'preview' store for the website
      String previewStoreName = WCMUtil.buildStagingPreviewStoreName(storeId);
      
      // tag store with properties - store type, web project DM nodeRef, DNS name
      props = new HashMap<QName, PropertyValue>(3);
      // tag the store with the store type
      props.put(SandboxConstants.PROP_SANDBOX_STAGING_PREVIEW, new PropertyValue(DataTypeDefinition.TEXT, null));
      // tag the store with the DNS name property
      addStoreDNSPath(previewStoreName, props, storeId, "preview");
      // The preview store depends on the main staging store (dist=1)
      addStoreBackgroundLayer(props, stagingStoreName, 1);
      
      avmService.createStore(previewStoreName, props);
      
      if (logger.isTraceEnabled())
      {
         logger.trace("Created staging preview sandbox store: " + previewStoreName +
                      " above " + stagingStoreName);
      }
      
      // create a layered directory pointing to 'www' in the staging area
      avmService.createLayeredDirectory(WCMUtil.buildStoreRootPath(stagingStoreName), 
                                        previewStoreName + ":/", 
                                        JNDIConstants.DIR_DEFAULT_WWW);
      
      
      // apply READ permissions for all users
      //dirRef = AVMNodeConverter.ToNodeRef(-1, WCMUtil.buildStoreRootPath(previewStoreName));
      //permissionService.setPermission(dirRef, PermissionService.ALL_AUTHORITIES, PermissionService.READ, true);
      
      // snapshot the store
      avmService.createSnapshot(previewStoreName, null, null);
      
      
      // tag all related stores to indicate that they are part of a single sandbox
      final String sandboxGuid = GUID.generate();
      
      props = new HashMap<QName, PropertyValue>(2);
      addSandboxGuid(sandboxGuid, props);
      
      avmService.setStoreProperties(stagingStoreName, props);
      avmService.setStoreProperties(previewStoreName, props);
      
      if (logger.isTraceEnabled())
      {
         dumpStoreProperties(avmService, stagingStoreName);
         dumpStoreProperties(avmService, previewStoreName);
      }
      
      SandboxInfo sbInfo =  getSandbox(stagingStoreName);
      
      if (logger.isTraceEnabled())
      {
         logger.trace("createStagingSandbox: " + sbInfo.getSandboxId() + " in "+(System.currentTimeMillis()-start)+" ms");
      }
      
      return sbInfo;
   }
   
   /**
    * Get sandbox info for given sandbox store id
    * 
    * @param sandboxId
    * @return SandboxInfo returns sandbox info or null if sandbox does not exist or is not visible
    */
   /* package */ SandboxInfo getSandbox(String sandboxId)
   {
       String wpStoreId = WCMUtil.getWebProjectStoreId(sandboxId);
       
       return getSandbox(wpStoreId, sandboxId, true);
   }
   
   private SandboxInfo getSandbox(String wpStoreId, String sandboxId, boolean withPreview)
   {
       AVMStoreDescriptor storeDesc = avmService.getStore(sandboxId);
       if (storeDesc == null)
       {
           return null;
       }
       
       String[] storeNames = null;
       
       // Check sandbox type
       Map<QName, PropertyValue> props = avmService.getStoreProperties(sandboxId);
       QName sandboxType = null;
       
       // derive name for now
       String name = null;
       
       String previewSandboxId = null;
       if (withPreview && (! WCMUtil.isPreviewStore(sandboxId)))
       {
           previewSandboxId = WCMUtil.getCorrespondingPreviewStoreName(sandboxId);
       }
       
       if (props.containsKey(SandboxConstants.PROP_SANDBOX_STAGING_MAIN))
       {
           sandboxType = SandboxConstants.PROP_SANDBOX_STAGING_MAIN;
           name = sandboxId;
           storeNames = new String[] {sandboxId, previewSandboxId};
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
           storeNames = new String[] {sandboxId, previewSandboxId};
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
           storeNames = new String[] {sandboxId, previewSandboxId};
       }
       else if (props.containsKey(SandboxConstants.PROP_SANDBOX_WORKFLOW_PREVIEW))
       {
           sandboxType = SandboxConstants.PROP_SANDBOX_WORKFLOW_PREVIEW;
           name = WCMUtil.getWorkflowId(sandboxId);
           storeNames = new String[] {WCMUtil.getCorrespondingMainStoreName(sandboxId), sandboxId};
       }
       else if (props.containsKey(SandboxConstants.PROP_SANDBOX_AUTHOR_WORKFLOW_MAIN))
       {
          sandboxType = SandboxConstants.PROP_SANDBOX_AUTHOR_WORKFLOW_MAIN;
          name = WCMUtil.getWorkflowId(sandboxId);
          storeNames = new String[] {sandboxId, previewSandboxId};
       }
       else if (props.containsKey(SandboxConstants.PROP_SANDBOX_AUTHOR_WORKFLOW_PREVIEW))
       {
          sandboxType = SandboxConstants.PROP_SANDBOX_AUTHOR_WORKFLOW_PREVIEW;
          name = WCMUtil.getWorkflowId(sandboxId);
       }
       else if (WCMUtil.isLocalhostDeployedStore(wpStoreId, sandboxId))
       {
           // TODO refactor - pending explicit WCM services support for deployment config
           sandboxType = PROP_SANDBOX_LOCALHOST_DEPLOYED;
           name = sandboxId;
           storeNames = new String[] {sandboxId};
       }
       
       if ((storeNames == null) || (storeNames.length == 0))
       {
           throw new AlfrescoRuntimeException("Must have at least one store");
       }
       
       if ((storeNames.length == 1) && 
           ((! sandboxType.equals(SandboxConstants.PROP_SANDBOX_STAGING_MAIN)) && (! sandboxType.equals(PROP_SANDBOX_LOCALHOST_DEPLOYED))))
       {
           throw new AlfrescoRuntimeException("Main store must be of type: " + SandboxConstants.PROP_SANDBOX_STAGING_MAIN);
       }

       return new SandboxInfoImpl(wpStoreId, sandboxId, sandboxType, name, storeNames, new Date(storeDesc.getCreateDate()), storeDesc.getCreator());
   }

   private void setStagingPermissions(String storeId, NodeRef wpNodeRef)
   {
       String storeName = WCMUtil.buildStagingStoreName(storeId);
       NodeRef dirRef = AVMNodeConverter.ToNodeRef(-1, WCMUtil.buildStoreRootPath(storeName));

       makeGroupsIfRequired(storeName, dirRef);
       // Apply specific user permissions as set on the web project
       // All these will be masked out
      
       Map<String, String> userRoles = WCMUtil.listWebUsers(nodeService, wpNodeRef);

       for (Map.Entry<String, String> userRole : userRoles.entrySet())
       {
           String username = userRole.getKey();
           String userrole = userRole.getValue();
      
           // permissionService.setPermission(dirRef, username, userrole, true);
           addToGroupIfRequired(storeName, username, userrole);
       }

       // Add group permissions
       for (String permission : PERMISSIONS)
       {
           String cms = authorityService.getName(AuthorityType.GROUP, storeName + "-" + permission);
           permissionService.setPermission(dirRef, cms, permission, true);
       }

       // TODO: does everyone get read writes?
       permissionService.setPermission(dirRef, PermissionService.ALL_AUTHORITIES, PermissionService.READ, true);
   }
   
   private void makeGroupsIfRequired(final String stagingStoreName, final NodeRef dirRef)
   {
       AuthenticationUtil.runAs(new RunAsWork<Object>(){

           public Object doWork() throws Exception
           {
               for (String permission : PERMISSIONS)
               {
                   String shortName = stagingStoreName + "-" + permission;
                   String group = authorityService.getName(AuthorityType.GROUP, shortName);
                   if (!authorityService.authorityExists(group))
                   {
                       authorityService.createAuthority(AuthorityType.GROUP, shortName, shortName, ZONES);
                   }
                   if (!isPermissionSet(dirRef, group, permission))
                   {
                       permissionService.setPermission(dirRef, group, permission, true);
                   }
               }
               return null;
           }}, AuthenticationUtil.getSystemUserName());
   }

   private boolean isPermissionSet(NodeRef nodeRef, String authority, String permission)
   {
       Set<AccessPermission> set = permissionService.getAllSetPermissions(nodeRef);
       for (AccessPermission ap : set)
       {
           if (ap.getAuthority().equals(authority) && ap.isSetDirectly() && ap.getPermission().equals(permission))
           {
               return true;
           }
       }
       return false;
   }

   private void addToGroupIfRequired(final String stagingStoreName, final String user, final String permission)
   {
       AuthenticationUtil.runAs(new RunAsWork<Object>(){

           public Object doWork() throws Exception
           {
               String shortName = stagingStoreName + "-" + permission;
               String group = authorityService.getName(AuthorityType.GROUP, shortName);
               if (!authorityService.authorityExists(group))
               {
                   authorityService.createAuthority(AuthorityType.GROUP, shortName, shortName, ZONES);
               }
               Set<String> members = authorityService.getContainedAuthorities(AuthorityType.USER, group, true);
               if (!members.contains(user))
               {
                   authorityService.addAuthority(group, user);
               }
               return null;
           }}, AuthenticationUtil.getSystemUserName());
       
   }

   private void removeFromGroupIfRequired(final String stagingStoreName, final String user, final String permission)
   {
       AuthenticationUtil.runAs(new RunAsWork<Object>(){

           public Object doWork() throws Exception
           {
               String shortName = stagingStoreName + "-" + permission;
               String group = authorityService.getName(AuthorityType.GROUP, shortName);
               if (authorityService.authorityExists(group))
               {
                   Set<String> members = authorityService.getContainedAuthorities(AuthorityType.USER, group, true);
                   if (members.contains(user))
                   {
                       authorityService.removeAuthority(group, user);
                   }
               }
               return null;
           }}, AuthenticationUtil.getSystemUserName());
   }

   private boolean isMaskSet(StoreRef storeRef, String authority, String permission)
   {
       Set<AccessPermission> set = permissionService.getAllSetPermissions(storeRef);
       for (AccessPermission ap : set)
       {
           if (ap.getAuthority().equals(authority) && ap.isSetDirectly() && ap.getPermission().equals(permission))
           {
               return true;
           }
       }
       return false;
   }
   
   public void setStagingPermissionMasks(String storeId)
   {
      String storeName = WCMUtil.buildStagingStoreName(storeId);
      NodeRef dirRef = AVMNodeConverter.ToNodeRef(-1, WCMUtil.buildStoreRootPath(storeName));
      
      // Set store permission masks
      String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
      addToGroupIfRequired(storeName, currentUser, PermissionService.WCM_CONTENT_MANAGER);

      String cms = authorityService.getName(AuthorityType.GROUP, storeName + "-" + PermissionService.WCM_CONTENT_MANAGER);
       
      // read early or the mask prevents access...
      boolean setReadPermissions = !isMaskSet(dirRef.getStoreRef(), cms, PermissionService.READ_PERMISSIONS);
       
      if (!isMaskSet(dirRef.getStoreRef(), cms, PermissionService.CHANGE_PERMISSIONS))
      {
          permissionService.setPermission(dirRef.getStoreRef(), cms, PermissionService.CHANGE_PERMISSIONS, true);
      }

      if (setReadPermissions)
      {
          permissionService.setPermission(dirRef.getStoreRef(), cms, PermissionService.READ_PERMISSIONS, true);
      }

      if (!isMaskSet(dirRef.getStoreRef(), PermissionService.ALL_AUTHORITIES, PermissionService.READ))
      {
          permissionService.setPermission(dirRef.getStoreRef(), PermissionService.ALL_AUTHORITIES, PermissionService.READ, true);
      }
   }

   /*
   private Set<String> getGroupMembers(String stagingStoreName, String permission)
   {
       String shortName = stagingStoreName + "-" + permission;
       String group = authorityService.getName(AuthorityType.GROUP, shortName);
       Set<String> members = authorityService.getContainedAuthorities(AuthorityType.USER, group, true);
       return members;

   }
   */
   
   private void updateStagingAreaManagers(String storeId, final List<String> managers)
   {
       // The stores have the mask set in updateSandboxManagers
       String storeName = WCMUtil.buildStagingStoreName(storeId);

       // Set<String> existingMembers = getGroupMembers(storeName, PermissionService.WCM_CONTENT_MANAGER);
       
       String shortName = storeName + "-" + PermissionService.WCM_CONTENT_MANAGER;
       String group = authorityService.getName(AuthorityType.GROUP, shortName);
       Set<String> members = authorityService.getContainedAuthorities(AuthorityType.USER, group, true);
       Set<String> toRemove = new HashSet<String>(members);

       for (String manager : managers)
       {
           addToGroupIfRequired(storeName, manager, PermissionService.WCM_CONTENT_MANAGER);
           toRemove.remove(manager);
       }

       for (String remove : toRemove)
       {
           removeFromGroupIfRequired(storeName, remove, PermissionService.WCM_CONTENT_MANAGER);
       }
   }
   
   public void addStagingAreaUser(String storeId, String authority, String role)
   {
       // The stores have the mask set in updateSandboxManagers
       
       //String storeName = WCMUtil.buildStagingStoreName(storeId);
       //NodeRef dirRef = AVMNodeConverter.ToNodeRef(-1, WCMUtil.buildStoreRootPath(storeName));

       addToGroupIfRequired(storeId, authority, role);
       // permissionService.setPermission(dirRef, authority, role, true);
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
    * @param username   Username of the user to create the sandbox for
    * @param role       Role permission for the user
    * @return           Summary information regarding the sandbox
    */
   public SandboxInfo createUserSandbox(String storeId, 
                                        String username, 
                                        String role)
   {
      long start = System.currentTimeMillis();
      
      // create the user 'main' store
      String userStoreName    = WCMUtil.buildUserMainStoreName(storeId, username);
      String previewStoreName = WCMUtil.buildUserPreviewStoreName(storeId, username);
      
      SandboxInfo userSandboxInfo = getSandbox(userStoreName);
      if (userSandboxInfo != null)
      {
          if (logger.isTraceEnabled())
          {
             logger.trace("Not creating author sandbox as it already exists: " + userStoreName);
          }
          return userSandboxInfo;
      }
      
      final String sandboxGuid = GUID.generate();
      
      String stagingStoreName = WCMUtil.buildStagingStoreName(storeId);
      
      // tag store with properties
      Map<QName, PropertyValue> props = new HashMap<QName, PropertyValue>(6);
      // tag the store with the store type
      props.put(SandboxConstants.PROP_SANDBOX_AUTHOR_MAIN, new PropertyValue(DataTypeDefinition.TEXT, null));
      // tag the store with the base name of the website so that corresponding staging areas can be found.
      props.put(SandboxConstants.PROP_WEBSITE_NAME, new PropertyValue(DataTypeDefinition.TEXT, storeId));
      
      // tag the store, oddly enough, with its own store name for querying.
      addSandboxPrefix(userStoreName, props);
      // tag all related stores to indicate that they are part of a single sandbox
      addSandboxGuid(sandboxGuid, props);
      // tag the store with the DNS name property
      addStoreDNSPath(userStoreName, props, storeId, WCMUtil.escapeStoreNameComponent(username));
      // The user store depends on the main staging store (dist=1)
      addStoreBackgroundLayer(props, stagingStoreName, 1);
      
      avmService.createStore(userStoreName, props);
      
      if (logger.isTraceEnabled())
      {
         logger.trace("Created user sandbox: " + userStoreName + " above staging store " + stagingStoreName);
      }
      
      // create a layered directory pointing to 'www' in the staging area
      avmService.createLayeredDirectory(WCMUtil.buildStoreRootPath(stagingStoreName), 
                                        userStoreName + ":/", 
                                        JNDIConstants.DIR_DEFAULT_WWW);
      NodeRef dirRef = AVMNodeConverter.ToNodeRef(-1, WCMUtil.buildStoreRootPath(userStoreName));
      
      // Apply access mask to the store (ACls are applie to the staging area)
      
      // apply the user role permissions to the sandbox
      String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
      // permissionService.setPermission(dirRef.getStoreRef(), currentUser, AVMUtil.ROLE_CONTENT_MANAGER, true);
      addToGroupIfRequired(stagingStoreName, currentUser, PermissionService.WCM_CONTENT_MANAGER);
      String cms = authorityService.getName(AuthorityType.GROUP, stagingStoreName + "-" + PermissionService.WCM_CONTENT_MANAGER);
      permissionService.setPermission(dirRef.getStoreRef(), cms, PermissionService.WCM_CONTENT_MANAGER, true);
      
      permissionService.setPermission(dirRef.getStoreRef(), username, PermissionService.ALL_PERMISSIONS, true);
      permissionService.setPermission(dirRef.getStoreRef(), PermissionService.ALL_AUTHORITIES, PermissionService.READ, true);
      
      // snapshot the store
      avmService.createSnapshot(userStoreName, null, null);
      
      // tag store with properties
      props = new HashMap<QName, PropertyValue>(6);
      // tag the store with the store type
      props.put(SandboxConstants.PROP_SANDBOX_AUTHOR_PREVIEW, new PropertyValue(DataTypeDefinition.TEXT, null));
      // tag the store with its own store name for querying.
      props.put(QName.createQName(null, SandboxConstants.PROP_SANDBOX_STORE_PREFIX + previewStoreName), new PropertyValue(DataTypeDefinition.TEXT, null));
      
      // tag all related stores to indicate that they are part of a single sandbox
      addSandboxGuid(sandboxGuid, props);
      // tag the store with the DNS name property
      addStoreDNSPath(previewStoreName, props, storeId, username, "preview");
      // The preview user store depends on the main user store (dist=1)
      addStoreBackgroundLayer(props, userStoreName, 1);
      // The preview user store depends on the main staging store (dist=2)
      addStoreBackgroundLayer(props, stagingStoreName, 2);
      
      // create the user 'preview' store
      avmService.createStore(previewStoreName, props);
      
      if (logger.isTraceEnabled())
      {
         logger.trace("Created user preview sandbox store: " + previewStoreName +
                      " above " + userStoreName);
      }
      
      // create a layered directory pointing to 'www' in the user 'main' store
      avmService.createLayeredDirectory(WCMUtil.buildStoreRootPath(userStoreName), 
                                        previewStoreName + ":/", 
                                        JNDIConstants.DIR_DEFAULT_WWW);
      dirRef = AVMNodeConverter.ToNodeRef(-1, WCMUtil.buildStoreRootPath(previewStoreName));
      
      // Apply access mask to the store (ACls are applied to the staging area)
      
      // apply the user role permissions to the sandbox
      permissionService.setPermission(dirRef.getStoreRef(), cms, PermissionService.WCM_CONTENT_MANAGER, true);
      permissionService.setPermission(dirRef.getStoreRef(), username, PermissionService.ALL_PERMISSIONS, true);
      permissionService.setPermission(dirRef.getStoreRef(), PermissionService.ALL_AUTHORITIES, PermissionService.READ, true);
      
      // snapshot the store
      avmService.createSnapshot(previewStoreName, null, null);
      
      if (logger.isTraceEnabled())
      {
         dumpStoreProperties(avmService, userStoreName);
         dumpStoreProperties(avmService, previewStoreName);
      }
      
      SandboxInfo sbInfo =  getSandbox(userStoreName);
      
      if (logger.isTraceEnabled())
      {
         logger.trace("createUserSandbox: " + sbInfo.getSandboxId() + " in "+(System.currentTimeMillis()-start)+" ms");
      }
      
      return sbInfo;
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
      long start = System.currentTimeMillis();
      
      String stagingStoreName = WCMUtil.buildStagingStoreName(storeId);
      
      // create the workflow 'main' store
      String packageName = WCMUtil.STORE_WORKFLOW + "-" + GUID.generate();
      String mainStoreName = WCMUtil.buildWorkflowMainStoreName(storeId, packageName);
      
      final String sandboxGuid = GUID.generate();
      
      // tag store with properties
      Map<QName, PropertyValue> props = new HashMap<QName, PropertyValue>(6);
      // tag the store with the store type
      props.put(SandboxConstants.PROP_SANDBOX_WORKFLOW_MAIN, new PropertyValue(DataTypeDefinition.TEXT, null));
      // tag the store with the base name of the website so that corresponding staging areas can be found.
      props.put(SandboxConstants.PROP_WEBSITE_NAME, new PropertyValue(DataTypeDefinition.TEXT, storeId));
      
      // tag the store, oddly enough, with its own store name for querying.
      addSandboxPrefix(mainStoreName, props);
      // tag all related stores to indicate that they are part of a single sandbox
      addSandboxGuid(sandboxGuid, props);
      // tag the store with the DNS name property
      addStoreDNSPath(mainStoreName, props, storeId, packageName);
      // The main workflow store depends on the main staging store (dist=1)
      addStoreBackgroundLayer(props, stagingStoreName, 1);
      
      avmService.createStore(mainStoreName, props);
      
      if (logger.isTraceEnabled())
      {
         logger.trace("Created workflow sandbox store: " + mainStoreName);
      }
      
      // create a layered directory pointing to 'www' in the staging area
      avmService.createLayeredDirectory(WCMUtil.buildStoreRootPath(stagingStoreName), 
                                        mainStoreName + ":/", 
                                        JNDIConstants.DIR_DEFAULT_WWW);
      
      // snapshot the store
      avmService.createSnapshot(mainStoreName, null, null);
      
      // create the workflow 'preview' store
      final String previewStoreName = WCMUtil.buildWorkflowPreviewStoreName(storeId, packageName);
      
      // tag store with properties
      props = new HashMap<QName, PropertyValue>(6);
      // tag the store with the store type
      props.put(SandboxConstants.PROP_SANDBOX_WORKFLOW_PREVIEW, new PropertyValue(DataTypeDefinition.TEXT, null));
      // tag the store with its own store name for querying.
      props.put(QName.createQName(null, SandboxConstants.PROP_SANDBOX_STORE_PREFIX + previewStoreName), new PropertyValue(DataTypeDefinition.TEXT, null));
      
      // tag all related stores to indicate that they are part of a single sandbox
      addSandboxGuid(sandboxGuid, props);
      
      // tag the store with the DNS name property
      addStoreDNSPath(previewStoreName, props, storeId, packageName, "preview");
      // The preview workflow store depends on the main workflow store (dist=1)
      addStoreBackgroundLayer(props, mainStoreName, 1);
      // The preview workflow store depends on the main staging store (dist=2)
      addStoreBackgroundLayer(props, stagingStoreName, 2);
      
      avmService.createStore(previewStoreName, props);
      
      if (logger.isTraceEnabled())
      {
         logger.trace("Created workflow sandbox preview store: " + previewStoreName);
      }
         
      // create a layered directory pointing to 'www' in the workflow 'main' store
      avmService.createLayeredDirectory(WCMUtil.buildStoreRootPath(mainStoreName), 
                                        previewStoreName + ":/", 
                                        JNDIConstants.DIR_DEFAULT_WWW);
      
      // snapshot the store
      avmService.createSnapshot(previewStoreName, null, null);
      
      if (logger.isTraceEnabled())
      {
         dumpStoreProperties(avmService, mainStoreName);
         dumpStoreProperties(avmService, previewStoreName);
      }
      
      SandboxInfo sbInfo =  getSandbox(mainStoreName);
      
      if (logger.isTraceEnabled())
      {
         logger.trace("createWorkflowSandbox: " + sbInfo.getSandboxId() + " in "+(System.currentTimeMillis()-start)+" ms");
      }
      
      return sbInfo;
   }
   
   /**
    * Create a read-only workflow sandbox for the named store.
    * 
    * Note: read-only means it's only safe to use in a workflow where the sandbox
    *       is not expected to be updated.  The sandbox does not protected itself
    *       from writes.
    * 
    * Note: this sandbox does not support the preview layer
    * Note: a snapshot within this sandbox is NOT taken
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
   public SandboxInfo createReadOnlyWorkflowSandbox(final String storeId)
   {
      long start = System.currentTimeMillis();
       
      String wpStoreId = WCMUtil.getWebProjectStoreId(storeId);
      String stagingStoreName = WCMUtil.buildStagingStoreName(storeId);
      
      // create the workflow 'main' store
      String packageName = WCMUtil.STORE_WORKFLOW + "-" + GUID.generate();
      String mainStoreName = WCMUtil.buildWorkflowMainStoreName(storeId, packageName);
      
      final String sandboxGuid = GUID.generate();
      
      // tag store with properties
      Map<QName, PropertyValue> props = new HashMap<QName, PropertyValue>(6);
      // tag the store with the store type
      props.put(SandboxConstants.PROP_SANDBOX_WORKFLOW_MAIN, new PropertyValue(DataTypeDefinition.TEXT, null));
      // tag the store with the base name of the website so that corresponding staging areas can be found.
      props.put(SandboxConstants.PROP_WEBSITE_NAME, new PropertyValue(DataTypeDefinition.TEXT, storeId));
      
      // tag the store, oddly enough, with its own store name for querying.
      addSandboxPrefix(mainStoreName, props);
      // tag all related stores to indicate that they are part of a single sandbox
      addSandboxGuid(sandboxGuid, props);
      // tag the store with the DNS name property
      addStoreDNSPath(mainStoreName, props, storeId, packageName);
      // The main workflow store depends on the main staging store (dist=1)
      addStoreBackgroundLayer(props, stagingStoreName, 1);
      
      avmService.createStore(mainStoreName, props);
      
      if (logger.isTraceEnabled())
      {
         logger.trace("Created read-only workflow sandbox store: " + mainStoreName);
      }
      
      // create a layered directory pointing to 'www' in the staging area
      avmService.createLayeredDirectory(WCMUtil.buildStoreRootPath(stagingStoreName), 
                                        mainStoreName + ":/", 
                                        JNDIConstants.DIR_DEFAULT_WWW);
      
      if (logger.isTraceEnabled())
      {
         dumpStoreProperties(avmService, mainStoreName);
      }
      
      SandboxInfo sbInfo = getSandbox(wpStoreId, mainStoreName, false); // no preview store
      
      if (logger.isTraceEnabled())
      {
         logger.trace("createReadOnlyWorkflowSandbox: " + sbInfo.getSandboxId() + " in "+(System.currentTimeMillis()-start)+" ms");
      }
      
      return sbInfo;
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
       long start = System.currentTimeMillis();
       
       // create the workflow 'main' store
       String packageName = "workflow-" + GUID.generate();
       String workflowStoreName = userStore + STORE_SEPARATOR + packageName;
       
       final String sandboxGuid = GUID.generate();
       
       // tag store with properties
       Map<QName, PropertyValue> props = new HashMap<QName, PropertyValue>(7);
       // tag the store with the store type
       props.put(SandboxConstants.PROP_SANDBOX_AUTHOR_WORKFLOW_MAIN, new PropertyValue(DataTypeDefinition.TEXT, null));
       // tag the store with the name of the author's store this one is layered over
       props.put(SandboxConstants.PROP_AUTHOR_NAME, new PropertyValue(DataTypeDefinition.TEXT, userStore));
       
       // tag the store, oddly enough, with its own store name for querying.
       addSandboxPrefix(workflowStoreName, props);
       // tag all related stores to indicate that they are part of a single sandbox
       addSandboxGuid(sandboxGuid, props);
       // tag the store with the DNS name property
       addStoreDNSPath(workflowStoreName, props, stagingStore, packageName);
       // the main workflow store depends on the main user store (dist=1)
       addStoreBackgroundLayer(props, userStore, 1);
       // The main workflow store depends on the main staging store (dist=2)
       addStoreBackgroundLayer(props, stagingStore, 2);
       
       avmService.createStore(workflowStoreName, props);
       
       if (logger.isTraceEnabled())
       {
           logger.trace("Created user workflow sandbox store: " + workflowStoreName);
       }
        
       // create a layered directory pointing to 'www' in the users store
       avmService.createLayeredDirectory(
                userStore + ":/" + JNDIConstants.DIR_DEFAULT_WWW, 
                workflowStoreName + ":/", JNDIConstants.DIR_DEFAULT_WWW);
        
       // snapshot the store
       avmService.createSnapshot(workflowStoreName, null, null);
        
       // create the workflow 'preview' store
       String previewStoreName = workflowStoreName + STORE_SEPARATOR + "preview";
       
       // tag store with properties
       props = new HashMap<QName, PropertyValue>(7);
       // tag the store with the store type
       props.put(SandboxConstants.PROP_SANDBOX_AUTHOR_WORKFLOW_PREVIEW, new PropertyValue(DataTypeDefinition.TEXT, null));
       // tag the store with its own store name for querying.
       props.put(QName.createQName(null, SandboxConstants.PROP_SANDBOX_STORE_PREFIX + previewStoreName), new PropertyValue(DataTypeDefinition.TEXT, null));
       
       // tag all related stores to indicate that they are part of a single sandbox
       addSandboxGuid(sandboxGuid, props);
       // tag the store with the DNS name property
       addStoreDNSPath(previewStoreName, props, userStore, packageName, "preview");
       // The preview worfkflow store depends on the main workflow store (dist=1)
       addStoreBackgroundLayer(props, workflowStoreName, 1);
       // The preview workflow store depends on the main user store (dist=2)
       addStoreBackgroundLayer(props, userStore, 2);
       // The preview workflow store depends on the main staging store (dist=3)
       addStoreBackgroundLayer(props, stagingStore, 3);
       
       avmService.createStore(previewStoreName, props);
       
       if (logger.isTraceEnabled())
       {
           logger.trace("Created user workflow sandbox preview store: " + previewStoreName);
       }
        
       // create a layered directory pointing to 'www' in the workflow 'main' store
       avmService.createLayeredDirectory(
                workflowStoreName + ":/" + JNDIConstants.DIR_DEFAULT_WWW, 
                previewStoreName + ":/", JNDIConstants.DIR_DEFAULT_WWW);
       
       // snapshot the store
       avmService.createSnapshot(previewStoreName, null, null);
       
       if (logger.isTraceEnabled())
       {
          logger.trace("createUserWorkflowSandbox: " + workflowStoreName + " in "+(System.currentTimeMillis()-start)+" ms");
       }
       
       // return the main workflow store name
       return workflowStoreName;
   }
   
   public List<SandboxInfo> listAllSandboxes(String wpStoreId)
   {
       return listAllSandboxes(wpStoreId, false, false);
   }
   
   public List<SandboxInfo> listAllSandboxes(String wpStoreId, boolean includeWorkflowSandboxes, boolean includeLocalhostDeployed)
   {
       long start = System.currentTimeMillis();
       
       List<AVMStoreDescriptor> stores = avmService.getStores();
       
       List<SandboxInfo> sbInfos = new ArrayList<SandboxInfo>();
       for (AVMStoreDescriptor store : stores)
       {
           String storeName = store.getName();
           
           // list main stores - not preview stores or workflow stores or locally deployed "live" ASR servers (LIVE or TEST)
           if ((WCMUtil.getWebProjectStoreId(storeName).equals(wpStoreId)) &&
               (! WCMUtil.isPreviewStore(storeName)) &&
               ((includeLocalhostDeployed || (! WCMUtil.isLocalhostDeployedStore(wpStoreId, storeName)))) &&
               ((includeWorkflowSandboxes || (! WCMUtil.isWorkflowStore(storeName))))
              )
           {
               sbInfos.add(getSandbox(wpStoreId, storeName, true));
           }
       }
       
       if (logger.isTraceEnabled())
       {
          logger.trace("listAllSandboxes: " + wpStoreId + "[" + sbInfos.size() + "] in "+(System.currentTimeMillis()-start)+" ms");
       }
       
       return sbInfos;
   }
   public void deleteSandbox(String sbStoreId)
   {
       deleteSandbox(sbStoreId, false);
   }
   
   public void deleteSandbox(String sbStoreId, boolean isSubmitDirectWorkflowSandbox)
   {
       deleteSandbox(getSandbox(WCMUtil.getWebProjectStoreId(sbStoreId), sbStoreId, true), isSubmitDirectWorkflowSandbox, true);
   }
   
   public void deleteSandbox(SandboxInfo sbInfo, boolean isSubmitDirectWorkflowSandbox, boolean removeLocks)
   {
       if (sbInfo != null)
       {
           long start = System.currentTimeMillis();
           
           String mainSandboxStore = sbInfo.getMainStoreName();
           String wpStoreId = WCMUtil.getWebProjectStoreId(mainSandboxStore);
           
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
            
            // optimization: direct submits no longer virtualize the workflow sandbox
            if (! isSubmitDirectWorkflowSandbox)
            {
                WCMUtil.removeAllVServerWebapps(virtServerRegistry, path, true);
            }
            
            // NOTE: Could use the .sandbox-id. GUID property to delete all sandboxes,
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
                
                if (removeLocks)
                {
                    Map<String, String> lockDataToMatch = Collections.singletonMap(WCMUtil.LOCK_KEY_STORE_NAME, avmStoreName);
                    avmLockingService.removeLocks(wpStoreId, lockDataToMatch);
                }
            }
            
            if (logger.isTraceEnabled())
            {
               logger.trace("deleteSandbox: " + mainSandboxStore + " in "+(System.currentTimeMillis()-start)+" ms");
            }
       }
   }
   
   /**
    * Update the permissions for the list of sandbox managers applied to a user sandbox.
    * <p>
    * Ensures that all managers in the list have full WRITE access to the specified user stores.
    * 
    * @param storeId
    *            The store id of the sandbox to update
    * @param managers
    *            The list of authorities who have ContentManager role in the web project
    */
   public void updateSandboxManagers(final String storeId, final List<String> managers)
   {
       String stagingStoreName = WCMUtil.buildStagingStoreName(storeId);

       updateStagingAreaManagers(stagingStoreName, managers);
   }
   
   /**
    * Update the permissions for the list of sandbox managers applied to a user sandbox.
    * <p>
    * Ensures that all managers in the list have full WRITE access to the specified user stores.
    * 
    * @param storeId
    *            The store id of the sandbox to update
    * @param managers
    *            The list of authorities who have ContentManager role in the web project
    */
   public void removeSandboxManagers(String storeId, List<String> managersToRemove)
   {
       removeStagingAreaManagers(storeId, managersToRemove);
   }
   
   /**
    * Removes the ContentManager role on staging area to ex-managers.
    * 
    * @param storeId            The store id of the sandbox to update
    * @param managersToRemove   The list of authorities who have had ContentManager role in the web project
    */
   private void removeStagingAreaManagers(String storeId, List<String> managersToRemove)
   {
       String storeName = WCMUtil.buildStagingStoreName(storeId);

       for (String remove : managersToRemove)
       {
           removeFromGroupIfRequired(storeName, remove, PermissionService.WCM_CONTENT_MANAGER);
       }
   }
   
   public void updateSandboxRoles(final String wpStoreId, List<UserRoleWrapper> usersToUpdate, Set<String> permissionsList)
   {
       // walk existing user sandboxes and remove manager permissions to exclude old managers
       List<SandboxInfo> sbInfos = listAllSandboxes(wpStoreId); // all sandboxes
       
       for (SandboxInfo sbInfo : sbInfos)
       {
           if (sbInfo.getSandboxType().equals(SandboxConstants.PROP_SANDBOX_AUTHOR_MAIN))
           {
               String username = sbInfo.getName();
               updateUserSandboxRole(wpStoreId, username ,usersToUpdate, permissionsList);
           }
       }
       
       updateStagingAreaRole(wpStoreId, usersToUpdate, permissionsList);
   }
   
   /**
    * Updates roles on the sandbox identified by username to users from usersToUpdate list.
    * 
    * @param storeId            The store id of the sandbox to update
    * @param username           Username of the user sandbox to update
    * @param usersToUpdate      The list of users who have role changes
    * @param permissionsList    List of permissions @see org.alfresco.web.bean.wcm.InviteWebsiteUsersWizard.getPermissionsForType(). It is not mandatory.
    */
   private void updateUserSandboxRole(String storeId, String username, List<UserRoleWrapper> usersToUpdate, Set<String> permissionsList)
   {
       final String storeName = WCMUtil.buildStagingStoreName(storeId);

       // If permissionsList is set remove all possible user permissions and set only necessary.
       // This will fix previous wrong role changes. (paranoid)
       // For little better performance just set permissionsList to null.
       // But in this case it removes only previous permission.
       if (permissionsList != null && permissionsList.size() != 0)
       {
           for (UserRoleWrapper user : usersToUpdate)
           {
               for (String permission : permissionsList)
               {
                   removeFromGroupIfRequired(storeName, user.getUserAuth(), permission);
               }

               addToGroupIfRequired(storeName, user.getUserAuth(), user.getNewRole());
           }
       }
       else
       {
           for (UserRoleWrapper user : usersToUpdate)
           {
               removeFromGroupIfRequired(storeName, user.getUserAuth(), user.getOldRole());
               addToGroupIfRequired(storeName, user.getUserAuth(), user.getNewRole());
           }
       }
   }
   
   /**
    * Updates roles on staging sandbox to users from usersToUpdate list.
    * 
    * @param storeId            The store id of the sandbox to update
    * @param usersToUpdate      The list of users who have role changes
    * @param permissionsList    List of permissions @see org.alfresco.web.bean.wcm.InviteWebsiteUsersWizard.getPermissionsForType(). It is not mandatory.
    */
   private void updateStagingAreaRole(String storeId, List<UserRoleWrapper> usersToUpdate, Set<String> permissionsList)
   {
       final String storeName = WCMUtil.buildStagingStoreName(storeId);

       // If permissionsList is set remove all possible user permissions and set only necessary.
       // This will fix previous wrong role changes. (paranoid)
       // For little better performance just set permissionsList to null.
       // But in this case it removes only previous permission.
       if (permissionsList != null && permissionsList.size() != 0)
       {
           for (UserRoleWrapper user : usersToUpdate)
           {
               for (String permission : permissionsList)
               {
                   removeFromGroupIfRequired(storeName, user.getUserAuth(), permission);
               }

               addToGroupIfRequired(storeName, user.getUserAuth(), user.getNewRole());
           }
       }
       else
       {
           for (UserRoleWrapper user : usersToUpdate)
           {
               removeFromGroupIfRequired(storeName, user.getUserAuth(), user.getOldRole());
               addToGroupIfRequired(storeName, user.getUserAuth(), user.getNewRole());
           }
       }
   }
   
   /**
    * Tag a named store with a DNS path meta-data attribute.
    * The DNS meta-data attribute is set to the system path 'store:/www/avm_webapps'
    * 
    * @param store  Name of the store to tag
    */
   private static void addStoreDNSPath(String store, Map<QName, PropertyValue> props, String... components)
   {
      String path = WCMUtil.buildSandboxRootPath(store);
      // DNS name mangle the property name - can only contain value DNS characters!
      String dnsName = DNSNameMangler.MakeDNSName(components);
      String dnsProp = SandboxConstants.PROP_DNS + dnsName;
      props.put(QName.createQName(null, dnsProp), new PropertyValue(DataTypeDefinition.TEXT, path));
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
   private static void addStoreBackgroundLayer(Map<QName, PropertyValue> props,
                                               String      backgroundStore, 
                                               int         distance)
   {
      String prop_key = SandboxConstants.PROP_BACKGROUND_LAYER + backgroundStore;
      props.put(QName.createQName(null, prop_key), new PropertyValue(DataTypeDefinition.INT, distance));
   }
   
   private static void addSandboxGuid(String sandboxGuid, Map<QName, PropertyValue> props)
   {
      final QName sandboxIdProp = QName.createQName(SandboxConstants.PROP_SANDBOXID + sandboxGuid);
      props.put(sandboxIdProp, new PropertyValue(DataTypeDefinition.TEXT, null));
   }
   
   private static void addSandboxPrefix(String storeName, Map<QName, PropertyValue> props)
   {
      props.put(QName.createQName(null, SandboxConstants.PROP_SANDBOX_STORE_PREFIX + storeName), new PropertyValue(DataTypeDefinition.TEXT, null));
   }


   /**
    * Debug helper method to dump the properties of a store
    *  
    * @param store   Store name to dump properties for
    */
   private static void dumpStoreProperties(AVMService avmService, String store)
   {
      logger.trace("Store " + store);
      Map<QName, PropertyValue> props = avmService.getStoreProperties(store);
      for (QName name : props.keySet())
      {
         logger.trace("   " + name + ": " + props.get(name));
      }
   }
   
   public class UserRoleWrapper
   {
       private String newRole;
       private String oldRole;
       private String userAuth;

       public UserRoleWrapper(String userAuth, String oldRole, String newRole)
       {
           this.userAuth = userAuth;
           this.oldRole = oldRole;
           this.newRole = newRole;
       }
       
       public String getNewRole()
       {
           return newRole;
       }
       public void setNewRole(String newRole)
       {
           this.newRole = newRole;
       }
       public String getOldRole()
       {
           return oldRole;
       }
       public void setOldRole(String oldRole)
       {
           this.oldRole = oldRole;
       }
       public String getUserAuth()
       {
           return userAuth;
       }
       public void setUserAuth(String userAuth)
       {
           this.userAuth = userAuth;
       }
   }
   
   public void removeGroupsForStore(final String storeRoot)
   {
       AuthenticationUtil.runAs(new RunAsWork<Void>()
       {

           public Void doWork() throws Exception
           {
               String[] permissions = new String[] { PermissionService.WCM_CONTENT_CONTRIBUTOR, PermissionService.WCM_CONTENT_MANAGER, PermissionService.WCM_CONTENT_PUBLISHER,
                       PermissionService.WCM_CONTENT_REVIEWER };
               for (String permission : permissions)
               {
                   String shortName = storeRoot + "-" + permission;
                   String group = authorityService.getName(AuthorityType.GROUP, shortName);
                   if (authorityService.authorityExists(group))
                   {
                       authorityService.deleteAuthority(group);
                   }
               }
               return null;
           }
       }, AuthenticationUtil.getSystemUserName());

   }
}
