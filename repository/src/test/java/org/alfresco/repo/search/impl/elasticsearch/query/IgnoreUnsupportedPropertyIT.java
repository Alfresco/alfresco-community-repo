/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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

import org.junit.Before;
import org.junit.Test;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.SearchParameters;

@SuppressWarnings("PMD.FieldNamingConventions")
public class IgnoreUnsupportedPropertyIT extends LuceneOrAFTSQueryIT
{

    private final String language;
    protected NodeRef big_yellow_banana;
    protected NodeRef yellowTaxi;

    public IgnoreUnsupportedPropertyIT(String language)
    {
        super(language);
        this.language = language;
    }

    @Before
    public void initDocuments()
    {
        big_yellow_banana = indexDocument("big yellow banana");
        yellowTaxi = indexDocument("yellow taxi test another");
    }

    @Test
    public void shouldReturnZeroResultsWhenSearchForNegatedUnsupportedField()
    {
        assertContainsOnly(searchFor(language, "-PNAME:\"0/wiki\""));
    }

    @Test
    public void shouldRemoveFilterWhenContainsOnlyUnsupportedCondition()
    {
        SearchParameters searchParameters = searchWithFilters("banana", "-PNAME:'0/wiki'");
        assertContainsOnly(searchFor(searchParameters), big_yellow_banana);
    }

    @Test
    public void shouldIgnoreUnsupportedFieldInFilter()
    {
        SearchParameters searchParameters = searchWithFilters("yellow", "taxi AND -PNAME:'0/wiki'");
        assertContainsOnly(searchFor(searchParameters), yellowTaxi);
    }

    @Test
    public void shouldIgnoreUnsupportedFieldWhenSearchingUsingAnd()
    {
        assertContainsOnly(searchFor(language, "banana AND TXCOMMITTIME:taxi"), big_yellow_banana);
        assertContainsOnly(searchFor(language, "TXCOMMITTIME:taxi AND banana"), big_yellow_banana);
    }

    @Test
    public void shouldIgnoreUnsupportedFieldWhenSearchingByWildcardUsingAnd()
    {
        assertContainsOnly(searchFor(language, "banana AND TXCOMMITTIME:*"), big_yellow_banana);
        assertContainsOnly(searchFor(language, "TXCOMMITTIME:* AND banana"), big_yellow_banana);
    }

    @Test
    public void shouldIgnoreUnsupportedFieldWhenSearchingUsingOr()
    {
        assertContainsOnly(searchFor(language, "banana OR TXCOMMITTIME:taxi"), big_yellow_banana);
        assertContainsOnly(searchFor(language, "TXCOMMITTIME:taxi OR banana"), big_yellow_banana);
    }

    @Test
    public void shouldIgnoreUnsupportedFieldWhenSearchingByWildcardUsingOr()
    {
        assertContainsOnly(searchFor(language, "banana OR TXCOMMITTIME:*"), big_yellow_banana);
        assertContainsOnly(searchFor(language, "TXCOMMITTIME:* OR banana"), big_yellow_banana);
    }

    @Test
    public void shouldReturnZeroResultsWhenSearchUsingUnsupportedProperty()
    {
        assertContainsOnly(searchFor(language, "TXCOMMITTIME:taxi"));
    }

    @Test
    public void shouldReturnZeroResultsWhenSearchWildcardUsingUnsupportedProperty()
    {
        assertContainsOnly(searchFor(language, "TXCOMMITTIME:*"));
    }

    private SearchParameters searchWithFilters(String query, String... filterQueries)
    {
        SearchParameters searchParams = new SearchParameters();
        searchParams.setQuery(query);
        searchParams.setLimit(10);
        searchParams.setSkipCount(0);
        searchParams.setLanguage(this.language);
        for (String fq : filterQueries)
        {
            searchParams.addFilterQuery(fq);
        }
        return searchParams;
    }

}
