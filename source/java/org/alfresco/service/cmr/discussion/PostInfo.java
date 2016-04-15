package org.alfresco.service.cmr.discussion;

import java.io.Serializable;
import java.util.Date;

import org.alfresco.repo.security.permissions.PermissionCheckValue;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * This class represents a Post in a Forum Topic.
 * 
 * To retrieve replies to this, see 
 *  {@link DiscussionService#listPostReplies(PostInfo, int)}
 * 
 * @author Nick Burch
 * @since 4.0
 */
public interface PostInfo extends Serializable, PermissionCheckValue 
{
   /**
    * @return the NodeRef of the underlying post
    */
   NodeRef getNodeRef();
   
   /**
    * @return the {@link TopicInfo} representing the topic this belongs to
    */
   TopicInfo getTopic();
   
   /**
    * @return the System generated name for the post
    */
   String getSystemName();
   
   /**
    * @return the Title of the post (if set)
    */
   String getTitle();
   
   /**
    * Sets the Title of the post. Normally only the Primary Post
    *  in a Topic has a Title set.
    */
   void setTitle(String title);
   
   /**
    * @return the HTML Content of the post
    */
   String getContents();
   
   /**
    * Sets the (HTML) Content of the post
    */
   void setContents(String contentHTML);
   
   /**
    * @return the creator of the post
    */
   String getCreator();
   
   /**
    * @return the modifier of the wiki page
    */
   String getModifier();
   
   /**
    * @return the creation date and time
    */
   Date getCreatedAt();
   
   /**
    * @return the modification date and time
    */
   Date getModifiedAt();
   
   /**
    * @return the updated-at date and time
    */
   Date getUpdatedAt();
}
