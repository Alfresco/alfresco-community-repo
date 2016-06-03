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

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.transfer.NodeCrawler;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;

/**
 * @author brian
 * 
 * A node finder that searches for child nodes with the association specified.
 * 
 * For example, could be used to find all children with the cm:contains relationship.
 * 
 <pre> 
      NodeCrawler crawler = nodeCrawlerFactory.getNodeCrawler(); 
      crawler.setNodeFinders(new ChildAssociatedNodeFinder(ContentModel.ASSOC_CONTAINS));
      Set<NodeRef> crawledNodes = crawler.crawl(rootNode);
 </pre> 
 * @see org.alfresco.service.cmr.transfer.NodeCrawlerFactory
 * 
 */
public class ChildAssociatedNodeFinder extends AbstractNodeFinder
{
    private Set<QName> suppliedAssociationTypes = new HashSet<QName>();
    private boolean exclude = false;
    private boolean initialised = false;
    private List<QName> childAssociationTypes = new ArrayList<QName>();

    public ChildAssociatedNodeFinder()
    {
    }

    public ChildAssociatedNodeFinder(Set<QName> associationTypeNames)
    {
        setAssociationTypes(associationTypeNames);
    }

    public ChildAssociatedNodeFinder(QName... associationTypeNames)
    {
        setAssociationTypes(associationTypeNames);
    }

    public ChildAssociatedNodeFinder(Set<QName> associationTypeNames, boolean exclude)
    {
        setAssociationTypes(associationTypeNames);
        this.exclude = exclude;
    }

    public void setAssociationTypes(QName... associationTypes)
    {
        setAssociationTypes(Arrays.asList(associationTypes));
    }

    public void setAssociationTypes(Collection<QName> associationTypes)
    {
        this.suppliedAssociationTypes = new HashSet<QName>(associationTypes);
        initialised = false;
    }

    /**
     * @param exclude
     *            the exclude to set
     */
    public void setExclude(boolean exclude)
    {
        this.exclude = exclude;
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

        // Find all the child nodes (filtering as necessary).
        List<ChildAssociationRef> children = nodeService.getChildAssocs(thisNode);
        boolean filterChildren = !childAssociationTypes.isEmpty();
        for (ChildAssociationRef child : children)
        {
            if (!filterChildren || !childAssociationTypes.contains(child.getTypeQName()))
            {
                results.add(child.getChildRef());
            }
        }
        return results;
    }

    private Set<NodeRef> processIncludedSet(NodeRef startingNode)
    {
        NodeService nodeService = serviceRegistry.getNodeService();
        Set<NodeRef> foundNodes = new HashSet<NodeRef>(89);
        for (QName assocType : childAssociationTypes)
        {
            List<ChildAssociationRef> children = nodeService.getChildAssocs(startingNode, assocType,
                    RegexQNamePattern.MATCH_ALL);
            for (ChildAssociationRef child : children)
            {
                foundNodes.add(child.getChildRef());
            }
        }
        return foundNodes;
    }

    public void init()
    {
        super.init();
        // Quickly scan the supplied association types and remove any that either
        // do not exist or are not child association types.
        DictionaryService dictionaryService = serviceRegistry.getDictionaryService();
        childAssociationTypes.clear();
        for (QName associationType : suppliedAssociationTypes)
        {
            AssociationDefinition assocDef = dictionaryService.getAssociation(associationType);
            if (assocDef != null && assocDef.isChild())
            {
                childAssociationTypes.add(associationType);
            }
        }
        initialised = true;
    }

}
