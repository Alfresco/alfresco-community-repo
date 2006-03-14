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
import org.alfresco.service.cmr.dictionary.DictionaryException;

/**
 * Base services for constraints.
 * 
 * @author Derek Hulley
 */
public abstract class AbstractConstraint implements Constraint
{
    public static final String ERR_PROP_NOT_SET = "d_dictionary.model.err.property_not_set";
    public static final String ERR_EVALUATE_EXCEPTION = "d_dictionary.model.err.evaluate_exception";

    /**
     * @see #evaluateSingleValue(Object)
     * @see #evaluateCollection(Collection)
     */
    @SuppressWarnings("unchecked")
    public final void evaluate(Object value)
    {
        try
        {
            // check for collection
            if (value instanceof Collection)
            {
                Collection collection = (Collection) value;
                evaluateCollection(collection);
            }
            else
            {
                evaluateSingleValue(value);
            }
        }
        catch (DictionaryException e)
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
            evaluateSingleValue(value);
        }
    }
    
    /**
     * Support for evaluation of properties.  The value passed in will never be a
     * collection.
     * 
     * @throws DictionaryException throw this when the evaluation fails
     */
    protected abstract void evaluateSingleValue(Object value);
}
