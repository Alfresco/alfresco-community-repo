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
package org.alfresco.service.cmr.wiki;

import java.util.Date;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.service.NotAuditable;

/**
 * The Wiki service.
 * 
 * @author Nick Burch
 * @since 4.0
 */
public interface WikiService {
   /**
    * Creates a new {@link WikiPageInfo} in the given site, with the
    *  specified contents
    *  
    * @return The newly created {@link WikiPageInfo}
    */
   @NotAuditable
   WikiPageInfo createWikiPage(String siteShortName, String title,
         String contents);
   
   /**
    * Updates an existing {@link WikiPageInfo} in the repository.
    *  
    * @return The updated {@link WikiPageInfo}
    */
   @NotAuditable
   WikiPageInfo updateWikiPage(WikiPageInfo wikiPage);
   
   /**
    * Deletes an existing {@link WikiPageInfo} from the repository
    */
   @NotAuditable
   void deleteWikiPage(WikiPageInfo wikiPage);
   
   /**
    * Retrieves an existing {@link WikiPageInfo} from the repository
    */
   @NotAuditable
   WikiPageInfo getWikiPage(String siteShortName, String pageName);

   /**
    * Retrieves all {@link WikiPageInfo} instances in the repository
    *  for the given site.
    */
   @NotAuditable
   PagingResults<WikiPageInfo> listWikiPages(String siteShortName, PagingRequest paging);

   /**
    * Retrieves all {@link WikiPageInfo} instances in the repository
    *  for the given site and the specified user.
    */
   @NotAuditable
   PagingResults<WikiPageInfo> listWikiPages(String siteShortName, String user, PagingRequest paging);

   /**
    * Retrieves all {@link WikiPageInfo} instances in the repository
    *  for the given site, created in the specified date range
    */
   @NotAuditable
   PagingResults<WikiPageInfo> listWikiPagesByCreated(String siteShortName, Date from, Date to, PagingRequest paging);

   /**
    * Retrieves all {@link WikiPageInfo} instances in the repository
    *  for the given site, modified in the specified date range
    */
   @NotAuditable
   PagingResults<WikiPageInfo> listWikiPagesByModified(String siteShortName, Date from, Date to, PagingRequest paging);
}
