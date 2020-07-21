/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.opencmis.mapping;

import java.io.Serializable;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.repo.search.adaptor.lucene.LuceneFunction;
import org.alfresco.repo.search.adaptor.lucene.LuceneQueryParserAdaptor;
import org.alfresco.repo.search.impl.querymodel.PredicateMode;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.SearchLanguageConversion;

/**
 * Property lucene builder for fixed value mapping (eg to null, true, etc)
 * 
 * @author andyh
 */
public class FixedValueLuceneBuilder extends BaseLuceneBuilder
{
    private Serializable value;

    /**
     * Construct
     * 
     * @param value Serializable
     */
    public FixedValueLuceneBuilder(Serializable value)
    {
        super();
        this.value = value;
    }

    @Override
    public <Q, S, E extends Throwable> Q buildLuceneEquality(LuceneQueryParserAdaptor<Q, S, E> lqpa, Serializable value, PredicateMode mode, LuceneFunction luceneFunction) throws E
    {
        if (EqualsHelper.nullSafeEquals(value, value))
        {
            return lqpa.getMatchAllQuery();
        }
        else
        {
            return lqpa.getMatchNoneQuery();
        }
    }

    @Override
    public <Q, S, E extends Throwable> Q buildLuceneExists(LuceneQueryParserAdaptor<Q, S, E> lqpa, Boolean not) throws E
    {
        if (not)
        {
            if (value == null)
            {
                return lqpa.getMatchAllQuery();
            }
            else
            {
                return lqpa.getMatchNoneQuery();
            }
        }
        else
        {
            if (value == null)
            {
                return lqpa.getMatchNoneQuery();
            }
            else
            {
                return lqpa.getMatchAllQuery();
            }
        }

    }

    @Override
    @SuppressWarnings("unchecked")
    public <Q, S, E extends Throwable> Q buildLuceneGreaterThan(LuceneQueryParserAdaptor<Q, S, E> lqpa, Serializable value, PredicateMode mode, LuceneFunction luceneFunction) throws E
    {
        if (value instanceof Comparable)
        {
            Comparable<Serializable> comparable = (Comparable<Serializable>) value;
            if (comparable.compareTo(value) > 0)
            {
                return lqpa.getMatchAllQuery();
            }
            else
            {
                return lqpa.getMatchNoneQuery();
            }
        }
        else
        {
            return lqpa.getMatchNoneQuery();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Q, S, E extends Throwable> Q buildLuceneGreaterThanOrEquals(LuceneQueryParserAdaptor<Q, S, E> lqpa, Serializable value, PredicateMode mode, LuceneFunction luceneFunction) throws E
    {
        if (value instanceof Comparable)
        {
            Comparable<Serializable> comparable = (Comparable<Serializable>) value;
            if (comparable.compareTo(value) >= 0)
            {
                return lqpa.getMatchAllQuery();
            }
            else
            {
                return lqpa.getMatchNoneQuery();
            }
        }
        else
        {
            return lqpa.getMatchNoneQuery();
        }
    }

    @Override
    public <Q, S, E extends Throwable> Q buildLuceneIn(LuceneQueryParserAdaptor<Q, S, E> lqpa, Collection<Serializable> values, Boolean not, PredicateMode mode) throws E
    {
        boolean in = false;
        for (Serializable value : values)
        {
            if (EqualsHelper.nullSafeEquals(value, value))
            {
                in = true;
                break;
            }
        }

        if (in == !not)
        {
            return lqpa.getMatchAllQuery();
        }
        else
        {
            return lqpa.getMatchNoneQuery();
        }
    }

    @Override
    public <Q, S, E extends Throwable> Q buildLuceneInequality(LuceneQueryParserAdaptor<Q, S, E> lqpa, Serializable value, PredicateMode mode, LuceneFunction luceneFunction) throws E
    {
        if (!EqualsHelper.nullSafeEquals(value, value))
        {
            return lqpa.getMatchAllQuery();
        }
        else
        {
            return lqpa.getMatchNoneQuery();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Q, S, E extends Throwable> Q buildLuceneLessThan(LuceneQueryParserAdaptor<Q, S, E> lqpa, Serializable value, PredicateMode mode, LuceneFunction luceneFunction) throws E
    {
        if (value instanceof Comparable)
        {
            Comparable<Serializable> comparable = (Comparable<Serializable>) value;
            if (comparable.compareTo(value) < 0)
            {
                return lqpa.getMatchAllQuery();            }
            else
            {
                return lqpa.getMatchNoneQuery();
            }
        }
        else
        {
            return lqpa.getMatchNoneQuery();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Q, S, E extends Throwable> Q buildLuceneLessThanOrEquals(LuceneQueryParserAdaptor<Q, S, E> lqpa, Serializable value, PredicateMode mode, LuceneFunction luceneFunction) throws E
    {
        if (value instanceof Comparable)
        {
            Comparable<Serializable> comparable = (Comparable<Serializable>) value;
            if (comparable.compareTo(value) <= 0)
            {
                return lqpa.getMatchAllQuery();
            }
            else
            {
                return lqpa.getMatchNoneQuery();
            }
        }
        else
        {
            return lqpa.getMatchNoneQuery();
        }
    }

    @Override
    public <Q, S, E extends Throwable> Q buildLuceneLike(LuceneQueryParserAdaptor<Q, S, E> lqpa, Serializable value, Boolean not) throws E
    {
        if (value != null)
        {
            boolean matches = false;

            Object converted = DefaultTypeConverter.INSTANCE.convert(value.getClass(), value);
            String asString = DefaultTypeConverter.INSTANCE.convert(String.class, converted);
            String regExpression = SearchLanguageConversion.convertSQLLikeToRegex(asString);
            Pattern pattern = Pattern.compile(regExpression);
            String target = DefaultTypeConverter.INSTANCE.convert(String.class, value);
            Matcher matcher = pattern.matcher(target);
            if (matcher.matches())
            {
                matches = true;
            }

            if (matches == !not)
            {
                return lqpa.getMatchAllQuery();
            }
            else
            {
                return lqpa.getMatchNoneQuery();
            }
        }
        else
        {
            return lqpa.getMatchNoneQuery();
        }
    }

    @Override
    public <Q, S, E extends Throwable> String getLuceneSortField(LuceneQueryParserAdaptor<Q, S, E> lqpa)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getLuceneFieldName()
    {
        throw new UnsupportedOperationException();
    }

}
