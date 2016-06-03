package org.alfresco.web.action.evaluator;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.web.bean.repository.Node;

/**
 * UI Action Evaluator - Allow comment editing only to coordinators, site managers, creator and owner
 */
public class EditPostEvaluator extends BaseActionEvaluator
{
   private static final long serialVersionUID = -5544290216536965941L;

   /**
    * @see org.alfresco.web.action.ActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
    */
   public boolean evaluate(Node node)
   {
      String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
      return (currentUser.equalsIgnoreCase((String)node.getProperties().get(ContentModel.PROP_OWNER)) ||
              currentUser.equalsIgnoreCase((String)node.getProperties().get(ContentModel.PROP_CREATOR)) ||
              node.hasPermission(SiteModel.SITE_MANAGER) ||
              node.hasPermission(PermissionService.COORDINATOR));
   }
}
