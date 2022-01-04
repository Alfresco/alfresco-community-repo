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

package org.alfresco.repo.security.permissions.impl;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.audit.RecordsManagementAuditService;
import org.alfresco.module.org_alfresco_module_rm.audit.event.AuditEvent;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.AccessControlEntry;
import org.alfresco.repo.security.permissions.AccessControlList;
import org.alfresco.repo.security.permissions.processor.PermissionPostProcessor;
import org.alfresco.repo.security.permissions.processor.PermissionPreProcessor;
import org.alfresco.repo.security.permissions.processor.PermissionProcessorRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEvent;


/**
 * Extends the core permission service implementation allowing the consideration of the read records permission.
 * <p>
 * This is required for SOLR support.
 *
 * @author Roy Wetherall
 */
public class ExtendedPermissionServiceImpl extends PermissionServiceImpl implements ExtendedPermissionService
{
    /** An audit key for the enable permission inheritance event. */
    private static final String AUDIT_ENABLE_INHERIT_PERMISSION = "enable-inherit-permission";
    /** An audit key for the disable permission inheritance event. */
    private static final String AUDIT_DISABLE_INHERIT_PERMISSION = "disable-inherit-permission";

    /** Writers simple cache */
    protected SimpleCache<Serializable, Set<String>> writersCache;

    /**
     * Configured Permission mapping.
     * <p>
     * This string comes from alfresco-global.properties and allows fine tuning of the how permissions are mapped.
     * This was added as a fix for MNT-16852 to enhance compatibility with our Outlook Integration.
     */
    protected List<String> configuredReadPermissions;
    /**
     * Configured Permission mapping.
     * <p>
     * This string also comes from alfresco-global.properties.
     */
    protected List<String> configuredFilePermissions;

    /** File plan service */
    private FilePlanService filePlanService;

    /** Permission processor registry */
    private PermissionProcessorRegistry permissionProcessorRegistry;

    /** The RM audit service. */
    private RecordsManagementAuditService recordsManagementAuditService;

    /** {@inheritDoc} Register the audit events. */
    @Override
    public void init()
    {
        super.init();
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                recordsManagementAuditService.registerAuditEvent(new AuditEvent(AUDIT_ENABLE_INHERIT_PERMISSION, "rm.audit.enable-inherit-permission"));
                recordsManagementAuditService.registerAuditEvent(new AuditEvent(AUDIT_DISABLE_INHERIT_PERMISSION, "rm.audit.disable-inherit-permission"));
                return null;
            }
        });
    }

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
     * @param permissionProcessorRegistry the permissions processor registry
     */
    public void setPermissionProcessorRegistry(PermissionProcessorRegistry permissionProcessorRegistry)
    {
        this.permissionProcessorRegistry = permissionProcessorRegistry;
    }

    /**
     * Set the RM audit service.
     *
     * @param recordsManagementAuditService The RM audit service.
     */
    public void setRecordsManagementAuditService(RecordsManagementAuditService recordsManagementAuditService)
    {
        this.recordsManagementAuditService = recordsManagementAuditService;
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
     * Maps the string from the properties file (rm.haspermissionmap.read)
     * to the list used in the hasPermission method
     *
     * @param readMapping the mapping of permissions to ReadRecord
     */
    public void setConfiguredReadPermissions(String readMapping)
    {
        this.configuredReadPermissions = Arrays.asList(readMapping.split(","));
    }

    /**
     * Maps the string set in the properties file (rm.haspermissionmap.write)
     * to the list used in the hasPermission method
     *
     * @param fileMapping the mapping of permissions to FileRecord
     */
    public void setConfiguredFilePermissions(String fileMapping)
    {
        this.configuredFilePermissions = Arrays.asList(fileMapping.split(","));
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
     * Override to deal with the possibility of hard coded permission checks in core code. Note: Eventually we need to
     * merge the RM permission model into the core to make this more robust.
     *
     * @see org.alfresco.repo.security.permissions.impl.ExtendedPermissionService#hasPermission(org.alfresco.service.cmr.repository.NodeRef,
     *      java.lang.String)
     */
    @Override
    public AccessStatus hasPermission(NodeRef nodeRef, String perm)
    {
        AccessStatus result = AccessStatus.UNDETERMINED;
        if (nodeService.exists(nodeRef))
        {
            // permission pre-processors
            List<PermissionPreProcessor> preProcessors = permissionProcessorRegistry.getPermissionPreProcessors();
            for (PermissionPreProcessor preProcessor : preProcessors)
            {
                // pre process permission
                result = preProcessor.process(nodeRef, perm);

                // veto if denied
                if (AccessStatus.DENIED.equals(result)) { return result; }
            }

            // evaluate permission
            result = hasPermissionImpl(nodeRef, perm);

            // permission post-processors
            List<PermissionPostProcessor> postProcessors = permissionProcessorRegistry.getPermissionPostProcessors();
            for (PermissionPostProcessor postProcessor : postProcessors)
            {
                // post process permission
                result = postProcessor.process(result, nodeRef, perm, this.configuredReadPermissions, this.configuredFilePermissions);
            }
        }
        return result;
    }

    /**
     * Implementation of hasPermission method call.
     * <p>
     * Separation also convenient for unit testing.
     *
     * @param nodeRef node reference
     * @param perm permission
     * @return {@link AccessStatus} access status result
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

        if (anyDenyDenies)
        {

            Set<String> aclReadersDenied = getReadersDenied(aclId);

            for (String auth : aclReadersDenied)
            {
                if (authorities.contains(auth)) { return AccessStatus.DENIED; }
            }

        }

        // test acl readers
        Set<String> aclReaders = getReaders(aclId);

        for (String auth : aclReaders)
        {
            if (authorities.contains(auth)) { return AccessStatus.ALLOWED; }
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
        if (acl == null) { return Collections.emptySet(); }

        Set<String> aclReaders = readersCache.get((Serializable) acl.getProperties());
        if (aclReaders != null) { return aclReaders; }

        HashSet<String> assigned = new HashSet<>();
        HashSet<String> readers = new HashSet<>();

        for (AccessControlEntry ace : acl.getEntries())
        {
            assigned.add(ace.getAuthority());
        }

        for (String authority : assigned)
        {
            UnconditionalAclTest test = new UnconditionalAclTest(getPermissionReference(PermissionService.READ));
            UnconditionalAclTest rmTest = new UnconditionalAclTest(
                        getPermissionReference(RMPermissionModel.READ_RECORDS));
            if (test.evaluate(authority, aclId) || rmTest.evaluate(authority, aclId))
            {
                readers.add(authority);
            }
        }

        aclReaders = Collections.unmodifiableSet(readers);
        readersCache.put((Serializable) acl.getProperties(), aclReaders);
        return aclReaders;
    }

    /**
     * Override with check for RM read
     *
     * @param aclId
     * @return
     */
    @Override
    public Set<String> getReadersDenied(Long aclId)
    {
        AccessControlList acl = aclDaoComponent.getAccessControlList(aclId);

        if (acl == null) { return Collections.emptySet(); }
        Set<String> denied = readersDeniedCache.get(aclId);
        if (denied != null) { return denied; }
        denied = new HashSet<>();
        Set<String> assigned = new HashSet<>();

        for (AccessControlEntry ace : acl.getEntries())
        {
            assigned.add(ace.getAuthority());
        }

        for (String authority : assigned)
        {
            UnconditionalDeniedAclTest test = new UnconditionalDeniedAclTest(
                        getPermissionReference(PermissionService.READ));
            UnconditionalDeniedAclTest rmTest = new UnconditionalDeniedAclTest(
                        getPermissionReference(RMPermissionModel.READ_RECORDS));
            if (test.evaluate(authority, aclId) || rmTest.evaluate(authority, aclId))
            {
                denied.add(authority);
            }
        }

        readersDeniedCache.put((Serializable) acl.getProperties(), denied);

        return denied;
    }

    /**
     * @see org.alfresco.repo.security.permissions.impl.ExtendedPermissionService#getWriters(java.lang.Long)
     */
    @Override
    public Set<String> getWriters(Long aclId)
    {
        AccessControlList acl = aclDaoComponent.getAccessControlList(aclId);
        if (acl == null) { return Collections.emptySet(); }

        Set<String> aclWriters = writersCache.get((Serializable) acl.getProperties());
        if (aclWriters != null) { return aclWriters; }

        HashSet<String> assigned = new HashSet<>();
        HashSet<String> readers = new HashSet<>();

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
        writersCache.put((Serializable) acl.getProperties(), aclWriters);
        return aclWriters;
    }

    /**
     * @see org.alfresco.repo.security.permissions.impl.PermissionServiceImpl#setInheritParentPermissions(org.alfresco.service.cmr.repository.NodeRef,
     *      boolean)
     */
    @Override
    public void setInheritParentPermissions(final NodeRef nodeRef, boolean inheritParentPermissions)
    {
        final String adminRole = getAdminRole(nodeRef);
        if (nodeService.hasAspect(nodeRef, RecordsManagementModel.ASPECT_FILE_PLAN_COMPONENT) && isNotBlank(adminRole)
                    && !inheritParentPermissions)
        {
            setPermission(nodeRef, adminRole, RMPermissionModel.FILING, true);
        }
        if (inheritParentPermissions != super.getInheritParentPermissions(nodeRef))
        {
            super.setInheritParentPermissions(nodeRef, inheritParentPermissions);
            String auditEvent = (inheritParentPermissions ? AUDIT_ENABLE_INHERIT_PERMISSION : AUDIT_DISABLE_INHERIT_PERMISSION);
            recordsManagementAuditService.auditEvent(nodeRef, auditEvent);
        }
    }

    /**
     * Helper method to the RM admin role scoped by the correct file plan.
     *
     * @param nodeRef   node reference
     * @return String   RM admin role
     */
    private String getAdminRole(NodeRef nodeRef)
    {
        String adminRole = null;
        NodeRef filePlan = getFilePlanService().getFilePlan(nodeRef);
        if (filePlan != null)
        {
            adminRole = authorityService.getName(AuthorityType.GROUP,
                        FilePlanRoleService.ROLE_ADMIN + filePlan.getId());
        }
        return adminRole;
    }

    /**
     * @see org.alfresco.repo.security.permissions.impl.ExtendedPermissionService#getReadersAndWriters(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public Pair<Set<String>, Set<String>> getReadersAndWriters(NodeRef nodeRef)
    {
        // get the documents readers
        Long aclId = nodeService.getNodeAclId(nodeRef);
        Set<String> readers = getReaders(aclId);
        Set<String> writers = getWriters(aclId);

        // add the current owner to the list of extended writers
        Set<String> modifiedWrtiers = new HashSet<>(writers);
        String owner = ownableService.getOwner(nodeRef);
        if (StringUtils.isNotBlank(owner) &&
            !owner.equals(OwnableService.NO_OWNER) &&
            authorityService.authorityExists(owner))
        {
            modifiedWrtiers.add(owner);
        }

        return new Pair<>(readers, modifiedWrtiers);
    }
}
