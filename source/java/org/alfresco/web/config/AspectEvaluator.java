package org.alfresco.web.config;

import java.util.Set;

import org.springframework.extensions.config.evaluator.Evaluator;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;

/**
 * Evaluator that determines whether a given object has a particular aspect applied
 * 
 * @author gavinc
 */
public final class AspectEvaluator implements Evaluator
{
   /**
    * Determines whether the given aspect is applied to the given object
    * 
    * @see org.springframework.extensions.config.evaluator.Evaluator#applies(java.lang.Object, java.lang.String)
    */
   public boolean applies(Object obj, String condition)
   {
      boolean result = false;
      
      if (obj instanceof Node)
      {
         Set aspects = ((Node)obj).getAspects();
         if (aspects != null)
         {
            QName spaceQName = Repository.resolveToQName(condition);
            result = aspects.contains(spaceQName);
         }
      }
      
      return result;
   }
}
