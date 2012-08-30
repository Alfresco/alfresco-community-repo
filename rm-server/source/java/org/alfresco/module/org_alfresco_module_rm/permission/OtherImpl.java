package org.alfresco.module.org_alfresco_module_rm.permission;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.domain.permissions.AclDAO;
import org.alfresco.repo.security.permissions.AccessControlEntry;
import org.alfresco.repo.security.permissions.AccessControlList;
import org.alfresco.repo.security.permissions.PermissionEntry;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.repo.security.permissions.impl.ModelDAO;
import org.alfresco.repo.security.permissions.impl.PermissionServiceImpl;
import org.alfresco.repo.security.permissions.impl.RequiredPermission;
import org.alfresco.repo.security.permissions.impl.SimplePermissionReference;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

public class OtherImpl extends PermissionServiceImpl
{
    static SimplePermissionReference RM_OLD_ALL_PERMISSIONS_REFERENCE = SimplePermissionReference.getPermissionReference(
            QName.createQName("", PermissionService.ALL_PERMISSIONS),
            PermissionService.ALL_PERMISSIONS);
    
    private SimpleCache<Serializable, Set<String>> rmReadersCache;
    
    private AclDAO rmAclDaoComponent;
    
    private ModelDAO rmModelDao;
    
    public void setRmReadersCache(SimpleCache<Serializable, Set<String>> rmReadersCache)
    {
        this.rmReadersCache = rmReadersCache;
    }
    
    public void setRmAclDAO(AclDAO rmAclDaoComponent)
    {
        this.rmAclDaoComponent = rmAclDaoComponent;
    }
    
    public void setRmModelDAO(ModelDAO rmModelDao)
    {
        this.rmModelDao = rmModelDao;
    }
    
    @Override
    public void setAnyDenyDenies(boolean anyDenyDenies)
    {
        super.setAnyDenyDenies(anyDenyDenies);
        rmReadersCache.clear();
    }
    
    @Override
    public Set<String> getReaders(Long aclId)
    {
        Set<String> dmReaders =  super.getReaders(aclId);
        
        Set<String> rmReaders = rmReadersCache.get(aclId);
        if (rmReaders == null)
        {
            rmReaders = buildRMReaders(aclId);
            rmReadersCache.put(aclId, rmReaders);
        }
        
        Set<String> result = new HashSet<String>();
        result.addAll(dmReaders);
        result.addAll(rmReaders);
        return result;
    }
    
    private Set<String> buildRMReaders(Long aclId)
    {
        AccessControlList acl = rmAclDaoComponent.getAccessControlList(aclId);
        if (acl == null)
        {
            return Collections.emptySet();
        }

        HashSet<String> assigned = new HashSet<String>();
        HashSet<String> readers = new HashSet<String>();

        for (AccessControlEntry ace : acl.getEntries())
        {
            assigned.add(ace.getAuthority());
        }

        PermissionReference permissionRef = getPermissionReference(RMPermissionModel.READ_RECORDS);
        
        for (String authority : assigned)
        {
            RMUnconditionalAclTest rmTest = new RMUnconditionalAclTest(permissionRef);
            if (rmTest.evaluate(authority, aclId))
            {
                readers.add(authority);
            }
        }
                        
        return Collections.unmodifiableSet(readers);
    }
    
    /**
     * Ignores type and aspect requirements on the node
     *
     */
    private class RMUnconditionalAclTest
    {
        /*
         * The required permission.
         */
        PermissionReference required;

        /*
         * Granters of the permission
         */
        Set<PermissionReference> granters;

        /*
         * The additional permissions required at the node level.
         */
        Set<PermissionReference> nodeRequirements = new HashSet<PermissionReference>();

        /*
         * Constructor just gets the additional requirements
         */
        RMUnconditionalAclTest(PermissionReference required)
        {
            this.required = required;

            // Set the required node permissions
            if (required.equals(getPermissionReference(ALL_PERMISSIONS)))
            {
                nodeRequirements = rmModelDao.getUnconditionalRequiredPermissions(getPermissionReference(PermissionService.FULL_CONTROL), RequiredPermission.On.NODE);
            }
            else
            {
                nodeRequirements = rmModelDao.getUnconditionalRequiredPermissions(required, RequiredPermission.On.NODE);
            }

            if (rmModelDao.getUnconditionalRequiredPermissions(required, RequiredPermission.On.PARENT).size() > 0)
            {
                throw new IllegalStateException("Parent permissions can not be checked for an acl");
            }

            if (rmModelDao.getUnconditionalRequiredPermissions(required,  RequiredPermission.On.CHILDREN).size() > 0)
            {
                throw new IllegalStateException("Child permissions can not be checked for an acl");
            }

            // Find all the permissions that grant the allowed permission
            // All permissions are treated specially.
            granters = new LinkedHashSet<PermissionReference>(128, 1.0f);
            granters.addAll(rmModelDao.getGrantingPermissions(required));
            granters.add(getAllPermissionReference());
            granters.add(RM_OLD_ALL_PERMISSIONS_REFERENCE);
        }

        /**
         * Internal hook point for recursion
         * 
         * @param authorisations
         * @param nodeRef
         * @param denied
         * @param recursiveIn
         * @return true if granted
         */
        boolean evaluate(String authority, Long aclId)
        {
            // Start out true and "and" all other results
            boolean success = true;

            // Check the required permissions but not for sets they rely on
            // their underlying permissions
            //if (modelDAO.checkPermission(required))
            //{

                // We have to do the test as no parent will help us out
                success &= hasSinglePermission(authority, aclId);

                if (!success)
                {
                    return false;
                }
            //}

            // Check the other permissions required on the node
            for (PermissionReference pr : nodeRequirements)
            {
                // Build a new test
                RMUnconditionalAclTest nt = new RMUnconditionalAclTest(pr);
                success &= nt.evaluate(authority, aclId);
                if (!success)
                {
                    return false;
                }
            }

            return success;
        }

        boolean hasSinglePermission(String authority, Long aclId)
        {
            // Check global permission

            if (checkGlobalPermissions(authority))
            {
                return true;
            }

            if(aclId == null)
            {
                return false;
            }
            else
            {
                return checkRequired(authority, aclId);
            }

        }

        /**
         * Check if we have a global permission
         * 
         * @param authorisations
         * @return true if granted
         */
        private boolean checkGlobalPermissions(String authority)
        {
            for (PermissionEntry pe : rmModelDao.getGlobalPermissionEntries())
            {
                if (isGranted(pe, authority))
                {
                    return true;
                }
            }
            return false;
        }

        /**
         * Check that a given authentication is available on a node
         * 
         * @param authorisations
         * @param nodeRef
         * @param denied
         * @return true if a check is required
         */
        boolean checkRequired(String authority, Long aclId)
        {
            AccessControlList acl = rmAclDaoComponent.getAccessControlList(aclId);

            if (acl == null)
            {
                return false;
            }

            Set<Pair<String, PermissionReference>> denied = new HashSet<Pair<String, PermissionReference>>();

            // Check if each permission allows - the first wins.
            // We could have other voting style mechanisms here
            for (AccessControlEntry ace : acl.getEntries())
            {
                if (isGranted(ace, authority, denied))
                {
                    return true;
                }
            }
            return false;
        }

        /**
         * Is a permission granted
         * 
         * @param pe -
         *            the permissions entry to consider
         * @param granters -
         *            the set of granters
         * @param authorisations -
         *            the set of authorities
         * @param denied -
         *            the set of denied permissions/authority pais
         * @return true if granted
         */
        private boolean isGranted(AccessControlEntry ace, String authority, Set<Pair<String, PermissionReference>> denied)
        {
            // If the permission entry denies then we just deny
            if (ace.getAccessStatus() == AccessStatus.DENIED)
            {
                denied.add(new Pair<String, PermissionReference>(ace.getAuthority(), ace.getPermission()));

                Set<PermissionReference> granters = rmModelDao.getGrantingPermissions(ace.getPermission());
                for (PermissionReference granter : granters)
                {
                    denied.add(new Pair<String, PermissionReference>(ace.getAuthority(), granter));
                }

                // All the things granted by this permission must be
                // denied
                Set<PermissionReference> grantees = rmModelDao.getGranteePermissions(ace.getPermission());
                for (PermissionReference grantee : grantees)
                {
                    denied.add(new Pair<String, PermissionReference>(ace.getAuthority(), grantee));
                }

                // All permission excludes all permissions available for
                // the node.
                if (ace.getPermission().equals(getAllPermissionReference()) || ace.getPermission().equals(RM_OLD_ALL_PERMISSIONS_REFERENCE))
                {
                    for (PermissionReference deny : rmModelDao.getAllPermissions())
                    {
                        denied.add(new Pair<String, PermissionReference>(ace.getAuthority(), deny));
                    }
                }

                return false;
            }

            // The permission is allowed but we deny it as it is in the denied
            // set

            if (denied != null)
            {
                Pair<String, PermissionReference> specific = new Pair<String, PermissionReference>(ace.getAuthority(), required);
                if (denied.contains(specific))
                {
                    return false;
                }
            }

            // If the permission has a match in both the authorities and
            // granters list it is allowed
            // It applies to the current user and it is granted
            if (authority.equals(ace.getAuthority()) && granters.contains(ace.getPermission()))
            {
                {
                    return true;
                }
            }

            // Default deny
            return false;
        }

        private boolean isGranted(PermissionEntry pe, String authority)
        {
            // If the permission entry denies then we just deny
            if (pe.isDenied())
            {
                return false;
            }

            // If the permission has a match in both the authorities and
            // granters list it is allowed
            // It applies to the current user and it is granted
            if (granters.contains(pe.getPermissionReference()) && authority.equals(pe.getAuthority()))
            {
                {
                    return true;
                }
            }

            // Default deny
            return false;
        }
    }
}
