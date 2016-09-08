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
package org.alfresco.module.org_alfresco_module_rm.query;

/**
 * Records management query DAO
 * 
 * NOTE:  a place holder that can be extended later when we want to enhance performance with canned queries.
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public interface RecordsManagementQueryDAO
{
    /**
     * Get the number of objects with the given identifier value.
     * 
     * Note:  this is provided as an example and is not currently used
     * 
     * @param identifierValue   id value
     * @return int  count
     */
    int getCountRmaIdentifier(String identifierValue);
}
