/*-
 * #%L
 * Alfresco Remote API
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

package org.alfresco.rest.api.search.impl;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.alfresco.rest.api.Nodes.PARAM_INCLUDE_ALLOWABLEOPERATIONS;
import static org.alfresco.rest.api.Nodes.PARAM_INCLUDE_ASPECTNAMES;
import static org.alfresco.rest.api.Nodes.PARAM_INCLUDE_ASSOCIATION;
import static org.alfresco.rest.api.Nodes.PARAM_INCLUDE_ISLINK;
import static org.alfresco.rest.api.Nodes.PARAM_INCLUDE_PATH;
import static org.alfresco.rest.api.Nodes.PARAM_INCLUDE_PROPERTIES;
import static org.alfresco.service.cmr.search.SearchService.LANGUAGE_CMIS_ALFRESCO;
import static org.alfresco.service.cmr.search.SearchService.LANGUAGE_FTS_ALFRESCO;
import static org.alfresco.service.cmr.search.SearchService.LANGUAGE_LUCENE;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.search.impl.lucene.LuceneQueryLanguageSPI;
import org.alfresco.rest.api.search.context.SearchRequestContext;
import org.alfresco.rest.api.search.model.Default;
import org.alfresco.rest.api.search.model.FacetField;
import org.alfresco.rest.api.search.model.FacetFields;
import org.alfresco.rest.api.search.model.FacetQuery;
import org.alfresco.rest.api.search.model.FilterQuery;
import org.alfresco.rest.api.search.model.Limits;
import org.alfresco.rest.api.search.model.Pivot;
import org.alfresco.rest.api.search.model.Query;
import org.alfresco.rest.api.search.model.Scope;
import org.alfresco.rest.api.search.model.SearchQuery;
import org.alfresco.rest.api.search.model.SortDef;
import org.alfresco.rest.api.search.model.Spelling;
import org.alfresco.rest.api.search.model.Template;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Params;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.GeneralHighlightParameters;
import org.alfresco.service.cmr.search.Interval;
import org.alfresco.service.cmr.search.IntervalParameters;
import org.alfresco.service.cmr.search.IntervalSet;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.RangeParameters;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchParameters.FieldFacet;
import org.alfresco.service.cmr.search.SearchParameters.FieldFacetMethod;
import org.alfresco.service.cmr.search.SearchParameters.FieldFacetSort;
import org.alfresco.service.cmr.search.SearchParameters.Operator;
import org.alfresco.service.cmr.search.SearchParameters.SortDefinition;
import org.alfresco.service.cmr.search.SearchParameters.SortDefinition.SortType;
import org.alfresco.service.cmr.search.StatsRequestParameters;
import org.alfresco.util.ParameterCheck;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * Maps from a json request and a solr SearchParameters object.
 *
 * @author Gethin James
 */
public class SearchMapper
{
    public static final List<String> PERMITTED_INCLUDES
                = Arrays.asList(PARAM_INCLUDE_ALLOWABLEOPERATIONS,PARAM_INCLUDE_ASPECTNAMES,
                                PARAM_INCLUDE_ISLINK, PARAM_INCLUDE_PATH, PARAM_INCLUDE_PROPERTIES,
                                PARAM_INCLUDE_ASSOCIATION);

    public static final String CMIS = "cmis";
    public static final String LUCENE = "lucene";
    public static final String AFTS = "afts";
    private StoreMapper storeMapper;

    /**
     * Turn the SearchQuery params serialized by Jackson into the Java SearchParameters object
     * @param params
     * @return SearchParameters
     */
    public SearchParameters toSearchParameters(Params params, SearchQuery searchQuery, SearchRequestContext searchRequestContext)
    {
        ParameterCheck.mandatory("query", searchQuery.getQuery());

        SearchParameters sp = new SearchParameters();
        setDefaults(sp);

        fromQuery(sp,  searchQuery.getQuery());
        fromPaging(sp, params.getPaging());
        fromSort(sp, searchQuery.getSort());
        fromTemplate(sp, searchQuery.getTemplates());
        fromTimezone(sp, searchQuery.getTimezone());
        validateInclude(searchQuery.getInclude());
        fromDefault(sp, searchQuery.getDefaults());
        fromFilterQuery(sp, searchQuery.getFilterQueries());
        fromFacetQuery(sp, searchQuery.getFacetQueries());
        fromPivot(sp, searchQuery.getStats(), searchQuery.getFacetFields(), searchQuery.getPivots(), searchRequestContext);
        fromStats(sp, searchQuery.getStats());
        fromFacetFields(sp, searchQuery.getFacetFields());
        fromSpellCheck(sp, searchQuery.getSpellcheck());
        fromHighlight(sp, searchQuery.getHighlight());
        fromFacetIntervals(sp, searchQuery.getFacetIntervals());
        fromRange(sp, searchQuery.getFacetRanges());
        fromScope(sp, searchQuery.getScope(), searchRequestContext);
        fromLimits(sp, searchQuery.getLimits());
        return sp;
    }

    /**
     * Sets the API defaults
     * @param sp
     */
    public void setDefaults(SearchParameters sp)
    {
        //Hardcode workspace store
        sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    }

    /**
     * SearchParameters from the Query object
     * @param sp SearchParameters
     * @param q Query
     */
    public void fromQuery(SearchParameters sp, Query q)
    {
        ParameterCheck.mandatoryString("query", q.getQuery());
        String lang = q.getLanguage()==null?AFTS:q.getLanguage();

        switch (lang.toLowerCase())
        {
            case AFTS:
                sp.setLanguage(LANGUAGE_FTS_ALFRESCO);
                break;
            case LUCENE:
                sp.setLanguage(LANGUAGE_LUCENE);
                break;
            case CMIS:
                sp.setLanguage(LANGUAGE_CMIS_ALFRESCO);
                break;
            default:
                throw new InvalidArgumentException(InvalidArgumentException.DEFAULT_MESSAGE_ID,
                            new Object[] { ": language allowed values: afts,lucene,cmis" });
        }
        sp.setQuery(q.getQuery());
        sp.setSearchTerm(q.getUserQuery());
    }

    /**
     * SearchParameters from the Paging object
     * @param sp SearchParameters
     * @param paging Paging
     */
    public void fromPaging(SearchParameters sp, Paging paging)
    {
        if (paging != null)
        {
            sp.setLimitBy(LimitBy.FINAL_SIZE);
            sp.setLimit(paging.getMaxItems());
            sp.setSkipCount(paging.getSkipCount());
        }
    }

    /**
     * SearchParameters from List<SortDef>
     * @param sp SearchParameters
     * @param sort List<SortDef>
     */
    public void fromSort(SearchParameters sp, List<SortDef> sort)
    {
        if (sort != null && !sort.isEmpty())
        {
            if (LANGUAGE_CMIS_ALFRESCO.equals(sp.getLanguage()))
            {
                throw new InvalidArgumentException(InvalidArgumentException.DEFAULT_MESSAGE_ID,
                            new Object[] { ": sort {} not allowed with cmis language" });
            }
            for (SortDef sortDef:sort)
            {

                try
                {
                    SortType sortType = SortType.valueOf(sortDef.getType());
                    String field = sortDef.getField();
                    sp.addSort(new SortDefinition(sortType, field, sortDef.isAscending()));
                }
                catch (IllegalArgumentException e)
                {
                    throw new InvalidArgumentException(InvalidArgumentException.DEFAULT_MESSAGE_ID, new Object[] { sortDef.getType() });
                }
            }
        }
    }

    /**
     * SearchParameters from List<Template>
     * @param sp SearchParameters
     * @param templates List<Template>
     */
    public void fromTemplate(SearchParameters sp, List<Template> templates)
    {
        if (templates != null && !templates.isEmpty())
        {
            for (Template aTemplate:templates)
            {
                ParameterCheck.mandatoryString("template name", aTemplate.getName());
                ParameterCheck.mandatoryString("template template", aTemplate.getTemplate());
                sp.addQueryTemplate(aTemplate.getName(), aTemplate.getTemplate());
            }
        }
    }

    /**
     * SearchParameters from Default object
     * @param sp SearchParameters
     * @param defaults Default
     */
    public void fromDefault(SearchParameters sp, Default defaults)
    {
        if (defaults != null)
        {
            List<String> txtAttribs = defaults.getTextAttributes();
            if (txtAttribs!= null && !txtAttribs.isEmpty())
            {
                for (String attrib:txtAttribs)
                {
                    sp.addTextAttribute(attrib);
                }
            }

            if (defaults.getDefaultFTSOperator() != null)
            {
                sp.setDefaultFTSOperator(Operator.valueOf(defaults.getDefaultFTSOperator().toUpperCase()));
            }
            if (defaults.getDefaultFTSFieldOperator() != null)
            {
                sp.setDefaultFTSFieldConnective(Operator.valueOf(defaults.getDefaultFTSFieldOperator().toUpperCase()));
            }

            sp.setNamespace(defaults.getNamespace());
            sp.setDefaultFieldName(defaults.getDefaultFieldName());
        }
    }

    /**
     * Validates the List<String> includes
     * @param includes List<String>
     */
    public void validateInclude(List<String> includes)
    {
        if (includes != null && !includes.isEmpty())
        {
            for (String inc:includes)
            {
                if (!PERMITTED_INCLUDES.contains(inc))
                {
                    throw new InvalidArgumentException(InvalidArgumentException.DEFAULT_MESSAGE_ID, new Object[] { inc });
                }
            }

        }
    }

    /**
     ** SearchParameters from List<FilterQuery>
     * @param sp
     * @param filterQueries
     */
    public void fromFilterQuery(SearchParameters sp, List<FilterQuery> filterQueries)
    {
        if (filterQueries != null && !filterQueries.isEmpty())
        {
            if (LANGUAGE_CMIS_ALFRESCO.equals(sp.getLanguage()))
            {
                throw new InvalidArgumentException(InvalidArgumentException.DEFAULT_MESSAGE_ID,
                            new Object[] { ": filterQueries {} not allowed with cmis language" });
            }
            for (FilterQuery fq:filterQueries)
            {
                String query = null;

                if (fq.getQuery() != null && !fq.getQuery().isEmpty())
                {
                    query = fq.getQuery().trim();
                }

                if (fq.getQueries() != null && !fq.getQueries().isEmpty() && query == null)
                {
                    query = String.join(" OR ", fq.getQueries());
                }

                ParameterCheck.mandatoryString("filterQueries query", query);

                if (fq.getTags() == null || fq.getTags().isEmpty() || query.contains("afts tag"))
                {
                    //If its already got tags then just let it through
                    sp.addFilterQuery(query);
                }
                else
                {
                    String tags = "tag="+String.join(",", fq.getTags());
                    Matcher matcher = LuceneQueryLanguageSPI.AFTS_QUERY.matcher(query);
                    if (matcher.find())
                    {
                        query = "{!afts "+tags+" "+matcher.group(1).trim()+"}"+matcher.group(2);
                    }
                    else
                    {
                        query = "{!afts "+tags+" }"+query;
                    }
                    sp.addFilterQuery(query);
                }
            }
        }
    }

    /**
     ** SearchParameters from List<FacetQuery>
     * @param sp
     * @param facetQueries
     */
    public void fromFacetQuery(SearchParameters sp, List<FacetQuery> facetQueries)
    {
        if (facetQueries != null && !facetQueries.isEmpty())
        {
            for (FacetQuery fq:facetQueries)
            {
                ParameterCheck.mandatoryString("facetQuery query", fq.getQuery());
                String query = fq.getQuery();
                String label = fq.getLabel()!=null?fq.getLabel():query;

                if (query.startsWith("{!afts"))
                {
                    throw new InvalidArgumentException(InvalidArgumentException.DEFAULT_MESSAGE_ID,
                                new Object[] { ": Facet queries should not start with !afts" });
                }
                query = "{!afts key='"+label+"'}"+query;
                sp.addFacetQuery(query);
            }
        }
    }


    /**
     * SearchParameters from FacetFields object
     * @param sp SearchParameters
     * @param FacetFields facetFields
     */
    public void fromFacetFields(SearchParameters sp, FacetFields facetFields)
    {
        if (facetFields != null)
        {
            ParameterCheck.mandatory("facetFields facets", facetFields.getFacets());

            if (facetFields.getFacets() != null && !facetFields.getFacets().isEmpty())
            {
                for (FacetField facet : facetFields.getFacets())
                {
                    ParameterCheck.mandatoryString("facetFields facet field", facet.getField());
                    String field = facet.getField();

                    FieldFacet ff = new FieldFacet(field);

                    if (facet.getSort() != null && !facet.getSort().isEmpty())
                    {
                        try
                        {
                            ff.setSort(FieldFacetSort.valueOf(facet.getSort()));
                        }
                        catch (IllegalArgumentException e)
                        {
                            throw new InvalidArgumentException(InvalidArgumentException.DEFAULT_MESSAGE_ID, new Object[] { facet.getSort() });
                        }
                    }
                    if (facet.getMethod() != null && !facet.getMethod().isEmpty())
                    {
                        try
                        {
                            ff.setMethod(FieldFacetMethod.valueOf(facet.getMethod()));
                        }
                        catch (IllegalArgumentException e)
                        {
                            throw new InvalidArgumentException(InvalidArgumentException.DEFAULT_MESSAGE_ID, new Object[] { facet.getMethod() });
                        }

                    }

                    ff.setPrefix(facet.getPrefix());
                    ff.setLabel(facet.getLabel());
                    ff.setExcludeFilters(facet.getExcludeFilters());
                    ff.setCountDocsMissingFacetField(facet.getMissing());
                    ff.setLimitOrNull(facet.getLimit());
                    ff.setOffset(facet.getOffset());
                    ff.setMinCount(facet.getMincount());
                    ff.setEnumMethodCacheMinDF(facet.getFacetEnumCacheMinDf());
                    sp.addFieldFacet(ff);
                }
            }
        }
    }

    /**
     * SearchParameters from SpellCheck object
     * @param sp SearchParameters
     * @param defaults SpellCheck
     */
    public void fromSpellCheck(SearchParameters sp, Spelling spelling)
    {
        if (spelling != null)
        {
            if (spelling.getQuery() != null && !spelling.getQuery().isEmpty())
            {
                sp.setSearchTerm(spelling.getQuery());
            }
            else
            {
                if (sp.getSearchTerm() == null || sp.getSearchTerm().isEmpty())
                {
                    //We don't have a valid search term to use with the spelling
                    throw new InvalidArgumentException(InvalidArgumentException.DEFAULT_MESSAGE_ID,
                                new Object[] { ": userQuery or spelling.query required." });
                }
            }
            sp.setSpellCheck(true);
        }
    }

    /**
     * SearchParameters from Scope object
     * @param Scope scope
     * @param sp SearchParameters
     * @param searchRequestContext
     */
    public void fromScope(SearchParameters sp, Scope scope, SearchRequestContext searchRequestContext)
    {
        if (scope != null)
        {
            List<String> stores = scope.getLocations();
            if (stores!= null && !stores.isEmpty())
            {
                //First reset the stores then add them.
                sp.getStores().clear();

                searchRequestContext.getStores().addAll(stores);
                for (String aStore:stores)
                {
                    try
                    {
                        sp.addStore(storeMapper.getStoreRef(aStore));
                    }
                    catch (AlfrescoRuntimeException are)
                    {
                        throw new InvalidArgumentException(InvalidArgumentException.DEFAULT_MESSAGE_ID,
                                    new Object[] { aStore });
                    }
                }

                if (stores.contains(StoreMapper.HISTORY) && (stores.size() > 1))
                {
                    throw new InvalidArgumentException(InvalidArgumentException.DEFAULT_MESSAGE_ID,
                                new Object[] { ": scope 'history' can only be used on its own" });
                }
            }
        }
    }

    /**
     * Sets the Interval Parameters object on search parameters
     *
     * It does some valiation then takes any "SETS" at the top level and sets them at every field level.
     *
     * @param sp SearchParameters
     * @param facetIntervals IntervalParameters
     */
    public void fromFacetIntervals(SearchParameters sp, IntervalParameters facetIntervals)
    {
        if (facetIntervals != null)
        {
            ParameterCheck.mandatory("facetIntervals intervals", facetIntervals.getIntervals());

            Set<IntervalSet> globalSets = facetIntervals.getSets();
            validateSets(globalSets, "facetIntervals");

            if (facetIntervals.getIntervals() != null && !facetIntervals.getIntervals().isEmpty())
            {
                List<String> intervalLabels = new ArrayList<>(facetIntervals.getIntervals().size());
                for (Interval interval:facetIntervals.getIntervals())
                {
                    ParameterCheck.mandatory("facetIntervals intervals field", interval.getField());
                    validateSets(interval.getSets(), "facetIntervals intervals "+interval.getField());
                    if (interval.getSets() != null && globalSets != null)
                    {
                        interval.getSets().addAll(globalSets);
                    }
                    ParameterCheck.mandatoryCollection("facetIntervals intervals sets", interval.getSets());

                    List<Map.Entry<String, Long>> duplicateSetLabels =
                                interval.getSets().stream().collect(groupingBy(IntervalSet::getLabel, Collectors.counting()))
                                .entrySet().stream().filter(e -> e.getValue().intValue() > 1).collect(toList());
                    if (!duplicateSetLabels.isEmpty())
                    {
                        throw new InvalidArgumentException(InvalidArgumentException.DEFAULT_MESSAGE_ID,
                                    new Object[] { ": duplicate set interval label "+duplicateSetLabels.toString() });

                    }
                    if (interval.getLabel() != null) intervalLabels.add(interval.getLabel());
                }

                List<Map.Entry<String, Long>> duplicateIntervalLabels =
                            intervalLabels.stream().collect(groupingBy(Function.identity(), Collectors.counting()))
                            .entrySet().stream().filter(e -> e.getValue().intValue() > 1).collect(toList());
                if (!duplicateIntervalLabels.isEmpty())
                {
                    throw new InvalidArgumentException(InvalidArgumentException.DEFAULT_MESSAGE_ID,
                                new Object[] { ": duplicate interval label "+duplicateIntervalLabels.toString() });
                }
            }

            if (facetIntervals.getSets() != null)
            {
                facetIntervals.getSets().clear();
            }
        }
        sp.setInterval(facetIntervals);
    }
    /**
     * Sets the Range Parameters object on search parameters
     * @param sp SearchParameters
     * @param rangeParams RangeParameters
     */
    public void fromRange(SearchParameters sp, List<RangeParameters> ranges)
    {
        if(ranges != null && !ranges.isEmpty())
        {
            for(RangeParameters rangeParams : ranges)
            {
                ParameterCheck.mandatory("ranges", rangeParams);
                ParameterCheck.mandatory("field", rangeParams.getField());
                ParameterCheck.mandatory("start", rangeParams.getStart());
                ParameterCheck.mandatory("end", rangeParams.getEnd());
                ParameterCheck.mandatory("gap", rangeParams.getGap());
            }
            sp.setRanges(ranges);
        }
        
    }
    public void fromPivot(SearchParameters sp, List<StatsRequestParameters> stats, FacetFields facetFields, List<Pivot> pivots, SearchRequestContext searchRequestContext)
    {
        if (facetFields != null && pivots != null && !pivots.isEmpty())
        {
            ParameterCheck.mandatory("facetFields facets", facetFields.getFacets());

            ListIterator<Pivot> piterator = pivots.listIterator();

            while (piterator.hasNext()) {

                Pivot pivot = piterator.next();
                ParameterCheck.mandatoryString("pivot key", pivot.getKey());
                String pivotKey = pivot.getKey();

                if (facetFields.getFacets() != null && !facetFields.getFacets().isEmpty())
                {
                    Optional<FacetField> found = facetFields.getFacets().stream()
                                .filter(queryable -> pivotKey.equals(queryable.getLabel() != null ? queryable.getLabel() : queryable.getField())).findFirst();

                    if (found.isPresent())
                    {
                        sp.addPivot(found.get().getField());
                        facetFields.getFacets().remove(found.get());
                        searchRequestContext.getPivotKeys().put(found.get().getField(), pivotKey);
                        continue;
                    }
                }

                if (piterator.hasNext())
                {
                    //Its not the last one so lets complain
                    throw new InvalidArgumentException(InvalidArgumentException.DEFAULT_MESSAGE_ID,
                                new Object[] { ": Pivot parameter " + pivotKey + " is does not reference a facet Field." });
                }
                else
                {
                    if (stats != null && !stats.isEmpty())
                    {
                        //It is the last one so it can reference stats
                        Optional<StatsRequestParameters> foundStat =  stats.stream().filter(stas -> pivotKey.equals(stas.getLabel()!=null?stas.getLabel():stas.getField())).findFirst();
                        if (foundStat.isPresent())
                        {
                            sp.addPivot(pivotKey);
                            searchRequestContext.getPivotKeys().put(pivotKey, pivotKey);
                            continue;
                        }

                    }
                    throw new InvalidArgumentException(InvalidArgumentException.DEFAULT_MESSAGE_ID,
                                new Object[] { ": Pivot parameter " + pivotKey + " is does not reference a facet Field or stats." });
                }
            }
        }
    }

    public void fromStats(SearchParameters sp, List<StatsRequestParameters> stats)
    {
        if (stats != null && !stats.isEmpty())
        {
            for (StatsRequestParameters aStat:stats)
            {
                ParameterCheck.mandatory("stats field", aStat.getField());

                List<Float> perc = aStat.getPercentiles();
                if (perc != null && !perc.isEmpty())
                {
                    for (Float percentile:perc)
                    {
                        if (percentile == null || percentile < 0 || percentile > 100)
                        {
                            throw new IllegalArgumentException("Invalid percentile "+percentile);
                        }
                    }
                }

                if (aStat.getCardinality() && (aStat.getCardinalityAccuracy() < 0 || aStat.getCardinalityAccuracy() > 1))
                {
                    throw new IllegalArgumentException("Invalid cardinality accuracy "+aStat.getCardinalityAccuracy() + " It must be between 0 and 1.");
                }
            }

            sp.setStats(stats);
        }

    }

    protected void validateSets(Set<IntervalSet> intervalSets, String prefix)
    {
        if (intervalSets != null && !intervalSets.isEmpty())
        {
            for (IntervalSet aSet:intervalSets)
            {
                ParameterCheck.mandatory(prefix+" sets start", aSet.getStart());
                ParameterCheck.mandatory(prefix+" sets end", aSet.getEnd());

                if (aSet.getLabel() == null)
                {
                    aSet.setLabel(aSet.toRange());
                }
            }
        }
    }

    /**
     * Sets the hightlight object on search parameters
     * @param sp SearchParameters
     * @param highlight GeneralHighlightParameters
     */
    public void fromHighlight(SearchParameters sp, GeneralHighlightParameters highlight)
    {
        sp.setHighlight(highlight);
    }

    /**
     * Validates and sets the timezone
     * @param sp SearchParameters
     * @param timezoneId a valid java.time.ZoneId
     */
    public void fromTimezone(SearchParameters sp, String timezoneId)
    {
        /*
         * java.util.TimeZone will not error if you set an invalid timezone
         * it just falls back to GMT without telling you.
         *
         * So I am using java.time.ZoneId because that throws an error,
         * if I then convert a ZoneId to Timezone I have the same problem (silently uses GMT)
         * so
         * I am converting using both methods:
         * If a timezoneId is invalid then an Invalid error is thrown
         * If its not possible to take a java.time.ZoneId and convert it to a java.util.TimeZone then an Incompatible error is thrown
         *
         */
        if (timezoneId!= null && !timezoneId.isEmpty())
        {
            ZoneId validZoneId = null;
            TimeZone timeZone = null;

            try
            {
                validZoneId = ZoneId.of(timezoneId);
                timeZone = TimeZone.getTimeZone(timezoneId);
            }
            catch (Exception e)
            {
                throw new IllegalArgumentException("Invalid timezoneId "+timezoneId);
            }

            if (validZoneId.getId().equals(timeZone.getID()))
            {
                sp.setTimezone(validZoneId.getId());
            }
            else
            {
                throw new IllegalArgumentException("Incompatible timezoneId "+timezoneId);
            }

        }
    }

    /**
     * SearchParameters from the Limits object
     * @param sp SearchParameters
     * @param paging Paging
     */
    public void fromLimits(SearchParameters sp, Limits limits)
    {
        if (limits != null)
        {
            if (limits.getPermissionEvaluationCount() != null)
            {
                sp.setLimit(-1);
                sp.setLimitBy(LimitBy.NUMBER_OF_PERMISSION_EVALUATIONS);
                sp.setMaxPermissionChecks(limits.getPermissionEvaluationCount());
            }

            if (limits.getPermissionEvaluationTime() != null)
            {
                sp.setLimit(-1);
                sp.setLimitBy(LimitBy.NUMBER_OF_PERMISSION_EVALUATIONS);
                sp.setMaxPermissionCheckTimeMillis(limits.getPermissionEvaluationTime());
            }
        }
    }

    public void setStoreMapper(StoreMapper storeMapper)
    {
        this.storeMapper = storeMapper;
    }
}
