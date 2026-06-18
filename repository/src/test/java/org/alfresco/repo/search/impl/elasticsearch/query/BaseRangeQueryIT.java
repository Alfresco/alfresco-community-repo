/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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
package org.alfresco.repo.search.impl.elasticsearch.query;

import static org.alfresco.repo.search.impl.elasticsearch.shared.ElasticsearchConstants.CONTENT_SIZE;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import org.alfresco.service.cmr.repository.NodeRef;

public abstract class BaseRangeQueryIT extends ElasticsearchBaseQueryIT
{

    // Numeric fields data set
    protected NodeRef doc100;
    protected NodeRef doc500;
    protected NodeRef doc1000;
    protected NodeRef docMax;
    protected NodeRef docMin;

    // Date fields data set
    protected NodeRef dateBefore1970;
    protected NodeRef[] testDates;
    protected NodeRef dateInNextCentury;

    @Before
    public void initDocuments()
    {
        doc100 = indexDocument("100.txt", "hundred", Map.of(CONTENT_SIZE, 100, "cm:ratingScore", 100.1));
        doc500 = indexDocument("500.txt", "five hundred", Map.of(CONTENT_SIZE, 500, "cm:ratingScore", 500.5));
        doc1000 = indexDocument("1000.txt", "thousand", Map.of(CONTENT_SIZE, 1000, "cm:ratingScore", 1000.1));
        docMax = indexDocument("max.txt", "max", Map.of(CONTENT_SIZE, Long.MAX_VALUE, "cm:ratingScore", Float.MAX_VALUE));
        docMin = indexDocument("min.txt", "min", Map.of(CONTENT_SIZE, Long.MIN_VALUE, "cm:ratingScore", Float.MIN_VALUE));

        dateBefore1970 = indexDefaultDocumentWithModifiedDate("1965-05-11T00:00:00+00:00");
        dateInNextCentury = indexDefaultDocumentWithModifiedDate("2121-05-11T00:00:00+00:00");
        testDates = new NodeRef[]{indexDefaultDocumentWithModifiedDate("2020-05-11T00:00:00+00:00"),
                indexDefaultDocumentWithModifiedDate("2020-05-11T01:02:00+00:00"),
                indexDefaultDocumentWithModifiedDate("2020-05-12T00:00:00+00:00"),
                indexDefaultDocumentWithModifiedDate("2020-05-12T23:59:59+00:00")};
    }

    @SuppressWarnings("PMD.UselessParentheses")
    private NodeRef indexDefaultDocumentWithModifiedDate(String modifiedDate)
    {
        return this.indexDocument(new IndexDocumentSourceBuilder()
                .withName("dateTest")
                .withDate(Date.from(OffsetDateTime.parse((modifiedDate)).toInstant())));
    }

    @Test
    public abstract void whenSearchUsingTimezone();

    @Test
    public abstract void whenSearchUsingTimezoneInQuery();

    @Test
    public abstract void whenSearchUsingDateRange();

    @Test
    public abstract void whenSearchUsingNumericRange();

    @Test
    public abstract void whenSearchUsingDecimalNumbersRange();

    @Test
    public abstract void whenSearchUsingPartialDate();

}
