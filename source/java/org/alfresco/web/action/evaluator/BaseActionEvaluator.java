package org.alfresco.web.action.evaluator;

import javax.faces.context.FacesContext;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.web.action.ActionEvaluator;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;

/**
 * Base class for all action evaluators.
 * 
 * @author gavinc
 */
public class BaseActionEvaluator implements ActionEvaluator
{
   public boolean evaluate(Node node)
   {
      // Allow the action by default
      
      return true;
   }
   
   public boolean evaluate(final Object obj)
   {
      // if a Node object is passed to this method call
      // the explicit evaluate(Node) method otherwise
      // allow the action by default.
      
      if (obj instanceof Node)
      {
         RetryingTransactionCallback<Boolean> txnCallback = new RetryingTransactionCallback<Boolean>()
         {
            @Override
            public Boolean execute() throws Throwable
            {
               return evaluate((Node)obj);
            }
         };
         TransactionService txnService =
             Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getTransactionService();
         return txnService.getRetryingTransactionHelper().doInTransaction(txnCallback, true, true);
      }
      else
      {
         return true;
      }
   }
}