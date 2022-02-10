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

package org.alfresco.module.org_alfresco_module_rm.relationship;

import java.util.Set;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * The relationship service interface
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
@AlfrescoPublicApi
public interface RelationshipService
{
    /** System relationship names */
    static final String RELATIONSHIP_VERSIONS = "versions";

    /**
     * Gets all the existing relationship definitions
     *
     * @return All existing relationship definitions
     */
    Set<RelationshipDefinition> getRelationshipDefinitions();

    /**
     * Gets the relationship definition for the given unique name
     *
     * @param uniqueName The unique name of the relationship definition
     * @return The relationship definition for the given unique name if it exist, <code>null</code> otherwise
     */
    RelationshipDefinition getRelationshipDefinition(String uniqueName);

    /**
     * Creates a relationship definition using the display name
     *
     * @param displayName The display name of the relationship definition
     * @return The new relationship definition
     */
    RelationshipDefinition createRelationshipDefinition(RelationshipDisplayName displayName);

    /**
     * Updates an existing relationship definition
     *
     * @param uniqueName The unique name of the relationship definition
     * @param displayName The display name of the relationship definition
     * @return The updated relationship definition
     */
    RelationshipDefinition updateRelationshipDefinition(String uniqueName, RelationshipDisplayName displayName);

    /**
     * Removes a relationship definition
     *
     * @param uniqueName The unique name of the relationship definition
     * @return <code>true</code> if the relationship definition was removed successfully, <code>false</code> otherwise
     */
    boolean removeRelationshipDefinition(String uniqueName);

    /**
     * Checks if a relationship exists or not
     *
     * @param uniqueName The unique name of the relationship definition
     * @return <code>true</code> if the relationship definition exists, <code>false</code> otherwise
     */
    boolean existsRelationshipDefinition(String uniqueName);

    /**
     * Gets all the relationships that come out from the given node reference
     *
     * @param nodeRef The node reference
     * @return All relationships that come out from the given node reference
     */
    Set<Relationship> getRelationshipsFrom(NodeRef nodeRef);

    /**
     * Gets all the relationships that come out from the given node reference
     * that match the a given name filter.
     * <p>
     * Exact match only.
     *
     * @param nodeRef The node reference
     * @param nameFilter Name filter for results
     * @return All relationships that come out from the given node reference
     *
     * @since 2.3.1
     */
    Set<Relationship> getRelationshipsFrom(NodeRef nodeRef, String nameFilter);

    /**
     * Gets all the relationships that go into the given node reference
     *
     * @param nodeRef The node reference
     * @return All relationships that go into the given node reference
     */
    Set<Relationship> getRelationshipsTo(NodeRef nodeRef);

    /**
     * Gets all the relationships that go into the given node reference
     * that match the a given name filter.
     * <p>
     * Exact match only.
     *
     * @param nodeRef The node reference
     * @param nameFilter Name filter for results
     * @return All relationships that go into the given node reference
     *
     * @since 2.3.1
     */
    Set<Relationship> getRelationshipsTo(NodeRef nodeRef, String nameFilter);

    /**
     * Adds a relationship from the given node <code>source</code>
     * to the give node <code>target</code> with the given unique name
     *
     * @param uniqueName The unique name of the relationship
     * @param source The node reference which the relationship come from
     * @param target The node reference which the relationship go to
     */
    void addRelationship(String uniqueName, NodeRef source, NodeRef target);

    /**
     * Removes the relationship from the given node <code>source</code>
     * to the given node <code>target</code> with the given unique name
     *
     * @param uniqueName The unique name of the relationship
     * @param source The node reference which the relationship come from
     * @param target The node reference which the relationship go to
     */
    void removeRelationship(String uniqueName, NodeRef source, NodeRef target);
}
