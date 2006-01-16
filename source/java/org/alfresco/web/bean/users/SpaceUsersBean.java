package org.alfresco.web.bean.users;

import org.alfresco.web.bean.repository.Node;

/**
 * Concrete implementation providing access to users of the current space.
 * 
 * @author gavinc
 */
public class SpaceUsersBean extends UserMembersBean
{
   /**
    * @return The space to work against
    */
   public Node getNode()
   {
      return this.browseBean.getActionSpace();
   }
}
