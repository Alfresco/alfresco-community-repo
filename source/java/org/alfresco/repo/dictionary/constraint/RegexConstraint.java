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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.service.cmr.dictionary.ConstraintException;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;

/**
 * Constraint implementation that performs regular expression comparisons.
 * Where possible, the {@link org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter type converter}
 * will be used to first convert the value to a <code>String</code>, so the evaluation
 * will be against the value's <code>String</code> equivalent.
 * <p>
 * The failure condition can be changed to occur either on a match or on a non-match by using
 * the {@link #setRequiresMatch(boolean) requiresMatch} property.  The default is <tt>true</tt>, i.e.
 * failures will occur if the object value does not match the given expression.
 * 
 * @see java.lang.String#matches(java.lang.String)
 * @see java.util.regex.Pattern
 * 
 * @author Derek Hulley
 */
public class RegexConstraint extends AbstractConstraint
{
    public static final String CONSTRAINT_REGEX_NO_MATCH = "d_dictionary.constraint.regex.no_match";
    public static final String CONSTRAINT_REGEX_MATCH = "d_dictionary.constraint.regex.match";

    private String expression;
    private Pattern patternMatcher;
    private boolean requiresMatch = true;
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(80);
        sb.append("RegexConstraint")
          .append("[ expression=").append(expression)
          .append(", requiresMatch=").append(requiresMatch)
          .append("]");
        return sb.toString();
    }

    /**
     * @return Returns the regular expression similar to the {@link String#matches(java.lang.String)}
     */
    public String getExpression()
    {
        return expression;
    }

    /**
     * Set the regular expression used to evaluate String values
     * @param regular expression similar to the {@link String#matches(java.lang.String)} argument
     */
    public void setExpression(String expression)
    {
        this.expression = expression;
    }

    /**
     * @return Returns <tt>true</tt> if the value must match the regular expression
     *      or <tt>false</tt> if the value must not match the regular expression
     */
    public boolean getRequiresMatch()
    {
        return requiresMatch;
    }

    /**
     * Set whether the regular expression must be matched or not
     * 
     * @param requiresMatch Set to <tt>true</tt> if the value must match the regular expression
     *      or <tt>false</tt> if the value must not match the regular expression
     */
    public void setRequiresMatch(boolean requiresMatch)
    {
        this.requiresMatch = requiresMatch;
    }
    
    public void initialize()
    {
        checkPropertyNotNull("expression", expression);

        this.patternMatcher = Pattern.compile(expression);
    }

    protected void evaluateSingleValue(Object value)
    {
        // convert the value to a String
        String valueStr = DefaultTypeConverter.INSTANCE.convert(String.class, value);
        Matcher matcher = patternMatcher.matcher(valueStr);
        boolean matches = matcher.matches();
        if (matches != requiresMatch)
        {
            if (requiresMatch)
            {
                throw new ConstraintException(RegexConstraint.CONSTRAINT_REGEX_NO_MATCH, value, expression);
            }
            else
            {
                throw new ConstraintException(RegexConstraint.CONSTRAINT_REGEX_MATCH, value, expression);
            }
        }
    }
}
