package org.alfresco.web.action.evaluator;

import java.util.Map;

import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.web.bean.repository.Node;

/**
 * UI Action Evaluator - 'Approve' workflow step for document or space.
 * 
 * @author Kevin Roast
 */
public class ApproveDocEvaluator extends BaseActionEvaluator
{
   private static final long serialVersionUID = 2958297435415449179L;

   /**
    * @see org.alfresco.web.action.ActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
    */
   public boolean evaluate(Node node)
   {
      Map<String,Object> properties = node.getProperties();
      
      Boolean approveMove = (Boolean) properties.get("app:approveMove");
      boolean isMove = approveMove == null ? false : approveMove; 
      
      boolean canProceed = (properties.get("app:approveStep") != null) && !node.isLocked();
      //If this approval is going to result in a move of the node then we check whether the user
      //has permission. The delete permission is required in order to move a node (odd, perhaps, but true).
      canProceed &= (!isMove || node.hasPermission(PermissionService.DELETE));
      
      return canProceed;
   }
}
