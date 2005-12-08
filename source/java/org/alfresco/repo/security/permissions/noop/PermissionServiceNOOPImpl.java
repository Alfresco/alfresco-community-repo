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
package org.alfresco.repo.security.permissions.noop;

import java.util.HashSet;
import java.util.Set;

import org.alfresco.repo.security.permissions.NodePermissionEntry;
import org.alfresco.repo.security.permissions.PermissionEntry;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.repo.security.permissions.PermissionServiceSPI;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.namespace.QName;


/**
 * Dummy implementation of Permissions Service
 *  
 */
public class PermissionServiceNOOPImpl
    implements PermissionServiceSPI
{
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.security.permissions.PermissionService#getOwnerAuthority()
     */
    public String getOwnerAuthority()
    {
        return OWNER_AUTHORITY;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.security.permissions.PermissionService#getAllAuthorities()
     */
    public String getAllAuthorities()
    {
        return ALL_AUTHORITIES;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.security.permissions.PermissionService#getAllPermission()
     */
    public String getAllPermission()
    {
        return ALL_PERMISSIONS;
    }    
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.security.permissions.PermissionService#getPermissions(org.alfresco.service.cmr.repository.NodeRef)
     */
    public Set<AccessPermission> getPermissions(NodeRef nodeRef)
    {
        return null;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.security.permissions.PermissionService#getAllPermissions(org.alfresco.service.cmr.repository.NodeRef)
     */
    public Set<AccessPermission> getAllSetPermissions(NodeRef nodeRef)
    {
        return null;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.security.permissions.PermissionService#getSettablePermissions(org.alfresco.service.cmr.repository.NodeRef)
     */
    public Set<String> getSettablePermissions(NodeRef nodeRef)
    {
        return getSettablePermissions((QName)null);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.security.permissions.PermissionService#getSettablePermissions(org.alfresco.service.namespace.QName)
     */
    public Set<String> getSettablePermissions(QName type)
    {
        HashSet<String> permissions = new HashSet<String>();
        permissions.add(ALL_PERMISSIONS);
        return permissions;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.security.permissions.PermissionService#hasPermission(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.repo.security.permissions.PermissionReference)
     */
    public AccessStatus hasPermission(NodeRef nodeRef, String perm)
    {
        return AccessStatus.ALLOWED;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.security.permissions.PermissionService#deletePermissions(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void deletePermissions(NodeRef nodeRef)
    {
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.security.permissions.PermissionService#deletePermission(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, org.alfresco.repo.security.permissions.PermissionReference, boolean)
     */
    public void deletePermission(NodeRef nodeRef, String authority, String perm, boolean allow)
    {
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.security.permissions.PermissionService#setPermission(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, org.alfresco.repo.security.permissions.PermissionReference, boolean)
     */
    public void setPermission(NodeRef nodeRef, String authority, String perm, boolean allow)
    {
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.security.permissions.PermissionService#setInheritParentPermissions(org.alfresco.service.cmr.repository.NodeRef, boolean)
     */
    public void setInheritParentPermissions(NodeRef nodeRef, boolean inheritParentPermissions)
    {
    }
    
    /* (non-Javadoc)
    * @see org.alfresco.service.cmr.security.PermissionService#getInheritParentPermissions(org.alfresco.service.cmr.repository.NodeRef)
    */
   public boolean getInheritParentPermissions(NodeRef nodeRef)
   {
      // TODO Auto-generated method stub
      return true;
   }

   public void clearPermission(NodeRef nodeRef, String authority)
    {
        
    }

    // SPI
    
    public void deletePermission(PermissionEntry permissionEntry)
    {
        
    }

    public void deletePermissions(NodePermissionEntry nodePermissionEntry)
    {
        
    }

    public void deletePermissions(String recipient)
    {
        
    }

    public NodePermissionEntry explainPermission(NodeRef nodeRef, PermissionReference perm)
    {
       throw new UnsupportedOperationException();
    }

    public PermissionReference getAllPermissionReference()
    {
        throw new UnsupportedOperationException();
    }

    public String getPermission(PermissionReference permissionReference)
    {
        throw new UnsupportedOperationException();
    }

    public PermissionReference getPermissionReference(QName qname, String permissionName)
    {
        throw new UnsupportedOperationException();
    }

    public PermissionReference getPermissionReference(String permissionName)
    {
        throw new UnsupportedOperationException();
    }

    public NodePermissionEntry getSetPermissions(NodeRef nodeRef)
    {
        throw new UnsupportedOperationException();
    }

    public Set<PermissionReference> getSettablePermissionReferences(NodeRef nodeRef)
    {
        throw new UnsupportedOperationException();
    }

    public Set<PermissionReference> getSettablePermissionReferences(QName type)
    {
        throw new UnsupportedOperationException();
    }

    public AccessStatus hasPermission(NodeRef nodeRef, PermissionReference perm)
    {
        throw new UnsupportedOperationException();
    }

    public void setPermission(NodePermissionEntry nodePermissionEntry)
    {
        throw new UnsupportedOperationException();
    }

    public void setPermission(PermissionEntry permissionEntry)
    {
        throw new UnsupportedOperationException();
    }
}
