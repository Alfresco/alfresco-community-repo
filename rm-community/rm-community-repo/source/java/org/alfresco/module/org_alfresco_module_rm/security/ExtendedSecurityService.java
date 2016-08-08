/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

package org.alfresco.module.org_alfresco_module_rm.security;

import java.util.Set;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Extended security service.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
@AlfrescoPublicApi
public interface ExtendedSecurityService
{
    static final String IPR_GROUP_PREFIX = "IPR";
    
	/**
	 * Indicates whether a node has extended security.
	 *
	 * @param nodeRef      node reference
	 * @return boolean     true if the node has extended security, false otherwise
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
     * As of, 2.5 this method no longer applies the extended security to parents.
     *
     * @param nodeRef   node reference
     * @param readers   set of authorities to add extended read permissions
     * @param writers   set of authorities to add extended write permissions
     *
     */
    // TODO rename to setExtendedSecurity to reflect that this doesn't update the extended security any more 
    void addExtendedSecurity(NodeRef nodeRef, Set<String> readers, Set<String> writers);

    /**
     * Remove all extended readers and writers from the given node reference.
     *
     * @param nodeRef   node reference
     */
    // TODO rename to removeExtendedSecurity 
    void removeAllExtendedSecurity(NodeRef nodeRef);
    
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
     * 
     * @deprecated as of 2.5, because extended security is no longer applied to parents.  Note that calling this method will
     * only apply the extended security to the node and the applyToParents parameter value will be ignored.
     * 
     * @see #addExtendedSecurity(NodeRef, Set, Set)
     */
    @Deprecated void addExtendedSecurity(NodeRef nodeRef, Set<String> readers, Set<String> writers, boolean applyToParents);
    
    /**
     * Remove the extended security for the specified authorities from a node.
     *
     * @param nodeRef   node reference
     * @param readers   set of authorities to remove as extended readers
     * @param writers   set of authorities to remove as extended writers
     * 
     * @deprecated as of 2.5, because partial removal of readers and writers from node or parents is no longer supported.
     * Note that calling this method will now remove all extended security from the node and never applied to parents.
     * 
     * @see #removeAllExtendedSecurity(NodeRef)
     */
    @Deprecated void removeExtendedSecurity(NodeRef nodeRef, Set<String> readers, Set<String> writers);

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
     * 
     * @deprecated as of 2.5, because partial removal of readers and writers from node or parents is no longer supported.
     * Note that calling this method will now remove all extended security from the node and never applied to parents.
     * 
     * @see #removeAllExtendedSecurity(NodeRef)
     */
    @Deprecated void removeExtendedSecurity(NodeRef nodeRef, Set<String> readers, Set<String> writers, boolean applyToParents);    

    /**
     * Remove all extended readers and writers from the given node reference.
     *
     * @param nodeRef           node reference
     * @param applyToParents    if true then apply removal to parent hierarchy (read only) false otherwise.
     * 
     * @deprecated as of 2.5, because partial removal of readers and writers from node or parents is no longer supported.
     * Note that calling this method will now remove all extended security from the node and never applied to parents.
     * 
     * @see #removeAllExtendedSecurity(NodeRef)
     */
    @Deprecated void removeAllExtendedSecurity(NodeRef nodeRef, boolean applyToParents);
}
