package org.alfresco.service.cmr.discussion;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.alfresco.repo.security.permissions.PermissionCheckValue;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * This class represents a Topic in a forum.
 * 
 * To retrieve either the Primary Post, or all Posts,
 *  use {@link DiscussionService#getPrimaryPost(TopicInfo)}
 *  and {@link DiscussionService#listPostReplies(TopicInfo, int)}
 *
 * @author Nick Burch
 * @since 4.0
 */
public interface TopicInfo extends Serializable, PermissionCheckValue 
{
   /**
    * @return the NodeRef of the underlying topic
    */
   NodeRef getNodeRef();
   
   /**
    * @return the NodeRef of the container this belongs to (Site or Otherwise)
    */
   NodeRef getContainerNodeRef();
   
   /**
    * @return the System generated name for the topic
    */
   String getSystemName();
   
   /**
    * @return the Title of the topic.
    */
   String getTitle();
   
   /**
    * Sets the Title of the topic. The Title of the
    *  topic will be shared with the Primary Post
    */
   void setTitle(String title);
   
   /**
    * @return the creator of the topic
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
    * @return the Tags associated with the topic 
    */
   List<String> getTags();
   
   /**
    * @return the site this topic is associated with
    */
   String getShortSiteName();
}
