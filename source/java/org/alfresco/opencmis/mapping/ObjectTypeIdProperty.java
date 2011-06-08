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

import org.alfresco.opencmis.dictionary.CMISDictionaryService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;

/**
 * Get the CMIS object type id property
 * 
 * @author andyh
 */
public class ObjectTypeIdProperty extends AbstractProperty
{
    private CMISDictionaryService dictionaryService;
    
    /**
     * Construct
     * 
     * @param serviceRegistry
     */
    public ObjectTypeIdProperty(ServiceRegistry serviceRegistry, CMISDictionaryService dictionaryService)
    {
        super(serviceRegistry, PropertyIds.OBJECT_TYPE_ID);
        this.dictionaryService = dictionaryService;
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
        QName type = getServiceRegistry().getNodeService().getType(nodeRef);
        return dictionaryService.findTypeForClass(type).getTypeId();
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
        QName type = assocRef.getTypeQName();
        return dictionaryService.findTypeForClass(type, BaseTypeId.CMIS_RELATIONSHIP).getTypeId();
    }
}
