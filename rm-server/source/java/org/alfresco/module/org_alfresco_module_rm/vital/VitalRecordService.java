/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.vital;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Period;

/**
 * Vital Record Service.
 * 
 * @author Roy Wetherall
 * @since 2.0
 */
public interface VitalRecordService
{  
    /** Period 'none' */
    static final Period PERIOD_NONE = new Period("none|0");
    
    /**
     * Gets the vital record definition details for the node.  
     * 
     * @param nodeRef                   node reference
     * @return VitalRecordDefinition    vital record definition details
     */
    VitalRecordDefinition getVitalRecordDefinition(NodeRef nodeRef);
    
    /**
     * Sets the vital record definition values for a given node.
     * 
     * @param nodeRef
     * @param enabled
     * @param reviewPeriod
     * @return
     */
    VitalRecordDefinition setVitalRecordDefintion(NodeRef nodeRef, boolean enabled, Period reviewPeriod);
  
    /**
     * Indicates whether the record is a vital one or not.
     * 
     * @param nodeRef   node reference
     * @return boolean  true if this is a vital record, false otherwise
     */
    boolean isVitalRecord(NodeRef nodeRef);  
}
