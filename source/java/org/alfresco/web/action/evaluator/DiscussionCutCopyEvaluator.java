package org.alfresco.web.action.evaluator;

import javax.faces.context.FacesContext;

import org.alfresco.model.ForumModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.action.ActionEvaluator;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;

/**
 * Evaluates whether the cut or copy action should be visible. 
 * 
 * If the node is a discussion don't allow the action.
 * 
 * @author gavinc
 */
public class DiscussionCutCopyEvaluator implements ActionEvaluator
{
   /**
    * @see org.alfresco.web.action.ActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
    */
   public boolean evaluate(Node node)
   {
      boolean result = true;
      
      // if the node in question is a forum...
      if (node.getType().equals(ForumModel.TYPE_FORUM))
      {
         // get the association type
         FacesContext context = FacesContext.getCurrentInstance();
         NodeService nodeService = Repository.getServiceRegistry(context).getNodeService();
         
         ChildAssociationRef parentAssoc = nodeService.getPrimaryParent(node.getNodeRef());
         QName assocType = parentAssoc.getTypeQName();
         
         // only allow the action if the association type is not the discussion assoc
         result = (assocType.equals(ForumModel.ASSOC_DISCUSSION) == false);
      }
      
      return result;
   }
}
