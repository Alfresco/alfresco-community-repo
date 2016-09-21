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
package org.alfresco.module.org_alfresco_module_rm.action.constraint;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionPolicy;
import org.alfresco.repo.action.constraint.BaseParameterConstraint;

/**
 * Recordable version config constraint
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public class VersionParameterConstraint extends BaseParameterConstraint
{
    /**
     * @see org.alfresco.repo.action.constraint.BaseParameterConstraint#getAllowableValuesImpl()
     */
    @Override
    protected Map<String, String> getAllowableValuesImpl()
    {
        RecordableVersionPolicy[] recordableVersionPolicies = RecordableVersionPolicy.values();
        Map<String, String> allowableValues = new HashMap<String, String>(recordableVersionPolicies.length);
        for (RecordableVersionPolicy recordableVersionPolicy : recordableVersionPolicies)
        {
            String policy = recordableVersionPolicy.toString();
            allowableValues.put(policy, getI18NLabel(policy));
        }
        return allowableValues;
    }
}
