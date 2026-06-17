/*
 * #%L
 * Alfresco Data model classes
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
package org.alfresco.opencmis.search;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.springframework.extensions.surf.util.I18NUtil;

import org.alfresco.repo.search.impl.querymodel.Argument;
import org.alfresco.repo.search.impl.querymodel.ArgumentDefinition;
import org.alfresco.repo.search.impl.querymodel.Constraint;
import org.alfresco.repo.search.impl.querymodel.Function;
import org.alfresco.repo.search.impl.querymodel.LiteralArgument;
import org.alfresco.repo.search.impl.querymodel.PropertyArgument;
import org.alfresco.repo.search.impl.querymodel.QueryModelFactory;
import org.alfresco.repo.search.impl.querymodel.QueryOptions;
import org.alfresco.repo.search.impl.querymodel.impl.BaseComparison;
import org.alfresco.repo.search.impl.querymodel.impl.functions.Equals;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;

/**
 * The options for a CMIS query
 *
 * @author andyh
 */
public class CMISQueryOptions extends QueryOptions
{
    public enum CMISQueryMode
    {
        CMS_STRICT, CMS_WITH_ALFRESCO_EXTENSIONS;
    }

    private CMISQueryMode queryMode = CMISQueryMode.CMS_STRICT;
    private CmisVersion cmisVersion = CmisVersion.CMIS_1_1;
    private CMISQueryFilter queryFilter = NoFilter.INSTANCE;

    public static CMISQueryOptions create(SearchParameters searchParameters)
    {
        String sql = searchParameters.getQuery();

        CMISQueryOptions options = new CMISQueryOptions(sql, searchParameters.getStores().get(0));
        options.setIncludeInTransactionData(!searchParameters.excludeDataInTheCurrentTransaction());
        options.setDefaultFTSConnective(searchParameters.getDefaultOperator() == SearchParameters.Operator.OR ? Connective.OR : Connective.AND);
        options.setDefaultFTSFieldConnective(searchParameters.getDefaultOperator() == SearchParameters.Operator.OR ? Connective.OR : Connective.AND);
        options.setSkipCount(searchParameters.getSkipCount());
        options.setMaxPermissionChecks(searchParameters.getMaxPermissionChecks());
        options.setMaxPermissionCheckTimeMillis(searchParameters.getMaxPermissionCheckTimeMillis());
        if (searchParameters.getLimitBy() == LimitBy.FINAL_SIZE)
        {
            options.setMaxItems(searchParameters.getLimit());
        }
        else
        {
            options.setMaxItems(searchParameters.getMaxItems());
        }
        options.setMlAnalaysisMode(searchParameters.getMlAnalaysisMode());
        options.setLocales(searchParameters.getLocales());
        options.setStores(searchParameters.getStores());
        options.setUseInMemorySort(searchParameters.getUseInMemorySort());
        options.setMaxRawResultSetSizeForInMemorySort(searchParameters.getMaxRawResultSetSizeForInMemorySort());
        // options.setQuery(); Done on conbstruction
        // options.setQueryMode(); Should set afterwards
        options.setQueryParameterDefinitions(searchParameters.getQueryParameterDefinitions());
        options.setDefaultFieldName(searchParameters.getDefaultFieldName());
        options.setBulkFetchEnabled(searchParameters.isBulkFetchEnabled());
        options.setExcludeTenantFilter(searchParameters.getExcludeTenantFilter());
        options.setSinceTxId(searchParameters.getSinceTxId());
        for (String name : searchParameters.getQueryTemplates().keySet())
        {
            String template = searchParameters.getQueryTemplates().get(name);
            options.addQueryTemplate(name, template);
        }

        Optional.of(searchParameters)
                .filter(CMISSearchParameters.class::isInstance)
                .map(CMISSearchParameters.class::cast)
                .map(CMISSearchParameters::getQueryFilter)
                .ifPresent(options::setQueryFilter);

        return options;
    }

    /**
     * Create a CMISQueryOptions instance with the default options other than the query and store ref. The query will be run using the locale returned by I18NUtil.getLocale()
     *
     * @param query
     *            - the query to run
     * @param storeRef
     *            - the store against which to run the query
     */
    public CMISQueryOptions(String query, StoreRef storeRef)
    {
        this(query, storeRef, I18NUtil.getLocale());
    }

    /**
     * Create a CMISQueryOptions instance with the default options other than the query, store ref and locale.
     *
     * @param query
     *            - the query to run
     * @param storeRef
     *            - the store against which to run the query
     */
    public CMISQueryOptions(String query, StoreRef storeRef, Locale locale)
    {
        super(query, storeRef, locale);
    }

    public CmisVersion getCmisVersion()
    {
        return cmisVersion;
    }

    public void setCmisVersion(CmisVersion cmisVersion)
    {
        this.cmisVersion = cmisVersion;
    }

    /**
     * Get the query mode.
     *
     * @return the queryMode
     */
    public CMISQueryMode getQueryMode()
    {
        return queryMode;
    }

    /**
     * Set the query mode.
     *
     * @param queryMode
     *            the queryMode to set
     */
    public void setQueryMode(CMISQueryMode queryMode)
    {
        this.queryMode = queryMode;
    }

    public void setQueryFilter(CMISQueryFilter queryFilter)
    {
        this.queryFilter = Objects.requireNonNull(queryFilter);
    }

    public void clearQueryFilter()
    {
        this.queryFilter = NoFilter.INSTANCE;
    }

    public CMISQueryFilter getQueryFilter()
    {
        return queryFilter;
    }

    /**
     * @return SearchParameters
     */
    public SearchParameters getAsSearchParmeters()
    {
        CMISSearchParameters searchParameters = new CMISSearchParameters();
        searchParameters.setDefaultFieldName(this.getDefaultFieldName());
        searchParameters.setDefaultFTSFieldConnective(this.getDefaultFTSFieldConnective() == Connective.OR ? SearchParameters.Operator.OR : SearchParameters.Operator.AND);
        searchParameters.setDefaultFTSOperator(this.getDefaultFTSConnective() == Connective.OR ? SearchParameters.Operator.OR : SearchParameters.Operator.AND);
        searchParameters.setDefaultOperator(this.getDefaultFTSConnective() == Connective.OR ? SearchParameters.Operator.OR : SearchParameters.Operator.AND);
        searchParameters.setLanguage(SearchService.LANGUAGE_CMIS_ALFRESCO);
        if (this.getMaxItems() > 0)
        {
            searchParameters.setLimit(this.getMaxItems());
            searchParameters.setLimitBy(LimitBy.FINAL_SIZE);
            searchParameters.setMaxItems(this.getMaxItems());
        }
        searchParameters.setMaxPermissionChecks(this.getMaxPermissionChecks());
        searchParameters.setMaxPermissionCheckTimeMillis(this.getMaxPermissionCheckTimeMillis());
        searchParameters.setMlAnalaysisMode(this.getMlAnalaysisMode());
        // searchParameters.setNamespace() TODO: Fix
        // searchParameters.setPermissionEvaluation()
        searchParameters.setQuery(this.getQuery());
        searchParameters.setSkipCount(this.getSkipCount());
        // searchParameters.addAllAttribute()
        for (Locale locale : this.getLocales())
        {
            searchParameters.addLocale(locale);
        }
        for (QueryParameterDefinition queryParameterDefinition : this.getQueryParameterDefinitions())
        {
            searchParameters.addQueryParameterDefinition(queryParameterDefinition);
        }
        // searchParameters.addQueryTemplate(name, template)
        // searchParameters.addSort()
        for (StoreRef storeRef : this.getStores())
        {
            searchParameters.addStore(storeRef);
        }
        // searchParameters.addTextAttribute()
        searchParameters.setBulkFetchEnabled(isBulkFetchEnabled());
        searchParameters.setQueryConsistency(this.getQueryConsistency());
        searchParameters.setSinceTxId(getSinceTxId());
        for (String name : getQueryTemplates().keySet())
        {
            String template = getQueryTemplates().get(name);
            searchParameters.addQueryTemplate(name, template);
        }
        searchParameters.setCMISQueryFilter(queryFilter);
        return searchParameters;
    }

    /**
     * The purpose of this class is to preserve the {@link CMISQueryFilter} when the {@link CMISQueryOptions} are transformed to the {@link SearchParameters}. This might happen multiple times, back and forth.
     * 
     * @see #create(SearchParameters)
     * @see #getAsSearchParmeters()
     */
    private static class CMISSearchParameters extends SearchParameters
    {
        private CMISQueryFilter queryFilter;

        public void setCMISQueryFilter(CMISQueryFilter queryFilter)
        {
            this.queryFilter = queryFilter;
            this.queryFilter.applyAFTSFilter(this::addFilterQuery);
        }

        public CMISQueryFilter getQueryFilter()
        {
            return queryFilter;
        }
    }

    /**
     * Allows to filter CMIS query results. Implementations are responsible for adjusting the DB and Index queries.
     */
    public interface CMISQueryFilter
    {
        static CMISQueryFilter propertyEquality(String propertyName, String propertyValue)
        {
            return new CMISPropertyEqualityFilter(propertyName, propertyValue);
        }

        /**
         * Responsible for adjusting already existing {@link Constraint}
         * 
         * @param constraint
         *            already existing constraints. Can be {@code null} (no {@code WHERE} clause).
         * @param queryModelFactory
         *            factory for creating {@link Constraint}s.
         * @return {@link Constraint} effective constraints.
         */
        Constraint applyTo(Constraint constraint, QueryModelFactory queryModelFactory);

        /**
         * CMIS DB query can fall back to the Search Index. When it happens this method is responsible for generating the AFTS filter the index query.
         * 
         * @param aftsConsumer
         *            {@link Consumer} for the AFTS filter.
         */
        void applyAFTSFilter(Consumer<String> aftsConsumer);
    }

    /**
     * Just a NOOP filter to avoid {@code null} checks.
     */
    private static final class NoFilter implements CMISQueryFilter
    {
        private static final NoFilter INSTANCE = new NoFilter();

        @Override
        public Constraint applyTo(Constraint constraint, QueryModelFactory queryModelFactory)
        {
            return constraint;
        }

        @Override
        public void applyAFTSFilter(Consumer<String> aftsConsumer)
        {
            // do nothing
        }
    }

    /**
     * The only supported filer based on single property value equality. Allows to limit the query results (both DB & Index).
     */
    private static final class CMISPropertyEqualityFilter implements CMISQueryFilter
    {
        private final String propertyName;
        private final String propertyValue;

        public CMISPropertyEqualityFilter(String propertyName, String propertyValue)
        {
            this.propertyName = Objects.requireNonNull(propertyName);
            this.propertyValue = Objects.requireNonNull(propertyValue);
        }

        @Override
        public Constraint applyTo(Constraint constraint, QueryModelFactory queryModelFactory)
        {
            final Constraint filterConstraint = getFilterConstraint(queryModelFactory);
            if (constraint == null)
            {
                return filterConstraint;
            }
            return queryModelFactory.createConjunction(List.of(filterConstraint, constraint));
        }

        @Override
        public void applyAFTSFilter(Consumer<String> aftsConsumer)
        {
            String aftsFilter = "{!afts}=" + propertyName + ":\"" + propertyValue + "\"";
            aftsConsumer.accept(aftsFilter);
        }

        private Constraint getFilterConstraint(QueryModelFactory queryModelFactory)
        {
            final Function function = queryModelFactory.getFunction(Equals.NAME);

            final ArgumentDefinition modeArgDef = function.getArgumentDefinition(BaseComparison.ARG_MODE);
            final LiteralArgument modeArg = queryModelFactory.createLiteralArgument(modeArgDef.getName(), DataTypeDefinition.TEXT, "SINGLE_VALUED_PROPERTY");

            final ArgumentDefinition lhsArgDef = function.getArgumentDefinition(BaseComparison.ARG_LHS);
            final PropertyArgument lhsArg = queryModelFactory.createPropertyArgument(lhsArgDef.getName(), true, false, "", propertyName);

            final ArgumentDefinition rhsArgDef = function.getArgumentDefinition(BaseComparison.ARG_RHS);
            final LiteralArgument rhsArg = queryModelFactory.createLiteralArgument(rhsArgDef.getName(), DataTypeDefinition.TEXT, propertyValue);

            final Map<String, Argument> args = Map.of(
                    modeArg.getName(), modeArg,
                    lhsArg.getName(), lhsArg,
                    rhsArg.getName(), rhsArg);

            return queryModelFactory.createFunctionalConstraint(function, args);
        }
    }
}
