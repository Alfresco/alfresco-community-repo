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

package org.alfresco.module.org_alfresco_module_rm.action.constraint;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.repo.action.constraint.BaseParameterConstraint;
import org.alfresco.repo.i18n.StaticMessageLookup;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.QName;

/**
 * Record type parameter constraint
 * 
 * @author Craig Tan
 * @since 2.1
 */
public class RecordTypeParameterConstraint extends BaseParameterConstraint
{
    /** Name constant */
    public static final String NAME = "rm-ac-record-types";
    
    private RecordService recordService;

    private DictionaryService dictionaryService;
    
    public void setRecordService(RecordService recordService)
    {
        this.recordService = recordService;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @see org.alfresco.service.cmr.action.ParameterConstraint#getAllowableValues()
     */
    protected Map<String, String> getAllowableValuesImpl()
    {   
        Set<QName> recordTypes = recordService.getRecordMetaDataAspects();

        Map<String, String> result = new HashMap<String, String>(recordTypes.size());
        for (QName recordType : recordTypes)
        {

            AspectDefinition aspectDefinition = dictionaryService.getAspect(recordType);
            if (aspectDefinition != null)
            {
                result.put(aspectDefinition.getName().getLocalName(), aspectDefinition.getTitle(new StaticMessageLookup()));
            }
        }        
        return result;
    }    
    
    
}
