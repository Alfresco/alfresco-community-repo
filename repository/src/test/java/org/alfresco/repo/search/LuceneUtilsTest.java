/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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
package org.alfresco.repo.search;

import java.util.Calendar;
import java.util.Date;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link org.alfresco.repo.search.LuceneUtils}.
 *
 * @author Neil Mc Erlean
 * @since 4.0
 */
public class LuceneUtilsTest
{
    @Test public void convertSimpleDate() throws Exception
    {
        Calendar cal = Calendar.getInstance();

        // November 12th, 1955. 10:04 pm exactly. :)
        final int year = 1955;
        final int month = 10; // 0-based
        final int day = 12;
        final int hours = 22;
        final int minutes = 04;
        final int seconds = 00;
        cal.set(year, month, day, hours, minutes, seconds);

        Date testDate = cal.getTime();

        String dateString = LuceneUtils.getLuceneDateString(testDate);
        final String expectedString = "1955\\-11\\-12T22:04:00";

        assertEquals("Incorrect data string.", expectedString, dateString);
    }
}