/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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

package org.alfresco.repo.action.constraint;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.QName;

/**
 * Type parameter constraint
 * 
 * @author Roy Wetherall
 */
public class AspectParameterConstraint extends BaseParameterConstraint
{
    /** Name constant */
    public static final String NAME = "ac-aspects";
    
    private DictionaryService dictionaryService;
    
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
          
    /**
     * @see org.alfresco.service.cmr.action.ParameterConstraint#getAllowableValues()
     */
    protected Map<String, String> getAllowableValuesImpl()
    {
        final Map<String, String> values = getValues();
        values.values().removeIf(Objects::isNull);
        return values;
    }

    @Override
    public Map<String, String> getValues()
    {
        return dictionaryService.getAllAspects().stream()
                .collect(LinkedHashMap::new, (m, v) -> m.put(v.toPrefixString(), getTitle(v)), LinkedHashMap::putAll);
    }

    private String getTitle(QName aspect)
    {
        final AspectDefinition aspectDef = dictionaryService.getAspect(aspect);
        return aspectDef != null ? aspectDef.getTitle(dictionaryService) : null;
    }
}
