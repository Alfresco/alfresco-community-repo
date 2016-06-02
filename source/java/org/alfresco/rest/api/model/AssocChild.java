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
package org.alfresco.rest.api.model;

/**
 * @author janv
 */
public class AssocChild extends Assoc
{
    private String childId;
    private Boolean isPrimary;

    public AssocChild()
    {
    }

    public AssocChild(String prefixAssocTypeQName, boolean isPrimary)
    {
        super(prefixAssocTypeQName);

        this.isPrimary = isPrimary;
    }

    public AssocChild(String childId, String prefixAssocTypeQName)
    {
        super(prefixAssocTypeQName);

        this.childId = childId;
    }

    public Boolean getIsPrimary()
    {
        return isPrimary;
    }

    public void setIsPrimary(Boolean isPrimary)
    {
        this.isPrimary = isPrimary;
    }

    public String getChildId()
    {
        return childId;
    }

    public void setChildId(String childId)
    {
        this.childId = childId;
    }
}