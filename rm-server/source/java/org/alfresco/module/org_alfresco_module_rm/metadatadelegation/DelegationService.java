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

import static org.alfresco.module.org_alfresco_module_rm.metadatadelegation.DelegationException.DelegationNotFound;

import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import java.io.Serializable;
import java.util.Map;

/**
 * This service provides read-only access to delegated metadata.
 * TODO complete.
 *
 * @author Neil Mc Erlean
 * @since 3.0.a
 */
public interface DelegationService
{
    /**
     * Checks if the specified nodeRef has an attached {@link Delegation} for the specified aspect.
     *
     * @param nodeRef    the nodeRef which may or may not have a delegate node for the specified aspect.
     * @param aspectName the name of the aspect for which the node may or may not have delegation.
     * @return whether the node is delegating metadata reads for the specified aspect.
     * @throws InvalidNodeRefException if the supplied nodeRef does not exist.
     * @throws DelegationNotFound if no delegation for the specified aspect has been attached.
     */
    boolean hasDelegateForAspect(NodeRef nodeRef, QName aspectName);

    /**
     * Gets the delegate node for the specified aspect, if there is one.
     *
     * @param nodeRef    the node with the delegate.
     * @param aspectName the aspect name.
     * @return the nodeRef of the delegate if there is one, else {@code null}.
     * @throws DelegationNotFound if no delegation for the specified aspect has been attached.
     */
    NodeRef getDelegateFor(NodeRef nodeRef, QName aspectName);

    /**
     * Gets all the property values from the delegate node for the specified aspect.
     *
     * @param nodeRef    the node with the delegate.
     * @param aspectName the aspect name which holds the properties we want.
     * @return the property values as obtained from the delegate node.
     */
    Map<QName, Serializable> getDelegateProperties(NodeRef nodeRef, QName aspectName);

    /**
     * Gets the specified property value from the delegate node.
     *
     * @param nodeRef    the node with the delegate.
     * @param propertyName the property name which we want.
     * @return the property value as obtained from the delegate node.
     */
    Serializable getDelegateProperty(NodeRef nodeRef, QName propertyName);

    /**
     * Determines if a given aspect is present on a node's delegates.
     *
     * @param nodeRef    the node for which a delegate is sought.
     * @param aspectName the aspect which is to be checked.
     * @return Returns true if the aspect has been applied to one of the given node's delegates,
     *      otherwise false
     */
    boolean hasAspectOnDelegate(NodeRef nodeRef, QName aspectName);

    /**
     * Gets all {@link Delegation delegations} currently attached to the specified node.
     *
     * @param nodeRef the node whose delegations are sought.
     * @return Returns a map of all {@link Delegation delegations} by NodeRef for the specified nodeRef.
     */
    Map<Delegation, NodeRef> getDelegations(NodeRef nodeRef);
}

