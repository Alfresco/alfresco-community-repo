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
package org.alfresco.service.cmr.links;

import java.util.Date;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.service.NotAuditable;

/**
 * The Links service.
 * 
 * @author Nick Burch
 * @since 4.0
 */
public interface LinksService {
   /**
    * Creates a new {@link LinkInfo} in the given site, with the
    *  specified settings
    *  
    * @return The newly created {@link LinkInfo}
    */
   @NotAuditable
   LinkInfo createLink(String siteShortName, String title,
         String description, String url, boolean internal);
   
   /**
    * Updates an existing {@link LinkInfo} in the repository.
    *  
    * @return The updated {@link LinkInfo}
    */
   @NotAuditable
   LinkInfo updateLink(LinkInfo link);
   
   /**
    * Deletes an existing {@link LinkInfo} from the repository
    */
   @NotAuditable
   void deleteLink(LinkInfo link);
   
   /**
    * Retrieves an existing {@link LinkInfo} from the repository
    */
   @NotAuditable
   LinkInfo getLink(String siteShortName, String linkName);

   /**
    * Retrieves all {@link LinkInfo} instances in the repository
    *  for the given site.
    */
   @NotAuditable
   PagingResults<LinkInfo> listLinks(String siteShortName, PagingRequest paging);

   /**
    * Retrieves all {@link LinkInfo} instances in the repository
    *  for the given site and the specified user.
    */
   @NotAuditable
   PagingResults<LinkInfo> listLinks(String siteShortName, String user, PagingRequest paging);

   /**
    * Retrieves all {@link LinkInfo} instances in the repository
    *  for the given site, created in the specified date range
    */
   @NotAuditable
   PagingResults<LinkInfo> listLinks(String siteShortName, Date from, Date to, PagingRequest paging);

   /**
    * Finds all {@link LinkInfo} instances indexed in the repository
    *  for the given site, created by the specified user in the specified
    *  date range, with the given tag
    */
   @NotAuditable
   PagingResults<LinkInfo> findLinks(String siteShortName, String user, Date from, Date to, String tag, PagingRequest paging);
}
