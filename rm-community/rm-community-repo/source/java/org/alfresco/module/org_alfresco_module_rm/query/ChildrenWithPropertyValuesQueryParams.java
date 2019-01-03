/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
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
package org.alfresco.module.org_alfresco_module_rm.query;

import java.util.Collection;

/**
 * Select parameter for <b>select_CountChildrenWithPropertyValues</b>.
 *
 * @author Ana Manolache
 * @since 2.6
 */
public class ChildrenWithPropertyValuesQueryParams
{
    private Long parentId;
    private Long propertyQnameId;
    private Collection propertyValues;

    public Long getParentId()
    {
        return parentId;
    }

    public void setParentId(Long parentId)
    {
        this.parentId = parentId;
    }

    public Long getPropertyQnameId()
    {
        return propertyQnameId;
    }

    public void setPropertyQnameId(Long propertyQnameId)
    {
        this.propertyQnameId = propertyQnameId;
    }

    public Collection getPropertyValues()
    {
        return propertyValues;
    }

    public void setPropertyValues(Collection propertyValues)
    {
        this.propertyValues = propertyValues;
    }
}

