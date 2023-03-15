/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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
package org.alfresco.rest.framework.resource.parameters.where;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.rest.antlr.WhereClauseParser;
import org.alfresco.rest.framework.tools.RecognizedParamsExtractor;
import org.alfresco.rest.workflow.api.impl.MapBasedQueryWalker;
import org.junit.Test;

/**
 * Tests verifying {@link QueryHelper.QueryResolver} functionality based on {@link BasicQueryWalker}.
 */
public class QueryResolverTest
{
    private final RecognizedParamsExtractor queryExtractor = new RecognizedParamsExtractor() {};

    @Test
    public void testResolveQuery_equals()
    {
        final Query query = queryExtractor.getWhereClause("(propName=testValue)");

        //when
        final WhereProperty property = QueryHelper.resolve(query).getProperty("propName");

        assertThat(property.containsType(WhereClauseParser.EQUALS, false)).isTrue();
        assertThat(property.containsType(WhereClauseParser.GREATERTHAN, false)).isFalse();
        assertThat(property.containsType(WhereClauseParser.LESSTHAN, false)).isFalse();
        assertThat(property.containsType(WhereClauseParser.GREATERTHANOREQUALS, false)).isFalse();
        assertThat(property.containsType(WhereClauseParser.LESSTHANOREQUALS, false)).isFalse();
        assertThat(property.containsType(WhereClauseParser.IN, false)).isFalse();
        assertThat(property.containsType(WhereClauseParser.MATCHES, false)).isFalse();
        assertThat(property.containsType(WhereClauseParser.BETWEEN, false)).isFalse();
        assertThat(property.containsType(WhereClauseParser.EXISTS, false)).isFalse();
        assertThat(property.containsType(WhereClauseParser.EQUALS, true)).isFalse();
        assertThat(property.containsType(WhereClauseParser.GREATERTHAN, true)).isFalse();
        assertThat(property.containsType(WhereClauseParser.LESSTHAN, true)).isFalse();
        assertThat(property.containsType(WhereClauseParser.GREATERTHANOREQUALS, true)).isFalse();
        assertThat(property.containsType(WhereClauseParser.LESSTHANOREQUALS, true)).isFalse();
        assertThat(property.containsType(WhereClauseParser.IN, true)).isFalse();
        assertThat(property.containsType(WhereClauseParser.MATCHES, true)).isFalse();
        assertThat(property.containsType(WhereClauseParser.BETWEEN, true)).isFalse();
        assertThat(property.containsType(WhereClauseParser.EXISTS, true)).isFalse();
        assertThat(property.getExpectedValuesFor(WhereClauseParser.EQUALS, false)).containsOnly("testValue");
    }

    @Test
    public void testResolveQuery_greaterThan()
    {
        final Query query = queryExtractor.getWhereClause("(propName > testValue)");

        //when
        final WhereProperty property = QueryHelper.resolve(query).getProperty("propName");

        assertThat(property.containsType(WhereClauseParser.GREATERTHAN, false)).isTrue();
        assertThat(property.getExpectedValuesFor(WhereClauseParser.GREATERTHAN, false)).containsOnly("testValue");
    }

    @Test
    public void testResolveQuery_greaterThanOrEquals()
    {
        final Query query = queryExtractor.getWhereClause("(propName >= testValue)");

        //when
        final WhereProperty property = QueryHelper.resolve(query).getProperty("propName");

        assertThat(property.containsType(WhereClauseParser.GREATERTHANOREQUALS, false)).isTrue();
        assertThat(property.getExpectedValuesFor(WhereClauseParser.GREATERTHANOREQUALS, false)).containsOnly("testValue");
    }

    @Test
    public void testResolveQuery_lessThan()
    {
        final Query query = queryExtractor.getWhereClause("(propName < testValue)");

        //when
        final WhereProperty property = QueryHelper.resolve(query).getProperty("propName");

        assertThat(property.containsType(WhereClauseParser.LESSTHAN, false)).isTrue();
        assertThat(property.getExpectedValuesFor(WhereClauseParser.LESSTHAN, false)).containsOnly("testValue");
    }

    @Test
    public void testResolveQuery_lessThanOrEquals()
    {
        final Query query = queryExtractor.getWhereClause("(propName <= testValue)");

        //when
        final WhereProperty property = QueryHelper.resolve(query).getProperty("propName");

        assertThat(property.containsType(WhereClauseParser.LESSTHANOREQUALS, false)).isTrue();
        assertThat(property.getExpectedValuesFor(WhereClauseParser.LESSTHANOREQUALS, false)).containsOnly("testValue");
    }

    @Test
    public void testResolveQuery_between()
    {
        final Query query = queryExtractor.getWhereClause("(propName BETWEEN (testValue, testValue2))");

        //when
        final WhereProperty property = QueryHelper.resolve(query).getProperty("propName");

        assertThat(property.containsType(WhereClauseParser.BETWEEN, false)).isTrue();
        assertThat(property.getExpectedValuesFor(WhereClauseParser.BETWEEN, false)).containsOnly("testValue", "testValue2");
    }

    @Test
    public void testResolveQuery_in()
    {
        final Query query = queryExtractor.getWhereClause("(propName IN (testValue, testValue2))");

        //when
        final WhereProperty property = QueryHelper.resolve(query).getProperty("propName");

        assertThat(property.containsType(WhereClauseParser.IN, false)).isTrue();
        assertThat(property.getExpectedValuesFor(WhereClauseParser.IN, false)).containsOnly("testValue", "testValue2");
    }

    @Test
    public void testResolveQuery_matches()
    {
        final Query query = queryExtractor.getWhereClause("(propName MATCHES ('*Value'))");

        //when
        final WhereProperty property = QueryHelper.resolve(query).getProperty("propName");

        assertThat(property.containsType(WhereClauseParser.MATCHES, false)).isTrue();
        assertThat(property.getExpectedValuesFor(WhereClauseParser.MATCHES, false)).containsOnly("*Value");
    }

    @Test
    public void testResolveQuery_exists()
    {
        final Query query = queryExtractor.getWhereClause("(EXISTS (propName))");

        //when
        final WhereProperty property = QueryHelper.resolve(query).getProperty("propName");

        assertThat(property.containsType(WhereClauseParser.EXISTS, false)).isTrue();
        assertThat(property.getExpectedValuesFor(WhereClauseParser.EXISTS, false)).isEmpty();
    }

    @Test
    public void testResolveQuery_notEquals()
    {
        final Query query = queryExtractor.getWhereClause("(NOT propName=testValue)");

        //when
        final WhereProperty property = QueryHelper.resolve(query).getProperty("propName");

        assertThat(property.containsType(WhereClauseParser.EQUALS, true)).isTrue();
        assertThat(property.containsType(WhereClauseParser.EQUALS, false)).isFalse();
        assertThat(property.containsType(WhereClauseParser.GREATERTHAN, false)).isFalse();
        assertThat(property.containsType(WhereClauseParser.LESSTHAN, false)).isFalse();
        assertThat(property.containsType(WhereClauseParser.GREATERTHANOREQUALS, false)).isFalse();
        assertThat(property.containsType(WhereClauseParser.LESSTHANOREQUALS, false)).isFalse();
        assertThat(property.containsType(WhereClauseParser.IN, false)).isFalse();
        assertThat(property.containsType(WhereClauseParser.MATCHES, false)).isFalse();
        assertThat(property.containsType(WhereClauseParser.BETWEEN, false)).isFalse();
        assertThat(property.containsType(WhereClauseParser.EXISTS, false)).isFalse();
        assertThat(property.containsType(WhereClauseParser.GREATERTHAN, true)).isFalse();
        assertThat(property.containsType(WhereClauseParser.LESSTHAN, true)).isFalse();
        assertThat(property.containsType(WhereClauseParser.GREATERTHANOREQUALS, true)).isFalse();
        assertThat(property.containsType(WhereClauseParser.LESSTHANOREQUALS, true)).isFalse();
        assertThat(property.containsType(WhereClauseParser.IN, true)).isFalse();
        assertThat(property.containsType(WhereClauseParser.MATCHES, true)).isFalse();
        assertThat(property.containsType(WhereClauseParser.BETWEEN, true)).isFalse();
        assertThat(property.containsType(WhereClauseParser.EXISTS, true)).isFalse();
        assertThat(property.getExpectedValuesFor(WhereClauseParser.EQUALS, true)).containsOnly("testValue");
    }

    @Test
    public void testResolveQuery_notGreaterThan()
    {
        final Query query = queryExtractor.getWhereClause("(NOT propName > testValue)");

        //when
        final WhereProperty property = QueryHelper.resolve(query).getProperty("propName");

        assertThat(property.containsType(WhereClauseParser.GREATERTHAN, true)).isTrue();
        assertThat(property.getExpectedValuesFor(WhereClauseParser.GREATERTHAN, true)).containsOnly("testValue");
    }

    @Test
    public void testResolveQuery_notGreaterThanOrEquals()
    {
        final Query query = queryExtractor.getWhereClause("(NOT propName >= testValue)");

        //when
        final WhereProperty property = QueryHelper.resolve(query).getProperty("propName");

        assertThat(property.containsType(WhereClauseParser.GREATERTHANOREQUALS, true)).isTrue();
        assertThat(property.getExpectedValuesFor(WhereClauseParser.GREATERTHANOREQUALS, true)).containsOnly("testValue");
    }

    @Test
    public void testResolveQuery_notLessThan()
    {
        final Query query = queryExtractor.getWhereClause("(NOT propName < testValue)");

        //when
        final WhereProperty property = QueryHelper.resolve(query).getProperty("propName");

        assertThat(property.containsType(WhereClauseParser.LESSTHAN, true)).isTrue();
        assertThat(property.getExpectedValuesFor(WhereClauseParser.LESSTHAN, true)).containsOnly("testValue");
    }

    @Test
    public void testResolveQuery_notLessThanOrEquals()
    {
        final Query query = queryExtractor.getWhereClause("(NOT propName <= testValue)");

        //when
        final WhereProperty property = QueryHelper.resolve(query).getProperty("propName");

        assertThat(property.containsType(WhereClauseParser.LESSTHANOREQUALS, true)).isTrue();
        assertThat(property.getExpectedValuesFor(WhereClauseParser.LESSTHANOREQUALS, true)).containsOnly("testValue");
    }

    @Test
    public void testResolveQuery_notBetween()
    {
        final Query query = queryExtractor.getWhereClause("(NOT propName BETWEEN (testValue, testValue2))");

        //when
        final WhereProperty property = QueryHelper.resolve(query).getProperty("propName");

        assertThat(property.containsType(WhereClauseParser.BETWEEN, true)).isTrue();
        assertThat(property.getExpectedValuesFor(WhereClauseParser.BETWEEN, true)).containsOnly("testValue", "testValue2");
    }

    @Test
    public void testResolveQuery_notIn()
    {
        final Query query = queryExtractor.getWhereClause("(NOT propName IN (testValue, testValue2))");

        //when
        final WhereProperty property = QueryHelper.resolve(query).getProperty("propName");

        assertThat(property.containsType(WhereClauseParser.IN, true)).isTrue();
        assertThat(property.getExpectedValuesFor(WhereClauseParser.IN, true)).containsOnly("testValue", "testValue2");
    }

    @Test
    public void testResolveQuery_notMatches()
    {
        final Query query = queryExtractor.getWhereClause("(NOT propName MATCHES ('*Value'))");

        //when
        final WhereProperty property = QueryHelper.resolve(query).getProperty("propName");

        assertThat(property.containsType(WhereClauseParser.MATCHES, true)).isTrue();
        assertThat(property.getExpectedValuesFor(WhereClauseParser.MATCHES, true)).containsOnly("*Value");
    }

    @Test
    public void testResolveQuery_notExists()
    {
        final Query query = queryExtractor.getWhereClause("(NOT EXISTS (propName))");

        //when
        final WhereProperty property = QueryHelper.resolve(query).getProperty("propName");

        assertThat(property.containsType(WhereClauseParser.EXISTS, true)).isTrue();
        assertThat(property.getExpectedValuesFor(WhereClauseParser.EXISTS, true)).isEmpty();
    }

    @Test
    public void testResolveQuery_propertyNotExpected()
    {
        final Query query = queryExtractor.getWhereClause("(propName=testValue AND differentName>18)");

        //when
        final Throwable actualException = catchThrowable(() -> QueryHelper.resolve(query).getProperty("differentName"));

        assertThat(actualException).isInstanceOf(InvalidQueryException.class);
    }

    @Test
    public void testResolveQuery_propertyNotExpectedUsingLenientApproach()
    {
        final Query query = queryExtractor.getWhereClause("(propName=testValue AND differentName>18)");

        //when
        final WhereProperty property = QueryHelper.resolve(query).leniently().getProperty("differentName");

        assertThat(property.containsType(WhereClauseParser.EQUALS, false)).isFalse();
        assertThat(property.containsType(WhereClauseParser.EQUALS, true)).isFalse();
        assertThat(property.getExpectedValuesFor(WhereClauseParser.EQUALS, false)).isNull();
        assertThat(property.getExpectedValuesFor(WhereClauseParser.EQUALS, true)).isNull();
        assertThat(property.containsType(WhereClauseParser.GREATERTHAN, false)).isTrue();
        assertThat(property.getExpectedValuesFor(WhereClauseParser.GREATERTHAN, false)).containsOnly("18");
    }

    @Test
    public void testResolveQuery_propertyNotPresentUsingLenientApproach()
    {
        final Query query = queryExtractor.getWhereClause("(propName=testValue)");

        //when
        final Throwable actualException = catchThrowable(() -> QueryHelper.resolve(query).getProperty("differentName"));

        assertThat(actualException).isInstanceOf(InvalidQueryException.class);
    }

    @Test
    public void testResolveQuery_slashInPropertyName()
    {
        final Query query = queryExtractor.getWhereClause("(EXISTS (prop/name/with/slashes))");

        //when
        final WhereProperty property = QueryHelper.resolve(query).getProperty("prop/name/with/slashes");

        assertThat(property.containsType(WhereClauseParser.EXISTS, false)).isTrue();
        assertThat(property.getExpectedValuesFor(WhereClauseParser.EXISTS, false)).isEmpty();
    }

    @Test
    public void testResolveQuery_propertyBetweenDates()
    {
        final Query query = queryExtractor.getWhereClause("(propName BETWEEN ('2012-01-01', '2012-12-31'))");

        //when
        final WhereProperty property = QueryHelper.resolve(query).getProperty("propName");

        assertThat(property.containsType(WhereClauseParser.BETWEEN, false)).isTrue();
        assertThat(property.getExpectedValuesFor(WhereClauseParser.BETWEEN, false)).containsOnly("2012-01-01", "2012-12-31");
    }

    @Test
    public void testResolveQuery_singlePropertyGreaterThanOrEqualsAndLessThan()
    {
        final Query query = queryExtractor.getWhereClause("(propName >= 18 AND propName < 65)");

        //when
        final WhereProperty property = QueryHelper.resolve(query).getProperty("propName");

        assertThat(property.containsType(WhereClauseParser.GREATERTHANOREQUALS, false)).isTrue();
        assertThat(property.getExpectedValuesFor(WhereClauseParser.GREATERTHANOREQUALS, false)).containsOnly("18");
        assertThat(property.containsType(WhereClauseParser.LESSTHAN, false)).isTrue();
        assertThat(property.getExpectedValuesFor(WhereClauseParser.LESSTHAN, false)).containsOnly("65");
    }

    @Test
    public void testResolveQuery_onePropertyGreaterThanAndSecondPropertyNotMatches()
    {
        final Query query = queryExtractor.getWhereClause("(propName1 > 20 AND NOT propName2 MATCHES ('external*'))");

        //when
        final List<WhereProperty> property = QueryHelper.resolve(query).getProperties("propName1", "propName2");

        assertThat(property.get(0).containsType(WhereClauseParser.GREATERTHAN, false)).isTrue();
        assertThat(property.get(0).getExpectedValuesFor(WhereClauseParser.GREATERTHAN, false)).containsOnly("20");
        assertThat(property.get(1).containsType(WhereClauseParser.MATCHES, true)).isTrue();
        assertThat(property.get(1).getExpectedValuesFor(WhereClauseParser.MATCHES, true)).containsOnly("external*");
    }

    @Test
    public void testResolveQuery_negationsForbidden()
    {
        final Query query = queryExtractor.getWhereClause("(NOT propName=testValue)");

        //when
        final Throwable actualException = catchThrowable(() -> QueryHelper.resolve(query).withoutNegations().getProperty("propName"));

        assertThat(actualException).isInstanceOf(InvalidQueryException.class);
    }

    @Test
    public void testResolveQuery_withoutNegations()
    {
        final Query query = queryExtractor.getWhereClause("(propName=testValue)");

        //when
        final WhereProperty actualProperty = QueryHelper.resolve(query).withoutNegations().getProperty("propName");

        assertThat(actualProperty.containsType(WhereClauseParser.EQUALS, false)).isTrue();
        assertThat(actualProperty.containsType(WhereClauseParser.EQUALS, true)).isFalse();
        assertThat(actualProperty.getExpectedValuesFor(WhereClauseParser.EQUALS).onlyNegated()).isNull();
        assertThat(actualProperty.getExpectedValuesFor(WhereClauseParser.EQUALS).skipNegated()).containsOnly("testValue");
    }

    @Test
    public void testResolveQuery_orNotAllowed()
    {
        final Query query = queryExtractor.getWhereClause("(propName=testValue OR propName BETWEEN (testValue2, testValue3))");

        //when
        final Throwable actualException = catchThrowable(() -> QueryHelper.resolve(query).getProperty("propName"));

        assertThat(actualException).isInstanceOf(InvalidQueryException.class);
    }

    @Test
    public void testResolveQuery_orAllowedInFavorOfAnd()
    {
        final Query query = queryExtractor.getWhereClause("(propName=testValue OR propName=testValue2)");

        //when
        final WhereProperty property = QueryHelper
            .resolve(query)
            .usingOrOperator()
            .getProperty("propName");

        assertThat(property.containsType(WhereClauseParser.EQUALS, false)).isTrue();
        assertThat(property.getExpectedValuesFor(WhereClauseParser.EQUALS, false)).containsOnly("testValue", "testValue2");
    }

    @Test
    public void testResolveQuery_usingCustomQueryWalker()
    {
        final Query query = queryExtractor.getWhereClause("(propName=testValue)");

        //when
        final Collection<String> propertyValues = QueryHelper
            .resolve(query)
            .usingWalker(new MapBasedQueryWalker(Set.of("propName"), null))
            .getProperty("propName", WhereClauseParser.EQUALS, false);

        assertThat(propertyValues).containsOnly("testValue");
    }

    @Test
    public void testResolveQuery_usingCustomBasicQueryWalkerExtension()
    {
        final Query query = queryExtractor.getWhereClause("(propName=testValue OR propName=testValue2)");

        //when
        final WhereProperty property = QueryHelper
            .resolve(query)
            .usingWalker(new BasicQueryWalker("propName")
            {
                @Override
                public void or() {}
                @Override
                public void and() {throw UNSUPPORTED;}
            })
            .withoutNegations()
            .getProperty("propName");

        assertThat(property.containsType(WhereClauseParser.EQUALS, false)).isTrue();
        assertThat(property.getExpectedValuesFor(WhereClauseParser.EQUALS, false)).containsOnly("testValue", "testValue2");
    }

    @Test
    public void testResolveQuery_equalsAndInNotAllowedTogether()
    {
        final Query query = queryExtractor.getWhereClause("(propName=testValue AND propName IN (testValue2, testValue3))");

        //when
        final Throwable actualException = catchThrowable(() -> QueryHelper.resolve(query).getProperty("propName"));

        assertThat(actualException).isInstanceOf(InvalidQueryException.class);
    }

    @Test
    public void testResolveQuery_equalsOrInAllowedTogether()
    {
        final Query query = queryExtractor.getWhereClause("(propName=testValue OR propName IN (testValue2, testValue3))");

        //when
        final WhereProperty whereProperty = QueryHelper.resolve(query).usingOrOperator().getProperty("propName");

        assertThat(whereProperty).isNotNull();
        assertThat(whereProperty.getExpectedValuesForAllOf(WhereClauseParser.EQUALS, WhereClauseParser.IN).skipNegated())
            .isEqualTo(Map.of(WhereClauseParser.EQUALS, Set.of("testValue"), WhereClauseParser.IN, Set.of("testValue2", "testValue3")));
    }

    @Test
    public void testResolveQuery_equalsAndInAllowedTogetherWithDifferentProperties()
    {
        final Query query = queryExtractor.getWhereClause("(propName=testValue AND propName2 IN (testValue2, testValue3))");

        //when
        final List<WhereProperty> properties = QueryHelper
            .resolve(query)
            .getProperties("propName", "propName2");

        assertThat(properties.get(0).containsType(WhereClauseParser.EQUALS, false)).isTrue();
        assertThat(properties.get(0).containsType(WhereClauseParser.IN, false)).isFalse();
        assertThat(properties.get(0).getExpectedValuesForAnyOf(WhereClauseParser.EQUALS, WhereClauseParser.IN).skipNegated().get(WhereClauseParser.EQUALS)).containsOnly("testValue");
        assertThat(properties.get(0).getExpectedValuesForAnyOf(WhereClauseParser.EQUALS, WhereClauseParser.IN).skipNegated().containsKey(WhereClauseParser.IN)).isFalse();
        assertThat(properties.get(1).containsType(WhereClauseParser.EQUALS, false)).isFalse();
        assertThat(properties.get(1).containsType(WhereClauseParser.IN, false)).isTrue();
        assertThat(properties.get(1).getExpectedValuesForAnyOf(WhereClauseParser.EQUALS, WhereClauseParser.IN).skipNegated().containsKey(WhereClauseParser.EQUALS)).isFalse();
        assertThat(properties.get(1).getExpectedValuesForAnyOf(WhereClauseParser.EQUALS, WhereClauseParser.IN).skipNegated().get(WhereClauseParser.IN)).containsOnly("testValue2", "testValue3");
    }

    @Test
    public void testResolveQuery_equalsAndInAllowedAlternately_equals()
    {
        final Query query = queryExtractor.getWhereClause("(propName=testValue)");

        //when
        final WhereProperty property = QueryHelper
            .resolve(query)
            .getProperty("propName");

        assertThat(property.containsType(WhereClauseParser.EQUALS, false)).isTrue();
        assertThat(property.containsType(WhereClauseParser.IN, false)).isFalse();
        assertThat(property.getExpectedValuesForAnyOf(WhereClauseParser.EQUALS, WhereClauseParser.IN).skipNegated().get(WhereClauseParser.EQUALS)).containsOnly("testValue");
        assertThat(property.getExpectedValuesForAnyOf(WhereClauseParser.EQUALS, WhereClauseParser.IN).skipNegated().containsKey(WhereClauseParser.IN)).isFalse();
    }

    @Test
    public void testResolveQuery_equalsAndInAllowedAlternately_in()
    {
        final Query query = queryExtractor.getWhereClause("(propName IN (testValue))");

        //when
        final WhereProperty property = QueryHelper
            .resolve(query)
            .getProperty("propName");

        assertThat(property.containsType(WhereClauseParser.EQUALS, false)).isFalse();
        assertThat(property.containsType(WhereClauseParser.IN, false)).isTrue();
        assertThat(property.getExpectedValuesForAnyOf(WhereClauseParser.EQUALS, WhereClauseParser.IN).skipNegated().containsKey(WhereClauseParser.EQUALS)).isFalse();
        assertThat(property.getExpectedValuesForAnyOf(WhereClauseParser.EQUALS, WhereClauseParser.IN).skipNegated().get(WhereClauseParser.IN)).containsOnly("testValue");
    }

    @Test
    public void testResolveQuery_missingEqualsClauseType()
    {
        final Query query = queryExtractor.getWhereClause("(propName MATCHES (testValue))");

        //when
        final WhereProperty property = QueryHelper
            .resolve(query)
            .getProperty("propName");

        assertThatExceptionOfType(InvalidQueryException.class)
            .isThrownBy(() -> property.getExpectedValuesForAllOf(WhereClauseParser.EQUALS, WhereClauseParser.MATCHES));
    }

    @Test
    public void testResolveQuery_ignoreUnexpectedClauseType()
    {
        final Query query = queryExtractor.getWhereClause("(propName=testValue AND propName MATCHES (testValue))");

        //when
        final WhereProperty property = QueryHelper
            .resolve(query)
            .getProperty("propName");

        assertThat(property.getExpectedValuesForAllOf(WhereClauseParser.EQUALS).skipNegated(WhereClauseParser.EQUALS)).containsOnly("testValue");
    }

    @Test
    public void testResolveQuery_complexAndQuery()
    {
        final Query query = queryExtractor.getWhereClause("(a=v1 AND b>18 AND b<=65 AND NOT c BETWEEN ('2012-01-01','2012-12-31') AND d IN (v1, v2) AND e MATCHES ('*@mail.com') AND EXISTS (f/g))");

        //when
        final List<WhereProperty> properties = QueryHelper
            .resolve(query)
            .getProperties("a", "b", "c", "d", "e", "f/g");

        assertThat(properties).hasSize(6);
        assertThat(properties.get(0).getExpectedValuesFor(WhereProperty.ClauseType.EQUALS)).containsOnly("v1");
        assertThat(properties.get(1).containsAllTypes(WhereProperty.ClauseType.GREATER_THAN, WhereProperty.ClauseType.LESS_THAN_OR_EQUALS)).isTrue();
        assertThat(properties.get(1).getExpectedValuesFor(WhereProperty.ClauseType.GREATER_THAN)).containsOnly("18");
        assertThat(properties.get(1).getExpectedValuesFor(WhereProperty.ClauseType.LESS_THAN_OR_EQUALS)).containsOnly("65");
        assertThat(properties.get(2).getExpectedValuesFor(WhereProperty.ClauseType.NOT_BETWEEN)).containsOnly("2012-01-01", "2012-12-31");
        assertThat(properties.get(3).getExpectedValuesFor(WhereProperty.ClauseType.IN)).containsOnly("v1", "v2");
        assertThat(properties.get(4).getExpectedValuesFor(WhereProperty.ClauseType.MATCHES)).containsOnly("*@mail.com");
        assertThat(properties.get(5).containsType(WhereProperty.ClauseType.EXISTS)).isTrue();
        assertThat(properties.get(5).getExpectedValuesFor(WhereProperty.ClauseType.EXISTS)).isEmpty();
    }

    @Test
    public void testResolveQuery_complexOrQuery()
    {
        final Query query = queryExtractor.getWhereClause("(a=v1 OR b>18 OR b<=65 OR NOT c BETWEEN ('2012-01-01','2012-12-31') OR d IN (v1, v2) OR e MATCHES ('*@mail.com') OR EXISTS (f/g))");

        //when
        final List<WhereProperty> properties = QueryHelper
            .resolve(query)
            .usingOrOperator()
            .getProperties("a", "b", "c", "d", "e", "f/g");

        assertThat(properties).hasSize(6);
        assertThat(properties.get(0).getExpectedValuesFor(WhereProperty.ClauseType.EQUALS)).containsOnly("v1");
        assertThat(properties.get(1).containsAllTypes(WhereProperty.ClauseType.GREATER_THAN, WhereProperty.ClauseType.LESS_THAN_OR_EQUALS)).isTrue();
        assertThat(properties.get(1).getExpectedValuesFor(WhereProperty.ClauseType.GREATER_THAN)).containsOnly("18");
        assertThat(properties.get(1).getExpectedValuesFor(WhereProperty.ClauseType.LESS_THAN_OR_EQUALS)).containsOnly("65");
        assertThat(properties.get(2).getExpectedValuesFor(WhereProperty.ClauseType.NOT_BETWEEN)).containsOnly("2012-01-01", "2012-12-31");
        assertThat(properties.get(3).getExpectedValuesFor(WhereProperty.ClauseType.IN)).containsOnly("v1", "v2");
        assertThat(properties.get(4).getExpectedValuesFor(WhereProperty.ClauseType.MATCHES)).containsOnly("*@mail.com");
        assertThat(properties.get(5).containsType(WhereProperty.ClauseType.EXISTS)).isTrue();
        assertThat(properties.get(5).getExpectedValuesFor(WhereProperty.ClauseType.EXISTS)).isEmpty();
    }

    @Test
    public void testResolveQuery_clauseTypeOptional()
    {
        final Query query = queryExtractor.getWhereClause("(propName MATCHES (testValue))");

        //when
        final WhereProperty property = QueryHelper
            .resolve(query)
            .getProperty("propName");

        assertThat(property.getExpectedValuesForAnyOf(WhereClauseParser.EQUALS, WhereClauseParser.MATCHES).skipNegated(WhereClauseParser.MATCHES)).containsOnly("testValue");
    }

    @Test
    public void testResolveQuery_optionalClauseTypesNotPresent()
    {
        final Query query = queryExtractor.getWhereClause("(propName=testValue AND propName MATCHES (testValue))");

        //when
        final WhereProperty property = QueryHelper
            .resolve(query)
            .getProperty("propName");

        assertThatExceptionOfType(InvalidQueryException.class)
            .isThrownBy(() -> property.getExpectedValuesForAnyOf(WhereClauseParser.IN));
    }

    @Test
    public void testResolveQuery_matchesOrMatchesAllowed()
    {
        final Query query = queryExtractor.getWhereClause("(propName MATCHES ('test*') OR propName MATCHES ('*value*'))");

        //when
        final Collection<String> expectedValues = QueryHelper
            .resolve(query)
            .usingOrOperator()
            .getProperty("propName")
            .getExpectedValuesFor(WhereClauseParser.MATCHES)
            .skipNegated();

        assertThat(expectedValues).containsOnly("test*", "*value*");
    }
}