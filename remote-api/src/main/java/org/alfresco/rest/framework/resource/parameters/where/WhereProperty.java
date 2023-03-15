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

import static java.util.function.Predicate.not;

import static org.alfresco.rest.framework.resource.parameters.where.BasicQueryWalker.MISSING_ANY_CLAUSE_OF_TYPE;
import static org.alfresco.rest.framework.resource.parameters.where.BasicQueryWalker.MISSING_CLAUSE_TYPE;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.alfresco.rest.antlr.WhereClauseParser;

/**
 * Map composed of property comparison type and compared values.
 * Map key is clause (comparison) type.
 */
public class WhereProperty extends HashMap<WhereProperty.ClauseType, Collection<String>>
{
    private final String name;
    private boolean validateStrictly;

    public WhereProperty(final String name, final ClauseType clauseType, final Collection<String> values)
    {
        super(Map.of(clauseType, new HashSet<>(values)));
        this.name = name;
        this.validateStrictly = true;
    }

    public WhereProperty(final String name, final ClauseType clauseType, final Collection<String> values, final boolean validateStrictly)
    {
        this(name, clauseType, values);
        this.validateStrictly = validateStrictly;
    }

    public String getName()
    {
        return name;
    }

    public void addValuesToType(final ClauseType clauseType, final Collection<String> values)
    {
        if (this.containsKey(clauseType))
        {
            this.get(clauseType).addAll(values);
        }
        else
        {
            this.put(clauseType, new HashSet<>(values));
        }
    }

    public boolean containsType(final ClauseType clauseType)
    {
        return this.containsKey(clauseType);
    }

    public boolean containsType(final int clauseType, final boolean negated)
    {
        return this.containsKey(ClauseType.of(clauseType, negated));
    }

    public boolean containsAllTypes(final ClauseType... clauseType)
    {
        return Arrays.stream(clauseType).distinct().filter(this::containsKey).count() == clauseType.length;
    }

    public boolean containsAnyOfTypes(final ClauseType... clauseType)
    {
        return Arrays.stream(clauseType).distinct().anyMatch(this::containsKey);
    }

    public Collection<String> getExpectedValuesFor(final ClauseType clauseType)
    {
        verifyAllClausesPresence(clauseType);
        return this.get(clauseType);
    }

    public HashMap<ClauseType, Collection<String>> getExpectedValuesForAllOf(final ClauseType... clauseTypes)
    {
        verifyAllClausesPresence(clauseTypes);
        return Arrays.stream(clauseTypes)
            .distinct()
            .collect(Collectors.toMap(type -> type, this::get, (type1, type2) -> type1, MultiTypeNegatableValuesMap::new));
    }

    public HashMap<ClauseType, Collection<String>> getExpectedValuesForAnyOf(final ClauseType... clauseTypes)
    {
        verifyAnyClausesPresence(clauseTypes);
        return Arrays.stream(clauseTypes)
            .distinct()
            .collect(Collectors.toMap(type -> type, this::get, (type1, type2) -> type1, MultiTypeNegatableValuesMap::new));
    }

    public Collection<String> getExpectedValuesFor(final int clauseType, final boolean negated)
    {
        verifyAllClausesPresence(ClauseType.of(clauseType, negated));
        return this.get(ClauseType.of(clauseType, negated));
    }

    public NegatableValuesMap getExpectedValuesFor(final int clauseType)
    {
        verifyAllClausesPresence(clauseType);
        final NegatableValuesMap values = new NegatableValuesMap();
        final ClauseType type = ClauseType.of(clauseType);
        final ClauseType negatedType = type.negate();
        if (this.containsKey(type))
        {
            values.put(false, this.get(type));
        }
        if (this.containsKey(negatedType))
        {
            values.put(true, this.get(negatedType));
        }
        return values;
    }

    public MultiTypeNegatableValuesMap getExpectedValuesForAllOf(final int... clauseTypes)
    {
        verifyAllClausesPresence(clauseTypes);
        return getExpectedValuesFor(clauseTypes);
    }

    public MultiTypeNegatableValuesMap getExpectedValuesForAnyOf(final int... clauseTypes)
    {
        verifyAnyClausesPresence(clauseTypes);
        return getExpectedValuesFor(clauseTypes);
    }

    private MultiTypeNegatableValuesMap getExpectedValuesFor(final int... clauseTypes)
    {
        final MultiTypeNegatableValuesMap values = new MultiTypeNegatableValuesMap();
        Arrays.stream(clauseTypes).distinct().forEach(clauseType -> {
            final ClauseType type = ClauseType.of(clauseType);
            final ClauseType negatedType = type.negate();
            if (this.containsKey(type))
            {
                values.put(type, this.get(type));
            }
            if (this.containsKey(negatedType))
            {
                values.put(negatedType, this.get(negatedType));
            }
        });

        return values;
    }

    /**
     * Verify if all specified clause types are present in this map, if not than throw {@link InvalidQueryException}.
     */
    private void verifyAllClausesPresence(final ClauseType... clauseTypes)
    {
        if (validateStrictly)
        {
            Arrays.stream(clauseTypes).distinct().forEach(clauseType -> {
                if (!this.containsType(clauseType))
                {
                    throw new InvalidQueryException(String.format(MISSING_CLAUSE_TYPE, this.name, WhereClauseParser.tokenNames[clauseType.getTypeNumber()]));
                }
            });
        }
    }

    /**
     * Verify if all specified clause types are present in this map, if not than throw {@link InvalidQueryException}.
     * Exception is thrown when both, negated and non-negated types are missing.
     */
    private void verifyAllClausesPresence(final int... clauseTypes)
    {
        if (validateStrictly)
        {
            Arrays.stream(clauseTypes).distinct().forEach(clauseType -> {
                if (!this.containsType(clauseType, false) && !this.containsType(clauseType, true))
                {
                    throw new InvalidQueryException(String.format(MISSING_CLAUSE_TYPE, this.name, WhereClauseParser.tokenNames[clauseType]));
                }
            });
        }
    }

    /**
     * Verify if any of specified clause types are present in this map, if not than throw {@link InvalidQueryException}.
     */
    private void verifyAnyClausesPresence(final ClauseType... clauseTypes)
    {
        if (validateStrictly)
        {
            if (!this.containsAnyOfTypes(clauseTypes))
            {
                throw new InvalidQueryException(String.format(MISSING_ANY_CLAUSE_OF_TYPE,
                    this.name, Arrays.stream(clauseTypes).map(type -> WhereClauseParser.tokenNames[type.getTypeNumber()]).collect(Collectors.toList())));
            }
        }
    }

    /**
     * Verify if any of specified clause types are present in this map, if not than throw {@link InvalidQueryException}.
     * Exception is thrown when both, negated and non-negated types are missing.
     */
    private void verifyAnyClausesPresence(final int... clauseTypes)
    {
        if (validateStrictly)
        {
            final Collection<ClauseType> expectedTypes = Arrays.stream(clauseTypes)
                .distinct()
                .boxed()
                .flatMap(type -> Stream.of(ClauseType.of(type), ClauseType.of(type, true)))
                .collect(Collectors.toSet());
            if (!this.containsAnyOfTypes(expectedTypes.toArray(ClauseType[]::new)))
            {
                throw new InvalidQueryException(String.format(MISSING_ANY_CLAUSE_OF_TYPE,
                    this.name, Arrays.stream(clauseTypes).mapToObj(type -> WhereClauseParser.tokenNames[type]).collect(Collectors.toList())));
            }
        }
    }

    public enum ClauseType
    {
        EQUALS(WhereClauseParser.EQUALS),
        NOT_EQUALS(WhereClauseParser.EQUALS, true),
        GREATER_THAN(WhereClauseParser.GREATERTHAN),
        NOT_GREATER_THAN(WhereClauseParser.GREATERTHAN, true),
        LESS_THAN(WhereClauseParser.LESSTHAN),
        NOT_LESS_THAN(WhereClauseParser.LESSTHAN, true),
        GREATER_THAN_OR_EQUALS(WhereClauseParser.GREATERTHANOREQUALS),
        NOT_GREATER_THAN_OR_EQUALS(WhereClauseParser.GREATERTHANOREQUALS, true),
        LESS_THAN_OR_EQUALS(WhereClauseParser.LESSTHANOREQUALS),
        NOT_LESS_THAN_OR_EQUALS(WhereClauseParser.LESSTHANOREQUALS, true),
        BETWEEN(WhereClauseParser.BETWEEN),
        NOT_BETWEEN(WhereClauseParser.BETWEEN, true),
        IN(WhereClauseParser.IN),
        NOT_IN(WhereClauseParser.IN, true),
        MATCHES(WhereClauseParser.MATCHES),
        NOT_MATCHES(WhereClauseParser.MATCHES, true),
        EXISTS(WhereClauseParser.EXISTS),
        NOT_EXISTS(WhereClauseParser.EXISTS, true);

        private final int typeNumber;
        private final boolean negated;

        ClauseType(final int typeNumber)
        {
            this.typeNumber = typeNumber;
            this.negated = false;
        }

        ClauseType(final int typeNumber, final boolean negated)
        {
            this.typeNumber = typeNumber;
            this.negated = negated;
        }

        public static ClauseType of(final int type)
        {
            return of(type, false);
        }

        public static ClauseType of(final int type, final boolean negated)
        {
            return Arrays.stream(ClauseType.values())
                .filter(clauseType -> clauseType.typeNumber == type && clauseType.negated == negated)
                .findFirst()
                .orElseThrow();
        }

        public ClauseType negate()
        {
            return of(typeNumber, !negated);
        }

        public int getTypeNumber()
        {
            return typeNumber;
        }

        public boolean isNegated()
        {
            return negated;
        }
    }

    public static class NegatableValuesMap extends HashMap<Boolean, Collection<String>>
    {
        public Collection<String> skipNegated()
        {
            return this.get(false);
        }

        public Collection<String> onlyNegated()
        {
            return this.get(true);
        }
    }

    public static class MultiTypeNegatableValuesMap extends HashMap<ClauseType, Collection<String>>
    {
        public Map<Integer, Collection<String>> skipNegated()
        {
            return this.keySet().stream()
                .filter(not(ClauseType::isNegated))
                .collect(Collectors.toMap(key -> key.typeNumber, this::get));
        }

        public Collection<String> skipNegated(final int clauseType)
        {
            return this.get(ClauseType.of(clauseType));
        }

        public Map<Integer, Collection<String>> onlyNegated()
        {
            return this.keySet().stream()
                .filter(not(ClauseType::isNegated))
                .collect(Collectors.toMap(key -> key.typeNumber, this::get));
        }

        public Collection<String> onlyNegated(final int clauseType)
        {
            return this.get(ClauseType.of(clauseType, true));
        }
    }
}
