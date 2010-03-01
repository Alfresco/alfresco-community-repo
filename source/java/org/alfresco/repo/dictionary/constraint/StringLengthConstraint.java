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

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.ConstraintException;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.TypeConversionException;

/**
 * Constraint implementation that ensures that the length of the String value.
 * 
 * @see #setMinLength(int)
 * @see #setMaxLength(int)
 * 
 * @author Derek Hulley
 */
public class StringLengthConstraint extends AbstractConstraint
{
    private static final String ERR_INVALID_MIN_LENGTH = "d_dictionary.constraint.string_length.invalid_min_length";
    private static final String ERR_INVALID_MAX_LENGTH = "d_dictionary.constraint.string_length.invalid_max_length";
    private static final String ERR_NON_STRING = "d_dictionary.constraint.string_length.non_string";
    private static final String ERR_INVALID_LENGTH = "d_dictionary.constraint.string_length.invalid_length";
    
    private int minLength = 0;
    private int maxLength = Integer.MAX_VALUE;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getType()
    {
        return "LENGTH";
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(80);
        sb.append("StringLengthConstraint")
          .append("[ minLength=").append(minLength)
          .append(", maxLength=").append(maxLength)
          .append("]");
        return sb.toString();
    }

    /**
     * @return Returns the minimum number of characters allowed
     */
    public int getMinLength()
    {
        return minLength;
    }

    /**
     * Set the minimum number of characters allowed.  Valid values are in
     * the range [0, {@link Integer#MAX_VALUE}].
     * 
     * @param minLength the minimum numbers of characters allowed
     */
    public void setMinLength(int minLength)
    {
        if (minLength > this.maxLength || minLength < 0)
        {
            throw new DictionaryException(ERR_INVALID_MIN_LENGTH, minLength);
        }
        this.minLength = minLength;
    }

    /**
     * @return Returns the maximum number of characters allowed
     */
    public int getMaxLength()
    {
        return maxLength;
    }

    /**
     * Set the maximum number of characters allowed.  Valid values are in
     * the range [0, {@link Integer#MAX_VALUE}].
     * 
     * @param maxLength the minimum numbers of characters allowed
     */
    public void setMaxLength(int maxLength)
    {
        if (maxLength < this.minLength)
        {
            throw new DictionaryException(ERR_INVALID_MAX_LENGTH, maxLength);
        }
        this.maxLength = maxLength;
    }

    @Override
    public Map<String, Object> getParameters()
    {
        Map<String, Object> params = new HashMap<String, Object>(2);
        
        params.put("minLength", this.minLength);
        params.put("maxLength", this.maxLength);
        
        return params;
    }

    protected void evaluateSingleValue(Object value)
    {
        // ensure that the value can be converted to a String
        String checkValue = null;
        try
        {
            checkValue = DefaultTypeConverter.INSTANCE.convert(String.class, value);
        }
        catch (TypeConversionException e)
        {
            throw new ConstraintException(ERR_NON_STRING, value);
        }
        
        // Check that the value length
        int length = checkValue.length();
        if (length > maxLength || length < minLength)
        {
            if (length > 20)
            {
                checkValue = checkValue.substring(0, 17) + "...";
            }
            throw new ConstraintException(ERR_INVALID_LENGTH, checkValue, minLength, maxLength);
        }
    }
}
