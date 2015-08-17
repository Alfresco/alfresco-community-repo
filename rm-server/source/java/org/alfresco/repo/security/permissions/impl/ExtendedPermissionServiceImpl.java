/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.repo.security.permissions.impl;

import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.security.ExtendedReaderDynamicAuthority;
import org.alfresco.module.org_alfresco_module_rm.security.ExtendedWriterDynamicAuthority;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.security.permissions.AccessControlEntry;
import org.alfresco.repo.security.permissions.AccessControlList;
import org.alfresco.repo.security.permissions.processor.PermissionPostProcessor;
import org.alfresco.repo.security.permissions.processor.PermissionPreProcessor;
import org.alfresco.repo.security.permissions.processor.PermissionProcessorRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.util.PropertyCheck;
import org.springframework.context.ApplicationEvent;

/**
 * Extends the core permission service implementation allowing the consideration of the read records
 * permission.
 * <p>
 * This is required for SOLR support.
 *
 * @author Roy Wetherall
 */
public class ExtendedPermissionServiceImpl extends PermissionServiceImpl
                                           implements ExtendedPermissionService
{
	/** Writers simple cache */
    protected SimpleCache<Serializable, Set<String>> writersCache;

    /** File plan service */
    private FilePlanService filePlanService;
    
    /** Permission processor registry */
    private PermissionProcessorRegistry permissionProcessorRegistry;

    /**
     * Gets the file plan service
     *
     * @return the filePlanService
     */
    public FilePlanService getFilePlanService()
    {
        return this.filePlanService;
    }

    /**
     * Sets the file plan service
     *
     * @param filePlanService the filePlanService to set
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }
    
    /**
     * Sets the permission processor registry
     * 
     * @param permissionProcessorRegistry	the permissions processor registry
     */
    public void setPermissionProcessorRegistry(PermissionProcessorRegistry permissionProcessorRegistry) 
    {
		this.permissionProcessorRegistry = permissionProcessorRegistry;
	}

    /**
     * @see org.alfresco.repo.security.permissions.impl.PermissionServiceImpl#setAnyDenyDenies(boolean)
     */
    @Override
    public void setAnyDenyDenies(boolean anyDenyDenies)
    {
        super.setAnyDenyDenies(anyDenyDenies);
        if (writersCache != null)
        {
        	writersCache.clear();
        }
    }

    /**
     * @param writersCache the writersCache to set
     */
    public void setWritersCache(SimpleCache<Serializable, Set<String>> writersCache)
    {
        this.writersCache = writersCache;
    }

    /**
     * @see org.alfresco.repo.security.permissions.impl.PermissionServiceImpl#onBootstrap(org.springframework.context.ApplicationEvent)
     */
    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        super.onBootstrap(event);
        PropertyCheck.mandatory(this, "writersCache", writersCache);
    }

    /**
     * Override to deal with the possibility of hard coded permission checks in core code.
     *
     * Note:  Eventually we need to merge the RM permission model into the core to make this more rebust.
     *
     * @see org.alfresco.repo.security.permissions.impl.ExtendedPermissionService#hasPermission(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    @Override
    public AccessStatus hasPermission(NodeRef nodeRef, String perm)
    {
    	AccessStatus result = AccessStatus.UNDETERMINED;
    	
    	// permission pre-processors
    	List<PermissionPreProcessor> preProcessors = permissionProcessorRegistry.getPermissionPreProcessors();
    	for (PermissionPreProcessor preProcessor : preProcessors) 
    	{
    		// pre process permission
    		result = preProcessor.process(nodeRef, perm);
    		
    		// veto if denied
    		if (AccessStatus.DENIED.equals(result))
    		{
    			return result;
    		}
		}
    	
    	// evaluate permission
        result = hasPermissionImpl(nodeRef, perm);
        
        // permission post-processors
        List<PermissionPostProcessor> postProcessors = permissionProcessorRegistry.getPermissionPostProcessors();
        for (PermissionPostProcessor postProcessor : postProcessors) 
        {
        	// post process permission
        	result = postProcessor.process(result, nodeRef, perm);
		}        
        
        return result;
    }
    
    /**
     * Implementation of hasPermission method call.
     * <p>
     * Separation also convenient for unit testing.
     * 
     * @param nodeRef	node reference
     * @param perm		permission
     * @return {@link AccessStatus}	access status result
     */
    protected AccessStatus hasPermissionImpl(NodeRef nodeRef, String perm)
    {
    	return super.hasPermission(nodeRef, perm);
    }

    /**
     * @see org.alfresco.repo.security.permissions.impl.PermissionServiceImpl#canRead(java.lang.Long)
     */
    @Override
    protected AccessStatus canRead(Long aclId)
    {
        Set<String> authorities = getAuthorisations();

        // test denied

        if(anyDenyDenies)
        {

            Set<String> aclReadersDenied = getReadersDenied(aclId);

            for(String auth : aclReadersDenied)
            {
                if(authorities.contains(auth))
                {
                    return AccessStatus.DENIED;
                }
            }

        }

        // test acl readers
        Set<String> aclReaders = getReaders(aclId);

        for(String auth : aclReaders)
        {
            if(authorities.contains(auth))
            {
                return AccessStatus.ALLOWED;
            }
        }

        return AccessStatus.DENIED;
    }

    /**
     * @see org.alfresco.repo.security.permissions.impl.PermissionServiceImpl#getReaders(java.lang.Long)
     */
    @Override
    public Set<String> getReaders(Long aclId)
    {
        AccessControlList acl = aclDaoComponent.getAccessControlList(aclId);
        if (acl == null)
        {
            return Collections.emptySet();
        }

        Set<String> aclReaders = readersCache.get((Serializable)acl.getProperties());
        if (aclReaders != null)
        {
            return aclReaders;
        }

        HashSet<String> assigned = new HashSet<String>();
        HashSet<String> readers = new HashSet<String>();

        for (AccessControlEntry ace : acl.getEntries())
        {
            assigned.add(ace.getAuthority());
        }

        for (String authority : assigned)
        {
            UnconditionalAclTest test = new UnconditionalAclTest(getPermissionReference(PermissionService.READ));
            UnconditionalAclTest rmTest = new UnconditionalAclTest(getPermissionReference(RMPermissionModel.READ_RECORDS));
            if (test.evaluate(authority, aclId) || rmTest.evaluate(authority, aclId))
            {
                readers.add(authority);
            }
        }

        aclReaders = Collections.unmodifiableSet(readers);
        readersCache.put((Serializable)acl.getProperties(), aclReaders);
        return aclReaders;
    }

    /**
     * Override with check for RM read
     *
     * @param aclId
     * @return
     */
    public Set<String> getReadersDenied(Long aclId)
    {
        AccessControlList acl = aclDaoComponent.getAccessControlList(aclId);

        if (acl == null)
        {
            return Collections.emptySet();
        }
        Set<String> denied = readersDeniedCache.get(aclId);
        if (denied != null)
        {
            return denied;
        }
        denied = new HashSet<String>();
        Set<String> assigned = new HashSet<String>();

        for (AccessControlEntry ace : acl.getEntries())
        {
            assigned.add(ace.getAuthority());
        }

        for(String authority : assigned)
        {
            UnconditionalDeniedAclTest test = new UnconditionalDeniedAclTest(getPermissionReference(PermissionService.READ));
            UnconditionalDeniedAclTest rmTest = new UnconditionalDeniedAclTest(getPermissionReference(RMPermissionModel.READ_RECORDS));
            if(test.evaluate(authority, aclId) || rmTest.evaluate(authority, aclId))
            {
                denied.add(authority);
            }
        }

        readersDeniedCache.put((Serializable)acl.getProperties(), denied);

        return denied;
    }

    /**
     * @see org.alfresco.repo.security.permissions.impl.ExtendedPermissionService#getWriters(java.lang.Long)
     */
    public Set<String> getWriters(Long aclId)
    {
        AccessControlList acl = aclDaoComponent.getAccessControlList(aclId);
        if (acl == null)
        {
            return Collections.emptySet();
        }

        Set<String> aclWriters = writersCache.get((Serializable)acl.getProperties());
        if (aclWriters != null)
        {
            return aclWriters;
        }

        HashSet<String> assigned = new HashSet<String>();
        HashSet<String> readers = new HashSet<String>();

        for (AccessControlEntry ace : acl.getEntries())
        {
            assigned.add(ace.getAuthority());
        }

        for (String authority : assigned)
        {
            UnconditionalAclTest test = new UnconditionalAclTest(getPermissionReference(PermissionService.WRITE));
            if (test.evaluate(authority, aclId))
            {
                readers.add(authority);
            }
        }

        aclWriters = Collections.unmodifiableSet(readers);
        writersCache.put((Serializable)acl.getProperties(), aclWriters);
        return aclWriters;
    }

    /**
     * @see org.alfresco.repo.security.permissions.impl.PermissionServiceImpl#setInheritParentPermissions(org.alfresco.service.cmr.repository.NodeRef, boolean)
     */
    @Override
    public void setInheritParentPermissions(final NodeRef nodeRef, boolean inheritParentPermissions)
    {
        final String adminRole = getAdminRole(nodeRef);
        if (nodeService.hasAspect(nodeRef, RecordsManagementModel.ASPECT_FILE_PLAN_COMPONENT) && isNotBlank(adminRole) && !inheritParentPermissions)
        {
            setPermission(nodeRef, ExtendedReaderDynamicAuthority.EXTENDED_READER, RMPermissionModel.READ_RECORDS, true);
            setPermission(nodeRef, ExtendedWriterDynamicAuthority.EXTENDED_WRITER, RMPermissionModel.FILING, true);
            setPermission(nodeRef, adminRole, RMPermissionModel.FILING, true);
        }
        super.setInheritParentPermissions(nodeRef, inheritParentPermissions);
    }

    private String getAdminRole(NodeRef nodeRef)
    {
        String adminRole = null;
        NodeRef filePlan = getFilePlanService().getFilePlan(nodeRef);
        if (filePlan != null)
        {
            adminRole = authorityService.getName(AuthorityType.GROUP, FilePlanRoleService.ROLE_ADMIN + filePlan.getId());
        }
        return adminRole;
    }
}
