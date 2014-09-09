/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.web.scripts.solr;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.search.StatsParameters;
import org.alfresco.service.cmr.search.StatsProcessor;
import org.alfresco.service.cmr.search.StatsResultSet;
import org.alfresco.service.cmr.search.StatsService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.joda.time.LocalDate;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Retrieves statistics using solr.  For a list of potential facets call it with /api/solr/stats?listFacets=true
 * You can pass one of these facets in eg. facet=content.creator .  The facet name can be used as a I18n resource bundle key,
 * it also has a predefined structure: group.property[.type] eg. content.created.datetime. The [.type] is optional, the default is String.</description>
 *
 * @author Gethin James
 */
public class StatsGet extends DeclarativeWebScript
{
    public static final String DATE_TIME_SUFFIX = "datetime";
    private StatsService stats;
    private SiteService siteService;
    private Map<String,String> facets;
    private Map<String,? extends StatsProcessor> postProcessors;
    private String statsField;

    public void setFacets(Map<String, String> facets)
    {
        this.facets = facets;
    }

    public void setStatsField(String statsField)
    {
        this.statsField = statsField;
    }

    public void setStats(StatsService stats)
    {
        this.stats = stats;
    }

    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }
    
    public void setPostProcessors(Map<String, ? extends StatsProcessor> postProcessors)
    {
        this.postProcessors = postProcessors;
    }
    
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) 
    {
       Map<String, Object> model = new HashMap<String, Object>(2, 1.0f);
       Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
       SiteInfo siteInfo = null;
       
       String listFacets = req.getParameter("listFacets");
       if (listFacets != null)
       {
           model.put("facets", facets.keySet());
           model.put("resultSize", 0);
           return model;
       }
       
       if (templateVars != null && templateVars.containsKey("siteId") )
       {
         siteInfo = siteService.getSite(templateVars.get("siteId"));
         if (siteInfo == null)
         {
             throw new AccessDeniedException("No such site: " + templateVars.get("siteId"));
         }
       }

       String facetKey = req.getParameter("facet");
       if (facetKey == null) facetKey = facets.entrySet().iterator().next().getKey();  //default
       String query;
       
       QName propFacet = findFacet(facetKey);
       Pair<LocalDate, LocalDate> startAndEnd = getStartAndEndDates(req.getParameter("startDate"),req.getParameter("endDate"));
       query = buildQuery(siteInfo, facetKey, startAndEnd);

       StatsParameters params = new StatsParameters(SearchService.LANGUAGE_SOLR_FTS_ALFRESCO, query, false);
       //params.addSort(new SortDefinition(SortDefinition.SortType.FIELD, this.statsField, false));
       params.addStatsParameter(StatsParameters.PARAM_FIELD, this.statsField);
       params.addStatsParameter(StatsParameters.PARAM_FACET, StatsParameters.FACET_PREFIX+propFacet.toString());
  
       StatsResultSet result = stats.query(params);
       
       if (postProcessors.containsKey(facetKey))
       {
           StatsProcessor processor = postProcessors.get(facetKey);
           result = processor.process(result);
       }
       model.put("result", result);
       model.put("resultSize", result.getStats().size());
       return model;
    }

    /**
     * Finds a facet based on its key
     * @param facetKey
     * @return QName facet
     */
    private QName findFacet(String facetKey)
    {         
       if (!facets.containsKey(facetKey))
       {
           throw new AccessDeniedException("Invalid facet key:"+facetKey);
       }
       QName propFacet = QName.createQName(facets.get(facetKey));
       return propFacet;

    }

    protected String buildQuery(SiteInfo siteInfo, String facetKey, Pair<LocalDate, LocalDate> startEndDate)
    {
        StringBuilder luceneQuery = new StringBuilder();
        luceneQuery.append("TYPE:\"" + ContentModel.TYPE_CONTENT + "\"");
        
        if (startEndDate != null)
        {
            //QName propFacet = QName.createQName(facets.get(facetKey));
            String dateFacet = ContentModel.PROP_CREATED.toString();//hard coded for now.
            luceneQuery.append(" AND "+dateFacet.toString()+":(\""+startEndDate.getFirst()+"\"..\""+startEndDate.getSecond()+"\")");
        }
        
        if (siteInfo != null)
        {
            luceneQuery.append(" AND ANCESTOR:\""+siteInfo.getNodeRef().toString()+"\"");
        }  
        return luceneQuery.toString();
    }
    
    /**
     * Parses ISO8601 formatted Date Strings.
     * @param start If start is null then defaults to 1 month
     * @param end If end is null then it defaults to now();
     * @return Pair <Start,End>
     */
    public static Pair<LocalDate, LocalDate> getStartAndEndDates(String start, String end)
    {
        if (start == null) return null;
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = end!=null?LocalDate.parse(end):LocalDate.now();
        return new Pair<LocalDate, LocalDate>(startDate, endDate);
    }

    /**
     * Allows you to add a facet to the list of available facets for Solr Statistics
     * @param facetKey e.g. content.mimetype
     * @param facetType e.g. {http://www.alfresco.org/model/content/1.0}content.mimetype
     */
    public void addFacet(String facetKey, String facetType)
    {
        facets.put(facetKey, facetType);
    }

}
