package org.alfresco.web.action.evaluator;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.users.UserPreferencesBean;
import org.alfresco.web.bean.repository.Node;

/**
 * Evaluates whether the Make Multilingual action should be visible. 
 * 
 * If the node is already Multilingual don't allow the action.
 * 
 * @author Yannick Pignot
 */
public class MakeMultilingualEvaluator extends BaseActionEvaluator
{
   private static final long serialVersionUID = 4417014487557744219L;

   public boolean evaluate(Node node)
   {
      FacesContext fc = FacesContext.getCurrentInstance();

      UserPreferencesBean userprefs = 
         (UserPreferencesBean) FacesHelper.getManagedBean(fc, "UserPreferencesBean");

      // the total number of available languages for the translation have to be greather that 0
      int contentFilterLanguagesCount  = userprefs.getContentFilterLanguages(false).length;

      return (node.isLocked() == false &&
              node.hasAspect(ContentModel.ASPECT_WORKING_COPY) == false &&
              node.hasAspect(ContentModel.ASPECT_MULTILINGUAL_DOCUMENT) == false &&
              contentFilterLanguagesCount > 0);
   }
}