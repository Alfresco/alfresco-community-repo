/*
 * #%L
 * Alfresco Data model classes
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
package org.alfresco.repo.dictionary.constraint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.dictionary.ConstraintException;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.i18n.MessageLookup;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.TypeConversionException;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.util.StringUtils;

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
    private static final String LOV_CONSTRAINT_VALUE = "listconstraint";
    public static final String CONSTRAINT_TYPE = "LIST";
    
    public static final String CASE_SENSITIVE_PARAM = "caseSensitive";
    public static final String ALLOWED_VALUES_PARAM = "allowedValues";
    public static final String SORTED_PARAM = "sorted";
    
    private static final String ERR_NO_VALUES = "d_dictionary.constraint.list_of_values.no_values";
    private static final String ERR_NON_STRING = "d_dictionary.constraint.string_length.non_string";
    private static final String ERR_INVALID_VALUE = "d_dictionary.constraint.list_of_values.invalid_value";

    private List<String> allowedValues;
    private List<String> allowedValuesUpper;
    private Set<String> allowedValuesSet;
    private Set<String> allowedValuesUpperSet;
    protected boolean caseSensitive;
    protected boolean sorted;
    
    public ListOfValuesConstraint()
    {
        caseSensitive = true;
        sorted = false;
        allowedValues = Collections.emptyList();
        allowedValuesUpper = Collections.emptyList();
        allowedValuesSet = Collections.emptySet();
        allowedValuesUpperSet = Collections.emptySet();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getType()
    {
        return CONSTRAINT_TYPE;
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(80);
        sb.append("ListOfValuesConstraint")
          .append("[ allowedValues=").append(allowedValues)
          .append(", caseSensitive=").append(caseSensitive)
          .append(", sorted=").append(sorted)
          .append("]");
        return sb.toString();
    }

    /**
     * Get the allowed values.  Note that these are <tt>String</tt> instances, but may 
     * represent non-<tt>String</tt> values.  It is up to the caller to distinguish.
     * 
     * Sorts list if appropriate.
     * 
     * @return Returns the values allowed
     */
    public List<String> getAllowedValues()
    {
        List<String> rawValues = getRawAllowedValues(); 
        if (sorted == true)
        {
            List<String> values = new ArrayList<String>(rawValues);
            Collections.sort(values);
            return values;
        }
        else
        {
            return rawValues;
        }
    }
    
    /**
     * Get the allowed values.  Note that these are <tt>String</tt> instances, but may 
     * represent non-<tt>String</tt> values.  It is up to the caller to distinguish.
     * 
     * @return Returns the values allowed
     */
    protected List<String> getRawAllowedValues()
    {
        return allowedValues;
    }
    
    /**
     * Get the display label for the specified allowable value in this constraint.
     * A key is constructed as follows:
     * <pre>
     *   "listconstraint." + constraintName + "." + constraintAllowableValue.
     *   e.g. listconstraint.test_listConstraintOne.VALUE_ONE.
     * </pre>
     * This key is then used to look up a properties bundle for the localised display label.
     * Spaces are allowed in the keys, but they should be escaped in the properties file as follows:
     * <pre>
     * listconstraint.test_listConstraintOne.VALUE\ WITH\ SPACES=Display label
     * </pre>
     * 
     * @param constraintAllowableValue String
     * @param messageLookup MessageLookup
     * @return the localised display label for the specified constraint value in the current locale.
     *         If no localisation is defined, it will return the allowed value itself.
     *         If the specified allowable value is not in the model, returns <code>null</code>.
     * @since 4.0
     * @see I18NUtil#getLocale()
     */
    public String getDisplayLabel(String constraintAllowableValue, MessageLookup messageLookup)
    {
        if (!allowedValues.contains(constraintAllowableValue))
        {
            return null;
        }
        
        String key = LOV_CONSTRAINT_VALUE;
        key += "." + this.getShortName();
        key += "." + constraintAllowableValue;
        key = StringUtils.replace(key, ":", "_");
        
        String message = messageLookup.getMessage(key, I18NUtil.getLocale());
        return message == null ? constraintAllowableValue : message;
    }

    
    /**
     * Set the values that are allowed by the constraint.
     *  
     * @param allowedValues a list of allowed values
     */
    public void setAllowedValues(List<String> allowedValues)
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
        this.allowedValuesSet = new HashSet<String>(allowedValues);
        // make the upper case versions
        this.allowedValuesUpper = new ArrayList<String>(valueCount);
        this.allowedValuesUpperSet = new HashSet<String>(valueCount);
        for (String allowedValue : this.allowedValues)
        {
            String allowedValueUpper = allowedValue.toUpperCase();
            allowedValuesUpper.add(allowedValueUpper);
            allowedValuesUpperSet.add(allowedValueUpper);
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
    
    /**
     * Indicates whether the list of values are sorted or not.
     * 
     * @return    <tt>true</tt> if sorted, <tt>false</tt> otherwise
     */
    public boolean isSorted() 
    {
        return sorted;
    }
    
    /**
     * Set whether the values are ordered or not.
     * 
     * @param sorted    <tt>true</tt> if sorted, <tt>false</tt> otherwise
     */
    public void setSorted(boolean sorted) 
    {
        this.sorted = sorted;
    }

    /**
     * @see org.alfresco.repo.dictionary.constraint.AbstractConstraint#initialize()
     */
    @Override
    public void initialize()
    {
        super.initialize();
        checkPropertyNotNull(ALLOWED_VALUES_PARAM, allowedValues);
    }
    
    /**
     * @see org.alfresco.repo.dictionary.constraint.AbstractConstraint#getParameters()
     */
    @Override
    public Map<String, Object> getParameters()
    {
        Map<String, Object> params = new HashMap<String, Object>(2);
        
        params.put(CASE_SENSITIVE_PARAM, this.caseSensitive);
        params.put(ALLOWED_VALUES_PARAM, this.allowedValues);
        params.put(SORTED_PARAM, this.sorted);
        
        return params;
    }

    /**
     * @see org.alfresco.repo.dictionary.constraint.AbstractConstraint#evaluateSingleValue(java.lang.Object)
     */
    @Override
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
            if (!allowedValuesSet.contains(valueStr))
            {
                throw new ConstraintException(ERR_INVALID_VALUE, value);
            }
        }
        else
        {
            if (!allowedValuesUpperSet.contains(valueStr.toUpperCase()))
            {
                throw new ConstraintException(ERR_INVALID_VALUE, value);
            }
        }
    }
}
