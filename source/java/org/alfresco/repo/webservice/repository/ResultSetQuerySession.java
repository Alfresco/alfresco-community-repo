/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.webservice.repository;

import java.io.Serializable;
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
            org.alfresco.repo.webservice.types.ResultSetRow[] rows = new org.alfresco.repo.webservice.types.ResultSetRow[currentBatchSize];
         
            int arrPos = 0;
            for (int x = this.position; x < lastRow; x++)
            {
               ResultSetRow origRow = searchResults.getRow(x);
               NodeRef nodeRef = origRow.getNodeRef();
               ResultSetRowNode rowNode = new ResultSetRowNode(nodeRef.getId(), nodeService.getType(nodeRef).toString(), null);
            
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
               
                  columns[col] = Utils.createNamedValue(dictionaryService, QName.createQName(attributeName), values.get(path)); //new NamedValue(attributeName, value);
                  col++;
               }
               
               // add one extra column for the node's path
               columns[col] = Utils.createNamedValue(dictionaryService, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "path"), nodeService.getPath(nodeRef).toString());
            
               org.alfresco.repo.webservice.types.ResultSetRow row = new org.alfresco.repo.webservice.types.ResultSetRow();
               row.setColumns(columns);
               row.setScore(origRow.getScore());
               row.setRowIndex(x);
               row.setNode(rowNode);
            
               // add the row to the overall results
               rows[arrPos] = row;
               arrPos++;
            }
         
            // TODO: build up the meta data data structure if we've been asked to
         
            // add the rows to the result set and set the total row count
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
