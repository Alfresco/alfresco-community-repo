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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Test;

@SuppressWarnings("PMD.UnitTestShouldIncludeAssert") // PMD misses the asserts inside lambdas
public class DateValueConverterTest
{
    @Test
    public void shouldConvertMonthNameToNumber()
    {
        Map.of("2025-Aug-05T", "2025-08-05T",
                "2025-aug-05T", "2025-08-05T",
                "2025-AUGUST-05T", "2025-08-05T",
                "2025-Dec-31", "2025-12-31",
                "2025-May", "2025-05",
                "2025-Aug-05T10\\:15", "2025-08-05T10\\:15")
                .forEach((date, expected) -> assertEquals(date, expected, DateValueConverter.withNumericMonth(date)));
    }

    @Test
    public void shouldKeepValuesWithoutMonthName()
    {
        List.of("2025-08-05T", "2025-Foo-05T", "Aug-2025", "NOW/DAY", "test")
                .forEach(value -> assertEquals(value, DateValueConverter.withNumericMonth(value)));
    }

    @Test
    public void shouldRecogniseYearOrMonth()
    {
        List.of("2025", "2025-08", "2025-8").forEach(value -> assertTrue(value, DateValueConverter.isYearOrMonth(value)));
        List.of("2025-08-05", "2025-08-05T", "2025-Aug", "20250", "now").forEach(value -> assertFalse(value, DateValueConverter.isYearOrMonth(value)));
    }

    @Test
    public void shouldRoundYearOrMonthRangeBoundToWholePeriod()
    {
        Map.of("2025", "2025||/y",
                "2025-08", "2025-08||/M",
                "2025-Aug", "2025-08||/M",
                "2025-Aug-05", "2025-08-05",
                "now/d", "now/d",
                "*", "*")
                .forEach((bound, expected) -> assertEquals(bound, expected, DateValueConverter.toRangeBound(bound)));
        assertNull(DateValueConverter.toRangeBound(null));
    }

    /** Elasticsearch reads ASCII digits only; String.format without a locale writes Arabic-Indic digits on ar-EG. */
    @Test
    public void shouldWriteAsciiDigitsWhateverTheDefaultLocale()
    {
        Locale defaultLocale = Locale.getDefault(Locale.Category.FORMAT);
        Locale.setDefault(Locale.Category.FORMAT, Locale.forLanguageTag("ar-EG"));
        try
        {
            assertEquals("2025-08-05T", DateValueConverter.withNumericMonth("2025-Aug-05T"));
            assertEquals("2025-08||/M", DateValueConverter.toRangeBound("2025-Aug"));
        }
        finally
        {
            Locale.setDefault(Locale.Category.FORMAT, defaultLocale);
        }
    }
}
