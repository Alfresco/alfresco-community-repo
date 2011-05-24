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

import java.io.Serializable;

import org.alfresco.model.ContentModel;
import org.alfresco.opencmis.CMISConnector;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.Path.ChildAssocElement;
import org.apache.chemistry.opencmis.commons.PropertyIds;

/**
 * Get the CMIS path property.
 * 
 * @author davidc
 */
public class PathProperty extends AbstractProperty
{
    private CMISConnector cmisConnector;

    /**
     * Construct
     * 
     * @param serviceRegistry
     */
    public PathProperty(ServiceRegistry serviceRegistry, CMISConnector cmisConnector)
    {
        super(serviceRegistry, PropertyIds.PATH);
        this.cmisConnector = cmisConnector;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.alfresco.cmis.mapping.AbstractProperty#getValue(org.alfresco.service
     * .cmr.repository.NodeRef)
     */
    public Serializable getValue(NodeRef nodeRef)
    {
        Path path = getServiceRegistry().getNodeService().getPath(nodeRef);
        return toDisplayPath(path);
    }

    private String toDisplayPath(Path path)
    {
        StringBuilder displayPath = new StringBuilder(64);

        // skip to CMIS root path
        NodeRef rootNode = cmisConnector.getRootNodeRef();
        int i = 0;
        while (i < path.size())
        {
            Path.Element element = path.get(i);
            if (element instanceof ChildAssocElement)
            {
                ChildAssociationRef assocRef = ((ChildAssocElement) element).getRef();
                NodeRef node = assocRef.getChildRef();
                if (node.equals(rootNode))
                {
                    break;
                }
            }
            i++;
        }

        if (i == path.size())
        {
            // TODO:
            // throw new AlfrescoRuntimeException("Path " + path +
            // " not in CMIS root node scope");
        }

        if (path.size() - i == 1)
        {
            // render root path
            displayPath.append("/");
        } else
        {
            // render CMIS scoped path
            i++;
            while (i < path.size())
            {
                Path.Element element = path.get(i);
                if (element instanceof ChildAssocElement)
                {
                    ChildAssociationRef assocRef = ((ChildAssocElement) element).getRef();
                    NodeRef node = assocRef.getChildRef();
                    displayPath.append("/");
                    displayPath.append(getServiceRegistry().getNodeService().getProperty(node, ContentModel.PROP_NAME));
                }
                i++;
            }
        }

        return displayPath.toString();
    }

}
