/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.opencmis.mapping;

import org.alfresco.opencmis.CMISConnector;
import org.alfresco.opencmis.dictionary.CMISActionEvaluator;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;

public class RootFolderEvaluator extends AbstractActionEvaluator<NodeRef>
{
    private CMISConnector cmisConnector;
    private CMISActionEvaluator<NodeRef> folderEvaluator;
    private boolean rootFolderValue;

    protected RootFolderEvaluator(ServiceRegistry serviceRegistry, CMISConnector cmisConnector,
            CMISActionEvaluator<NodeRef> folderEvaluator, boolean rootFolderValue)
    {
        super(serviceRegistry, folderEvaluator.getAction());
        this.cmisConnector = cmisConnector;
        this.folderEvaluator = folderEvaluator;
        this.rootFolderValue = rootFolderValue;
    }

    public boolean isAllowed(NodeRef nodeRef)
    {
        if (cmisConnector.getRootNodeRef().equals(nodeRef))
        {
            return rootFolderValue;
        }

        return folderEvaluator.isAllowed(nodeRef);
    }
}
