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

package org.alfresco.module.org_alfresco_module_rm.security;

import java.util.Set;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;

/**
 * Extended security service.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
@AlfrescoPublicApi
public interface ExtendedSecurityService extends DeprecatedExtendedSecurityService
{
    /** IPR group prefix */
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
     * @return {@link Set}&lt;{@link String}&gt;  set of extended readers
     */
    Set<String> getReaders(NodeRef nodeRef);

    /**
     * Get the set of authorities that are extended writers for the given node.
     *
     * @param nodeRef   node reference
     * @return {@link Set}&lt;{@link String}&gt; set of extended writers
     */
    Set<String> getWriters(NodeRef nodeRef);
    
    /**
     * Helper to allow caller to provide authority sets as a pair where the
     * first is the readers and the second is the writers.
     * 
     * @see #set(NodeRef, Set, Set)
     * 
     * @param nodeRef               node reference
     * @param readersAndWriters     pair where first is the set of readers and the
     *                              second is the set of writers
     */
    void set(NodeRef nodeRef, Pair<Set<String>, Set<String>> readersAndWriters);
    
    /**
     * Set extended security for a node, where the readers will be granted ReadRecord
     * permission and ViewRecord capability to the node and where the writers will be 
     * granted Filling permission and Filling capability to the node.
     * <p>
     * Note it is vaild to provide 'null' values for readers and/or writers.
     * 
     * @param nodeRef   node reference
     * @param readers   set of readers
     * @param writers   set of writers
     * 
     * @since 2.5
     */
    void set(NodeRef nodeRef, Set<String> readers, Set<String> writers);
    
    /**
     * Removes all extended security from a node.
     * 
     * @param nodeRef   node reference
     */
    void remove(NodeRef nodeRef);
}
