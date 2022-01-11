/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.record;

import java.util.Map;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;

/**
 * Record metadata bootstrap bean.
 * <p>
 * This method of bootstrapping record metadata aspects into the RecordService deprecates the 
 * previous practice of extending rma:recordMetaData.
 * 
 * @author Roy Wetherall
 * @since 2.2
 */
public class RecordMetadataBootstrap
{
    /** record service */
    private RecordService recordService;
    
    /** namespace service */
    private NamespaceService namespaceService;
    
    /** map of record metadata aspects against file plan type */
    private Map<String, String> recordMetadataAspects;
    
    /**
     * @param recordMetadataAspects map of record metadata aspects against file plan types
     */
    public void setRecordMetadataAspects(Map<String, String> recordMetadataAspects)
    {
        this.recordMetadataAspects = recordMetadataAspects;
    }
    
    /**
     * @param namespaceService  namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    /**
     * @param recordService record service
     */
    public void setRecordService(RecordService recordService)
    {
        this.recordService = recordService;
    }
    
    /**
     * Init method
     */
    public void init()
    {
        ParameterCheck.mandatory("recordService", recordService);
        ParameterCheck.mandatory("namespaceService", namespaceService);
        
        if (recordMetadataAspects != null)
        {
            for (Map.Entry<String, String> entry : recordMetadataAspects.entrySet())
            {
                // convert to qname's
                QName recordMetadataAspect = QName.createQName(entry.getKey(), namespaceService);
                QName filePlanType = QName.createQName(entry.getValue(), namespaceService);

                // register with record service
                recordService.registerRecordMetadataAspect(recordMetadataAspect, filePlanType);                    
            }
        }
    }
}
