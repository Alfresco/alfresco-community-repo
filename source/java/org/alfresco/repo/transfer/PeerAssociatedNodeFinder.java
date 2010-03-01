/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
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

package org.alfresco.repo.transfer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.transfer.NodeFinder;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * @author brian
 * 
 */
public class PeerAssociatedNodeFinder implements NodeFinder
{
    private Set<QName> suppliedAssociationTypes = new HashSet<QName>();
    private boolean exclude = false;
    private boolean initialised = false;
    private List<QName> peerAssociationTypes = new ArrayList<QName>();
    private ServiceRegistry serviceRegistry;

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

    public PeerAssociatedNodeFinder(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    public PeerAssociatedNodeFinder(ServiceRegistry serviceRegistry, Collection<QName> associationTypeNames)
    {
        this(serviceRegistry);
        setAssociationTypes(associationTypeNames);
    }

    public PeerAssociatedNodeFinder(ServiceRegistry serviceRegistry, QName... associationTypeNames)
    {
        this(serviceRegistry);
        setAssociationTypes(associationTypeNames);
    }

    public PeerAssociatedNodeFinder(ServiceRegistry serviceRegistry, Collection<QName> associationTypeNames, boolean exclude)
    {
        this(serviceRegistry);
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
     * @param thisNode
     * @param serviceRegistry
     * @return
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
        ParameterCheck.mandatory("serviceRegistry", serviceRegistry);
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

    /**
     * @param serviceRegistry
     *            the serviceRegistry to set
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

}
