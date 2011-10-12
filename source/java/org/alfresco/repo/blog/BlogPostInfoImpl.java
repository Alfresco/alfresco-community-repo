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
package org.alfresco.repo.blog;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.blog.BlogPostInfo;
import org.alfresco.service.cmr.repository.NodeRef;

/**
  * An implementation of a {@link BlogPostInfo} 
  * 
  * @author Nick Burch (based on the existing webscript conrollers in the REST API)
  * @since 4.0
  */
 public class BlogPostInfoImpl implements BlogPostInfo
 {
     private final NodeRef nodeRef;
     private final NodeRef containerNodeRef;
     private final String systemName;
     private String title;
     
     public BlogPostInfoImpl(NodeRef nodeRef, NodeRef containerNodeRef, String systemName)
     {
         this.nodeRef = nodeRef;
         this.containerNodeRef = containerNodeRef;
         this.systemName = systemName;
     }
     
     /**
      * Gets the NodeRef representing this blog-post.
      */
     @Override
     public NodeRef getNodeRef()
     {
         return nodeRef;
     }
     
     @Override
     public NodeRef getContainerNodeRef() 
     {
        return containerNodeRef;
     }
     
     /**
      * Gets the {@link ContentModel#PROP_NAME cm:name} of the blog post.
      * @return
      */
     @Override
     public String getSystemName()
     {
         return systemName;
     }
     
     /**
      * @return the Title of the blog post.
      */
     @Override
     public String getTitle()
     {
        return title;
     }
     /**
      * Set the Title of the blog post.
      */
     @Override
     public void setTitle(String title)
     {
        this.title = title;
     }
 }