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

package org.alfresco.repo.transfer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;

/**
 * This class can be used to find nodes that are associated with peer associations 
 * (as opposed to child associations).
 * 
 * @author brian
 * @since 3.3
 */
public class PeerAssociatedNodeFinder extends AbstractNodeFinder
{
    private Set<QName> suppliedAssociationTypes = new HashSet<QName>();
    private boolean exclude = false;
    private boolean initialised = false;
    private List<QName> peerAssociationTypes = new ArrayList<QName>();

    public PeerAssociatedNodeFinder()
    {
    }

    public PeerAssociatedNodeFinder(Collection<QName> associationTypeNames)
    {
        setAssociationTypes(associationTypeNames);
    }

    public PeerAssociatedNodeFinder(QName... associationTypeNames)
    {
        setAssociationTypes(associationTypeNames);
    }

    public PeerAssociatedNodeFinder(Collection<QName> associationTypeNames, boolean exclude)
    {
        setAssociationTypes(associationTypeNames);
        this.exclude = exclude;
    }

    /**
     * @param exclude
     *            Set to true to exclude the specified association types, and false to include only the specified
     *            association types.
     */
    public void setExclude(boolean exclude)
    {
        this.exclude = exclude;
    }

    public void setAssociationTypes(QName... associationTypes)
    {
        setAssociationTypes(Arrays.asList(associationTypes));
    }

    public void setAssociationTypes(Collection<QName> associationTypes)
    {
        suppliedAssociationTypes = new HashSet<QName>(associationTypes);
        initialised = false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.service.cmr.transfer.NodeFinder#findFrom(org.alfresco.service.cmr.repository.NodeRef,
     * org.alfresco.service.ServiceRegistry)
     */
    public Set<NodeRef> findFrom(NodeRef thisNode)
    {
        if (!initialised)
        {
            init();
        }
        if (exclude)
        {
            return processExcludedSet(thisNode);
        }
        else
        {
            return processIncludedSet(thisNode);
        }
    }

    /**
     * @param thisNode NodeRef
     * @return Set<NodeRef>
     */
    private Set<NodeRef> processExcludedSet(NodeRef thisNode)
    {
        Set<NodeRef> results = new HashSet<NodeRef>(89);
        NodeService nodeService = serviceRegistry.getNodeService();

        // Find any peer nodes (filtering as necessary)
        List<AssociationRef> targets = nodeService.getTargetAssocs(thisNode, RegexQNamePattern.MATCH_ALL);
        boolean filterPeers = !peerAssociationTypes.isEmpty();
        for (AssociationRef target : targets)
        {
            if (!filterPeers || !peerAssociationTypes.contains(target.getTypeQName()))
            {
                results.add(target.getTargetRef());
            }
        }
        return results;
    }

    private Set<NodeRef> processIncludedSet(NodeRef startingNode)
    {
        NodeService nodeService = serviceRegistry.getNodeService();
        Set<NodeRef> foundNodes = new HashSet<NodeRef>(89);
        for (QName assocType : peerAssociationTypes)
        {
            List<AssociationRef> targets = nodeService.getTargetAssocs(startingNode, assocType);
            for (AssociationRef target : targets)
            {
                foundNodes.add(target.getTargetRef());
            }
        }
        return foundNodes;
    }

    public void init()
    {
        super.init();
        DictionaryService dictionaryService = serviceRegistry.getDictionaryService();
        peerAssociationTypes.clear();
        for (QName associationType : suppliedAssociationTypes)
        {
            AssociationDefinition assocDef = dictionaryService.getAssociation(associationType);
            if (assocDef != null && !assocDef.isChild())
            {
                peerAssociationTypes.add(associationType);
            }
        }
        initialised = true;
    }
}
