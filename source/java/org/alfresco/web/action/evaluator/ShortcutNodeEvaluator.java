package org.alfresco.web.action.evaluator;

import javax.faces.context.FacesContext;

import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.NavigationBean;
import org.alfresco.web.bean.repository.Node;

/**
 * UI Action Evaluator - Create a shortcut to a node.
 * 
 * @author Kevin Roast
 */
public class ShortcutNodeEvaluator extends BaseActionEvaluator
{
   private static final long serialVersionUID = 8768692540125721144L;

   /**
    * @see org.alfresco.web.action.ActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
    */
   public boolean evaluate(Node node)
   {
      NavigationBean nav =
         (NavigationBean)FacesHelper.getManagedBean(FacesContext.getCurrentInstance(), NavigationBean.BEAN_NAME);
      return (nav.getIsGuest() == false);
   }
}
