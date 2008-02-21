/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.bean.wcm;

import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.util.ISO9075;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;


/**
 * Helper methods for deployment
 * 
 * @author Gavin Cornwell
 */
public final class DeploymentUtil
{
   public static List<NodeRef> findDeploymentAttempts(String store)
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

      ResultSet results = null;
      List<NodeRef> attempts = new ArrayList<NodeRef>();
      try
      {
         // execute the query
         results = searchService.query(Repository.getStoreRef(), 
               SearchService.LANGUAGE_LUCENE, query.toString());
         
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
    * Returns the test server allocated to the given store.
    * 
    * @param store The store to get the test server for
    * @return The allocated server or null if there isn't one
    */
   public static NodeRef findAllocatedTestServer(String store)
   {
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
      NodeRef testServer = null;
      try
      {
         // execute the query
         results = searchService.query(Repository.getStoreRef(), 
               SearchService.LANGUAGE_LUCENE, query.toString());
         
         if (results.length() == 1)
         {
            testServer = results.getNodeRef(0);
         }
         else if (results.length() > 1)
         {
            throw new IllegalStateException("More than one allocated test server for store '" +
                     store + "' was found, should only be one!");
         }
      }
      finally
      {
         if (results != null)
         {
            results.close();
         }
      }
      
      return testServer;
   }
   
   /**
    * Returns a list of NodeRefs representing the 'live' servers configured
    * for the given web project.
    *  
    * @param webProject Web project to get live servers for 
    * @return List of live servers
    */
   public static List<NodeRef> findLiveServers(NodeRef webProject)
   {
      return findServers(webProject, true, false);
   }
   
   /**
    * Returns a list of NodeRefs representing the 'test' servers configured
    * for the given web project.
    *  
    * @param webProject Web project to get test servers for 
    * @param allocated true only returns those servers already allocated 
    * @return List of test servers
    */
   public static List<NodeRef> findTestServers(NodeRef webProject, boolean allocated)
   {
      return findServers(webProject, false, allocated);
   }
   
   
   private static List<NodeRef> findServers(NodeRef webProject, boolean live, boolean allocated)
   {
      FacesContext context = FacesContext.getCurrentInstance();
      NodeService nodeService = Repository.getServiceRegistry(context).getNodeService();
      SearchService searchService = Repository.getServiceRegistry(context).getSearchService();
      
      // build the query
      String webProjectName = (String)nodeService.getProperty(webProject, ContentModel.PROP_NAME);
      String safeProjectName = ISO9075.encode(webProjectName); 
      StringBuilder query = new StringBuilder("PATH:\"/");
      query.append(Application.getRootPath(context));
      query.append("/");
      query.append(Application.getWebsitesFolderName(context));
      query.append("/cm:");
      query.append(safeProjectName);
      query.append("/*\" AND @");
      query.append(NamespaceService.WCMAPP_MODEL_PREFIX);
      query.append("\\:");
      query.append(WCMAppModel.PROP_DEPLOYSERVERTYPE.getLocalName());
      query.append(":\"");
      if (live)
      {
         query.append(WCMAppModel.CONSTRAINT_LIVESERVER);
      }
      else
      {
         query.append(WCMAppModel.CONSTRAINT_TESTSERVER);
      }      
      query.append("\"");
      
      // if required filter the test servers
      if (live == false && allocated == false)
      {
         query.append(" AND ISNULL:\"");
         query.append(WCMAppModel.PROP_DEPLOYSERVERALLOCATEDTO.toString());
         query.append("\"");
      }
      
      // execute the query
      ResultSet results = null;
      List<NodeRef> servers = new ArrayList<NodeRef>();
      try
      {
         results = searchService.query(Repository.getStoreRef(), 
               SearchService.LANGUAGE_LUCENE, query.toString());
         
         for (NodeRef server : results.getNodeRefs())
         {
            servers.add(server);
         }
      }
      finally
      {
         if (results != null)
         {
            results.close();
         }
      }
      
      return servers;
   }
}
