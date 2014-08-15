/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.version;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.version.Node2ServiceImpl;
import org.alfresco.repo.version.common.VersionUtil;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * 
 * @author Roy Wetherall
 * @since 2.3
 */
public class RecordableVersionNodeServiceImpl extends Node2ServiceImpl
                                              implements RecordableVersionModel
{

    @Override
    public Map<QName, Serializable> getProperties(NodeRef nodeRef) throws InvalidNodeRefException
    {
        NodeRef converted = VersionUtil.convertNodeRef(nodeRef);
        if (dbNodeService.hasAspect(converted, ASPECT_RECORDED_VERSION))
        {
            NodeRef record = (NodeRef)dbNodeService.getProperty(converted, PROP_RECORD_NODE_REF);
            Map<QName, Serializable> properties =  dbNodeService.getProperties(record);
            return processProperties(properties);
        }
        else
        {
            return super.getProperties(nodeRef);
        }
    }
    
    protected Map<QName, Serializable> processProperties(Map<QName, Serializable> properties)
    {
        // revert modified record name
        properties.put(ContentModel.PROP_NAME, properties.get(RecordsManagementModel.PROP_ORIGIONAL_NAME));
        
        // remove all rma, rmc, rmr and rmv properties
        
        // remove any properties relating to custom record-meta data
        
        // do standard property processing
        VersionUtil.convertFrozenToOriginalProps(properties);
        
        return properties;
    }
}
