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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.service.cmr.dictionary.ConstraintException;
import org.alfresco.service.cmr.dictionary.DictionaryException;
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
        if (expression == null)
        {
            throw new DictionaryException(AbstractConstraint.ERR_PROP_NOT_SET, "expression");
        }
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
