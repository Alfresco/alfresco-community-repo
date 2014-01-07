/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.action;

import org.alfresco.repo.action.ActionConditionDefinitionImpl;

/**
 * Records management condition definition implementation.
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public class RecordsManagementActionConditionDefinitionImpl extends ActionConditionDefinitionImpl
                                                     implements RecordsManagementActionConditionDefinition
{
    /** Serial Version UID */
    private static final long serialVersionUID = -7599279732731533610L;

    /**
     * Default constructor.
     * 
     * @param name  name of the condition
     */
    public RecordsManagementActionConditionDefinitionImpl(String name)
    {
        super(name);
    }
}
