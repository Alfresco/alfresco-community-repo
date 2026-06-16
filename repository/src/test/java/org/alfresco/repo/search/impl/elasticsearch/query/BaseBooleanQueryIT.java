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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.SearchParameters;

@SuppressWarnings("PMD")
public abstract class BaseBooleanQueryIT extends ElasticsearchBaseQueryIT
{

    protected NodeRef big_yellow_banana;
    protected NodeRef yellowTaxi;
    protected NodeRef banana_split;
    protected NodeRef just_a_test;
    protected NodeRef just_a_another_test;
    protected NodeRef xbc;
    protected NodeRef xb;
    protected NodeRef xc;
    protected NodeRef bc;
    protected NodeRef xd;
    protected NodeRef dc;
    protected NodeRef x;
    protected NodeRef b;
    protected NodeRef c;

    @Before
    public void initDocuments()
    {
        big_yellow_banana = indexDocument("big yellow banana");
        yellowTaxi = indexDocument("yellow taxi test another");
        banana_split = indexDocument("bigger banana split");
        just_a_test = indexDocument("just a test");
        just_a_another_test = indexDocument("just a another test");
        xbc = indexDocument("x b c");
        xb = indexDocument("x b");
        bc = indexDocument("b c");
        xc = indexDocument("x c");
        xd = indexDocument("x d");
        dc = indexDocument("d c");
        x = indexDocument("x");
        b = indexDocument("b");
        c = indexDocument("c");

    }

    @Test
    public abstract void whenSearchUsingAND();

    @Test
    public abstract void whenSearchUsingOR();

    @Test
    public abstract void whenSearchUsingNot();

    @Test
    public abstract void whenSearchMixingAndNot();

    @Test
    public abstract void whenSearchMixingAndORShouldRespectOperatorPrecedence();

    @Test
    public abstract void whenSearchMixingOrNot();

    @Test
    public void usingParenthesis_shouldRespectPrecedence()
    {
        assertContainsOnly(aftsSearch("cm:name:(x AND (b OR c))"), xbc, xb, xc);
        assertContainsOnly(aftsSearch("cm:name:((x AND b) OR c)"), xb, xbc, c, bc, xc, dc);

        assertContainsOnly(aftsSearch("cm:name:(x OR (b AND c))"), x, xd, xc, xb, xbc, bc);
        assertContainsOnly(aftsSearch("cm:name:((x OR b) AND c)"), xc, bc, xbc);
    }

    /* These tests are broken because of SEARCH-2656, please enable them back when fixed */
    @Ignore
    @Test
    public void defaultBooleanOperator_shouldNotAffectExplicitOccurrenceOperators()
    {
        assertContainsOnly(aftsSearch("|big |yellow |banana", SearchParameters.Operator.AND), big_yellow_banana, yellowTaxi, banana_split);// with all optional, minimum should match kicks in
        assertContainsOnly(aftsSearch("|test |yellow !taxi", SearchParameters.Operator.AND), big_yellow_banana, just_a_test, just_a_another_test);
        assertContainsOnly(aftsSearch("+yellow |banana"), yellowTaxi, big_yellow_banana);
        assertContainsOnly(aftsSearch("+yellow +banana"), big_yellow_banana);
        assertContainsOnly(aftsSearch("+just +test !another", SearchParameters.Operator.AND), just_a_test);
        assertContainsOnly(aftsSearch("+just +test !another", SearchParameters.Operator.OR), just_a_test);
        assertContainsOnly(aftsSearch("|test |yellow !taxi"), big_yellow_banana, just_a_test, just_a_another_test);
        assertContainsOnly(aftsSearch("test yellow !taxi"), big_yellow_banana, just_a_test, just_a_another_test);
    }

    /* These tests are broken because of SEARCH-2656, please enable them back when fixed */
    @Ignore
    @Test
    public void defaultBooleanOperator_shouldNotAffectNotClause()
    {
        assertContainsOnly(aftsSearch("test OR yellow NOT taxi", SearchParameters.Operator.AND), big_yellow_banana, just_a_test, just_a_another_test);
        assertContainsOnly(aftsSearch("test OR yellow NOT taxi", SearchParameters.Operator.OR), big_yellow_banana, just_a_test, just_a_another_test);
        assertContainsOnly(aftsSearch("(test OR yellow) NOT taxi", SearchParameters.Operator.AND), big_yellow_banana, just_a_test,
                just_a_another_test);
    }
}
