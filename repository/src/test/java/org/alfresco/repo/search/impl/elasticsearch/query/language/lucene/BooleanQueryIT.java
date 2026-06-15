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

package org.alfresco.repo.search.impl.elasticsearch.query.language.lucene;

import org.alfresco.repo.search.impl.elasticsearch.query.BaseBooleanQueryIT;
import org.alfresco.service.cmr.search.SearchParameters;

public class BooleanQueryIT extends BaseBooleanQueryIT
{
    @Override
    public void whenSearchUsingAND()
    {
        assertContainsOnly(luceneSearch("@cm\\:name:(big AND yellow AND banana)"), big_yellow_banana);
        assertContainsOnly(luceneSearch("@cm\\:name:big AND @cm\\:name:yellow AND @cm\\:name:banana"), big_yellow_banana);
        assertContainsOnly(luceneSearch("big AND yellow AND banana"), big_yellow_banana);
        assertContainsOnly(luceneSearch("+yellow +banana +yellow"), big_yellow_banana);
        assertContainsOnly(searchFor("lucene", "big yellow banana", SearchParameters.Operator.AND), big_yellow_banana);
        assertContainsOnly(luceneSearch("TEXT:big AND TEXT:yellow AND TEXT:banana"), big_yellow_banana);
    }

    @Override
    public void whenSearchUsingOR()
    {
        assertContainsOnly(luceneSearch("@cm\\:name:(big yellow banana)"), big_yellow_banana, yellowTaxi, banana_split);
        assertContainsOnly(luceneSearch("@cm\\:name:big @cm\\:name:yellow @cm\\:name:banana"), big_yellow_banana, yellowTaxi, banana_split);
        assertContainsOnly(luceneSearch("big yellow banana"), big_yellow_banana, yellowTaxi, banana_split);
        assertContainsOnly(luceneSearch("big yellow banana"), big_yellow_banana, yellowTaxi, banana_split);
        assertContainsOnly(searchFor("lucene", "big yellow banana", SearchParameters.Operator.OR), big_yellow_banana, yellowTaxi, banana_split);
        assertContainsOnly(luceneSearch("big OR yellow OR banana"), big_yellow_banana, yellowTaxi, banana_split);
        assertContainsOnly(luceneSearch("TEXT:big TEXT:yellow TEXT:banana"), big_yellow_banana, yellowTaxi, banana_split);
        assertContainsOnly(luceneSearch("TEXT:big OR TEXT:yellow OR TEXT:banana"), big_yellow_banana, yellowTaxi, banana_split);
    }

    @Override
    public void whenSearchUsingNot()
    {
        assertContainsOnly(luceneSearch("-yellow"), just_a_test, banana_split, just_a_another_test, x, b, c, dc, xd, bc, xc, xb, xbc);
        assertContainsOnly(luceneSearch("-@cm\\:name:yellow"), just_a_test, banana_split, just_a_another_test, x, b, c, dc, xd, bc, xc, xb, xbc);
        assertContainsOnly(luceneSearch("NOT yellow"), just_a_test, banana_split, just_a_another_test, x, b, c, dc, xd, bc, xc, xb, xbc);
        assertContainsOnly(luceneSearch("NOT(just a test)"), big_yellow_banana, banana_split, x, b, c, dc, xd, bc, xc, xb, xbc);
    }

    @Override
    public void whenSearchMixingAndNot()
    {
        String query = "just AND test NOT another";
        assertContainsOnly(luceneSearch(query), just_a_test);
        assertContainsOnly(searchFor("lucene", query, SearchParameters.Operator.AND), just_a_test);
        assertContainsOnly(searchFor("lucene", "+just +test -another", SearchParameters.Operator.AND), just_a_test);
    }

    @Override
    public void whenSearchMixingAndORShouldRespectOperatorPrecedence()
    {
        assertContainsOnly(luceneSearch("big AND yellow OR banana"), big_yellow_banana);
        assertContainsOnly(luceneSearch("(big AND yellow) OR banana"), big_yellow_banana, banana_split);
    }

    @Override
    public void whenSearchMixingOrNot()
    {
        assertContainsOnly(luceneSearch("test OR yellow NOT taxi"), big_yellow_banana, just_a_test, just_a_another_test);
        assertContainsOnly(searchFor("lucene", "test OR yellow NOT taxi", SearchParameters.Operator.AND), big_yellow_banana, just_a_test, just_a_another_test);
        assertContainsOnly(searchFor("lucene", "(test OR yellow) NOT taxi", SearchParameters.Operator.AND), big_yellow_banana, just_a_test, just_a_another_test);
        assertContainsOnly(luceneSearch("test yellow -taxi"), big_yellow_banana, just_a_test, just_a_another_test);
    }
}
