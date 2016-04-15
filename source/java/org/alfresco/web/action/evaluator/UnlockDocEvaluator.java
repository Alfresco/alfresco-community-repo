package org.alfresco.web.action.evaluator;

import javax.faces.context.FacesContext;

import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.repository.Node;

/**
 * UI Action Evaluator - Unlock a locked document.
 * 
 * @author Kevin Roast
 */
public class UnlockDocEvaluator extends BaseActionEvaluator
{
   private static final long serialVersionUID = -7056759932698306087L;

   /**
    * @see org.alfresco.web.action.ActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
    */
   public boolean evaluate(Node node)
   {
      if (node.isLocked())
      {
         FacesContext fc = FacesContext.getCurrentInstance();
         CheckOutCheckInService checkOutCheckInService =  (CheckOutCheckInService) FacesHelper.getManagedBean(fc, "CheckoutCheckinService");
         if (checkOutCheckInService.getWorkingCopy(node.getNodeRef()) == null)
         {
            return true;
         }
      }
       
      return false;
   }
}
