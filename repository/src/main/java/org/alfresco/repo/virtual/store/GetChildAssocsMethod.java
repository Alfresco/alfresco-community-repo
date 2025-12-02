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

package org.alfresco.repo.virtual.store;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.virtual.ActualEnvironment;
import org.alfresco.repo.virtual.VirtualContentModel;
import org.alfresco.repo.virtual.ref.AbstractProtocolMethod;
import org.alfresco.repo.virtual.ref.GetActualNodeRefMethod;
import org.alfresco.repo.virtual.ref.NodeProtocol;
import org.alfresco.repo.virtual.ref.ProtocolMethodException;
import org.alfresco.repo.virtual.ref.Reference;
import org.alfresco.repo.virtual.ref.VirtualProtocol;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ChildAssocsSlice;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;

public class GetChildAssocsMethod extends AbstractProtocolMethod<ChildAssocsSlice>
{
    private final VirtualStore smartStore;
    private final ActualEnvironment environment;
    private final boolean preload;
    private final int skipResults;
    private final int maxResults;
    private final QNamePattern qnamePattern;
    private final QNamePattern typeQNamePattern;

    public GetChildAssocsMethod(VirtualStore smartStore, ActualEnvironment environment, boolean preload, int skipResults,
            int maxResults, QNamePattern qnamePattern, QNamePattern typeQNamePattern)
    {
        super();
        this.smartStore = smartStore;
        this.environment = environment;
        this.preload = preload;
        this.skipResults = skipResults;
        this.maxResults = maxResults;
        this.qnamePattern = qnamePattern;
        this.typeQNamePattern = typeQNamePattern;
    }

    @Override
    public ChildAssocsSlice execute(VirtualProtocol virtualProtocol, Reference reference)
            throws ProtocolMethodException
    {
        if (!typeQNamePattern.isMatch(ContentModel.ASSOC_CONTAINS))
        {
            return ChildAssocsSlice.EMPTY;
        }
        List<ChildAssociationRef> childAssocs = new LinkedList<>();
        List<Reference> children = smartStore.list(reference);
        NodeRef nodeRefReference = reference.toNodeRef();
        int skipped = 0;
        int total = 0;
        for (Reference child : children)
        {
            NodeRef childNodeRef = child.toNodeRef();
            Serializable childName = environment.getProperty(childNodeRef, ContentModel.PROP_NAME);
            QName childAssocQName = QName.createQNameWithValidLocalName(VirtualContentModel.VIRTUAL_CONTENT_MODEL_1_0_URI, childName.toString());
            if (qnamePattern.isMatch(childAssocQName))
            {
                total++;
                if (skipped < skipResults)
                {
                    skipped++;
                    continue;
                }
                if (childAssocs.size() >= maxResults)
                {
                    continue;
                }
                ChildAssociationRef childAssoc = new ChildAssociationRef(ContentModel.ASSOC_CONTAINS,
                        nodeRefReference,
                        childAssocQName,
                        childNodeRef,
                        true,
                        -1);
                childAssocs.add(childAssoc);
            }
        }
        return new ChildAssocsSlice(childAssocs, total);
    }

    @Override
    public ChildAssocsSlice execute(NodeProtocol protocol, Reference reference) throws ProtocolMethodException
    {
        NodeRef actualNodeRef = reference.execute(new GetActualNodeRefMethod(null));
        NodeRef nodeRefReference = reference.toNodeRef();
        List<ChildAssociationRef> referenceAssociations = new LinkedList<>();
        if (environment.isSubClass(environment.getType(nodeRefReference), ContentModel.TYPE_FOLDER))
        {
            return ChildAssocsSlice.EMPTY;
        }
        var childAssocsSlice = environment.getChildAssocs(actualNodeRef,
                typeQNamePattern,
                qnamePattern,
                skipResults,
                maxResults,
                preload);
        List<ChildAssociationRef> actualAssociations = childAssocsSlice.childAssocs();
        int totalCount = childAssocsSlice.totalCount();

        for (ChildAssociationRef actualAssoc : actualAssociations)
        {
            ChildAssociationRef referenceChildAssocRef = new ChildAssociationRef(actualAssoc.getTypeQName(),
                    nodeRefReference,
                    actualAssoc.getQName(),
                    actualAssoc.getChildRef(),
                    actualAssoc.isPrimary(),
                    actualAssoc.getNthSibling());

            referenceAssociations.add(referenceChildAssocRef);
        }
        return new ChildAssocsSlice(referenceAssociations, totalCount);
    }
}
