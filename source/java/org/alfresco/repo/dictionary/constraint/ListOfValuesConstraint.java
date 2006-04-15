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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alfresco.service.cmr.dictionary.ConstraintException;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.TypeConversionException;

/**
 * Constraint implementation that ensures the value is one of a constrained
 * <i>list of values</i>.  By default, this constraint is case-sensitive.
 * 
 * @see #setAllowedValues(List)
 * @see #setCaseSensitive(boolean)
 * 
 * @author Derek Hulley
 */
public class ListOfValuesConstraint extends AbstractConstraint
{
    private static final String ERR_NO_VALUES = "d_dictionary.constraint.list_of_values.no_values";
    private static final String ERR_NON_STRING = "d_dictionary.constraint.string_length.non_string";
    private static final String ERR_INVALID_VALUE = "d_dictionary.constraint.list_of_values.invalid_value";

    private List<String> allowedValues;
    private List<String> allowedValuesUpper;
    private boolean caseSensitive;
    
    public ListOfValuesConstraint()
    {
        caseSensitive = true;
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(80);
        sb.append("ListOfValuesConstraint")
          .append("[ allowedValues=").append(allowedValues)
          .append(", caseSensitive=").append(caseSensitive)
          .append("]");
        return sb.toString();
    }

    /**
     * Get the allowed values.  Note that these are <tt>String</tt> instances, but may 
     * represent non-<tt>String</tt> values.  It is up to the caller to distinguish.
     * 
     * @return Returns the values allowed
     */
    public List<String> getAllowedValues()
    {
        return allowedValues;
    }
    
    /**
     * Set the values that are allowed by the constraint.
     *  
     * @param values a list of allowed values
     */
    @SuppressWarnings("unchecked")
    public void setAllowedValues(List allowedValues)
    {
        if (allowedValues == null)
        {
            throw new DictionaryException(ERR_NO_VALUES);
        }
        int valueCount = allowedValues.size();
        if (valueCount == 0)
        {
            throw new DictionaryException(ERR_NO_VALUES);
        }
        this.allowedValues = Collections.unmodifiableList(allowedValues);
        // make the upper case versions
        this.allowedValuesUpper = new ArrayList<String>(valueCount);
        for (String allowedValue : this.allowedValues)
        {
            allowedValuesUpper.add(allowedValue.toUpperCase());
        }
    }

    /**
     * @return Returns <tt>true</tt> if this constraint is case-sensitive (default)
     */
    public boolean isCaseSensitive()
    {
        return caseSensitive;
    }

    /**
     * Set the handling of case checking.
     * 
     * @param caseSensitive <tt>true</tt> if the constraint is case-sensitive (default),
     *      or <tt>false</tt> for case-insensitive.
     */
    public void setCaseSensitive(boolean caseSensitive)
    {
        this.caseSensitive = caseSensitive;
    }

    public void initialize()
    {
        checkPropertyNotNull("allowedValues", allowedValues);
    }

    protected void evaluateSingleValue(Object value)
    {
        // convert the value to a String
        String valueStr = null;
        try
        {
            valueStr = DefaultTypeConverter.INSTANCE.convert(String.class, value);
        }
        catch (TypeConversionException e)
        {
            throw new ConstraintException(ERR_NON_STRING, value);
        }
        // check that the value is in the set of allowed values
        if (caseSensitive)
        {
            if (!allowedValues.contains(valueStr))
            {
                throw new ConstraintException(ERR_INVALID_VALUE, value);
            }
        }
        else
        {
            if (!allowedValuesUpper.contains(valueStr.toUpperCase()))
            {
                throw new ConstraintException(ERR_INVALID_VALUE, value);
            }
        }
    }
}
