/*
 * Copyright (C) 2005 Alfresco, Inc.
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

import org.alfresco.service.cmr.dictionary.ConstraintException;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;

/**
 * Constraint implementation that ensures that the value is a numeric value bewteen a given
 * minimum and maximum value.  If a minimum or maximum value are not provided, then the JAVA
 * Double's {@link Double#MIN_VALUE minimum value} or {@link Double#MAX_VALUE maximum value}
 * are assumed.
 * 
 * @see #setMinValue(double)
 * @see #setMaxValue(double)
 * @see java.lang.Double#parseDouble(java.lang.String)
 * 
 * @author Derek Hulley
 */
public class NumericRangeConstraint extends AbstractConstraint
{
    private static final String ERR_INVALID_MIN_VALUE = "d_dictionary.constraint.numeric_range.invalid_min_value";
    private static final String ERR_INVALID_MAX_VALUE = "d_dictionary.constraint.numeric_range.invalid_max_value";
    private static final String ERR_NON_NUMERIC = "d_dictionary.constraint.numeric_range.non_numeric";
    private static final String ERR_OUT_OF_RANGE = "d_dictionary.constraint.numeric_range.out_of_range";
    
    private double minValue = Double.MIN_VALUE;
    private double maxValue = Double.MAX_VALUE;
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(80);
        sb.append("NumericRangeConstraint")
          .append("[ minValue=").append(minValue)
          .append(", maxValue=").append(maxValue)
          .append("]");
        return sb.toString();
    }

    /**
     * @return Returns the minimum value allowed by the constraint
     */
    public double getMinValue()
    {
        return minValue;
    }

    /**
     * Set the minimum value allowed, which can be any value between
     * {@link Double#MIN_VALUE} and {@link Double#MAX_VALUE}.
     * 
     * @param minValue the minimum value allowed by the constraint
     */
    public void setMinValue(double minValue)
    {
        if (minValue > this.maxValue)
        {
            throw new DictionaryException(ERR_INVALID_MIN_VALUE, minValue);
        }
        this.minValue = minValue;
    }

    /**
     * @return Returns the minimum value allowed by the constraint
     */
    public double getMaxValue()
    {
        return maxValue;
    }

    /**
     * Set the maximum value allowed, which can be any value between
     * {@link Double#MIN_VALUE} and {@link Double#MAX_VALUE}.
     * 
     * @param maxValue the minimum value allowed by the constraint
     */
    public void setMaxValue(double maxValue)
    {
        if (maxValue < this.minValue)
        {
            throw new DictionaryException(ERR_INVALID_MAX_VALUE, minValue);
        }
        this.maxValue = maxValue;
    }
    
    public void initialize()
    {
    }

    protected void evaluateSingleValue(Object value)
    {
        // ensure that the value can be converted to a double
        double checkValue = Double.NaN;
        try
        {
            checkValue = DefaultTypeConverter.INSTANCE.doubleValue(value);
        }
        catch (NumberFormatException e)
        {
            throw new ConstraintException(ERR_NON_NUMERIC, value);
        }
        
        // Infinity and NaN cannot match
        if (Double.isInfinite(checkValue) || Double.isNaN(checkValue))
        {
            throw new ConstraintException(ERR_OUT_OF_RANGE, checkValue, minValue, maxValue);
        }
        
        // Check that the value is in range
        if (checkValue > maxValue || checkValue < minValue)
        {
            throw new ConstraintException(ERR_OUT_OF_RANGE, checkValue, minValue, maxValue);
        }
    }
}
