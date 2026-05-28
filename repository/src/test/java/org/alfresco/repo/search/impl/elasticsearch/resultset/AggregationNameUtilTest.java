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
package org.alfresco.repo.search.impl.elasticsearch.resultset;

import static org.junit.Assert.*;

import org.junit.Test;

public class AggregationNameUtilTest
{
    @Test
    public void shouldEncodeExample()
    {
        String input = "@{http://www.alfresco.org/model/content/1.0}created:[NOW/DAY-6MONTHS TO NOW/DAY+1DAY]";

        String output = AggregationNameUtil.encode(input);

        String expected = "%40%7Bhttp%3A%2F%2Fwww.alfresco.org%2Fmodel%2Fcontent%2F1.0%7Dcreated%3A%5BNOW%2FDAY-6MONTHS+TO+NOW%2FDAY%2B1DAY%5D";
        assertEquals(expected, output);
    }

    @Test
    public void shouldDecodeExample()
    {
        String input = "%40%7Bhttp%3A%2F%2Fwww.alfresco.org%2Fmodel%2Fcontent%2F1.0%7Dcontent.size%3A%5B1048576+TO+16777216%5D";

        String output = AggregationNameUtil.decode(input);

        String expected = "@{http://www.alfresco.org/model/content/1.0}content.size:[1048576 TO 16777216]";
        assertEquals(expected, output);
    }
}
