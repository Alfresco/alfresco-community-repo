/*
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
package org.alfresco.repo.web.scripts.discussion;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.alfresco.query.EmptyPagingResults;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.discussion.TopicInfoImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.discussion.PostInfo;
import org.alfresco.service.cmr.discussion.TopicInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.util.ISO9075;
import org.alfresco.util.Pair;
import org.alfresco.util.ScriptPagingDetails;
import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Gets topics matching the filters passed to it in the URL.
 * 
 * topics = 'mine' (searches for posts by the user) or 'all' (ignores the author in the search)
 * history = days in the past to search
 * resultSize = the number of topics returned in the results
 * 
 * @author Jamie Allison
 */
public class ForumTopicsFilteredGet extends AbstractDiscussionWebScript
{
   //Filter Defaults
   protected static final String DEFAULT_TOPIC_AUTHOR = "mine";
   protected static final int DEFAULT_TOPIC_LATEST_POST_DAYS_AGO = 1;
   protected static final int DEFAULT_MAX_RESULTS = 10;

   protected static final StoreRef SPACES_STORE = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");

   protected static final String SEARCH_QUERY = "TYPE:\"{http://www.alfresco.org/model/forum/1.0}post\""
         + " AND PATH:\"/app:company_home/st:sites/%s/cm:discussions/*/*\""
         + " AND @cm:created:[\"%s\" TO NOW]";

   /** Spring-injected services */
   private SearchService searchService;

   /**
    * Sets the searchService.
    * 
    * @param searchService SearchService
    */
   public void setSearchService(SearchService searchService)
   {
      this.searchService = searchService;
   }

   /**
    * Overrides AbstractDiscussionWebScript to allow a null site
    * 
    * @param req WebScriptRequest
    * @param status Status
    * @param cache Cache
    * 
    * @return Map
    */
   @Override
   protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) 
   {
      Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
      if (templateVars == null)
      {
         String error = "No parameters supplied";
         throw new WebScriptException(Status.STATUS_BAD_REQUEST, error);
      }

      SiteInfo site = null;

      if (templateVars.containsKey("site"))
      {
         // Site, and optionally topic
         String siteName = templateVars.get("site");
         site = siteService.getSite(siteName);
         if (site == null)
         {
            String error = "Could not find site: " + siteName;
            throw new WebScriptException(Status.STATUS_NOT_FOUND, error);
         }
      }

      // Have the real work done
      return executeImpl(site, null, null, null, req, null, null, null); 
   }

   /**
    * @param site SiteInfo
    * @param nodeRef Not required. It is only included because it is overriding the parent class.
    * @param topic Not required. It is only included because it is overriding the parent class.
    * @param post Not required. It is only included because it is overriding the parent class.
    * @param req WebScriptRequest
    * @param status Not required. It is only included because it is overriding the parent class.
    * @param cache Not required. It is only included because it is overriding the parent class.
    * 
    * @return Map
    */
   @Override
   protected Map<String, Object> executeImpl(SiteInfo site, NodeRef nodeRef, TopicInfo topic,
         PostInfo post, WebScriptRequest req, JSONObject json, Status status, Cache cache)
   {
      // They shouldn't be trying to list of an existing Post or Topic
      if (topic != null || post != null)
      {
         String error = "Can't list Topics inside an existing Topic or Post";
         throw new WebScriptException(Status.STATUS_BAD_REQUEST, error);
      }

      // Set search filter to users topics or all topics
      String pAuthor = req.getParameter("topics");
      String author = DEFAULT_TOPIC_AUTHOR;
      if (pAuthor != null)
      {
         author = pAuthor;  
      }
      // Set the number of days in the past to search from 
      String pDaysAgo = req.getParameter("history");
      int daysAgo = DEFAULT_TOPIC_LATEST_POST_DAYS_AGO;
      if (pDaysAgo != null)
      {
         try
         {
            daysAgo = Integer.parseInt(pDaysAgo);
         }
         catch (NumberFormatException e)
         {
            //do nothing. history has already been preset to the default value.
         }  
      }
      
      // Get the complete search query
      Pair<String, String> searchQuery = getSearchQuery(site, author, daysAgo);

      // Get the filtered topics
      PagingRequest paging = buildPagingRequest(req);
      PagingResults<TopicInfo> topics = doSearch(searchQuery, false, paging);

      // Build the common model parts
      Map<String, Object> model = buildCommonModel(site, topic, post, req);

      // Have the topics rendered
      model.put("data", renderTopics(topics, paging, site));

      // All done
      return model;
   }

   /**
    * Do the actual search
    * 
    * @param searchQuery Pair with query string in first and query language in second
    * @param sortAscending boolean
    * @param paging PagingRequest
    */
   protected PagingResults<TopicInfo> doSearch(Pair<String, String> searchQuery, boolean sortAscending, PagingRequest paging)
   {
      ResultSet resultSet = null;
      PagingResults<TopicInfo> pagedResults = new EmptyPagingResults<TopicInfo>();

      String sortOn = "@{http://www.alfresco.org/model/content/1.0}created";

      // Setup the search parameters
      SearchParameters sp = new SearchParameters();
      sp.addStore(SPACES_STORE);
      sp.setQuery(searchQuery.getFirst());
      sp.setLanguage(searchQuery.getSecond());
      sp.addSort(sortOn, sortAscending);
      if (paging.getMaxItems() > 0)
      {
         //Multiply maxItems by 10.  This is to catch topics that have multiple replies and ensure that the maximum number of topics is shown.
         sp.setLimit(paging.getMaxItems()*10);
         sp.setLimitBy(LimitBy.FINAL_SIZE);
      }
      if (paging.getSkipCount() > 0)
      {
         sp.setSkipCount(paging.getSkipCount());
      }

      try
      {
         resultSet = searchService.query(sp);
         pagedResults = wrap(resultSet, paging);
      }
      finally
      {
         try
         {
            resultSet.close();
         }
         catch(Exception e)
         {
            //do nothing
         }
      }

      return pagedResults;
   }

   /**
    * Build the search query from the passed in parameters and SEARCH_QUERY constant
    * 
    * @param site SiteInfo
    * @param author String
    * @param daysAgo int
    * @return Pair with the query string in first and query language in second
    */
   protected Pair<String, String> getSearchQuery(SiteInfo site, String author, int daysAgo)
   {
      String search = String.format(SEARCH_QUERY,
            (site != null ? "cm:" + ISO9075.encode(site.getShortName()) : "*"),
            getDateXDaysAgo(daysAgo)
      );

      // If author equals 'mine' add cm:creator to the search query otherwise leave out
      if(author.equals(DEFAULT_TOPIC_AUTHOR))
      {
         search += " AND @cm:creator:\"" + AuthenticationUtil.getFullyAuthenticatedUser() + "\"";
      }

      // Add the query string and language to the returned results
      Pair<String, String> searchQuery = new Pair<String, String>(search, SearchService.LANGUAGE_FTS_ALFRESCO);

      return searchQuery;
   }

   /**
    * Get the date x days ago in the format 'yyyy-MM-dd'
    * 
    * @param daysAgo int
    * @return String
    */
   protected String getDateXDaysAgo(int daysAgo)
   {
      Calendar calendar = Calendar.getInstance();
      calendar.add(Calendar.DAY_OF_MONTH, (daysAgo * -1));
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

      return sdf.format(calendar.getTime());
   }

   /**
    * Builds up a listing Paging request, based on the arguments specified in the URL
    *  
    * @param req WebScriptRequest
    * @return PagingRequest
    */
   @Override
   protected PagingRequest buildPagingRequest(WebScriptRequest req)
   {
      // Grab the number of topics to return
      String pResultSize = req.getParameter("resultSize");
      int resultSize = DEFAULT_MAX_RESULTS;
      if (pResultSize != null)
      {
         try
         {
            resultSize = Integer.parseInt(pResultSize);
         }
         catch (NumberFormatException e)
         {
            //do nothing. ResultSize has already been preset to the default value.
         }  
      }
      return new ScriptPagingDetails(req, resultSize);
   }

   /**
    * Wrap up search results as {@link TopicInfo} instances
    * 
    * @param finalResults ResultSet
    * @param paging PagingRequest
    */
   protected PagingResults<TopicInfo> wrap(final ResultSet finalResults, PagingRequest paging)
   {
      int maxItems = paging.getMaxItems();
      Comparator<TopicInfo> lastPostDesc = new Comparator<TopicInfo>()
      {
         @Override
         public int compare(TopicInfo t1, TopicInfo t2)
         {
            Date t1LastPostDate = t1.getCreatedAt();
            if(discussionService.getMostRecentPost(t1) != null)
            {
               t1LastPostDate = discussionService.getMostRecentPost(t1).getCreatedAt();
            }

            Date t2LastPostDate = t2.getCreatedAt();
            if(discussionService.getMostRecentPost(t2) != null)
            {
               t2LastPostDate = discussionService.getMostRecentPost(t2).getCreatedAt();
            }
            return t2LastPostDate.compareTo(t1LastPostDate);
         }
      };

      final Set<TopicInfo> topics = new TreeSet<TopicInfo>(lastPostDesc);

      for (ResultSetRow row : finalResults)
      {
         Pair<TopicInfo, PostInfo> pair = discussionService.getForNodeRef(row.getNodeRef());
         TopicInfo topic = pair.getFirst();
         if(topic != null)
         {
            String path = nodeService.getPath(topic.getNodeRef()).toDisplayPath(nodeService, permissionService);
            String site = path.split("/")[3];
            TopicInfoImpl tii = (TopicInfoImpl)topic;
            tii.setShortSiteName(site);
            topics.add(tii);
            
            if(topics.size() >= maxItems)
            {
               break;
            }
         }
      }
      
      // Wrap
      return new PagingResults<TopicInfo>() 
      {
         @Override
         public boolean hasMoreItems() 
         {
            try
            {
               return finalResults.hasMore();
            }
            catch(UnsupportedOperationException e)
            {
               // Not all search results support paging
               return false;
            }
         }

         @Override
         public Pair<Integer, Integer> getTotalResultCount() 
         {
            int skipCount = 0;
            int itemsRemainingAfterThisPage = 0;
            try
            {
               skipCount = finalResults.getStart();
            }
            catch(UnsupportedOperationException e) {}
            try
            {
               itemsRemainingAfterThisPage = finalResults.length();
            }
            catch(UnsupportedOperationException e) {}

            final int totalItemsInUnpagedResultSet = skipCount + itemsRemainingAfterThisPage;
            return new Pair<Integer, Integer>(totalItemsInUnpagedResultSet, totalItemsInUnpagedResultSet);
         }

         @Override
         public List<TopicInfo> getPage() 
         {
            return new ArrayList<TopicInfo>(topics);
         }

         @Override
         public String getQueryExecutionId() 
         {
            return null;
         }
      };
   }
}
