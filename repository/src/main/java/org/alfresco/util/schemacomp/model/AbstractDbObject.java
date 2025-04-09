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
package org.alfresco.util.schemacomp.model;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.util.schemacomp.ComparisonUtils;
import org.alfresco.util.schemacomp.DbProperty;
import org.alfresco.util.schemacomp.DefaultComparisonUtils;
import org.alfresco.util.schemacomp.DiffContext;
import org.alfresco.util.schemacomp.validator.DbValidator;

/**
 * Useful base class for many, if not all the {@link DbObject} implementations.
 * 
 * @author Matt Ward
 */
public abstract class AbstractDbObject implements DbObject
{
    private DbObject parent;
    private String name;
    protected ComparisonUtils comparisonUtils = new DefaultComparisonUtils();
    private final List<DbValidator> validators = new ArrayList<DbValidator>();

    /**
     * Instantiate, giving the object a parent and a name.
     * 
     * @param parent
     *            DbObject
     * @param name
     *            String
     */
    public AbstractDbObject(DbObject parent, String name)
    {
        this.parent = parent;
        this.name = name;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        if (this.name == null)
        {
            return "";
        }
        return this.name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public boolean sameAs(DbObject other)
    {
        if (other == null)
        {
            return false;
        }
        if (this == other)
        {
            return true;
        }
        if (!this.getClass().equals(other.getClass()))
        {
            // Objects are not the same type, so are not the same - even if they
            // do have the same name and parent.
            return false;
        }
        if (getName() != null && other != null && other.getName() != null)
        {
            boolean sameParent = false;

            if (getParent() == null && other.getParent() == null)
            {
                sameParent = true;
            }
            else if (getParent() != null && getParent().sameAs(other.getParent()))
            {
                sameParent = true;
            }
            // Same parent & same name - it must be considered the same object.
            return sameParent && getName().equalsIgnoreCase(other.getName());
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        result = prime * result + ((this.parent == null) ? 0 : this.parent.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbstractDbObject other = (AbstractDbObject) obj;
        if (this.name == null)
        {
            if (other.name != null)
                return false;
        }
        else if (!this.name.equals(other.name))
            return false;
        if (this.parent == null)
        {
            if (other.parent != null)
                return false;
        }
        else if (!this.parent.equals(other.parent))
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append(getClass().getSimpleName());
        sb.append("[name=");

        if (getName() != null)
        {
            sb.append(getName());
        }
        else
        {
            sb.append("null");
        }

        sb.append("]");

        return sb.toString();
    }

    /**
     * Provides an implementation of {@link DbObject#diff(DbObject, DiffContext)}. The template method {@link #doDiff(DbObject, DiffContext)} provides the subclass specific diffing logic, whilst this method handles the workflow required in most cases: set the path's prefix that will be used to explain where differences occur; compare the name fields of the two objects; delegate to the subclass specific diffing (if any); remove the last path addition ready for the next object to perform its diff correctly.
     */
    @Override
    public void diff(DbObject right, DiffContext ctx)
    {
        DbProperty leftNameProp = new DbProperty(this, "name");
        DbProperty rightNameProp = new DbProperty(right, "name");
        comparisonUtils.compareSimple(leftNameProp, rightNameProp, ctx);

        doDiff(right, ctx);
    }

    @Override
    public DbObject getParent()
    {
        return parent;
    }

    @Override
    public void setParent(DbObject parent)
    {
        this.parent = parent;
    }

    /**
     * Override this method to provide subclass specific diffing logic.
     * 
     * @param right
     *            DbObject
     * @param ctx
     *            DiffContext
     */
    protected void doDiff(DbObject right, DiffContext ctx)
    {}

    /**
     * If a ComparisonUtils other than the default is required, then this setter can be used. Useful for testing, where a mock can be injected.
     * 
     * @param comparisonUtils
     *            the comparisonUtils to set
     */
    public void setComparisonUtils(ComparisonUtils comparisonUtils)
    {
        this.comparisonUtils = comparisonUtils;
    }

    @Override
    public List<DbValidator> getValidators()
    {
        return validators;
    }

    /**
     * @param validators
     *            the validators to set
     */
    @Override
    public void setValidators(List<DbValidator> validators)
    {
        this.validators.clear();
        if (validators != null)
        {
            this.validators.addAll(validators);
        }
    }

    @Override
    public boolean hasValidators()
    {
        return getValidators().size() > 0;
    }

    @Override
    public boolean hasObjectLevelValidator()
    {
        for (DbValidator validator : getValidators())
        {
            if (validator.validatesFullObject())
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getTypeName()
    {
        return getClass().getSimpleName().toLowerCase();
    }
}
