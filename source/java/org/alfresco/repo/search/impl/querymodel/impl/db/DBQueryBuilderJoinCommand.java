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
package org.alfresco.repo.search.impl.querymodel.impl.db;

/**
 * @author Andy
 */
public class DBQueryBuilderJoinCommand
{
    String alias = null;

    DBQueryBuilderJoinCommandType type;

    Long qnameId = null;

    boolean outer = false;

    /**
     * @return the alias
     */
    public String getAlias()
    {
        return alias;
    }

    /**
     * @param alias the alias to set
     */
    public void setAlias(String alias)
    {
        this.alias = alias;
    }

    /**
     * @return the type
     */
    public String getType()
    {
        return type.toString();
    }

    /**
     * @param type the type to set
     */
    public void setType(DBQueryBuilderJoinCommandType type)
    {
        this.type = type;
    }

    /**
     * @return the qnameId
     */
    public Long getQnameId()
    {
        return qnameId;
    }

    /**
     * @param qnameId the qnameId to set
     */
    public void setQnameId(Long qnameId)
    {
        this.qnameId = qnameId;
    }

    /**
     * @return the outer
     */
    public boolean isOuter()
    {
        return outer;
    }

    /**
     * @param outer the outer to set
     */
    public void setOuter(boolean outer)
    {
        this.outer = outer;
    }
    
    
}
