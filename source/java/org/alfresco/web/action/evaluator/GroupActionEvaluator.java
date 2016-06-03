package org.alfresco.web.action.evaluator;

import java.util.Map;

import org.alfresco.web.bean.groups.GroupsDialog;


/**
 * Evaluator that determines whether the add group user action should
 * be visible - only visible when the dialog is not showing the root
 * group.
 * 
 * @author Gavin Cornwell
 */
public class GroupActionEvaluator extends BaseActionEvaluator
{
   @Override
   public boolean evaluate(Object obj)
   {
      boolean result = true;
      
      if (obj instanceof GroupsDialog)
      {
         // if the object is the GroupsDialog check whether the group is null,
         // if it is it represents the root group so disallow the action
         result = (((GroupsDialog)obj).getGroup() != null);
      }
      else if (obj instanceof Map)
      {
         // if the object is a Map retrieve the group and check for null,
         // if it is it represents the root group so disallow the action
         Object group = ((Map)obj).get(GroupsDialog.PARAM_GROUP);
         result = (group != null);
      }
      else
      {
         result = super.evaluate(obj);
      }
      
      return result;
   }
}
