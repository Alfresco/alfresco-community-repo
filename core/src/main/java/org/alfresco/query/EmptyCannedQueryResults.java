/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.query;

import java.util.Collections;
import java.util.List;

import org.alfresco.util.Pair;

/**
 * An always empty {@link CannedQueryResults}, used when you know
 *  you can short circuit a query when no results are found.
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class EmptyCannedQueryResults<R> extends EmptyPagingResults<R> implements CannedQueryResults<R>
{
   private CannedQuery<R> query;
   
   public EmptyCannedQueryResults(CannedQuery<R> query)
   {
      this.query = query;
   }

   @Override
   public CannedQuery<R> getOriginatingQuery() {
      return query;
   }

   @Override
   public int getPageCount() {
      return 0;
   }

   @Override
   public int getPagedResultCount() {
      return 0;
   }

   @Override
   public List<List<R>> getPages() {
      return Collections.emptyList();
   }

   @Override
   public R getSingleResult() {
      return null; 
   }
}
