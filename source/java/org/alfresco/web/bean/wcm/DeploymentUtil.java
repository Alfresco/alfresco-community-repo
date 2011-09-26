/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.web.bean.wcm;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.faces.context.FacesContext;

import org.alfresco.model.WCMAppModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.web.bean.repository.Repository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Helper methods for deployment
 * 
 * @author Gavin Cornwell
 */
public final class DeploymentUtil
{
   private static final Log logger = LogFactory.getLog(DeploymentUtil.class);
   
   /**
    * Returns all deployment attempts for the given store
    * 
    * @param store The store to get the deployment attempts for
    * @return List of NodeRef's representing the deployment attempts
    */
   public static List<NodeRef> findDeploymentAttempts(String store)
   {
      // return all deployment attempts
      return findDeploymentAttempts(store, null, null);
   }
   
   /**
    * Returns all deployment attempts for the given store
    * 
    * @param store The store to get the deployment attempts for
    * @param fromDate If present only attempts after the given date are returned
    * @param toDate If present only attempts before the given date are returned, if null
    *               toDate defaults to today's date
    * @return List of NodeRef's representing the deployment attempts
    */
   public static List<NodeRef> findDeploymentAttempts(String store, Date fromDate, Date toDate)
   {
      FacesContext fc = FacesContext.getCurrentInstance();
      SearchService searchService = Repository.getServiceRegistry(fc).getSearchService();
      
      // query for all deploymentattempt nodes with the deploymentattemptstore
      // set to the given store id
      StringBuilder query = new StringBuilder("@");
      query.append(NamespaceService.WCMAPP_MODEL_PREFIX);
      query.append("\\:");
      query.append(WCMAppModel.PROP_DEPLOYATTEMPTSTORE.getLocalName());
      query.append(":\"");
      query.append(store);
      query.append("\"");
      
      // constrain the search by date if a fromDate is applied
      if (fromDate != null)
      {
         if (toDate == null)
         {
            toDate = new Date();
         }
         
         // see if the dates are the same (ignoring the time)
         boolean sameDate = false;
         Calendar fromCal = new GregorianCalendar();
         fromCal.setTime(fromDate);
         Calendar toCal = new GregorianCalendar();
         toCal.setTime(toDate);
         if ((fromCal.get(Calendar.YEAR) == toCal.get(Calendar.YEAR)) && 
             (fromCal.get(Calendar.MONTH) == toCal.get(Calendar.MONTH)) &&
             (fromCal.get(Calendar.DAY_OF_MONTH) == toCal.get(Calendar.DAY_OF_MONTH)))
         {
            sameDate = true;
         }
         
         // add date to query
         query.append(" AND @");
         query.append(NamespaceService.WCMAPP_MODEL_PREFIX);
         query.append("\\:");
         query.append(WCMAppModel.PROP_DEPLOYATTEMPTTIME.getLocalName());
         query.append(":");
         
         if (sameDate)
         {
            // convert date into format needed for range query
            String queryDate = formatLuceneQueryDate(fromDate, false);
            
            // query for exact date
            query.append("\"");
            query.append(queryDate);
            query.append("\"");
         }
         else
         {
            // convert to date into format needed for range query
            String queryFromDate = formatLuceneQueryDate(fromDate, true);
            String queryToDate = formatLuceneQueryDate(toDate, true);
            
            // create a date range query
            query.append("[");
            query.append(queryFromDate);
            query.append(" TO ");
            query.append(queryToDate);
            query.append("]");
         }
      }
      
      if (logger.isDebugEnabled())
         logger.debug("Finding deploymentattempt nodes using query: " + query.toString());
      
      ResultSet results = null;
      List<NodeRef> attempts = new ArrayList<NodeRef>();
      try
      {
         // sort the results by deploymentattempttime
         SearchParameters sp = new SearchParameters();
         sp.addStore(Repository.getStoreRef());
         sp.setLanguage(SearchService.LANGUAGE_LUCENE);
         sp.setQuery(query.toString());
         sp.addSort("@" + WCMAppModel.PROP_DEPLOYATTEMPTTIME, false);
         
         // execute the query
         results = searchService.query(sp);
         
         if (logger.isDebugEnabled())
            logger.debug("Found " + results.length() + " deployment attempts");
         
         for (NodeRef attempt : results.getNodeRefs())
         {
            attempts.add(attempt);
         }
      }
      finally
      {
         if (results != null)
         {
            results.close();
         }
      }
      
      return attempts;
   }
   
   /**
    * Retrieves the NodeRef of the deploymentattempt node with the given id
    * 
    * @param attemptId The deployattemptid of the node to be found
    * @return The NodeRef of the deploymentattempt node or null if not found
    */
   public static NodeRef findDeploymentAttempt(String attemptId)
   {
      FacesContext fc = FacesContext.getCurrentInstance();
      SearchService searchService = Repository.getServiceRegistry(fc).getSearchService();
      
      // construct the query
      StringBuilder query = new StringBuilder("@");
      query.append(NamespaceService.WCMAPP_MODEL_PREFIX);
      query.append("\\:");
      query.append(WCMAppModel.PROP_DEPLOYATTEMPTID.getLocalName());
      query.append(":\"");
      query.append(attemptId);
      query.append("\"");
      
      ResultSet results = null;
      NodeRef attempt = null;
      try
      {
         // execute the query
         results = searchService.query(Repository.getStoreRef(), 
               SearchService.LANGUAGE_LUCENE, query.toString());
         
         if (results.length() == 1)
         {
            attempt = results.getNodeRef(0);
         }
         else if (results.length() > 1)
         {
            throw new IllegalStateException(
               "More than one deployment attempt node was found, there should only be one!");
         }
      }
      finally
      {
         if (results != null)
         {
            results.close();
         }
      }
      
      return attempt;
   }
   
   /**
    * Returns the test servers allocated to the given store.
    * 
    * @param store The store to get the test server for
    * @return The allocated server(s), an empty list if there isn't one
    */
   public static List<NodeRef> findAllocatedTestServers(String store)
   {
	  List<NodeRef>serverList = new ArrayList<NodeRef>();
	  
      FacesContext fc = FacesContext.getCurrentInstance();
      SearchService searchService = Repository.getServiceRegistry(fc).getSearchService();
      
      // construct the query
      StringBuilder query = new StringBuilder("@");
      query.append(NamespaceService.WCMAPP_MODEL_PREFIX);
      query.append("\\:");
      query.append(WCMAppModel.PROP_DEPLOYSERVERALLOCATEDTO.getLocalName());
      query.append(":\"");
      query.append(store);
      query.append("\"");
      
      ResultSet results = null;
      try
      {
         // execute the query
         results = searchService.query(Repository.getStoreRef(), 
               SearchService.LANGUAGE_LUCENE, query.toString());
         
         if (results.length() > 0)
         {
        	 for(int i = 0; i < results.length(); i++)
        	 {
        		 serverList.add(results.getNodeRef(i));
        	 }
         }
      }
      finally
      {
         if (results != null)
         {
            results.close();
         }
      }
      
      return serverList;
   }
   
   private static String formatLuceneQueryDate(Date date, boolean range)
   {
      Calendar cal = new GregorianCalendar();
      cal.setTime(date);
      
      StringBuilder queryDate = new StringBuilder();
      queryDate.append(cal.get(Calendar.YEAR));
      if (range)
      {
         queryDate.append("\\");
      }
      queryDate.append("-");
      queryDate.append((cal.get(Calendar.MONTH)+1));
      if (range)
      {
         queryDate.append("\\");
      }
      queryDate.append("-");
      queryDate.append(cal.get(Calendar.DAY_OF_MONTH));
      queryDate.append("T00:00:00");
      
      return queryDate.toString();
   }
}
