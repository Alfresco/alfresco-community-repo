package org.alfresco.web.config;

import org.springframework.extensions.config.evaluator.Evaluator;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;

/**
 * Evaluator that determines whether a given object has a particular node type
 * 
 * @author gavinc
 */
public class NodeTypeEvaluator implements Evaluator
{
   /**
    * Determines whether the given node type matches the path of the given object
    * 
    * @see org.springframework.extensions.config.evaluator.Evaluator#applies(java.lang.Object, java.lang.String)
    */
   public boolean applies(Object obj, String condition)
   {
      boolean result = false;
      
      if (obj instanceof Node)
      {
         QName type = ((Node)obj).getType();
         if (type != null)
         {
            result = type.equals(Repository.resolveToQName(condition));
         }
      }
      
      return result;
   }

}
