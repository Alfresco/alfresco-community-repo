/*
 * Copyright (C) 2005 Alfresco, Inc.
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
