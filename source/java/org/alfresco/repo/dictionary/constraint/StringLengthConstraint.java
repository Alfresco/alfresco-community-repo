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

    public void initialize()
    {
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
