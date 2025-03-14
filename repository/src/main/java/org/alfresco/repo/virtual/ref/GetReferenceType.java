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

import org.alfresco.model.ContentModel;
import org.alfresco.repo.virtual.ActualEnvironment;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Returns actual {@link QName} Type of a given reference <br>
 * indicated by the given protocol reference.
 */
public class GetReferenceType extends AbstractProtocolMethod<QName>
{
    private ActualEnvironment environment;

    public GetReferenceType(ActualEnvironment environment)
    {
        super();
        this.environment = environment;
    }

    @Override
    public QName execute(NodeProtocol protocol, Reference reference) throws ProtocolMethodException
    {
        NodeRef nodeRef = protocol.getNodeRef(reference);

        return environment.getType(nodeRef);
    }

    @Override
    public QName execute(VirtualProtocol virtualProtocol, Reference reference) throws ProtocolMethodException
    {
        return ContentModel.TYPE_FOLDER;
    }

}
