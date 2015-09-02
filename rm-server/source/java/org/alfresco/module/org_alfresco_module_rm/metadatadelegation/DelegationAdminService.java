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
package org.alfresco.module.org_alfresco_module_rm.metadatadelegation;

import static org.alfresco.module.org_alfresco_module_rm.metadatadelegation.DelegationException.ChainedDelegationUnsupported;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import java.util.Set;

/**
 * A service to manage the delegation of aspect metadata.
 * Using this service a node can be {@link #attachDelegate linked} to a delegate node for a configured set of aspects.
 * (Note that this delegate node must already exist within the database.)
 * Then any read request for relevant metadata such as hasAspect or getProperties can be delegated to the
 * linked node.
 * <p/>
 * For a link to be made, there must be a {@link #getDefinedDelegations() defined Delegation} already in the system.
 * This means that a peer-association type will have to have been declared and that a spring bean will have to have
 * defined which aspects are to be handled by this {@code Delegation}.
 *
 * @author Neil Mc Erlean
 * @since 3.0.a
 */
public interface DelegationAdminService
{
    /**
     * Creates a link between two nodes such that the first {@code nodeRef} can 'inherit' or reuse some aspect
     * metadata from another node - the {@code delegateNodeRef}.
     * <p/>
     * Note that links can currently only extend between two pairs of nodes and cannot be chained.
     *
     * @param nodeRef         the node which is to inherit additional metadata.
     * @param delegateNodeRef the node which will provide the additional metadata.
     * @param assocType       the type of the peer association which will link the two nodes.
     * @return a {@link Delegation} object which defines the link type.
     * @throws ChainedDelegationUnsupported if an attempt is made to attach nodes such that a chain would be made.
     */
    Delegation attachDelegate(NodeRef nodeRef, NodeRef delegateNodeRef, QName assocType);

    /**
     * Removes an existing metadata delegation link between two nodes.
     *
     * @param nodeRef   the node which has been linked to a delegate.
     * @param assocType the type of the peer assocation forming the link.
     * @return the removed {@link Delegation}.
     */
    Delegation detachDelegate(NodeRef nodeRef, QName assocType);

    /**
     * Gets the set of defined {@link Delegation}s.
     *
     * @return the set of defined Delegations.
     */
    Set<Delegation> getDefinedDelegations();

    /**
     * Gets the {@link Delegation} which contains the specified {@code aspectName} if there is one.
     * Note that only one {@link Delegation} may declare that it handles any particular aspect.
     *
     * @param aspectName the name of the aspect whose {@link Delegation} is sought.
     * @return the {@link Delegation} which handles the specified aspect, if there is one.
     */
    Delegation getDelegationFor(QName aspectName);

    /**
     * Gets the set of {@link Delegation}s which are in effect from the specified {@code nodeRef}.
     * From these, you can retrieve the types of peer associations which are linked to the specified
     * {@code nodeRef} as well as the aspect types that are handled.
     *
     * @param nodeRef the NodeRef whose delegations are sought.
     * @return the set of {@link Delegation}s from the specified nodeRef.
     */
    Set<Delegation> getDelegationsFrom(NodeRef nodeRef);
}
