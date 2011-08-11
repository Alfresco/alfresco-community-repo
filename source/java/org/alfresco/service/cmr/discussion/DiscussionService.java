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

import org.alfresco.model.ForumModel;
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
    * Creates a new {@link PostInfo} in the given topic,
    *  specified contents
    *  
    * @return The newly created {@link PostInfo}
    */
   @NotAuditable
   PostInfo createPost(TopicInfo topic, String contents);
   
   /**
    * Creates a new {@link TopicInfo} in the given site
    */
   @NotAuditable
   TopicInfo createTopic(String siteShortName, String title);
   
   /**
    * Creates a new {@link TopicInfo} attached to the specified Node. 
    * The parent Node should normally either be a Site Container, or a 
    * {@link ForumModel#TYPE_FORUM}
    */
   @NotAuditable
   TopicInfo createTopic(NodeRef parentNodeRef, String title);
   
   /**
    * Updates an existing {@link PostInfo} in the repository.
    *  
    * @return The updated {@link PostInfo}
    */
   @NotAuditable
   PostInfo updatePost(PostInfo post);
   
   /**
    * Updates an existing {@link TopicInfo} in the repository.
    *  
    * @return The updated {@link TopicInfo}
    */
   @NotAuditable
   TopicInfo updateTopic(TopicInfo topic);
   
   /**
    * Deletes an existing {@link PostInfo} from the repository
    */
   @NotAuditable
   void deletePost(PostInfo post);
   
   /**
    * Deletes an existing {@link TopicInfo} from the repository
    */
   @NotAuditable
   void deleteTopic(TopicInfo topic);
   
   /**
    * Retrieves an existing {@link PostInfo} from the repository
    */
   @NotAuditable
   PostInfo getPost(TopicInfo topic, String postName);

   /**
    * Retrieves the Primary (Root) Post in a topic, to which all
    *  replies belong.
    * Returns null if the topic currently has no posts
    */
   @NotAuditable
   PostInfo getPrimaryPost(TopicInfo topic);
   
   /**
    * Retrieves an existing {@link TopicInfo} from the repository,
    *  which is within a site
    */
   @NotAuditable
   TopicInfo getTopic(String siteShortName, String linkName);
   
   /**
    * Retrieves an existing {@link TopicInfo} from the repository,
    *  which is attached to the specified Node.
    * The parent Node should normally either be a Site Container, or a 
    *  {@link ForumModel#TYPE_FORUM}
    */
   @NotAuditable
   TopicInfo getTopic(NodeRef parentNodeRef, String topicName);
   
   
   /**
    * Retrieves all replies on a Topic
    */
   @NotAuditable
   PagingResults<PostInfo> listPostReplies(TopicInfo forum, int levels, PagingRequest paging);

   /**
    * Retrieves all replies to a Post
    */
   @NotAuditable
   PagingResults<PostInfo> listPostReplies(PostInfo primaryPost, int levels, PagingRequest paging);
   
   /**
    * Retrieves all posts in a site
    */
   @NotAuditable
   PagingResults<PostInfo> listPosts(String siteShortName, PagingRequest paging);

   /**
    * Retrieves all posts attached to the specified Node
    */
   @NotAuditable
   PagingResults<PostInfo> listPosts(NodeRef nodeRef, PagingRequest paging);

   // TODO Hot, New and Mine listing support
}
