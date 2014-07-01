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
import org.alfresco.service.namespace.NamespaceService;
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
    StatsService stats;
    SiteService siteService;
    
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
       Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
       SiteInfo siteInfo = null;
       
       if (templateVars != null && templateVars.containsKey("siteId") )
       {
         siteInfo = siteService.getSite(templateVars.get("siteId"));
         if (siteInfo == null)
         {
             throw new AccessDeniedException("No such site: " + templateVars.get("siteId"));
         }
       } 

       String contentProp = req.getParameter("contentProp");
       if (contentProp == null) contentProp = "created";  //default
       
       QName prop = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, contentProp);
       
       String query = buildQuery(siteInfo);
       
       StatsParameters params = new StatsParameters(SearchService.LANGUAGE_SOLR_FTS_ALFRESCO, query);
       params.addSort(new SortDefinition(SortDefinition.SortType.FIELD, "contentsize", false));
       params.addStatsParameter(StatsParameters.PARAM_FIELD, "contentsize");
       params.addStatsParameter(StatsParameters.PARAM_FACET, StatsParameters.FACET_PREFIX+prop.toString());
  
       StatsResultSet result = stats.query(params);
       
       Map<String, Object> model = new HashMap<String, Object>(1, 1.0f);
       model.put("result", result);
       model.put("resultSize", result.getStats().size());
       return model;
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

}
