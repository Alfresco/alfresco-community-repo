package org.alfresco.service.cmr.discussion;

import java.util.List;

import org.alfresco.repo.security.permissions.PermissionCheckValue;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * This class holds a post and all replies to it, possibly nested.
 * 
 * This is used with {@link DiscussionService#listPostReplies(PostInfo, int)}
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class PostWithReplies implements PermissionCheckValue 
{
   private PostInfo post;
   private List<PostWithReplies> replies;
   
   public PostWithReplies(PostInfo post, List<PostWithReplies> replies)
   {
      this.post = post;
      this.replies = replies;
   }

   public PostInfo getPost() 
   {
      return post;
   }

   public List<PostWithReplies> getReplies() 
   {
      return replies;
   }

   @Override
   public NodeRef getNodeRef() 
   {
      return post.getNodeRef();
   }
}
