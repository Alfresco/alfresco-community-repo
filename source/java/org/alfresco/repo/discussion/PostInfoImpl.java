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
package org.alfresco.repo.discussion;

import java.util.Date;

import org.alfresco.service.cmr.discussion.PostInfo;
import org.alfresco.service.cmr.discussion.TopicInfo;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * An implementation of {@link PostInfo}
 *
 * @author Nick Burch (based on existing webscript controllers in the REST API)
 * @since 4.0
 */
public class PostInfoImpl implements PostInfo 
{
   private NodeRef nodeRef;
   private TopicInfo topic;
   private String systemName;
   private String title;
   private String contents;
   private String creator;
   private String modifier;
   private Date createdAt;
   private Date modifiedAt;
   private Date updatedAt;

   /**
    * Creates a new, empty {@link PostInfo}
    */
   public PostInfoImpl()
   {
   }
   
   /**
    * Create a {@link PostInfo} object from an existing node
    */
   public PostInfoImpl(NodeRef nodeRef, String systemName, TopicInfo topic)
   {
      this.nodeRef = nodeRef;
      this.systemName = systemName;
      this.topic = topic;
   }

   @Override
   public TopicInfo getTopic() 
   {
      return topic;
   }

   @Override
   public NodeRef getNodeRef() 
   {
      return nodeRef;
   }
   
   @Override
   public String getSystemName() 
   {
      return systemName;
   }

   @Override
   public String getTitle() 
   {
      return title;
   }

   @Override
   public String getContents() 
   {
      return contents;
   }

   @Override
   public String getCreator() 
   {
      return creator;
   }

   @Override
   public String getModifier() 
   {
      return modifier;
   }

   @Override
   public Date getCreatedAt() 
   {
      return createdAt;
   }

   @Override
   public Date getModifiedAt() 
   {
      return modifiedAt;
   }

   @Override
   public Date getUpdatedAt() 
   {
      return updatedAt;
   }

   @Override
   public void setTitle(String title) 
   {
      this.title = title;
   }

   @Override
   public void setContents(String contents) 
   {
      this.contents = contents;
   }

   public void setCreator(String creator) 
   {
      this.creator = creator;
   }

   public void setModifier(String modifier) 
   {
      this.modifier = modifier;
   }

   public void setCreatedAt(Date createdAt) 
   {
      this.createdAt = createdAt;
   }

   public void setModifiedAt(Date modifiedAt) 
   {
      this.modifiedAt = modifiedAt;
   }

   public void setUpdatedAt(Date updatedAt) 
   {
      this.updatedAt = updatedAt;
   }
}
