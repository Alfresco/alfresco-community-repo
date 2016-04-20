
package org.alfresco.repo.domain.permissions;

import java.util.List;
import java.util.Map;

import org.alfresco.repo.security.permissions.ACLType;
import org.alfresco.repo.security.permissions.impl.AclChange;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;

/**
 * This abstracts the reading and writing of ACLs on nodes from particular node implementations.
 * 
 * @author andyh
 * @author janv
 */
public interface AccessControlListDAO
{
    /**
     * Get the ACL from a node.
     * 
     * @param nodeRef
     *            The reference to the node.
     * @return The ACL.
     * @throws InvalidNodeRefException
     */
    public Acl getAccessControlList(NodeRef nodeRef);

    /**
     * Set the ACL on a node.
     * 
     * @param nodeRef
     *            The reference to the node.
     * @param acl
     *            The ACL.
     * @throws InvalidNodeRefException
     */
    public void setAccessControlList(NodeRef nodeRef, Acl acl);

    /**
     * Set the ACL on a node.
     * 
     * @param nodeRef
     *            The reference to the node.
     * @param aclId
     *            The ID of the ACL entity.
     * @throws InvalidNodeRefException          if the noderef is invalid
     */
    public void setAccessControlList(NodeRef nodeRef, Long aclId);

    /**
     * Update any associated ACLs
     */
    public void updateChangedAcls(NodeRef startingPoint, List<AclChange> changes);

    /**
     * Update inheritance
     */
    public List<AclChange> setInheritanceForChildren(NodeRef parent, Long inheritFrom, Long sharedAclToReplace);
    
    /**
     * Set the inheritance on a given node and it's children. If the operation takes 
     * too long and asyncCall parameter set accordingly, fixed ACLs method will be synchronously called.
     */
    public List<AclChange> setInheritanceForChildren(NodeRef parent, Long inheritFrom, Long sharedAclToReplace, boolean asyncCall);

    public Long getIndirectAcl(NodeRef nodeRef);

    public Long getInheritedAcl(NodeRef nodeRef);

    public void forceCopy(NodeRef nodeRef);
    
    public Map<ACLType, Integer> patchAcls();

    public Acl getAccessControlList(StoreRef storeRef);
    
    public void setAccessControlList(StoreRef storeRef, Acl acl);
    
    public void updateInheritance(Long childNodeId, Long oldParentAclId, Long newParentAclId);
    
    public void setFixedAcls(Long nodeId, Long inheritFrom, Long mergeFrom, Long sharedAclToReplace, List<AclChange> changes, boolean set);
}
