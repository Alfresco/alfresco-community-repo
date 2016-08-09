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
     * 
     * @param nodeRef
     * @param readersAndWriters
     */
    void set(NodeRef nodeRef, Pair<Set<String>, Set<String>> readersAndWriters);
    
    /**
     * 
     * @param nodeRef
     * @param readers
     * @param writers
     */
    void set(NodeRef nodeRef, Set<String> readers, Set<String> writers);
    
    /**
     * 
     * @param nodeRef
     */
    void remove(NodeRef nodeRef);
}
