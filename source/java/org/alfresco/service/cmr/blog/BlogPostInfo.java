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
     
     /**
      * Sets the Title of the blog post.
      */
     void setTitle(String title);
     
     // TODO Remaining fields
     // TODO Tags
 }