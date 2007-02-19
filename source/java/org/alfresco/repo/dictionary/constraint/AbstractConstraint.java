/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.dictionary.constraint;

import java.util.Collection;

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
            throw new DictionaryException(AbstractConstraint.ERR_PROP_NOT_SET, value);
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
