/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */

package org.alfresco.repo.virtual.ref;

import java.util.HashSet;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.db.traitextender.NodeServiceTrait;
import org.alfresco.repo.virtual.ActualEnvironment;
import org.alfresco.repo.virtual.ActualEnvironmentException;
import org.alfresco.repo.virtual.VirtualContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

public class GetAspectsMethod extends AbstractProtocolMethod<Set<QName>>
{
    private NodeServiceTrait nodeServiceTrait;

    private ActualEnvironment environment;

    public GetAspectsMethod(NodeServiceTrait nodeServiceTrait, ActualEnvironment environment)
    {
        super();
        this.nodeServiceTrait = nodeServiceTrait;
        this.environment = environment;
    }

    private Set<QName> createVirtualAspects()
    {
        Set<QName> aspects = new HashSet<QName>();
        aspects.add(VirtualContentModel.ASPECT_VIRTUAL);
        aspects.add(ContentModel.ASPECT_TITLED);
        return aspects;
    }

    private Set<QName> createVirtualDocumentAspects()
    {
        Set<QName> aspects = new HashSet<QName>();
        aspects.add(VirtualContentModel.ASPECT_VIRTUAL_DOCUMENT);
        return aspects;
    }

    @Override
    public Set<QName> execute(NodeProtocol protocol, Reference reference) throws ProtocolMethodException
    {

        NodeRef nodeRef = protocol.getNodeRef(reference);
        Set<QName> nodeAspects = nodeServiceTrait.getAspects(nodeRef);
        Set<QName> aspects = createVirtualDocumentAspects();
        aspects.addAll(nodeAspects);
        return aspects;
    }

    @Override
    public Set<QName> execute(VirtualProtocol virtualProtocol, Reference reference) throws ProtocolMethodException
    {
        try
        {
            Set<QName> aspects = createVirtualAspects();
            String path = virtualProtocol.getTemplatePath(reference);
            if (PATH_SEPARATOR.equals(path.trim()))
            {
                NodeRef nodeRef;

                nodeRef = virtualProtocol.getActualNodeLocation(reference).asNodeRef(environment);

                Set<QName> nodeAspects = nodeServiceTrait.getAspects(nodeRef);
                aspects.addAll(nodeAspects);
            }
            return aspects;
        }
        catch (ActualEnvironmentException e)
        {
            throw new ProtocolMethodException(e);
        }
    }
}
