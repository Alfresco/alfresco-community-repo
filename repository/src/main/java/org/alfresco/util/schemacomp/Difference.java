/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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
package org.alfresco.util.schemacomp;

import java.util.Locale;

import org.springframework.extensions.surf.util.I18NUtil;



/**
 * Result of a comparison between two database objects.
 * 
 * @author Matt Ward
 */
public final class Difference extends Result
{
    /** Specifies the type of differences */
    public enum Where { ONLY_IN_REFERENCE, ONLY_IN_TARGET, IN_BOTH_NO_DIFFERENCE, IN_BOTH_BUT_DIFFERENCE };
    private final Where where;
    private final DbProperty left;
    private final DbProperty right;
    
    
    public Difference(Where where, DbProperty left, DbProperty right)
    {
        // Sanity check parameters
        if (left == null && right == null)
        {
            throw new IllegalArgumentException("DbProperty parameters cannot BOTH be null.");
        }
        
        this.where = where;
        this.left = left;
        this.right = right;
    }

    /**
     * @return the where
     */
    public Where getWhere()
    {
        return this.where;
    }

    /**
     * @return the left
     */
    public DbProperty getLeft()
    {
        return this.left;
    }

    /**
     * @return the right
     */
    public DbProperty getRight()
    {
        return this.right;
    }

    
    @Override
    public String describe()
    {
        return doDescribe(I18NUtil.getLocale());
    }

    @Override
    public String describe(Locale locale)
    {
        return doDescribe(locale);
    }

    private String doDescribe(Locale locale)
    {
        if (getLeft() == null)
        {
            return I18NUtil.getMessage(
                        "system.schema_comp.diff.target_only",
                        locale,
                        getRight().getDbObject().getTypeName(),
                        getRight().getPath(),
                        getRight().getPropertyValue());
        }
        if (getRight() == null)
        {
            return I18NUtil.getMessage(
                        "system.schema_comp.diff.ref_only",
                        locale,
                        getLeft().getDbObject().getTypeName(),
                        getLeft().getPath(),
                        getLeft().getPropertyValue());
        }
        
        return I18NUtil.getMessage(
                    "system.schema_comp.diff",
                    locale,
                    getLeft().getDbObject().getTypeName(),
                    getLeft().getPath(),
                    getLeft().getPropertyValue(),
                    getRight().getPath(),
                    getRight().getPropertyValue());
    }

    @Override
    public String toString()
    {
        return "Difference [where=" + this.where + ", left=" + this.left + ", right=" + this.right + "]";
    }

}
