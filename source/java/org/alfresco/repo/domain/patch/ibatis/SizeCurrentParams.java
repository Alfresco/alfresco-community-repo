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
package org.alfresco.repo.domain.patch.ibatis;

/**
 * Parameters for the <b>cm:sizeCurrent</b> update.
 */
public class SizeCurrentParams
{
    private Long sizeCurrentQNameId;
    private Long personTypeQNameId;
    private Long defaultLocaleId;

    public boolean getFalse()
    {
        return false;
    }
    
    public Long getSizeCurrentQNameId()
    {
        return sizeCurrentQNameId;
    }
    public void setSizeCurrentQNameId(Long sizeCurrentQNameId)
    {
        this.sizeCurrentQNameId = sizeCurrentQNameId;
    }
    public Long getPersonTypeQNameId()
    {
        return personTypeQNameId;
    }
    public void setPersonTypeQNameId(Long personTypeQNameId)
    {
        this.personTypeQNameId = personTypeQNameId;
    }
    public Long getDefaultLocaleId()
    {
        return defaultLocaleId;
    }
    public void setDefaultLocaleId(Long defaultLocaleId)
    {
        this.defaultLocaleId = defaultLocaleId;
    }
}
