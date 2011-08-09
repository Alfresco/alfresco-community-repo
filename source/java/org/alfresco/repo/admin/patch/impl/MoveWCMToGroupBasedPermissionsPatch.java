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
package org.alfresco.repo.admin.patch.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.search.AVMSnapShotTriggeredIndexingMethodInterceptor;
import org.alfresco.repo.search.AVMSnapShotTriggeredIndexingMethodInterceptorImpl.StoreType;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Remove ACLs on all but staging area stores On staging area stores, set ACls according to the users and roles as set
 * on the web site Note: runs as the system user
 * 
 * @author andyh
 */
public class MoveWCMToGroupBasedPermissionsPatch extends AbstractPatch
{
    public static final String[] PERMISSIONS = new String[]
    {
        PermissionService.WCM_CONTENT_MANAGER, PermissionService.WCM_CONTENT_PUBLISHER,
        PermissionService.WCM_CONTENT_CONTRIBUTOR, PermissionService.WCM_CONTENT_REVIEWER
    };
    
    protected static final String WCM_STORE_SEPARATOR = "--";

    private static final String MSG_SUCCESS = "patch.moveWCMToGroupBasedPermissionsPatch.result";

    AVMSnapShotTriggeredIndexingMethodInterceptor avmSnapShotTriggeredIndexingMethodInterceptor;

    AVMService avmService;

    PermissionService permissionService;

    AuthorityService authorityService;

    String replaceAllWith = PermissionService.WCM_CONTENT_MANAGER;

    public void setAvmService(AVMService avmService)
    {
        this.avmService = avmService;
    }

    public void setAvmSnapShotTriggeredIndexingMethodInterceptor(
            AVMSnapShotTriggeredIndexingMethodInterceptor avmSnapShotTriggeredIndexingMethodInterceptor)
    {
        this.avmSnapShotTriggeredIndexingMethodInterceptor = avmSnapShotTriggeredIndexingMethodInterceptor;
    }

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

    public void setReplaceAllWith(String replaceAllWith)
    {
        this.replaceAllWith = replaceAllWith;
    }

    @Override
    protected String applyInternal() throws Exception
    {
        List<AVMStoreDescriptor> stores = this.avmService.getStores();
        for (AVMStoreDescriptor store : stores)
        {

            Map<QName, PropertyValue> storeProperties = this.avmService.getStoreProperties(store.getName());

            switch (StoreType.getStoreType(store.getName(), store, storeProperties))
            {
            /* Set permissions in staging */
            case STAGING:
                fixAllPermissions(store);
                setStagingAreaPermissions(store);
                setStagingAreaMasks(store);
                // TODO: mark read only
                break;
            /* Clear permissions */
            case AUTHOR:
            case AUTHOR_PREVIEW:
            case AUTHOR_WORKFLOW:
            case AUTHOR_WORKFLOW_PREVIEW:
                fixAllStagingPermissions(store);
                setSandBoxMasks(store);
                break;
            case STAGING_PREVIEW:
                fixAllStagingPermissions(store);
                setStagingAreaMasks(store);
                // TODO: mark read only
                break;
            case WORKFLOW:
            case WORKFLOW_PREVIEW:
                break;
            /* non WCM stores - nothing to do */
            case UNKNOWN:
            default:
            }
        }

        // build the result message
        String msg = I18NUtil.getMessage(MoveWCMToGroupBasedPermissionsPatch.MSG_SUCCESS);
        // done
        return msg;
    }

    private boolean isPermissionSet(NodeRef nodeRef, String authority, String permission)
    {
        Set<AccessPermission> set = this.permissionService.getAllSetPermissions(nodeRef);
        for (AccessPermission ap : set)
        {
            if (ap.getAuthority().equals(authority) && ap.isSetDirectly() && ap.getPermission().equals(permission))
            {
                return true;
            }
        }
        return false;
    }

    private boolean isMaskSet(StoreRef storeRef, String authority, String permission)
    {
        Set<AccessPermission> set = this.permissionService.getAllSetPermissions(storeRef);
        for (AccessPermission ap : set)
        {
            if (ap.getAuthority().equals(authority) && ap.isSetDirectly() && ap.getPermission().equals(permission))
            {
                return true;
            }
        }
        return false;
    }

    private void makeGroupsIfRequired(String stagingStoreName, NodeRef dirRef)
    {
        for (String permission : MoveWCMToGroupBasedPermissionsPatch.PERMISSIONS)
        {
            String shortName = stagingStoreName + "-" + permission;
            String group = this.authorityService.getName(AuthorityType.GROUP, shortName);
            if (!this.authorityService.authorityExists(group))
            {
                String newGroup = this.authorityService.createAuthority(AuthorityType.GROUP, shortName);
                this.permissionService.setPermission(dirRef, newGroup, permission, true);
            }
        }
    }

    protected void addToGroupIfRequired(String stagingStoreName, String user, String permission)
    {
        String shortName = stagingStoreName + "-" + permission;
        String group = this.authorityService.getName(AuthorityType.GROUP, shortName);
        Set<String> members = this.authorityService.getContainedAuthorities(AuthorityType.USER, group, true);
        if (!members.contains(user))
        {
            this.authorityService.addAuthority(group, user);
        }
    }

    private void fixAllPermissions(AVMStoreDescriptor store)
    {
        fixAllPermissionsImpl(store.getName());
    }

    private void fixAllStagingPermissions(AVMStoreDescriptor store)
    {

        String stagingAreaName = extractStagingAreaName(store.getName());
        fixAllPermissionsImpl(stagingAreaName);
    }

    private void fixAllPermissionsImpl(String stagingStoreName)
    {
        QName propQName = QName.createQName(null, ".web_project.noderef");

        PropertyValue pValue = this.avmService.getStoreProperty(stagingStoreName, propQName);

        if (pValue != null)
        {
            NodeRef webProjectNodeRef = (NodeRef) pValue.getValue(DataTypeDefinition.NODE_REF);

            // Apply sepcific user permissions as set on the web project
            List<ChildAssociationRef> userInfoRefs = this.nodeService.getChildAssocs(webProjectNodeRef,
                    WCMAppModel.ASSOC_WEBUSER, RegexQNamePattern.MATCH_ALL);
            for (ChildAssociationRef ref : userInfoRefs)
            {
                NodeRef userInfoRef = ref.getChildRef();
                //String username = (String) this.nodeService.getProperty(userInfoRef, WCMAppModel.PROP_WEBUSERNAME); // not used
                String userrole = (String) this.nodeService.getProperty(userInfoRef, WCMAppModel.PROP_WEBUSERROLE);

                if (userrole.equals(PermissionService.ALL_PERMISSIONS))
                {
                    this.nodeService.setProperty(userInfoRef, WCMAppModel.PROP_WEBUSERROLE, this.replaceAllWith);
                }

            }
        }
    }

    protected void setStagingAreaPermissions(AVMStoreDescriptor store) throws Exception
    {
        QName propQName = QName.createQName(null, ".web_project.noderef");

        NodeRef dirRef = AVMNodeConverter.ToNodeRef(-1, store.getName() + ":/www");

        makeGroupsIfRequired(store.getName(), dirRef);

        if (!isPermissionSet(dirRef, PermissionService.ALL_AUTHORITIES, PermissionService.READ))
        {
            this.permissionService.setPermission(dirRef, PermissionService.ALL_AUTHORITIES, PermissionService.READ,
                    true);
        }

        // Add group permissions
        for (String permission : MoveWCMToGroupBasedPermissionsPatch.PERMISSIONS)
        {
            String cms = this.authorityService.getName(AuthorityType.GROUP, store.getName() + "-" + permission);
            this.permissionService.setPermission(dirRef, cms, permission, true);
        }

        PropertyValue pValue = this.avmService.getStoreProperty(store.getName(), propQName);

        if (pValue != null)
        {
            NodeRef webProjectNodeRef = (NodeRef) pValue.getValue(DataTypeDefinition.NODE_REF);

            // Apply sepcific user permissions as set on the web project
            List<ChildAssociationRef> userInfoRefs = this.nodeService.getChildAssocs(webProjectNodeRef,
                    WCMAppModel.ASSOC_WEBUSER, RegexQNamePattern.MATCH_ALL);
            for (ChildAssociationRef ref : userInfoRefs)
            {
                NodeRef userInfoRef = ref.getChildRef();
                String username = (String) this.nodeService.getProperty(userInfoRef, WCMAppModel.PROP_WEBUSERNAME);
                String userrole = (String) this.nodeService.getProperty(userInfoRef, WCMAppModel.PROP_WEBUSERROLE);

                // remove existing

                if (isPermissionSet(dirRef, username, userrole))
                {
                    this.permissionService.deletePermission(dirRef, username, userrole);
                }

                addToGroupIfRequired(store.getName(), username, userrole);
            }
        }
    }

    protected void setStagingAreaMasks(AVMStoreDescriptor store)
    {
        // groups must exist
        NodeRef dirRef = AVMNodeConverter.ToNodeRef(-1, store.getName() + ":/www");

        if (!isMaskSet(dirRef.getStoreRef(), PermissionService.ALL_AUTHORITIES, PermissionService.READ))
        {
            this.permissionService.setPermission(dirRef.getStoreRef(), PermissionService.ALL_AUTHORITIES,
                    PermissionService.READ, true);
        }

        String cms = this.authorityService.getName(AuthorityType.GROUP, store.getName() + "-"
                + PermissionService.WCM_CONTENT_MANAGER);
        if (!isMaskSet(dirRef.getStoreRef(), cms, PermissionService.CHANGE_PERMISSIONS))
        {
            this.permissionService.setPermission(dirRef.getStoreRef(), cms, PermissionService.CHANGE_PERMISSIONS, true);
        }

        if (!isMaskSet(dirRef.getStoreRef(), cms, PermissionService.READ_PERMISSIONS))
        {
            this.permissionService.setPermission(dirRef.getStoreRef(), cms, PermissionService.READ_PERMISSIONS, true);
        }

        QName propQName = QName.createQName(null, ".web_project.noderef");

        PropertyValue pValue = this.avmService.getStoreProperty(store.getName(), propQName);

        if (pValue != null)
        {
            NodeRef webProjectNodeRef = (NodeRef) pValue.getValue(DataTypeDefinition.NODE_REF);

            // Apply sepcific user permissions as set on the web project
            List<ChildAssociationRef> userInfoRefs = this.nodeService.getChildAssocs(webProjectNodeRef,
                    WCMAppModel.ASSOC_WEBUSER, RegexQNamePattern.MATCH_ALL);
            for (ChildAssociationRef ref : userInfoRefs)
            {
                NodeRef userInfoRef = ref.getChildRef();
                String username = (String) this.nodeService.getProperty(userInfoRef, WCMAppModel.PROP_WEBUSERNAME);
                String userrole = (String) this.nodeService.getProperty(userInfoRef, WCMAppModel.PROP_WEBUSERROLE);

                if (userrole.equals(PermissionService.WCM_CONTENT_MANAGER))
                {
                    // remove existing

                    if (isMaskSet(dirRef.getStoreRef(), username, PermissionService.CHANGE_PERMISSIONS))
                    {
                        this.permissionService.deletePermission(dirRef.getStoreRef(), username,
                                PermissionService.CHANGE_PERMISSIONS);
                    }

                    if (isMaskSet(dirRef.getStoreRef(), username, PermissionService.READ_PERMISSIONS))
                    {
                        this.permissionService.deletePermission(dirRef.getStoreRef(), username,
                                PermissionService.READ_PERMISSIONS);
                    }
                }
            }
        }

    }

    protected void setSandBoxMasks(AVMStoreDescriptor sandBoxStore)
    {
        // get the settings from the staging store ...

        String owner = extractOwner(sandBoxStore.getName());
        String stagingAreaName = extractStagingAreaName(sandBoxStore.getName());

        QName propQName = QName.createQName(null, ".web_project.noderef");

        NodeRef dirRef = AVMNodeConverter.ToNodeRef(-1, sandBoxStore.getName() + ":/www");

        //Map<QName, PropertyValue> woof = this.avmService.getStoreProperties(stagingAreaName); // not used
        PropertyValue pValue = this.avmService.getStoreProperty(stagingAreaName, propQName);

        if (!isMaskSet(dirRef.getStoreRef(), PermissionService.ALL_AUTHORITIES, PermissionService.READ))
        {
            this.permissionService.setPermission(dirRef.getStoreRef(), PermissionService.ALL_AUTHORITIES,
                    PermissionService.READ, true);
        }

        String cms = this.authorityService.getName(AuthorityType.GROUP, stagingAreaName + "-"
                + PermissionService.WCM_CONTENT_MANAGER);
        if (!isMaskSet(dirRef.getStoreRef(), cms, PermissionService.WCM_CONTENT_MANAGER))
        {
            this.permissionService
                    .setPermission(dirRef.getStoreRef(), cms, PermissionService.WCM_CONTENT_MANAGER, true);
        }

        if (pValue != null)
        {
            NodeRef webProjectNodeRef = (NodeRef) pValue.getValue(DataTypeDefinition.NODE_REF);

            // Apply sepcific user permissions as set on the web project
            List<ChildAssociationRef> userInfoRefs = this.nodeService.getChildAssocs(webProjectNodeRef,
                    WCMAppModel.ASSOC_WEBUSER, RegexQNamePattern.MATCH_ALL);
            for (ChildAssociationRef ref : userInfoRefs)
            {
                NodeRef userInfoRef = ref.getChildRef();
                String username = (String) this.nodeService.getProperty(userInfoRef, WCMAppModel.PROP_WEBUSERNAME);
                String userrole = (String) this.nodeService.getProperty(userInfoRef, WCMAppModel.PROP_WEBUSERROLE);

                if (username.equals(owner))
                {
                    this.permissionService.setPermission(dirRef.getStoreRef(), username,
                            PermissionService.ALL_PERMISSIONS, true);
                }
                else if (userrole.equals("ContentManager"))
                {
                    if (isMaskSet(dirRef.getStoreRef(), username, userrole))
                    {
                        this.permissionService.deletePermission(dirRef.getStoreRef(), username, userrole);
                    }
                }
            }
        }
    }

    private String extractOwner(String name)
    {
        int start = name.indexOf(WCM_STORE_SEPARATOR);
        if (start == -1)
        {
            throw new UnsupportedOperationException(name);
        }
        int end = name.indexOf(WCM_STORE_SEPARATOR, start + 1);
        if (end == -1)
        {
            return name.substring(start + 2);
        }
        return name.substring(start + 2, end);
    }

    protected String extractStagingAreaName(String name)
    {
        int index = name.indexOf(WCM_STORE_SEPARATOR);
        if (index == -1)
        {
            throw new UnsupportedOperationException(name);
        }
        return name.substring(0, index);
    }
}
