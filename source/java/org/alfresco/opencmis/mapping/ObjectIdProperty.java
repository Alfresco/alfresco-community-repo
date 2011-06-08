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
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.chemistry.opencmis.commons.PropertyIds;

/**
 * Get the CMIS object id property.
 * 
 * @author andyh
 * @author dward
 */
public class ObjectIdProperty extends AbstractVersioningProperty
{
    /**
     * Construct
     * 
     * @param serviceRegistry
     */
    public ObjectIdProperty(ServiceRegistry serviceRegistry)
    {
        super(serviceRegistry, PropertyIds.OBJECT_ID);
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
        if (isWorkingCopy(nodeRef))
        {
            return nodeRef.toString();
        }

        QName typeQName = getServiceRegistry().getNodeService().getType(nodeRef);
        if (typeQName.equals(CMISMapping.DOCUMENT_QNAME)
                || getServiceRegistry().getDictionaryService().isSubClass(typeQName, ContentModel.TYPE_CONTENT))
        {
            Serializable versionLabel = getServiceRegistry().getNodeService().getProperty(nodeRef,
                    ContentModel.PROP_VERSION_LABEL);
            if (versionLabel == null)
            {
                versionLabel = CMISConnector.UNVERSIONED_VERSION_LABEL;
            }

            NodeRef versionSeries = getVersionSeries(nodeRef);
            return new StringBuilder(1024).append(versionSeries.toString()).append(CMISConnector.ID_SEPERATOR)
                    .append(versionLabel).toString();
        }

        return nodeRef.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.alfresco.cmis.mapping.AbstractProperty#getValue(org.alfresco.service
     * .cmr.repository.AssociationRef)
     */
    public Serializable getValue(AssociationRef assocRef)
    {
        return CMISConnector.ASSOC_ID_PREFIX + assocRef.getId();
    }

}
