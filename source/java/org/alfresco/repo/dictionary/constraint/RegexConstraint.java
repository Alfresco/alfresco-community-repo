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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.extensions.surf.util.I18NUtil;
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
    public static final String CONSTRAINT_REGEX_MSG_PREFIX = "d_dictionary.constraint.regex.error.";

    private String expression;
    private Pattern patternMatcher;
    private boolean requiresMatch = true;

    /**
     * {@inheritDoc}
     */
    public String getType()
    {
        return "REGEX";
    }

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

    @Override
    public Map<String, Object> getParameters()
    {
        Map<String, Object> params = new HashMap<String, Object>(2);
        
        params.put("expression", this.expression);
        params.put("requiresMatch", this.requiresMatch);
        
        return params;
    }
    
    @Override
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
            // Look for a message corresponding to this constraint name
            String messageId = CONSTRAINT_REGEX_MSG_PREFIX + getShortName();
            if (I18NUtil.getMessage(messageId, value) != null)
            {
                throw new ConstraintException(messageId, value);
            }
            // Otherwise, fall back to a generic (but unfriendly) message
            else if (requiresMatch)
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
