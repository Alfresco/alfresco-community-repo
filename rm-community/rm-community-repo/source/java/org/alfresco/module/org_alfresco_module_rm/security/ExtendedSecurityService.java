package org.alfresco.module.org_alfresco_module_rm.security;

import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Extended security service.
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public interface ExtendedSecurityService
{
	/**
	 * Indicates whether a node has extended security.
	 *  
	 * @param nodeRef      node reference
	 * @return boolean     true if the node has extedned security, false otherwise
	 */
    boolean hasExtendedSecurity(NodeRef nodeRef);
    
    /**
     * Gets the set of authorities that are extended readers for the given node.
     * 
     * @param nodeRef   node reference
     * @return {@link Set}<{@link String}>  set of extended readers
     */
    Set<String> getExtendedReaders(NodeRef nodeRef);
    
    /**
     * Get the set of authorities that are extended writers for the given node.
     * 
     * @param nodeRef   node reference
     * @return {@link Set}<{@link String}>  set of extended writers
     */
    Set<String> getExtendedWriters(NodeRef nodeRef);

    /**
     * Add extended security for the specified authorities to a node.
     * 
     * @param nodeRef   node reference
     * @param readers   set of authorities to add extended read permissions
     * @param writers   set of authorities to add extended write permissions
     */
    void addExtendedSecurity(NodeRef nodeRef, Set<String> readers, Set<String> writers);
    
    /**
     * Add extended security for the specified authorities to a node.
     * <p>
     * If specified, the read and write extended permissions are applied to all parents up to the file plan as 
     * extended read.  This ensures parental read, but not parental write.
     * 
     * @param nodeRef   node reference
     * @param readers   set of authorities to add extended read permissions
     * @param writers   set of authorities to add extended write permissions
     * @param applyToParents true if extended security applied to parents (read only) false otherwise.
     */
    void addExtendedSecurity(NodeRef nodeRef, Set<String> readers, Set<String> writers, boolean applyToParents);
    
    /**
     * Remove the extended security for the specified authorities from a node.
     * 
     * @param nodeRef   node reference
     * @param readers   set of authorities to remove as extended readers
     * @param writers   set of authorities to remove as extended writers
     */
    void removeExtendedSecurity(NodeRef nodeRef, Set<String> readers, Set<String> writers);
    
    /**
     * Remove the extended security for the specified authorities from a node.
     * <p>
     * If specified, extended security will also be removed from the parent hierarchy.(read only).  Note that 
     * extended security is records as a reference count, so security will only be utterly removed from the parent
     * hierarchy if all references to the authority are removed.
     * 
     * @param nodeRef           node reference
     * @param readers           set of authorities to remove as extended readers
     * @param writers           set of authorities to remove as extedned writers
     * @param applyToParents    true if removal of extended security is applied to parent hierarchy (read only), false
     *                          otherwise
     */
    void removeExtendedSecurity(NodeRef nodeRef, Set<String> readers, Set<String> writers, boolean applyToParents);
    
    /**
     * Remove all extended readers and writers from the given node reference.
     * 
     * @param nodeRef   node reference
     */
    void removeAllExtendedSecurity(NodeRef nodeRef);
    
    /**
     * Remove all extended readers and writers from the given node reference.
     * 
     * @param nodeRef           node reference
     * @param applyToParents    if true then apply removal to parent hierarchy (read only) false otherwise.
     */
    void removeAllExtendedSecurity(NodeRef nodeRef, boolean applyToParents);
}
