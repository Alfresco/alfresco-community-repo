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
package org.alfresco.repo.dictionary.constraint;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.Constraint;
import org.alfresco.service.cmr.dictionary.ConstraintException;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;

/**
 * Base services for constraints.
 * 
 * @author Derek Hulley
 */
public abstract class AbstractConstraint implements Constraint
{
    public static final String ERR_PROP_NOT_SET = "d_dictionary.constraint.err.property_not_set";
    public static final String ERR_EVALUATE_EXCEPTION = "d_dictionary.constraint.err.evaluate_exception";
    
    
    /** The constraint name. May be useful in error messages */
    private String shortName;
    private String title;
    
    private ConstraintRegistry registry;

    /**
     * Sets the constraint name. Automatically called after construction. Please excuse the strange method name as we
     * want the property name to begin with an underscore to avoid property name clashes.
     * 
     * @param shortName
     * @deprecated
     */
    public void set_shortName(String shortName)
    {
        setShortName(shortName);
    }

    /**
     * Sets the constraint name
     * @param name
     */
    public void setShortName(String name)
    {
        this.shortName = name;
    }

    /**
     * Gets the constraint name.
     * 
     * @return the constraint name.
     */
    public String getShortName()
    {
        return this.shortName;
    }

    /**
     * Optionally specify the registry that will be used to register the constraint.
     * This is used when instantiating constraints outside the dictionary.
     * 
     * @param registry          the constraint registry
     */
    public void setRegistry(ConstraintRegistry registry)
    {
        this.registry = registry;
    }
    
    public String getType()
    {
        return this.getClass().getName();
    }
    
    public void setTitle(String title)
    {
        this.title = title;
    }
    
    public String getTitle()
    {
        return title;
    }
    
    public Map<String, Object> getParameters()
    {
        return new HashMap<String, Object>(3);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Registers the constraint with the registry, if present.  Call this method if
     * you want the constraint to be auto-registered.
     */
    public void initialize()
    {
        if (registry != null)
        {
            registry.register(shortName, this);
        }
    }

    /**
     * Check that the given value is not <tt>null</tt>.
     * 
     * @param name the name of the property
     * @param value the value to check for <tt>null</tt>
     * 
     * @throws DictionaryException if the the property is null
     */
    protected void checkPropertyNotNull(String name, Object value)
    {
        if (value == null)
        {
            throw new DictionaryException(AbstractConstraint.ERR_PROP_NOT_SET, name, getShortName());
        }
    }

    /**
     * @see #evaluateSingleValue(Object)
     * @see #evaluateCollection(Collection)
     */
    @SuppressWarnings("unchecked")
    public final void evaluate(Object value)
    {
        if (value == null)
        {
            // null values are never evaluated
            return;
        }
        try
        {
            // ensure that we can handle collections
            if (DefaultTypeConverter.INSTANCE.isMultiValued(value))
            {
                Collection collection = DefaultTypeConverter.INSTANCE.getCollection(Object.class, value);
                evaluateCollection(collection);
            }
            else
            {
                evaluateSingleValue(value);
            }
        }
        catch (ConstraintException e)
        {
            // this can go
            throw e;
        }
        catch (Throwable e)
        {
            throw new DictionaryException(AbstractConstraint.ERR_EVALUATE_EXCEPTION, this, e.getMessage());
        }
    }
    
    /**
     * Only override if there is some specific evaluation that needs to be performed on the
     * collection as a whole.
     * 
     * @param collection the collection of values to evaluate
     * 
     * @see #evaluateSingleValue(Object)
     */
    protected void evaluateCollection(Collection<Object> collection)
    {
        for (Object value : collection)
        {
            if (value == null)
            {
                // contract states that it will always pass
                continue;
            }
            evaluateSingleValue(value);
        }
    }
    
    /**
     * Support for evaluation of properties.  The value passed in will never be a
     * <tt>Collection</tt> and will never be <tt>null</tt>.
     * 
     * @throws ConstraintException throw this when the evaluation fails
     */
    protected abstract void evaluateSingleValue(Object value);
}
