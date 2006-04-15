/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
