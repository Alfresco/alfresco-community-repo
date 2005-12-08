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

import org.alfresco.util.GUID;

/**
 * Abstract implementation of a QuerySession providing support
 * for automatic id generation and provides support for 
 * paging through query results.
 * 
 * @author gavinc
 */
public abstract class AbstractQuerySession implements QuerySession
{
   protected int batchSize;
   protected int position = 0;
   
   private String id;
   
   /**
    * Common constructor that initialises the session's id and batch size 
    * 
    * @param batchSize The batch size this session will use
    */
   public AbstractQuerySession(int batchSize)
   {
      this.id = GUID.generate();
      this.batchSize = batchSize;
   }
   
   /**
    * @see org.alfresco.repo.webservice.repository.QuerySession#getId()
    */
   public String getId()
   {
      return this.id;
   }
 
   /**
    * Calculates the index of the last row to retrieve. 
    * 
    * @param totalRowCount The total number of rows in the results
    * @return The index of the last row to return
    */
   protected int calculateLastRowIndex(int totalRowCount)
   {
      int lastRowIndex = totalRowCount;
      
      // set the last row index if there are more results available 
      // than the batch size
      if ((this.batchSize != -1) && ((this.position + this.batchSize) < totalRowCount))
      {
         lastRowIndex = this.position + this.batchSize;
      }
      
      return lastRowIndex;
   }
   
   /**
    * Calculates the value of the next position.
    * If the end of the result set is reached the position is set to -1
    * 
    * @param totalRowCount The total number of rows in the results
    * @param queryResult The QueryResult object being returned to the client,
    * if there are no more results the id is removed from the QueryResult instance
    */
   protected void updatePosition(int totalRowCount, QueryResult queryResult)
   {
      this.position += this.batchSize;
      if (this.position >= totalRowCount)
      {
         // signify that there are no more results 
         this.position = -1;
         queryResult.setQuerySession(null);
      }
   }
}
