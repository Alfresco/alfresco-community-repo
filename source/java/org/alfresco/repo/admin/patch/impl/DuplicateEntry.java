/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.admin.patch.impl;

/**
 * Wrapper for duplicate entries
 * 
 * @author Dmitry Velichkevich
 */
public class DuplicateEntry
{
    private static final int ODD_MULTIPLICATOR = 37;

    private Long id;

    private Long parentId;

    private String name;

    private Long amount;

    public DuplicateEntry()
    {
    }

    public DuplicateEntry(Long id, Long parentId, String name, Long amount)
    {
        this.id = id;
        this.parentId = parentId;
        this.name = name;
        this.amount = amount;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getParentId()
    {
        return parentId;
    }

    public void setParentId(Long parentId)
    {
        this.parentId = parentId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Long getAmount()
    {
        return amount;
    }

    public void setAmount(Long amount)
    {
        this.amount = amount;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof DuplicateEntry))
        {
            return false;
        }
        DuplicateEntry converted = (DuplicateEntry) obj;
        return (id == converted.getId()) && (parentId == converted.getParentId()) && ((null == name) ? (null == converted.getName()) : (name.equals(converted.getName())));
    }

    @Override
    public int hashCode()
    {
        int result = (int) id.intValue();
        result = DuplicateEntry.ODD_MULTIPLICATOR * result + (int) parentId.longValue();
        result = DuplicateEntry.ODD_MULTIPLICATOR * result + (int) amount.longValue();
        result = DuplicateEntry.ODD_MULTIPLICATOR * result + ((null != name) ? (name.hashCode()) : (0));
        return result;
    }
}
