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
package org.alfresco.repo.webservice.repository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.webservice.Utils;
import org.alfresco.repo.webservice.types.NamedValue;
import org.alfresco.repo.webservice.types.Query;
import org.alfresco.repo.webservice.types.ResultSetRowNode;
import org.alfresco.repo.webservice.types.Store;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of a QuerySession that retrieves results from a repository ResultSet
 * 
 * @author gavinc
 */
public class ResultSetQuerySession extends AbstractQuerySession
{
    private static final long serialVersionUID = -9154514445963635138L;

   private transient static Log logger = LogFactory.getLog(ResultSetQuerySession.class);
   
   private Store store;
   private Query query;
   @SuppressWarnings("unused")
   private boolean includeMetaData;
   
   /**
    * Constructs a ResultSetQuerySession
    * 
    * @param batchSize The batch size to use for this session
    * @param store The repository store to query against
    * @param query The query to execute
    * @param includeMetaData Whether to include metadata in the query results
    */
   public ResultSetQuerySession(int batchSize, Store store, Query query, boolean includeMetaData)
   {
      super(batchSize);
      
      this.store = store;
      this.query = query;
      this.includeMetaData = includeMetaData;
   }
   
   /**
    * @see org.alfresco.repo.webservice.repository.QuerySession#getNextResultsBatch(org.alfresco.service.cmr.search.SearchService, org.alfresco.service.cmr.repository.NodeService, org.alfresco.service.namespace.NamespaceService)
    */
   public QueryResult getNextResultsBatch(SearchService searchService, NodeService nodeService, NamespaceService namespaceService, DictionaryService dictionaryService)
   {
      QueryResult queryResult = null;
      
      if (this.position != -1)
      {
         if (logger.isDebugEnabled())
            logger.debug("Before getNextResultsBatch: " + toString());
         
         // handle the special search string of * meaning, get everything
         String statement = query.getStatement();
         if (statement.equals("*"))
         {
            statement = "ISNODE:*";
         }
         ResultSet searchResults = null;
         try
         {
            searchResults = searchService.query(Utils.convertToStoreRef(this.store), 
                  this.query.getLanguage(), statement);
         
            int totalRows = searchResults.length();
            int lastRow = calculateLastRowIndex(totalRows);
            int currentBatchSize = lastRow - this.position;
         
            if (logger.isDebugEnabled())
               logger.debug("Total rows = " + totalRows + ", current batch size = " + currentBatchSize);
         
            org.alfresco.repo.webservice.types.ResultSet batchResults = new org.alfresco.repo.webservice.types.ResultSet();      
            List<org.alfresco.repo.webservice.types.ResultSetRow> rowList =
            	new ArrayList<org.alfresco.repo.webservice.types.ResultSetRow>();
            
            for (int x = this.position; x < lastRow; x++)
            {
               ResultSetRow origRow = searchResults.getRow(x);
               NodeRef nodeRef = origRow.getNodeRef();
               
               // search can return nodes that no longer exist, so we need to  ignore these
               if(nodeService.exists(nodeRef) == false) 
               {
            	   if(logger.isDebugEnabled())
            	   {
            		   logger.warn("Search returned node that doesn't exist: " + nodeRef);
            	   }
            	   continue;
               }
               
               ResultSetRowNode rowNode = createResultSetRowNode(nodeRef, nodeService);
            
               // get the data for the row and build up the columns structure
               Map<Path, Serializable> values = origRow.getValues();
               NamedValue[] columns = new NamedValue[values.size() + 1];
               int col = 0;
               for (Path path : values.keySet())
               {
                  // Get the attribute QName from the result path
                  String attributeName = path.last().toString();
                  if (attributeName.startsWith("@") == true)
                  {
                      attributeName = attributeName.substring(1);
                  }
               
                  columns[col] = Utils.createNamedValue(dictionaryService, QName.createQName(attributeName), values.get(path));
                  col++;
               }
               
               // add one extra column for the node's path
               columns[col] = Utils.createNamedValue(dictionaryService, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "path"), nodeService.getPath(nodeRef).toString());
            
               org.alfresco.repo.webservice.types.ResultSetRow row = new org.alfresco.repo.webservice.types.ResultSetRow();
               row.setColumns(columns);
               row.setScore(origRow.getScore());
               row.setRowIndex(x);
               row.setNode(rowNode);
               
               // add the row to the overall results list
               rowList.add(row);
            }
         
            // TODO: build up the meta data data structure if we've been asked to
         
            // add the rows to the result set and set the total row count
            org.alfresco.repo.webservice.types.ResultSetRow[] rows = 
            	rowList.toArray(new org.alfresco.repo.webservice.types.ResultSetRow[rowList.size()]);;
            batchResults.setRows(rows);
            batchResults.setTotalRowCount(totalRows);
         
            queryResult = new QueryResult(getId(), batchResults);
         
            // move the position on
            updatePosition(totalRows, queryResult);
         
            if (logger.isDebugEnabled())
               logger.debug("After getNextResultsBatch: " + toString());
         }
         finally
         {
             if (searchResults != null)
             {
                 searchResults.close();
             }
         }
      }
      
      return queryResult;
   }

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      StringBuilder builder = new StringBuilder(super.toString());
      builder.append(" (id=").append(getId());
      builder.append(" batchSize=").append(this.batchSize);
      builder.append(" position=").append(this.position);
      builder.append(" store=").append(this.store.getScheme()).append(":").append(this.store.getAddress());
      builder.append(" language=").append(this.query.getLanguage());
      builder.append(" statement=").append(this.query.getStatement());
      builder.append(")");
      return builder.toString();
   }
}
