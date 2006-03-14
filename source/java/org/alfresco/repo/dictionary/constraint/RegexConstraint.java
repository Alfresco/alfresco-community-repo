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


import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;

/**
 * Constraint implementation that performs regular expression comparisons.
 * Where possible, the {@link org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter type converter}
 * will be used to first convert the value to a <code>String</code>, so the evaluation
 * will be against the value's <code>String</code> equivalent.
 * 
 * @see java.lang.String#matches(java.lang.String)
 * 
 * @author Derek Hulley
 */
public class RegexConstraint extends AbstractConstraint
{
    public static final String CONSTRAINT_REGEX_NO_MATCH = "d_dictionary.constraint.regex.no_match";

    private String expression;

    /**
     * Set the regular expression used to evaluate string values
     * @param expression similar to the {@link String#matches(java.lang.String) argument
     */
    public void setExpression(String expression)
    {
        this.expression = expression;
    }

    public void initialize()
    {
        if (expression == null)
        {
            throw new DictionaryException(AbstractConstraint.ERR_PROP_NOT_SET, "expression");
        }
    }

    public void evaluateSingleValue(Object value)
    {
        // convert the value to a String
        String valueStr = DefaultTypeConverter.INSTANCE.convert(String.class, value);
        boolean matches = valueStr.matches(expression);
        if (!matches)
        {
            throw new DictionaryException(RegexConstraint.CONSTRAINT_REGEX_NO_MATCH, value, expression);
        }
    }
}
