/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts.archive;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * This class is used to filter nodes based on node type.
 * 
 * @author Neil McErlean
 * @since 3.5
 */
public class NodeTypeFilter implements ArchivedNodesFilter
{
    private NodeService nodeService;
    private NamespaceService namespaceService;
    private List<QName> excludedTypes;
    
    /**
     * This method sets the NamespaceService object.
     * @param namespaceService the namespaceService.
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    /**
     * This method sets the NodeService object.
     * @param nodeService the node service.
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * Sets the List of node types to exclude. These node types should be in the
     * short form e.g. cm:myType.
     * 
     * @param excludedTypesStg a List of node types which are to be excluded.
     */
    public void setExcludedTypes(List<String> excludedTypesStg)
    {
        // Convert the Strings to QNames.
        this.excludedTypes = new ArrayList<QName>(excludedTypesStg.size());
        for (String s : excludedTypesStg)
        {
            QName typeQName = QName.createQName(s, namespaceService);
            this.excludedTypes.add(typeQName);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.web.scripts.archive.ArchivedNodesFilter#accept(org.alfresco.service.cmr.repository.NodeRef)
     */
    public boolean accept(NodeRef nodeRef)
    {
        boolean typeIsExcluded = this.excludedTypes.contains(nodeService.getType(nodeRef));
        return !typeIsExcluded;
    }
}

