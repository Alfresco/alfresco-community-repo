/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2026 Alfresco Software Limited
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

import static java.time.temporal.ChronoField.MONTH_OF_YEAR;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Date values that Elasticsearch would not match the way Solr does. */
final class DateValueConverter
{
    private static final Pattern MONTH_NAME_DATE = Pattern.compile("(\\d{4})-(\\p{Alpha}+)(.*)");
    private static final Pattern YEAR = Pattern.compile("\\d{4}");
    private static final Pattern YEAR_OR_MONTH = Pattern.compile("\\d{4}(-\\d{1,2})?");
    private static final String YEAR_ROUNDING = "||/y";
    private static final String MONTH_ROUNDING = "||/M";
    private static final DateTimeFormatter MONTH_NAME = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("[MMMM][MMM]")
            .toFormatter(Locale.ENGLISH);

    private DateValueConverter()
    {}

    /** 2025-Aug-05T to 2025-08-05T: Elasticsearch parses numeric months only. Other values are returned unchanged. */
    static String withNumericMonth(String date)
    {
        Matcher matcher = MONTH_NAME_DATE.matcher(date);
        if (!matcher.matches())
        {
            return date;
        }
        try
        {
            int month = MONTH_NAME.parse(matcher.group(2)).get(MONTH_OF_YEAR);
            return String.format(Locale.ROOT, "%s-%02d%s", matcher.group(1), month, matcher.group(3));
        }
        catch (DateTimeParseException e)
        {
            return date;
        }
    }

    /**
     * A year (2025) or a month (2025-08): Solr matches the whole period, Elasticsearch the first day only.
     *
     * @see <a href="https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-range-query#missing-date-components">Missing date components</a>
     */
    static boolean isYearOrMonth(String date)
    {
        return YEAR_OR_MONTH.matcher(date).matches();
    }

    /**
     * {@link #withNumericMonth}, and a year or month rounded to the whole period with date math (2025||/y, 2025-08||/M). A null (open) bound stays null.
     *
     * @see <a href="https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-range-query#range-query-date-math-rounding">Date math rounding</a>
     */
    static String toRangeBound(String bound)
    {
        if (bound == null)
        {
            return null;
        }
        String date = withNumericMonth(bound);
        if (!isYearOrMonth(date))
        {
            return date;
        }
        return date + roundingToWholePeriod(date);
    }

    private static String roundingToWholePeriod(String yearOrMonth)
    {
        return YEAR.matcher(yearOrMonth).matches() ? YEAR_ROUNDING : MONTH_ROUNDING;
    }
}
