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
package org.alfresco.service.cmr.blog;

import java.io.Serializable;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.permissions.PermissionCheckValue;
import org.alfresco.service.cmr.repository.NodeRef;

/**
  * This class represents a blog post in a blog. 
  * 
  * @author Neil Mc Erlean
  * @since 4.0
  */
 public interface BlogPostInfo extends Serializable, PermissionCheckValue
 {
     /**
      * Gets the NodeRef representing this blog-post.
      */
     NodeRef getNodeRef();
     
     /**
      * @return the NodeRef of the container this belongs to (Site or Otherwise)
      */
     NodeRef getContainerNodeRef();
     
     /**
      * Gets the {@link ContentModel#PROP_NAME cm:name} of the blog post.
      */
     String getSystemName();
     
     /**
      * @return the Title of the blog post.
      */
     String getTitle();
     
     // TODO Remaining fields
 }