package org.alfresco.web.config;

import org.springframework.extensions.config.evaluator.Evaluator;
import org.alfresco.web.bean.repository.Node;

/**
 * Evaluator that determines whether a given object has a particular path
 * 
 * @author gavinc
 */
public class PathEvaluator implements Evaluator
{
   /**
    * Determines whether the given path matches the path of the given object
    * 
    * @see org.springframework.extensions.config.evaluator.Evaluator#applies(java.lang.Object, java.lang.String)
    */
   public boolean applies(Object obj, String condition)
   {
      boolean result = false;

      // TODO: Also deal with NodeRef object's being passed in
      
      if (obj instanceof Node)
      {
         String path = (String)((Node)obj).getPath();
         if (path != null)
         {
            result = path.equalsIgnoreCase(condition);
         }
      }
      
      return result;
   }

}
