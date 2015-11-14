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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see http://www.gnu.org/licenses/.
 */

package org.alfresco.repo.virtual.store;

import org.alfresco.repo.virtual.ActualEnvironment;
import org.alfresco.repo.virtual.ActualEnvironmentException;
import org.alfresco.repo.virtual.VirtualizationException;
import org.alfresco.repo.virtual.ref.Reference;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * An {@link AspectVirtualizationMethod} that uses an aspect defined repository
 * association to a node that holds the template contents.
 * 
 * @author Bogdan Horje
 */
public class CustomVirtualizationMethod extends AspectVirtualizationMethod
{
    /** Template association {@link QName} */
    private QName associationQName;

    /**
     * String representation of the template association. Will be converted into
     * a {@link QName} during {@link #init()}
     */
    private String associationName;

    public CustomVirtualizationMethod()
    {

    }

    /**
     * Bean initialization.
     */
    @Override
    public void init()
    {
        super.init();
        if (associationName != null)
        {
            associationQName = QName.createQName(associationName,
                                                 namespacePrefixResolver);
        }
    }

    public void setAssociationName(String associationName)
    {
        this.associationName = associationName;
    }

    @Override
    public Reference virtualize(ActualEnvironment env, NodeRef nodeRef) throws VirtualizationException
    {
        NodeRef templateNode = env.getTargetAssocs(nodeRef,
                                                   associationQName);

        if (templateNode != null)
        {
            return newVirtualReference(env,
                                       nodeRef,
                                       templateNode);
        }
        else
        {
            // default branch - invalid virtual node
            throw new VirtualizationException("Invalid virtualization : missing template association.");
        }
    }

    @Override
    public boolean canVirtualize(ActualEnvironment env, NodeRef nodeRef) throws ActualEnvironmentException
    {
        boolean canVirtualize = super.canVirtualize(env,
                                                    nodeRef);
        if (canVirtualize)
        {
            // TODO: optimize - should not need another repository meta data access !!!

            NodeRef templateNode = env.getTargetAssocs(nodeRef,
                                                       associationQName);
            canVirtualize = templateNode != null;
        }

        return canVirtualize;
    }

}
