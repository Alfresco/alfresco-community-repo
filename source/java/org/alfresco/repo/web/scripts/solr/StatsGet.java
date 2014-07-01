package org.alfresco.repo.web.scripts.solr;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.search.SearchParameters.SortDefinition;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.search.StatsParameters;
import org.alfresco.service.cmr.search.StatsResultSet;
import org.alfresco.service.cmr.search.StatsService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Gets stats on Solr content
 *
 * @author Gethin James
 */
public class StatsGet extends DeclarativeWebScript
{

    private StatsService stats;
    private SiteService siteService;
    private Map<String,String> facets;
    
    public void setFacets(Map<String, String> facets)
    {
        this.facets = facets;
    }
    
    public void setStats(StatsService stats)
    {
        this.stats = stats;
    }

    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
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

       QName propFacet = findFacet(req.getParameter("facet"));
       String query = buildQuery(siteInfo);
       
       StatsParameters params = new StatsParameters(SearchService.LANGUAGE_SOLR_FTS_ALFRESCO, query);
       params.addSort(new SortDefinition(SortDefinition.SortType.FIELD, "contentsize", false));
       params.addStatsParameter(StatsParameters.PARAM_FIELD, "contentsize");
       params.addStatsParameter(StatsParameters.PARAM_FACET, StatsParameters.FACET_PREFIX+propFacet.toString());
  
       StatsResultSet result = stats.query(params);
       
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
       if (facetKey == null) facetKey = facets.entrySet().iterator().next().getKey();  //default
           
       if (!facets.containsKey(facetKey))
       {
           throw new AccessDeniedException("Invalid facet key:"+facetKey);
       }
           
       QName propFacet = QName.createQName(facets.get(facetKey));
       return propFacet;
    }

    protected String buildQuery(SiteInfo siteInfo)
    {
        StringBuilder luceneQuery = new StringBuilder();
        luceneQuery.append("TYPE:\"" + ContentModel.TYPE_CONTENT + "\"");
        
        if (siteInfo != null)
        {
            luceneQuery.append(" AND ANCESTOR:\""+siteInfo.getNodeRef().toString()+"\"");
        }
        return luceneQuery.toString();
    }
    
    /**
     * Allows you to add a facet to the list of available facets for Solr Statistics
     * @param facetKey e.g. content.mimetype
     * @param facetType e.g. @{http://www.alfresco.org/model/content/1.0}content.mimetype
     */
    public void addFacet(String facetKey, String facetType)
    {
        facets.put(facetKey, facetType);
    }


}
