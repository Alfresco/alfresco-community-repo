/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.security;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementService;
import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService;
import org.alfresco.module.org_alfresco_module_rm.capability.RMEntryVoter;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Records management permission service implementation
 * 
 * @author Roy Wetherall
 */
public class RecordsManagementSecurityServiceImpl implements RecordsManagementSecurityService, 
                                                             RecordsManagementModel,
                                                             ApplicationContextAware,
                                                             NodeServicePolicies.OnMoveNodePolicy
                                                             
{
    /** Capability service */
    private CapabilityService capabilityService;
    
    /** Authority service */
    private AuthorityService authorityService;
    
    /** Permission service */
    private PermissionService permissionService;
    
    /** Policy component */
    private PolicyComponent policyComponent;
    
    /** Records management service */
    private RecordsManagementService recordsManagementService;
    
    /** Node service */
    private NodeService nodeService;
    
    /** RM Entry voter */
    private RMEntryVoter voter;
    
    /** Records management role zone */
    public static final String RM_ROLE_ZONE_PREFIX = "rmRoleZone";
    
    /** Unfiled record container name */
    private static final String NAME_UNFILED_CONTAINER = "Unfiled Records";
    
    /** Logger */
    private static Log logger = LogFactory.getLog(RecordsManagementSecurityServiceImpl.class);
    
    /** Application context */
    private ApplicationContext applicationContext;
    
    /**
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;
    }
    
    /**
     * Set the capability service
     * 
     * @param capabilityService
     */
    public void setCapabilityService(CapabilityService capabilityService)
    {
        this.capabilityService = capabilityService;
    }
    
    /**
     * Set the authortiy service
     * 
     * @param authorityService
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }
    
    /**
     * Set the permission service
     * 
     * @param permissionService
     */
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }  
    
    /**
     * Set the policy component
     * 
     * @param policyComponent
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }
    
    /**
     * Set records management service
     * 
     * @param recordsManagementService  records management service
     */
    public void setRecordsManagementService(RecordsManagementService recordsManagementService)
    {
        this.recordsManagementService = recordsManagementService;
    }
    
    /**
     * Set the node service
     * 
     * @param nodeService
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * Set the RM voter
     * 
     * @param voter
     */
    public void setVoter(RMEntryVoter voter)
    {
        this.voter = voter;
    }
        
    /**
     * Initialisation method
     */
    public void init()
    {
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnCreateNodePolicy.QNAME, 
                TYPE_FILE_PLAN, 
                new JavaBehaviour(this, "onCreateRootNode", NotificationFrequency.TRANSACTION_COMMIT));
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnDeleteNodePolicy.QNAME, 
                TYPE_FILE_PLAN, 
                new JavaBehaviour(this, "onDeleteRootNode", NotificationFrequency.TRANSACTION_COMMIT));
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnCreateNodePolicy.QNAME, 
                TYPE_RECORD_CATEGORY, 
                new JavaBehaviour(this, "onCreateRMContainer", NotificationFrequency.TRANSACTION_COMMIT));
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnCreateNodePolicy.QNAME,  
                TYPE_RECORD_FOLDER, 
                new JavaBehaviour(this, "onCreateRecordFolder", NotificationFrequency.TRANSACTION_COMMIT));    
        
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnMoveNodePolicy.QNAME, 
                ASPECT_RECORD, 
                new JavaBehaviour(this, "onMoveNode", NotificationFrequency.TRANSACTION_COMMIT));
    }
    
    /**
     * Create root node behaviour
     * 
     * @param childAssocRef
     */
    public void onCreateRootNode(ChildAssociationRef childAssocRef)
    {       
        final NodeRef rmRootNode = childAssocRef.getChildRef();
        
        // Do not execute behaviour if this has been created in the archive store
        if(rmRootNode.getStoreRef().equals(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE) == true)
        {
            // This is not the spaces store - probably the archive store
            return;
        }
        
        if (nodeService.exists(rmRootNode) == true)
        {
            NodeRef unfiledContainer = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<NodeRef>()
            {
                public NodeRef doWork()
                {
                    // Create "all" role group for root node
                    String allRoles = authorityService.createAuthority(AuthorityType.GROUP, getAllRolesGroupShortName(rmRootNode), "All Roles", null);                    
                    
                    // Set the permissions
                    permissionService.setInheritParentPermissions(rmRootNode, false);
                    permissionService.setPermission(rmRootNode, allRoles, RMPermissionModel.READ_RECORDS, true);
                    permissionService.setPermission(rmRootNode, ExtendedReaderDynamicAuthority.EXTENDED_READER, RMPermissionModel.READ_RECORDS, true);
                    permissionService.setPermission(rmRootNode, ExtendedReaderDynamicAuthority.EXTENDED_READER, RMPermissionModel.VIEW_RECORDS, true);
                    
                    // Create the unfiled record container
                    return createUnfiledContainer(rmRootNode, allRoles);
                }
            }, AuthenticationUtil.getSystemUserName());
                        
            // Bootstrap in the default set of roles for the newly created root node
            bootstrapDefaultRoles(rmRootNode, unfiledContainer);
        }
    }
    
    /**
     * Creates unfiled container node and sets up permissions
     * 
     * @param rmRootNode
     * @param allRoles
     */
    private NodeRef createUnfiledContainer(NodeRef rmRootNode, String allRoles)
    {
        // create the properties map
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(1);
        properties.put(ContentModel.PROP_NAME, NAME_UNFILED_CONTAINER);
        
        // create the unfiled container
        NodeRef container = nodeService.createNode(
                        rmRootNode, 
                        ASSOC_UNFILED_RECORDS, 
                        QName.createQName(RM_URI, NAME_UNFILED_CONTAINER), 
                        TYPE_UNFILED_RECORD_CONTAINER,
                        properties).getChildRef();
        
        // set inheritance to false
        permissionService.setInheritParentPermissions(container, false);
        permissionService.setPermission(container, allRoles, RMPermissionModel.READ_RECORDS, true);
        permissionService.setPermission(container, ExtendedReaderDynamicAuthority.EXTENDED_READER, RMPermissionModel.READ_RECORDS, true);
        
        return container;
    }
    
    /**
     * Delete root node behaviour
     * 
     * @param childAssocRef
     */
    public void onDeleteRootNode(ChildAssociationRef childAssocRef, boolean isNodeArchived)
    {
        logger.debug("onDeleteRootNode called");
        
        // get the deleted node
        final NodeRef rmRootNode = childAssocRef.getChildRef();
        
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
        {
            public Object doWork()
            {
                // cascade delete the 'all' roles group for the site
                String allRolesGroup = authorityService.getName(AuthorityType.GROUP, getAllRolesGroupShortName(rmRootNode));                                   
                Set<String> groups = authorityService.getContainedAuthorities(AuthorityType.GROUP, allRolesGroup, true);
                for (String group : groups)
                {
                    authorityService.deleteAuthority(group);
                }
                
                authorityService.deleteAuthority(allRolesGroup, false);
                
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());    
    }
    
    /**
     * Get all the roles by short name
     * 
     * @param rmRootNode
     * @return
     */
    private String getAllRolesGroupShortName(NodeRef rmRootNode)
    {
        return "AllRoles" + rmRootNode.getId();
    }
    
    /**
     * @param childAssocRef
     */
    public void onCreateRMContainer(ChildAssociationRef childAssocRef)
    {
        setUpPermissions(childAssocRef.getChildRef());
    }
    
    /**
     * @param childAssocRef
     */
    public void onCreateRecordFolder(ChildAssociationRef childAssocRef)
    {
      final NodeRef folderNodeRef = childAssocRef.getChildRef();
        setUpPermissions(folderNodeRef);
        
        // Pull any permissions found on the parent (ie the record category)
        final NodeRef catNodeRef = childAssocRef.getParentRef();
        if (nodeService.exists(catNodeRef) == true)
        {
         AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
            {
                public Object doWork()
                {
                  Set<AccessPermission> perms = permissionService.getAllSetPermissions(catNodeRef);
                  for (AccessPermission perm : perms) 
                  {
                      if (ExtendedReaderDynamicAuthority.EXTENDED_READER.equals(perm.getAuthority()) == false)
                      {
                        AccessStatus accessStatus = perm.getAccessStatus();
                        boolean allow = false;
                        if (AccessStatus.ALLOWED.equals(accessStatus) == true)
                        {
                           allow = true;
                        }
                        permissionService.setPermission(
                              folderNodeRef, 
                              perm.getAuthority(), 
                              perm.getPermission(), 
                              allow);
                      }
               }
                  
                    return null;
                }
            }, AuthenticationUtil.getSystemUserName());  
        }
    }
    
    /**
     * 
     * @param nodeRef
     */
    public void setUpPermissions(final NodeRef nodeRef)
    {
        if (nodeService.exists(nodeRef) == true)
        {        
            AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
            {
                public Object doWork()
                {
                    // break inheritance 
                    permissionService.setInheritParentPermissions(nodeRef, false);
                    
                    // set extended reader permissions    
                    permissionService.setPermission(nodeRef, ExtendedReaderDynamicAuthority.EXTENDED_READER, RMPermissionModel.READ_RECORDS, true);
                                    
                    return null;
                }
            }, AuthenticationUtil.getSystemUserName());         
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService#getProtectedAspects()
     */
    public Set<QName> getProtectedAspects()
    {
        return voter.getProtetcedAscpects();
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService#getProtectedProperties()
     */
    public Set<QName> getProtectedProperties()
    {
       return voter.getProtectedProperties();
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService#bootstrapDefaultRoles(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void bootstrapDefaultRoles(NodeRef rmRootNode)
    {
        bootstrapDefaultRoles(rmRootNode, null);
    }
    
    private void bootstrapDefaultRoles(final NodeRef rmRootNode, final NodeRef unfiledContainer)
    {
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
        {
            public Object doWork()
            {
                try
                {
                    JSONArray array = null;
                    try
                    {
                        // Load up the default roles from JSON
                        InputStream is = getClass().getClassLoader().getResourceAsStream("alfresco/module/org_alfresco_module_rm/security/rm-default-roles-bootstrap.json");
                        if  (is == null)
                        {
                            throw new AlfrescoRuntimeException("Could not load default bootstrap roles configuration");
                        }
                        array = new JSONArray(convertStreamToString(is));
                    }
                    catch (IOException ioe)
                    {
                        throw new AlfrescoRuntimeException("Unable to load rm-default-roles-bootstrap.json configuration file.", ioe);
                    }
                    
                    // Add each role to the rm root node
                    for (int i = 0; i < array.length(); i++)
                    {
                        JSONObject object = array.getJSONObject(i);
                        
                        // Get the name of the role
                        String name = null;
                        if (object.has("name") == true)
                        {
                            name = object.getString("name");
                            if (existsRole(rmRootNode, name) == true)
                            {
                                throw new AlfrescoRuntimeException("The bootstrap role " + name + " already exists on the rm root node " + rmRootNode.toString());
                            }
                        }
                        else
                        {
                            throw new AlfrescoRuntimeException("No name given to default bootstrap role.  Check json configuration file.");
                        }
                        
                                                
                        // Get the role's display label
                        String displayLabel = name;
                        if (object.has("displayLabel") == true)
                        {
                            displayLabel = object.getString("displayLabel");
                        }
                        
                        // Determine whether the role is an admin role or not
                        boolean isAdmin = false;
                        if (object.has("isAdmin") == true)
                        {
                            isAdmin = object.getBoolean("isAdmin");
                        }
                                                
                        // Get the roles capabilities
                        Set<Capability> capabilities = new HashSet<Capability>(30);
                        if (object.has("capabilities") == true)
                        {
                            JSONArray arrCaps = object.getJSONArray("capabilities");
                            for (int index = 0; index < arrCaps.length(); index++)
                            {
                                String capName = arrCaps.getString(index);
                                Capability capability = capabilityService.getCapability(capName);
                                if (capability == null)
                                {
                                    throw new AlfrescoRuntimeException("The capability '" + capName + "' configured for the deafult boostrap role '" + name + "' is invalid.");
                                }
                                capabilities.add(capability);
                            }
                        }
                        
                        // Create the role
                        Role role = createRole(rmRootNode, name, displayLabel, capabilities);
                        
                        // Add any additional admin permissions
                        if (isAdmin == true)
                        {
                            // Admin has filing
                            permissionService.setPermission(rmRootNode, role.getRoleGroupName(), RMPermissionModel.FILING, true);
                            if (unfiledContainer != null)
                            {
                                permissionService.setPermission(unfiledContainer, role.getRoleGroupName(), RMPermissionModel.FILING, true);
                            }
                            
                            // Add the creating user to the administration group
                            String user = AuthenticationUtil.getFullyAuthenticatedUser();
                            authorityService.addAuthority(role.getRoleGroupName(), user);
                        }
                    }
                }
                catch (JSONException exception)
                {
                    throw new AlfrescoRuntimeException("Error loading json configuration file rm-default-roles-bootstrap.json", exception);
                }
                
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }
    
    public String convertStreamToString(InputStream is) throws IOException
    {
        /*
        * To convert the InputStream to String we use the BufferedReader.readLine()
        * method. We iterate until the BufferedReader return null which means
        * there's no more data to read. Each line will appended to a StringBuilder
        * and returned as String.
        */
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
         
        String line = null;
        try 
        {
            while ((line = reader.readLine()) != null) 
            {
                sb.append(line + "\n");
            }
        }
        finally 
        {
            try {is.close();} catch (IOException e) {}
        }
         
        return sb.toString();
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService#getRoles()
     */
    public Set<Role> getRoles(final NodeRef rmRootNode)
    {  
        return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Set<Role>>()
        {
            public Set<Role> doWork() throws Exception
            {
                Set<Role> result = new HashSet<Role>(13);
                
                Set<String> roleAuthorities = authorityService.getAllAuthoritiesInZone(getZoneName(rmRootNode), AuthorityType.GROUP);        
                for (String roleAuthority : roleAuthorities)
                {
                    String name = getShortRoleName(authorityService.getShortName(roleAuthority), rmRootNode);
                    String displayLabel = authorityService.getAuthorityDisplayName(roleAuthority);
                    Set<String> capabilities = getCapabilitiesImpl(rmRootNode, roleAuthority);
                    
                    Role role = new Role(name, displayLabel, capabilities, roleAuthority);
                    result.add(role);            
                }
                
                return result;
            }
        }, AuthenticationUtil.getSystemUserName());
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService#getRolesByUser(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public Set<Role> getRolesByUser(final NodeRef rmRootNode, final String user)
    {
        return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Set<Role>>()
        {
            public Set<Role> doWork() throws Exception
            {
                Set<Role> result = new HashSet<Role>(13);
                
                Set<String> roleAuthorities = authorityService.getAllAuthoritiesInZone(getZoneName(rmRootNode), AuthorityType.GROUP);        
                for (String roleAuthority : roleAuthorities)
                {
                    Set<String> users = authorityService.getContainedAuthorities(AuthorityType.USER, roleAuthority, false);
                    if (users.contains(user) == true)
                    {                    
                        String name = getShortRoleName(authorityService.getShortName(roleAuthority), rmRootNode);
                        String displayLabel = authorityService.getAuthorityDisplayName(roleAuthority);
                        Set<String> capabilities = getCapabilitiesImpl(rmRootNode, roleAuthority);
                        
                        Role role = new Role(name, displayLabel, capabilities, roleAuthority);
                        result.add(role);  
                    }
                }
                
                return result;
            }
        }, AuthenticationUtil.getSystemUserName());
    }
    
    /**
     * 
     * @param rmRootNode
     * @return
     */
    private String getZoneName(NodeRef rmRootNode)
    {
        return RM_ROLE_ZONE_PREFIX + rmRootNode.getId();
    }
    
    /**
     * Get the full role name
     * 
     * @param role
     * @param rmRootNode
     * @return
     */
    private String getFullRoleName(String role, NodeRef rmRootNode)
    {
        return role + rmRootNode.getId();
    }
    
    /**
     * Get the short role name
     * 
     * @param fullRoleName
     * @param rmRootNode
     * @return
     */
    private String getShortRoleName(String fullRoleName, NodeRef rmRootNode)
    {
        return fullRoleName.replaceAll(rmRootNode.getId(), "");
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService#getRole(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public Role getRole(final NodeRef rmRootNode, final String role)
    {
        return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Role>()
        {
            public Role doWork() throws Exception
            {                
                Role result = null;
                
                String roleAuthority = authorityService.getName(AuthorityType.GROUP, getFullRoleName(role, rmRootNode));
                if (authorityService.authorityExists(roleAuthority) == true)
                {
                    String name = getShortRoleName(authorityService.getShortName(roleAuthority), rmRootNode);
                    String displayLabel = authorityService.getAuthorityDisplayName(roleAuthority);                
                    Set<String> capabilities = getCapabilitiesImpl(rmRootNode, roleAuthority);
                    
                    result = new Role(name, displayLabel, capabilities, roleAuthority);
                }
                
                return result;
            }
        }, AuthenticationUtil.getSystemUserName());
    }
    
    /**
     * 
     * @param rmRootNode
     * @param roleAuthority
     * @return
     */
    private Set<String> getCapabilitiesImpl(NodeRef rmRootNode, String roleAuthority)
    {
        Set<AccessPermission> permissions = permissionService.getAllSetPermissions(rmRootNode);
        Set<String> capabilities = new HashSet<String>(52);
        for (AccessPermission permission : permissions)

        {
            if (permission.getAuthority().equals(roleAuthority) == true)
            {
                String capabilityName = permission.getPermission();
                if (capabilityService.getCapability(capabilityName) != null)
                {
                    capabilities.add(permission.getPermission());
                }
            }

        }

        return capabilities;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService#existsRole(java.lang.String)
     */
    public boolean existsRole(final NodeRef rmRootNode, final String role)
    {
        return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Boolean>()
        {
            public Boolean doWork() throws Exception
            {                                
                String fullRoleName = authorityService.getName(AuthorityType.GROUP, getFullRoleName(role, rmRootNode));
            
                String zone = getZoneName(rmRootNode);
                Set<String> roles = authorityService.getAllAuthoritiesInZone(zone, AuthorityType.GROUP);
                return new Boolean(roles.contains(fullRoleName));
            }
        }, AuthenticationUtil.getSystemUserName()).booleanValue();
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService#hasRMAdminRole(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public boolean hasRMAdminRole(NodeRef rmRootNode, String user)
    {
        boolean isRMAdmin = false;
        
        Set<Role> userRoles = this.getRolesByUser(rmRootNode, user);
        if (userRoles != null)
        {
            for (Role role : userRoles)
            {
                if (role.getName().equals("Administrator"))
                {
                    isRMAdmin = true;
                    break;
                }
            }
        }
        
        return isRMAdmin;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService#createRole(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, java.lang.String, java.util.Set)
     */
    public Role createRole(final NodeRef rmRootNode, final String role, final String roleDisplayLabel, final Set<Capability> capabilities)
    {
        return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Role>()
        {
            public Role doWork() throws Exception
            {
                String fullRoleName = getFullRoleName(role, rmRootNode);
                
                // Check that the role does not already exist for the rm root node
                if (authorityService.authorityExists(authorityService.getName(AuthorityType.GROUP, fullRoleName)))
                {
                    throw new AlfrescoRuntimeException("The role " + role + " already exists for root rm node " + rmRootNode.getId());
                }
                
                // Create a group that relates to the records management role
                Set<String> zones = new HashSet<String>(2);
                zones.add(getZoneName(rmRootNode));
                zones.add(AuthorityService.ZONE_APP_DEFAULT);
                String roleGroup = authorityService.createAuthority(AuthorityType.GROUP, fullRoleName, roleDisplayLabel, zones);
                
                // Add the roleGroup to the "all" role group
                String allRoleGroup = authorityService.getName(AuthorityType.GROUP, getAllRolesGroupShortName(rmRootNode));
                authorityService.addAuthority(allRoleGroup, roleGroup);
                
                // Assign the various capabilities to the group on the root records management node
                Set<String> capStrings = new HashSet<String>(53);
                if (capabilities != null)
                {
                    for (Capability capability : capabilities)
                    {
                        permissionService.setPermission(rmRootNode, roleGroup, capability.getName(), true);
                    }
                    
                    // Create the role
                    for (Capability capability : capabilities)
                    {
                        capStrings.add(capability.getName());
                    }
                }
                
                return new Role(role, roleDisplayLabel, capStrings, roleGroup);
            }
        }, AuthenticationUtil.getSystemUserName());
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService#updateRole(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, java.lang.String, java.util.Set)
     */
    public Role updateRole(final NodeRef rmRootNode, final String role, final String roleDisplayLabel, final Set<Capability> capabilities)
    {
        return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Role>()
        {
            public Role doWork() throws Exception
            {                                
                String roleAuthority = authorityService.getName(AuthorityType.GROUP, getFullRoleName(role, rmRootNode));
                
                // Reset the role display name
                authorityService.setAuthorityDisplayName(roleAuthority, roleDisplayLabel);

                // TODO this needs to be improved, removing all and readding is not ideal
                
                // Clear the current capabilities
                permissionService.clearPermission(rmRootNode, roleAuthority);
                
                // Re-add the provided capabilities
                for (Capability capability : capabilities)
                {
                    permissionService.setPermission(rmRootNode, roleAuthority, capability.getName(), true);
                }
                
                Set<String> capStrings = new HashSet<String>(capabilities.size());
                for (Capability capability : capabilities)
                {
                    capStrings.add(capability.getName());
                }
                return new Role(role, roleDisplayLabel, capStrings, roleAuthority);
                
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService#deleteRole(java.lang.String)
     */
    public void deleteRole(final NodeRef rmRootNode, final String role)
    {
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
        {
            public Boolean doWork() throws Exception
            {                                
                String roleAuthority = authorityService.getName(AuthorityType.GROUP, getFullRoleName(role, rmRootNode));
                authorityService.deleteAuthority(roleAuthority);                
                return null;
                
            }
        }, AuthenticationUtil.getSystemUserName());
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService#assignRoleToAuthority(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, java.lang.String)
     */
    public void assignRoleToAuthority(final NodeRef rmRootNode, final String role, final String authorityName)
    {
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
        {
            public Boolean doWork() throws Exception
            {                                
                String roleAuthority = authorityService.getName(AuthorityType.GROUP, getFullRoleName(role, rmRootNode));
                authorityService.addAuthority(roleAuthority, authorityName);             
                return null;
                
            }
        }, AuthenticationUtil.getSystemUserName());
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService#setPermission(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, java.lang.String, boolean)
     */
    public void setPermission(final NodeRef nodeRef, final String authority, final String permission)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);
        ParameterCheck.mandatory("authority", authority);
        ParameterCheck.mandatory("permission", permission);
        
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
        {
            public Boolean doWork() throws Exception
            { 
                if (recordsManagementService.isFilePlan(nodeRef) == false &&
                    recordsManagementService.isRecordsManagementContainer(nodeRef) == true)
                {
                    setReadPermissionUp(nodeRef, authority);
                    setPermissionDown(nodeRef, authority, permission);
                }
                else if (recordsManagementService.isRecordFolder(nodeRef) == true)
                {
                    setReadPermissionUp(nodeRef, authority);
                    setPermissionImpl(nodeRef, authority, permission);
                }
                else
                {
                    if (logger.isWarnEnabled() == true)
                    {
                        logger.warn("Setting permissions for this node is not supported.  (nodeRef=" + nodeRef + ", authority=" + authority + ", permission=" + permission + ")");
                    }
                }
                
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }
    
    /**
     * Helper method to set the read permission up the hierarchy
     * 
     * @param nodeRef
     * @param authority
     */
    private void setReadPermissionUp(NodeRef nodeRef, String authority)
    {
        NodeRef parent = nodeService.getPrimaryParent(nodeRef).getParentRef();
        if (parent != null &&
            recordsManagementService.isFilePlan(parent) == false)
        {
            setPermissionImpl(parent, authority, RMPermissionModel.READ_RECORDS);
            setReadPermissionUp(parent, authority);
        }
    }
    
    /**
     * Helper method to set the permission down the hierarchy
     * 
     * @param nodeRef
     * @param authority
     * @param permission
     */
    private void setPermissionDown(NodeRef nodeRef, String authority, String permission)
    {
        setPermissionImpl(nodeRef, authority, permission);
        if (recordsManagementService.isRecordsManagementContainer(nodeRef) == true)
        {
            List<ChildAssociationRef> assocs = nodeService.getChildAssocs(nodeRef, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
            for (ChildAssociationRef assoc : assocs)
            {
                NodeRef child = assoc.getChildRef();
                if (recordsManagementService.isRecordsManagementContainer(child) == true ||
                    recordsManagementService.isRecordFolder(child) == true)
                {
                    setPermissionDown(child, authority, permission);
                }
            }
        }
    }
    
    /**
     * Set the permission, taking into account that filing is a superset of read
     * 
     * @param nodeRef
     * @param authority
     * @param permission
     */
    private void setPermissionImpl(NodeRef nodeRef, String authority, String permission)
    {
        if (RMPermissionModel.FILING.equals(permission) == true)
        {
            // Remove record read permission before adding filing permission
            permissionService.deletePermission(nodeRef, authority, RMPermissionModel.READ_RECORDS);
        }
        
        permissionService.setPermission(nodeRef, authority, permission, true);
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService#deletePermission(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, java.lang.String)
     */
    public void deletePermission(final NodeRef nodeRef, final String authority, final String permission)
    {
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
        {
            public Boolean doWork() throws Exception
            { 
                // Delete permission on this node
                permissionService.deletePermission(nodeRef, authority, permission);
                
                if (recordsManagementService.isRecordsManagementContainer(nodeRef) == true)
                {
                    List<ChildAssociationRef> assocs = nodeService.getChildAssocs(nodeRef, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
                    for (ChildAssociationRef assoc : assocs)
                    {
                        NodeRef child = assoc.getChildRef();
                        if (recordsManagementService.isRecordsManagementContainer(child) == true ||
                            recordsManagementService.isRecordFolder(child) == true)
                        {
                            deletePermission(child, authority, permission);
                        }
                    }
                }
                
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());        
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService#hasExtendedReaders(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public boolean hasExtendedReaders(NodeRef nodeRef)
    {
        boolean result = false;
        Set<String> extendedReaders = getExtendedReaders(nodeRef);
        if (extendedReaders != null && extendedReaders.size() != 0)
        {
            result = true;
        }
        return result;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService#getExtendedReaders(org.alfresco.service.cmr.repository.NodeRef)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Set<String> getExtendedReaders(NodeRef nodeRef)
    {
        NodeService nodeService = (NodeService)applicationContext.getBean("nodeService");
        Set<String> result = null;
        
        Map<String, Integer> readerMap = (Map<String, Integer>)nodeService.getProperty(nodeRef, PROP_READERS);
        if (readerMap != null)
        {
            result = readerMap.keySet();
        }
        
        return result;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService#setExtendedReaders(org.alfresco.service.cmr.repository.NodeRef, java.util.Set)
     */
    @Override
    public void setExtendedReaders(NodeRef nodeRef, Set<String> readers)
    {
        setExtendedReaders(nodeRef, readers, true);
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService#setExtendedReaders(org.alfresco.service.cmr.repository.NodeRef, java.util.Set, boolean)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void setExtendedReaders(NodeRef nodeRef, java.util.Set<String> readers, boolean applyToParents)
    {        
        ParameterCheck.mandatory("nodeRef", nodeRef);
        ParameterCheck.mandatory("readers", readers);
        
        NodeService nodeService = (NodeService)applicationContext.getBean("nodeService");
        RecordsManagementService recordsManagementService = (RecordsManagementService)applicationContext.getBean("recordsManagementService");
        
        if (nodeRef != null && 
            readers.isEmpty() == false)
        {
            // add the aspect if missing
            if (nodeService.hasAspect(nodeRef, ASPECT_EXTENDED_READERS) == false)
            {
                nodeService.addAspect(nodeRef, ASPECT_EXTENDED_READERS, null);
            }
            
            // get reader map
            Map<String, Integer> readersMap = (Map<String, Integer>)nodeService.getProperty(nodeRef, PROP_READERS);
            if (readersMap == null)
            {
                // create reader map
                readersMap = new HashMap<String, Integer>(7);
            }
            
            for (String reader : readers)
            {
                if (readersMap.containsKey(reader) == true)
                {
                    // increment reference count
                    Integer count = readersMap.get(reader);
                    readersMap.put(reader, Integer.valueOf(count.intValue()+1));
                }
                else
                {
                    // add reader with initial count
                    readersMap.put(reader, Integer.valueOf(1));
                }
            }
            
            // set the readers property (this will in turn apply the aspect if required)
            nodeService.setProperty(nodeRef, PROP_READERS, (Serializable)readersMap);
            
            // apply the readers to any renditions of the content
            if (recordsManagementService.isRecord(nodeRef) == true)
            {
                List<ChildAssociationRef> assocs = nodeService.getChildAssocs(nodeRef, RenditionModel.ASSOC_RENDITION, RegexQNamePattern.MATCH_ALL);
                for (ChildAssociationRef assoc : assocs)
                {
                    NodeRef child = assoc.getChildRef();
                    setExtendedReaders(child, readers, false);
                }
            }
            
            if  (applyToParents == true)
            {            
                // apply the extended readers up the file plan primary hierarchy
                NodeRef parent = nodeService.getPrimaryParent(nodeRef).getParentRef();
                if (parent != null &&
                    recordsManagementService.isFilePlanComponent(parent) == true)
                {
                    setExtendedReaders(parent, readers);
                }
            }
        }
    }
    
    @Override
    public void removeExtendedReaders(NodeRef nodeRef, Set<String> readers)
    {
        // TODO Auto-generated method stub
        
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService#removeAllExtendedReaders(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void removeAllExtendedReaders(NodeRef nodeRef)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onMoveNode(final ChildAssociationRef origAssoc, final ChildAssociationRef newAssoc)
    {
        // TODO temp solution for demo
        
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>() 
        {

            @Override
            public Void doWork() throws Exception
            {
                NodeRef record = newAssoc.getChildRef();
                NodeRef parent = newAssoc.getParentRef();
                
                Set<String> readers = getExtendedReaders(record);
                if (readers != null && readers.size() != 0)
                {
                    setExtendedReaders(parent, readers);
                }
                
                return null;
            }});
        
        
        
    }
}
