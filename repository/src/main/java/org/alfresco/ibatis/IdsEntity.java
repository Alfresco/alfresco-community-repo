/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.ibatis;

import java.util.List;

/**
 * Entity bean to carry ID-style information
 *
 * @author Derek Hulley
 * @since 3.2
 */
public class IdsEntity
{
    private Long idOne;
    private Long idTwo;
    private Long idThree;
    private Long idFour;
    private List<Long> ids;
    private boolean ordered;
    public Long getIdOne()
    {
        return idOne;
    }
    public void setIdOne(Long id)
    {
        this.idOne = id;
    }
    public Long getIdTwo()
    {
        return idTwo;
    }
    public void setIdTwo(Long id)
    {
        this.idTwo = id;
    }
    public Long getIdThree()
    {
        return idThree;
    }
    public void setIdThree(Long idThree)
    {
        this.idThree = idThree;
    }
    public Long getIdFour()
    {
        return idFour;
    }
    public void setIdFour(Long idFour)
    {
        this.idFour = idFour;
    }
    public List<Long> getIds()
    {
        return ids;
    }
    public void setIds(List<Long> ids)
    {
        this.ids = ids;
    }
    public boolean isOrdered()
    {
        return ordered;
    }
    public void setOrdered(boolean ordered)
    {
        this.ordered = ordered;
    }
}
