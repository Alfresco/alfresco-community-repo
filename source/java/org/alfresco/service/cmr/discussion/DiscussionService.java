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
package org.alfresco.service.cmr.discussion;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.service.NotAuditable;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * The Discussions service.
 * 
 * @author Nick Burch
 * @since 4.0
 */
public interface DiscussionService {
   /**
    * Creates a new {@link ForumPostInfo} in the given topic,
    *  specified settings
    *  
    * @return The newly created {@link ForumPostInfo}
    */
   @NotAuditable
   ForumPostInfo createForumPost(ForumTopicInfo topic, String title,
         String contents);
   
   /**
    * Creates a new {@link ForumTopicInfo} in the given site
    */
   @NotAuditable
   ForumTopicInfo createForumTopic(String siteShortName);
   
   /**
    * Creates a new {@link ForumTopicInfo} attached to the
    *  specified Node
    */
   @NotAuditable
   ForumTopicInfo createForumTopic(NodeRef nodeRef);
   
   /**
    * Updates an existing {@link ForumPostInfo} in the repository.
    *  
    * @return The updated {@link ForumPostInfo}
    */
   @NotAuditable
   ForumPostInfo updateForumPost(ForumPostInfo post);
   
   /**
    * Deletes an existing {@link ForumPostInfo} from the repository
    */
   @NotAuditable
   void deleteForumPost(ForumPostInfo post);
   
   /**
    * Deletes an existing {@link ForumTopicInfo} from the repository
    */
   @NotAuditable
   void deleteForumTopic(ForumTopicInfo topic);
   
   /**
    * Retrieves an existing {@link ForumPostInfo} from the repository
    */
   @NotAuditable
   ForumPostInfo getForumPost(ForumTopicInfo topic, String linkName);

   /**
    * Retrieves an existing {@link ForumTopicInfo} from the repository,
    *  which is within a site
    */
   @NotAuditable
   ForumTopicInfo getForumTopic(String siteShortName, String linkName);
   
   /**
    * Retrieves an existing {@link ForumTopicInfo} from the repository,
    *  which is attached to the specified Node
    */
   @NotAuditable
   ForumTopicInfo getForumTopic(NodeRef nodeRef, String linkName);
   
   // TODO Decide about the primary post on a topic
   
   
   /**
    * Retrieves all replies on a Topic
    */
   @NotAuditable
   PagingResults<ForumPostInfo> listForumPostReplies(ForumTopicInfo forum, int levels, PagingRequest paging);

   /**
    * Retrieves all replies to a Post
    */
   @NotAuditable
   PagingResults<ForumPostInfo> listForumPostReplies(ForumPostInfo primaryPost, int levels, PagingRequest paging);
   
   /**
    * Retrieves all posts in a site
    */
   @NotAuditable
   PagingResults<ForumPostInfo> listForumPosts(String siteShortName, PagingRequest paging);

   /**
    * Retrieves all posts attached to the specified Node
    */
   @NotAuditable
   PagingResults<ForumPostInfo> listForumPosts(NodeRef nodeRef, PagingRequest paging);

   // TODO Hot, New and Mine listing support
}
