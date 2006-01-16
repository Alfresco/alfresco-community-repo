package org.alfresco.web.bean.users;

import org.alfresco.web.bean.repository.Node;

/**
 * Concrete implementation providing access to users of the current content/document.
 * 
 * @author gavinc
 */
public class ContentUsersBean extends UserMembersBean
{
   /**
    * @return The space to work against
    */
   public Node getNode()
   {
      return this.browseBean.getDocument();
   }
}
