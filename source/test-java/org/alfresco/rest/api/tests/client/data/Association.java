/*
 * Copyright (C) 2005-2016 Alfresco Software Limited.
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
package org.alfresco.rest.api.tests.client.data;

public class Association
{
    // assoc type (child or peer)
    private String assocType;

    public String getAssocType()
    {
        return assocType;
    }

    public void setAssocType(String assocType)
    {
        this.assocType = assocType;
    }

    // child assoc
    private Boolean isPrimaryParent;

    public Boolean getIsPrimaryParent()
    {
        return isPrimaryParent;
    }

    public void setIsPrimaryParent(Boolean isPrimaryParent)
    {
        this.isPrimaryParent = isPrimaryParent;
    }
}
