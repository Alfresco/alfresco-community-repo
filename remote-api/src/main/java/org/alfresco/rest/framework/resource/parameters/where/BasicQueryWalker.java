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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.alfresco.rest.antlr.WhereClauseParser;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Basic implementation of {@link QueryHelper.WalkerCallbackAdapter} providing universal handling of Where query clauses.
 * This implementation supports AND operator and all clause types.
 * Be default, walker verifies strictly if expected or unexpected properties, and it's comparison types are present in query
 * and throws {@link InvalidQueryException} if they are missing.
 */
public class BasicQueryWalker extends QueryHelper.WalkerCallbackAdapter
{
    private static final String EQUALS_AND_IN_NOT_ALLOWED_TOGETHER = "Where query error: cannot use '=' (EQUALS) AND 'IN' clauses with same property: %s";
    private static final String MISSING_PROPERTY = "Where query error: property with name: %s not present";
    static final String MISSING_CLAUSE_TYPE = "Where query error: property with name: %s expects clause: %s";
    static final String MISSING_ANY_CLAUSE_OF_TYPE = "Where query error: property with name: %s expects at least one of clauses: %s";
    private static final String PROPERTY_NOT_EXPECTED = "Where query error: property with name: %s is not expected";
    private static final String PROPERTY_NOT_NEGATABLE = "Where query error: property with name: %s cannot be negated";
    private static final String PROPERTY_NAMES_EMPTY = "Cannot verify WHERE query without expected property names";

    private Collection<String> expectedPropertyNames;
    private final Map<String, WhereProperty> properties;
    protected boolean clausesNegatable = true;
    protected boolean validateStrictly = true;

    public BasicQueryWalker()
    {
        this.properties = new HashMap<>();
    }

    public BasicQueryWalker(final String... expectedPropertyNames)
    {
        this();
        this.expectedPropertyNames = Set.of(expectedPropertyNames);
    }

    public BasicQueryWalker(final Collection<String> expectedPropertyNames)
    {
        this();
        this.expectedPropertyNames = expectedPropertyNames;
    }

    public void setClausesNegatable(final boolean clausesNegatable)
    {
        this.clausesNegatable = clausesNegatable;
    }

    public void setValidateStrictly(boolean validateStrictly)
    {
        this.validateStrictly = validateStrictly;
    }

    @Override
    public void exists(String propertyName, boolean negated)
    {
        verifyPropertyExpectedness(propertyName);
        verifyClausesNegatability(negated, propertyName);
        addProperties(propertyName, WhereClauseParser.EXISTS, negated);
    }

    @Override
    public void between(String propertyName, String firstValue, String secondValue, boolean negated)
    {
        verifyPropertyExpectedness(propertyName);
        verifyClausesNegatability(negated, propertyName);
        addProperties(propertyName, WhereClauseParser.BETWEEN, negated, firstValue, secondValue);
    }

    @Override
    public void comparison(int type, String propertyName, String propertyValue, boolean negated)
    {
        verifyPropertyExpectedness(propertyName);
        verifyClausesNegatability(negated, propertyName);
        if (WhereClauseParser.EQUALS == type && isAndSupported() && containsProperty(propertyName, WhereClauseParser.IN, negated))
        {
            throw new InvalidQueryException(String.format(EQUALS_AND_IN_NOT_ALLOWED_TOGETHER, propertyName));
        }

        addProperties(propertyName, type, negated, propertyValue);
    }

    @Override
    public void in(String propertyName, boolean negated, String... propertyValues)
    {
        verifyPropertyExpectedness(propertyName);
        verifyClausesNegatability(negated, propertyName);
        if (isAndSupported() && containsProperty(propertyName, WhereClauseParser.EQUALS, negated))
        {
            throw new InvalidQueryException(String.format(EQUALS_AND_IN_NOT_ALLOWED_TOGETHER, propertyName));
        }

        addProperties(propertyName, WhereClauseParser.IN, negated, propertyValues);
    }

    @Override
    public void matches(final String propertyName, String propertyValue, boolean negated)
    {
        verifyPropertyExpectedness(propertyName);
        verifyClausesNegatability(negated, propertyName);
        addProperties(propertyName, WhereClauseParser.MATCHES, negated, propertyValue);
    }

    @Override
    public void and()
    {
        // Don't need to do anything here - it's enough to enable AND operator.
        // OR is not supported at the same time.
    }

    /**
     * Verify if property is expected, if not throws {@link InvalidQueryException}.
     */
    protected void verifyPropertyExpectedness(final String propertyName)
    {
        if (validateStrictly && CollectionUtils.isNotEmpty(expectedPropertyNames) && !this.expectedPropertyNames.contains(propertyName))
        {
            throw new InvalidQueryException(String.format(PROPERTY_NOT_EXPECTED, propertyName));
        }
        else if (validateStrictly && CollectionUtils.isEmpty(expectedPropertyNames))
        {
            throw new IllegalStateException(PROPERTY_NAMES_EMPTY);
        }
    }

    /**
     * Verify if clause negations are allowed, if not throws {@link InvalidQueryException}.
     */
    protected void verifyClausesNegatability(final boolean negated, final String propertyName)
    {
        if (!clausesNegatable && negated)
        {
            throw new InvalidQueryException(String.format(PROPERTY_NOT_NEGATABLE, propertyName));
        }
    }

    protected boolean isAndSupported()
    {
        try
        {
            and();
            return true;
        }
        catch (InvalidQueryException ignore)
        {
            return false;
        }
    }

    protected void addProperties(final String propertyName, final int clauseType, final String... propertyValues)
    {
        this.addProperties(propertyName, clauseType, false, propertyValues);
    }

    protected void addProperties(final String propertyName, final int clauseType, final boolean negated, final String... propertyValues)
    {
        final WhereProperty.ClauseType type = WhereProperty.ClauseType.of(clauseType, negated);
        final Set<String> propertiesToAdd = Optional.ofNullable(propertyValues).map(Set::of).orElse(Collections.emptySet());
        if (this.containsProperty(propertyName))
        {
            this.properties.get(propertyName).addValuesToType(type, propertiesToAdd);
        }
        else
        {
            this.properties.put(propertyName, new WhereProperty(propertyName, type, propertiesToAdd, validateStrictly));
        }
    }

    protected boolean containsProperty(final String propertyName)
    {
        return this.properties.containsKey(propertyName);
    }

    protected boolean containsProperty(final String propertyName, final int clauseType, final boolean negated)
    {
        return this.properties.containsKey(propertyName) && this.properties.get(propertyName).containsType(clauseType, negated);
    }

    @Override
    public Collection<String> getProperty(String propertyName, int type, boolean negated)
    {
        return this.getProperty(propertyName).getExpectedValuesFor(type, negated);
    }

    public WhereProperty getProperty(final String propertyName)
    {
        if (validateStrictly && !this.containsProperty(propertyName))
        {
            throw new InvalidQueryException(String.format(MISSING_PROPERTY, propertyName));
        }

        return this.properties.get(propertyName);
    }

    public List<WhereProperty> getProperties(final String... propertyNames)
    {
        return Arrays.stream(propertyNames)
            .filter(StringUtils::isNotBlank)
            .distinct()
            .peek(propertyName -> {
                if (validateStrictly && !this.containsProperty(propertyName))
                {
                    throw new InvalidQueryException(String.format(MISSING_PROPERTY, propertyName));
                }
            })
            .map(this.properties::get)
            .collect(Collectors.toList());
    }

    public Map<String, WhereProperty> getPropertiesAsMap(final String... propertyNames)
    {
        return Arrays.stream(propertyNames)
            .filter(StringUtils::isNotBlank)
            .distinct()
            .peek(propertyName -> {
                if (validateStrictly && !this.containsProperty(propertyName))
                {
                    throw new InvalidQueryException(String.format(MISSING_PROPERTY, propertyName));
                }
            })
            .collect(Collectors.toMap(propertyName -> propertyName, this.properties::get));
    }
}
