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
import java.util.List;
import java.util.Map;

import org.alfresco.repo.webservice.Utils;
import org.alfresco.repo.webservice.types.NamedValue;
import org.alfresco.repo.webservice.types.Reference;
import org.alfresco.repo.webservice.types.ResultSetRow;
import org.alfresco.repo.webservice.types.ResultSetRowNode;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of a QuerySession that stores the results from a query for children
 * 
 * @author gavinc
 */
public class ChildrenQuerySession extends AbstractQuerySession
{
   private static final long serialVersionUID = -5347036309571057074L;

   private transient static Log logger = LogFactory.getLog(ChildrenQuerySession.class);
   
   private Reference node;
   
   /**
    * Constructs a ChildrenQuerySession
    * 
    * @param batchSize The batch size to use for this session
    * @param node The node to retrieve the parents
    */
   public ChildrenQuerySession(int batchSize, Reference node)
   {
      super(batchSize);
      
      this.node = node;
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
         
         // create the node ref and get the children from the repository
         NodeRef nodeRef = Utils.convertToNodeRef(this.node, nodeService, searchService, namespaceService);
         List<ChildAssociationRef> kids = nodeService.getChildAssocs(nodeRef);
         
         int totalRows = kids.size();
         int lastRow = calculateLastRowIndex(totalRows);
         int currentBatchSize = lastRow - this.position;
         
         if (logger.isDebugEnabled())
            logger.debug("Total rows = " + totalRows + ", current batch size = " + currentBatchSize);
         
         org.alfresco.repo.webservice.types.ResultSet batchResults = new org.alfresco.repo.webservice.types.ResultSet();      
         org.alfresco.repo.webservice.types.ResultSetRow[] rows = new org.alfresco.repo.webservice.types.ResultSetRow[currentBatchSize];
            
         int arrPos = 0;
         for (int x = this.position; x < lastRow; x++)
         {
            ChildAssociationRef assoc = kids.get(x);
            NodeRef childNodeRef = assoc.getChildRef();
            ResultSetRowNode rowNode = new ResultSetRowNode(childNodeRef.getId(), nodeService.getType(childNodeRef).toString(), null);
            
            // create columns for all the properties of the node
            // get the data for the row and build up the columns structure
            Map<QName, Serializable> props = nodeService.getProperties(childNodeRef);
            NamedValue[] columns = new NamedValue[props.size()+5];
            int col = 0;
            for (QName propName : props.keySet())
            {
               columns[col] = Utils.createNamedValue(dictionaryService, propName, props.get(propName));
               col++;
            }
            
            // Now add the system columns containing the association details
            columns[col] = new NamedValue(SYS_COL_ASSOC_TYPE, Boolean.FALSE, assoc.getTypeQName().toString(), null);
            col++;
            columns[col] = new NamedValue(SYS_COL_ASSOC_NAME, Boolean.FALSE, assoc.getQName().toString(), null);
            col++;
            columns[col] = new NamedValue(SYS_COL_IS_PRIMARY, Boolean.FALSE, Boolean.toString(assoc.isPrimary()), null);
            col++;
            columns[col] = new NamedValue(SYS_COL_NTH_SIBLING, Boolean.FALSE, Integer.toString(assoc.getNthSibling()), null);
            
            // Add one more column for the node's path
            col++;
            columns[col] = Utils.createNamedValue(dictionaryService, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "path"), nodeService.getPath(childNodeRef).toString());
                  
            ResultSetRow row = new ResultSetRow();
            row.setRowIndex(x);
            row.setNode(rowNode);
            row.setColumns(columns);
            
            // add the row to the overall results
            rows[arrPos] = row;
            arrPos++;
         }
         
         // add the rows to the result set and set the total row count
         batchResults.setRows(rows);
         batchResults.setTotalRowCount(totalRows);
         
         queryResult = new QueryResult(getId(), batchResults);
         
         // move the position on
         updatePosition(totalRows, queryResult);
         
         if (logger.isDebugEnabled())
            logger.debug("After getNextResultsBatch: " + toString());
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
      builder.append(" node-id=").append(this.node.getUuid()).append(")");
      return builder.toString();
   }
}
