/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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

package org.alfresco.repo.search.impl.elasticsearch.query.language.afts;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Helper class to convert AFTS date math expressions into Elasticsearch date math expressions. */
public class DateMathConverter
{
    /** Regular expression to match AFTS date math manipulations. */
    private static final String AFTS_MATH_REGEX = "(([+-]\\d+|/)(SECOND|MINUTE|HOUR|DAY|WEEK|MONTH|YEAR)S?)+";
    /** Regular expression pattern to match AFTS date math manipulations. */
    private static final Pattern AFTS_MATH_PATTERN = Pattern.compile(AFTS_MATH_REGEX);
    /** Elasticsearch separates ISO dates from date operations using this string. */
    private static final String ES_DATE_MATH_SEPARATOR = "||";
    /** The string used by AFTS to represent the current date time. */
    private static final String AFTS_NOW = "NOW";
    /** The string used by Elasticsearch to represent the current date time. */
    private static final String ES_NOW = "now";

    /** Private constructor for helper class. */
    private DateMathConverter()
    {}

    /**
     * Convert an AFTS date math expression into an Elasticsearch date math expression.
     * 
     * @param dateMathExpression
     *            The AFTS date math expression.
     * @return The ES date math expression.
     */
    public static String convert(String dateMathExpression)
    {
        dateMathExpression = dateMathExpression.replaceAll(AFTS_NOW, ES_NOW);
        Matcher matcher = AFTS_MATH_PATTERN.matcher(dateMathExpression);
        boolean foundMathExpression = matcher.find();
        if (!foundMathExpression)
        {
            return dateMathExpression;
        }
        String mathPart = matcher.group(0);
        String separator = (dateMathExpression.startsWith(ES_NOW) ? "" : ES_DATE_MATH_SEPARATOR);
        String datePart = dateMathExpression.substring(0, dateMathExpression.length() - mathPart.length());
        return datePart + separator + toESDateFormat(mathPart);
    }

    /** Convert time period names to ES format. */
    private static String toESDateFormat(String mathPart)
    {
        return mathPart.replace("SECONDS", "s").replace("SECOND", "s")
                .replace("MINUTES", "m").replace("MINUTE", "m")
                .replace("HOURS", "h").replace("HOUR", "h")
                .replace("DAYS", "d").replace("DAY", "d")
                .replace("WEEKS", "w").replace("WEEK", "w")
                .replace("MONTHS", "M").replace("MONTH", "M")
                .replace("YEARS", "y").replace("YEAR", "y");
    }
}
