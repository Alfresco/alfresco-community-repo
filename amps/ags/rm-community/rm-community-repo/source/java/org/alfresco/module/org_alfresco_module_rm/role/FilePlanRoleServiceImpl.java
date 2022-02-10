/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.role;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.bootstrap.BootstrapImporterModuleComponent;
import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.util.FileUtils;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authority.RMAuthority;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Role service implementation
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public class FilePlanRoleServiceImpl implements FilePlanRoleService,
                                                RecordsManagementModel
{
    /** Logger for the class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(FilePlanRoleServiceImpl.class);
    /** I18N */
    private static final String MSG_ALL_ROLES = "rm.role.all";

    /** Location of bootstrap role JSON */
    private static final String BOOTSTRAP_ROLE_JSON_LOCATION = "alfresco/module/org_alfresco_module_rm/security/rm-default-roles-bootstrap.json";

    /** JSON names */
    private static final String JSON_NAME = "name";
    private static final String JSON_DISPLAY_LABEL = "displayLabel";
    private static final String JSON_IS_ADMIN = "isAdmin";
    private static final String JSON_CAPABILITIES = "capabilities";

    /** Capability service */
    private CapabilityService capabilityService;

    /** Authority service */
    private AuthorityService authorityService;

    /** Permission service */
    private PermissionService permissionService;

    /** File plan service */
    private FilePlanService filePlanService;

    /** Node service */
    private NodeService nodeService;

    private BootstrapImporterModuleComponent bootstrapImporterModule;

    /** Records management role zone */
    public static final String RM_ROLE_ZONE_PREFIX = "rmRoleZone";

    /** Records Management Config Node */
    private static final String CONFIG_NODEID = "rm_config_folder";

    /**
     * @param capabilityService capability service
     */
    public void setCapabilityService(CapabilityService capabilityService)
    {
        this.capabilityService = capabilityService;
    }

    /**
     * @param authorityService  authority service
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

    /**
     * @param permissionService permission service
     */
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    /**
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param filePlanService   file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }

    /**
     *
     * @param bootstrapImporterModuleComponent
     */
    public void setBootstrapImporterModuleComponent(BootstrapImporterModuleComponent bootstrapImporterModuleComponent)
    {
        this.bootstrapImporterModule = bootstrapImporterModuleComponent;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService#setupFilePlanRoles(NodeRef)
     */
    @Override
    public void setupFilePlanRoles(final NodeRef filePlan)
    {
        // Do not execute behaviour if this has been created in the archive store
        if(filePlan.getStoreRef().equals(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE))
        {
            // This is not the spaces store - probably the archive store
            return;
        }

        if (nodeService.exists(filePlan))
        {
            List<NodeRef> systemContainers = AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<List<NodeRef>>()
            {
                public List<NodeRef> doWork()
                {
                    List<NodeRef> systemContainers = new ArrayList<>(3);

                    //In a multi tenant store we need to initialize the rm config if it has been done yet
                    NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, CONFIG_NODEID);
                    if (!nodeService.exists(nodeRef))
                    {
                        bootstrapImporterModule.execute();
                    }

                    // Create "all" role group for root node
                    String allRoles = authorityService.createAuthority(
                    						AuthorityType.GROUP,
                    						getAllRolesGroupShortName(filePlan),
                    						I18NUtil.getMessage(MSG_ALL_ROLES),
                            new HashSet<>(Arrays.asList(RMAuthority.ZONE_APP_RM)));

                    // Set the permissions
                    permissionService.setInheritParentPermissions(filePlan, false);
                    permissionService.setPermission(filePlan, allRoles, RMPermissionModel.READ_RECORDS, true);

                    // Create the transfer and hold containers
                    systemContainers.add(filePlanService.createHoldContainer(filePlan));
                    systemContainers.add(filePlanService.createTransferContainer(filePlan));

                    // Create the unfiled record container
                    systemContainers.add(filePlanService.createUnfiledContainer(filePlan));

                    return systemContainers;
                }
            });

            // Bootstrap in the default set of roles for the newly created root node
            bootstrapDefaultRoles(filePlan, systemContainers);
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService#tearDownFilePlanRoles(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void tearDownFilePlanRoles(final NodeRef filePlan)
    {
        AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Object>()
        {
            public Object doWork()
            {
                // cascade delete the 'all' roles group for the site
                String allRolesGroup = authorityService.getName(AuthorityType.GROUP, getAllRolesGroupShortName(filePlan));
                Set<String> groups = authorityService.getContainedAuthorities(AuthorityType.GROUP, allRolesGroup, true);
                for (String group : groups)
                {
                    authorityService.deleteAuthority(group);
                }

                authorityService.deleteAuthority(allRolesGroup, false);

                return null;
            }
        });
    }

    /**
     * Get all the roles by short name
     *
     * @param rmRootNode
     * @return
     */
    private String getAllRolesGroupShortName(NodeRef rmRootNode)
    {
        return RMAuthority.ALL_ROLES_PREFIX + rmRootNode.getId();
    }

    /**
     * Bootstraps the default roles
     *
     * @param filePlan          file plan
     * @param systemContainers  system containers
     */
    private void bootstrapDefaultRoles(final NodeRef filePlan, final List<NodeRef> systemContainers)
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
                        InputStream is = getClass().getClassLoader().getResourceAsStream(BOOTSTRAP_ROLE_JSON_LOCATION);
                        if  (is == null)
                        {
                            throw new AlfrescoRuntimeException("Could not load default bootstrap roles configuration");
                        }
                        array = new JSONArray(FileUtils.convertStreamToString(is));
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
                        if (object.has(JSON_NAME))
                        {
                            name = object.getString(JSON_NAME);
                            if (existsRole(filePlan, name))
                            {
                                throw new AlfrescoRuntimeException("The bootstrap role " + name + " already exists on the rm root node " + filePlan.toString());
                            }
                        }
                        else
                        {
                            throw new AlfrescoRuntimeException("No name given to default bootstrap role.  Check json configuration file.");
                        }


                        // Get the role's display label
                        String displayLabel = name;
                        if (object.has(JSON_DISPLAY_LABEL))
                        {
                            displayLabel = object.getString(JSON_DISPLAY_LABEL);
                        }

                        // Determine whether the role is an admin role or not
                        boolean isAdmin = false;
                        if (object.has(JSON_IS_ADMIN))
                        {
                            isAdmin = object.getBoolean(JSON_IS_ADMIN);
                        }

                        // Get the roles capabilities
                        Set<Capability> capabilities = new HashSet<>(30);
                        if (object.has(JSON_CAPABILITIES))
                        {
                            JSONArray arrCaps = object.getJSONArray(JSON_CAPABILITIES);
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
                        Role role = createRole(filePlan, name, displayLabel, capabilities);

                        // Add any additional admin permissions
                        if (isAdmin)
                        {
                            // Admin has filing
                            permissionService.setPermission(filePlan, role.getRoleGroupName(), RMPermissionModel.FILING, true);
                            if (systemContainers != null)
                            {
                                for (NodeRef systemContainer : systemContainers)
                                {
                                    permissionService.setPermission(systemContainer, role.getRoleGroupName(), RMPermissionModel.FILING, true);
                                }
                            }

                            // Add the creating user to the administration group
                            String user = AuthenticationUtil.getFullyAuthenticatedUser();
                            authorityService.addAuthority(role.getRoleGroupName(), user);

                            if (!AuthenticationUtil.getAdminUserName().equals(user))
                            {
                                // add the dynamic admin authority
                                authorityService.addAuthority(role.getRoleGroupName(), AuthenticationUtil.getAdminUserName());
                            }
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

    /**
     * Helper method to check whether the current authority is a system role or not
     *
     * @param roleAuthority The role to check
     * @return Returns true if roleAuthority is a system role, false otherwise
     */
    private boolean isSystemRole(String roleAuthority)
    {
        boolean isSystemRole = false;

        for (String systemRole : SYSTEM_ROLES)
        {
            if (StringUtils.contains(roleAuthority, systemRole))
            {
                isSystemRole = true;
                break;
            }
        }

        return isSystemRole;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService#getRoles(NodeRef)
     */
    public Set<Role> getRoles(final NodeRef rmRootNode)
    {
        return getRoles(rmRootNode, true);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService#getRoles(NodeRef, boolean)
     */
    @Override
    public Set<Role> getRoles(final NodeRef rmRootNode, final boolean includeSystemRoles)
    {
        return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Set<Role>>()
        {
            public Set<Role> doWork()
            {
                Set<Role> result = new HashSet<>(13);

                Set<String> roleAuthorities = authorityService.getAllAuthoritiesInZone(getZoneName(rmRootNode), AuthorityType.GROUP);
                for (String roleAuthority : roleAuthorities)
                {
                    if (includeSystemRoles || !isSystemRole(roleAuthority))
                    {
                        String groupShortName = authorityService.getShortName(roleAuthority);
                        String name = getShortRoleName(groupShortName, rmRootNode);
                        String displayLabel = authorityService.getAuthorityDisplayName(roleAuthority);
                        String translated = I18NUtil.getMessage(displayLabel);
                        if (translated != null)
                        {
                            displayLabel = translated;
                        }
                        Set<Capability> capabilities = getCapabilitiesImpl(rmRootNode, roleAuthority);

                        Role role = new Role(name, displayLabel, capabilities, roleAuthority, groupShortName);
                        result.add(role);
                    }
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
        return getRolesByUser(rmRootNode, user, true);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService#getRolesByUser(NodeRef, String, boolean)
     */
    @Override
    public Set<Role> getRolesByUser(final NodeRef rmRootNode, final String user, final boolean includeSystemRoles)
    {
        return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Set<Role>>()
        {
            public Set<Role> doWork()
            {
                Set<Role> result = new HashSet<>(13);

                Set<String> roleAuthorities = authorityService.getAllAuthoritiesInZone(getZoneName(rmRootNode), AuthorityType.GROUP);
                for (String roleAuthority : roleAuthorities)
                {
                    Set<String> users = authorityService.getContainedAuthorities(AuthorityType.USER, roleAuthority, false);
                    if (users.contains(user) && (includeSystemRoles || !isSystemRole(roleAuthority)))
                    {
                        String groupShortName = authorityService.getShortName(roleAuthority);
                        String name = getShortRoleName(groupShortName, rmRootNode);
                        String displayLabel = authorityService.getAuthorityDisplayName(roleAuthority);
                        String translated = I18NUtil.getMessage(displayLabel);
                        if (translated != null)
                        {
                            displayLabel = translated;
                        }
                        Set<Capability> capabilities = getCapabilitiesImpl(rmRootNode, roleAuthority);

                        Role role = new Role(name, displayLabel, capabilities, roleAuthority, groupShortName);
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
     * @see org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService#getRole(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public Role getRole(final NodeRef rmRootNode, final String role)
    {
        return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Role>()
        {
            public Role doWork()
            {
                Role result = null;

                String roleAuthority = authorityService.getName(AuthorityType.GROUP, getFullRoleName(role, rmRootNode));
                if (authorityService.authorityExists(roleAuthority))
                {
                    String name = getShortRoleName(authorityService.getShortName(roleAuthority), rmRootNode);
                    String displayLabel = authorityService.getAuthorityDisplayName(roleAuthority);
                    Set<Capability> capabilities = getCapabilitiesImpl(rmRootNode, roleAuthority);

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
    private Set<Capability> getCapabilitiesImpl(NodeRef rmRootNode, String roleAuthority)
    {
        Set<AccessPermission> permissions = permissionService.getAllSetPermissions(rmRootNode);
        Set<Capability> capabilities = new HashSet<>(52);
        for (AccessPermission permission : permissions)
        {
            if (permission.getAuthority().equals(roleAuthority))
            {
                String capabilityName = permission.getPermission();
                Capability capability = capabilityService.getCapability(capabilityName);
                if (capability != null && !capability.isPrivate())
                {
                    capabilities.add(capability);
                }
            }
        }

        return capabilities;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService#existsRole(NodeRef, java.lang.String)
     */
    public boolean existsRole(final NodeRef rmRootNode, final String role)
    {
        return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Boolean>()
        {
            public Boolean doWork()
            {
                String fullRoleName = authorityService.getName(AuthorityType.GROUP, getFullRoleName(role, rmRootNode));

                String zone = getZoneName(rmRootNode);
                Set<String> roles = authorityService.getAllAuthoritiesInZone(zone, AuthorityType.GROUP);
                return Boolean.valueOf(roles.contains(fullRoleName));
            }
        }, AuthenticationUtil.getSystemUserName()).booleanValue();
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService#hasRMAdminRole(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     *
     * TODO .. change this to check a property of the role its self
     */
    public boolean hasRMAdminRole(NodeRef rmRootNode, String user)
    {
        boolean isAdmin = false;

        Set<Role> userRoles = this.getRolesByUser(rmRootNode, user);
        if (userRoles != null)
        {
            for (Role role : userRoles)
            {
                if (role.getName().equals("Administrator"))
                {
                    isAdmin = true;
                    break;
                }
            }
        }

        return isAdmin;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService#createRole(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, java.lang.String, java.util.Set)
     */
    public Role createRole(final NodeRef filePlan, final String role, final String roleDisplayLabel, final Set<Capability> capabilities)
    {
        return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Role>()
        {
            public Role doWork()
            {
                String fullRoleName = getFullRoleName(role, filePlan);

                // Check that the role does not already exist for the rm root node
                if (authorityService.authorityExists(authorityService.getName(AuthorityType.GROUP, fullRoleName)))
                {
                    throw new AlfrescoRuntimeException("The role " + role + " already exists for root rm node " + filePlan.getId());
                }

                // Create a group that relates to the records management role
                Set<String> zones = new HashSet<>(2);
                zones.add(getZoneName(filePlan));
                zones.add(RMAuthority.ZONE_APP_RM);

                // Look up string, default to passed value if none found
                String groupDisplayLabel = I18NUtil.getMessage(roleDisplayLabel);
                if (groupDisplayLabel == null)
                {
                    groupDisplayLabel = roleDisplayLabel;
                }

                String roleGroup = authorityService.createAuthority(AuthorityType.GROUP, fullRoleName, groupDisplayLabel, zones);

                // do not add system roles to "all"
                if (!isSystemRole(role))
                {
                    // Add the roleGroup to the "all" role group
                    String allRoleGroup = authorityService.getName(AuthorityType.GROUP, getAllRolesGroupShortName(filePlan));
                    authorityService.addAuthority(allRoleGroup, roleGroup);
                }

                // TODO .. we should be creating a permission set containing all the capabilities and then assigning that
                //         single permission group to the file plan .. would be tidier

                // Assign the various capabilities to the group on the root records management node
                if (capabilities != null)
                {
                    for (Capability capability : capabilities)
                    {
                        permissionService.setPermission(filePlan, roleGroup, capability.getName(), true);
                    }
                }

                return new Role(role, roleDisplayLabel, capabilities, roleGroup);
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
            public Role doWork()
            {
                if (!existsRole(rmRootNode, role))
                {
                    throw new AlfrescoRuntimeException("Unable to update role " + role + ", because it does not exist.");
                }

                String roleAuthority = authorityService.getName(AuthorityType.GROUP, getFullRoleName(role, rmRootNode));

                // Reset the role display name
                authorityService.setAuthorityDisplayName(roleAuthority, roleDisplayLabel);

                // TODO this needs to be improved, removing all and reading is not ideal

                // Clear the current capabilities
                permissionService.clearPermission(rmRootNode, roleAuthority);

                // Re-add the provided capabilities
                for (Capability capability : capabilities)
                {
                    permissionService.setPermission(rmRootNode, roleAuthority, capability.getName(), true);
                }

                return new Role(role, roleDisplayLabel, capabilities, roleAuthority);

            }
        }, AuthenticationUtil.getSystemUserName());
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService#deleteRole(NodeRef,String)
     */
    public void deleteRole(final NodeRef rmRootNode, final String role)
    {
        // ensure that we are not trying to delete the admin role
        if (ROLE_ADMIN.equals(role))
        {
            throw new AlfrescoRuntimeException("Can not delete the records management administration role.");
        }

        // ensure that we are not trying to delete a system role
        if (FilePlanRoleService.SYSTEM_ROLES.contains(role))
        {
            throw new AlfrescoRuntimeException("'" + role + "' is a system role and cannot be deleted.");
        }

        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
        {
            public Void doWork()
            {
                String roleAuthority = authorityService.getName(AuthorityType.GROUP, getFullRoleName(role, rmRootNode));
                authorityService.deleteAuthority(roleAuthority);
                return null;

            }
        }, AuthenticationUtil.getSystemUserName());
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService#getUsersAssignedToRole(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    @Override
    public Set<String> getUsersAssignedToRole(final NodeRef filePlan, final String roleName)
    {
        ParameterCheck.mandatory("filePlan", filePlan);
        ParameterCheck.mandatory("roleName", roleName);

        return getAuthoritiesAssignedToRole(filePlan, roleName, AuthorityType.USER);
    }

    /**
     * Gets all the authorities of a given type directly assigned to the given role in the file plan.
     *
     * @param filePlan          file plan
     * @param roleName          role name
     * @param authorityType     authority type
     * @return Set<String>      directly assigned authorities
     */
    private Set<String> getAuthoritiesAssignedToRole(final NodeRef filePlan, final String roleName, final AuthorityType authorityType)
    {
        return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Set<String>>()
        {
            public Set<String> doWork()
            {
                Role role = getRole(filePlan, roleName);
                if (role == null)
                {
                    throw new AlfrescoRuntimeException("Can not get authorities for role " + roleName + ", because it does not exist. (filePlan=" + filePlan.toString() + ")");
                }
                return authorityService.getContainedAuthorities(authorityType, role.getRoleGroupName(), true);
            }
        }, AuthenticationUtil.getSystemUserName());

    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService#getGroupsAssignedToRole(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    @Override
    public Set<String> getGroupsAssignedToRole(final NodeRef filePlan, final String roleName)
    {
        ParameterCheck.mandatory("filePlan", filePlan);
        ParameterCheck.mandatory("roleName", roleName);

        return getAuthoritiesAssignedToRole(filePlan, roleName, AuthorityType.GROUP);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService#getAllAssignedToRole(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    @Override
    public Set<String> getAllAssignedToRole(NodeRef filePlan, String role)
    {
        ParameterCheck.mandatory("filePlan", filePlan);
        ParameterCheck.mandatory("roleName", role);

        Set<String> result = new HashSet<>(21);
        result.addAll(getUsersAssignedToRole(filePlan, role));
        result.addAll(getGroupsAssignedToRole(filePlan, role));
        return result;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService#assignRoleToAuthority(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, java.lang.String)
     */
    public void assignRoleToAuthority(final NodeRef filePlan, final String role, final String authorityName)
    {
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>()
        {
            public Void doWork()
            {
                if (authorityService.authorityExists(authorityName) &&
                    !getAllAssignedToRole(filePlan, role).contains(authorityName))
                {
                    String roleAuthority = authorityService.getName(AuthorityType.GROUP, getFullRoleName(role, filePlan));
                    try
                    {
                    	authorityService.addAuthority(roleAuthority, authorityName);
                    }
                    catch (DuplicateChildNodeNameException exception)
                	{
                    	// ignore, because the work has already been performed
                	}
                }
                return null;

            }
        }, AuthenticationUtil.getSystemUserName());
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService#unassignRoleFromAuthority(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, java.lang.String)
     */
    @Override
    public void unassignRoleFromAuthority(final NodeRef filePlan, final String role, final String authorityName)
    {
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>()
        {
            public Void doWork()
            {
                String roleAuthority = authorityService.getName(AuthorityType.GROUP, getFullRoleName(role, filePlan));
                authorityService.removeAuthority(roleAuthority, authorityName);
                return null;

            }
        }, AuthenticationUtil.getSystemUserName());
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService#getAllRolesContainerGroup(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public String getAllRolesContainerGroup(NodeRef filePlan)
    {
        return authorityService.getName(AuthorityType.GROUP, getAllRolesGroupShortName(filePlan));
    }
}
