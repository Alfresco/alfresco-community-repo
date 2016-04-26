package org.alfresco.web.bean.actions.handlers;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.repo.action.executer.CopyActionExecuter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wizard.IWizardBean;

/**
 * Action handler implementation for the "copy" action.
 * 
 * @author gavinc
 */
public class CopyHandler extends BaseActionHandler
{
   private static final long serialVersionUID = -3570705279828902437L;
   
   private static final String PROP_DEEPCOPY = "deepCopy";
   
   
   public String getJSPPath()
   {
      return getJSPPath(CopyActionExecuter.NAME);
   }

   public void prepareForSave(Map<String, Serializable> actionProps,
         Map<String, Serializable> repoProps)
   {
      // add the destination space id to the action properties
      NodeRef destNodeRef = (NodeRef)actionProps.get(PROP_DESTINATION);
      repoProps.put(CopyActionExecuter.PARAM_DESTINATION_FOLDER, destNodeRef);
      
      // deep copy option
      Boolean deepCopy = (Boolean)actionProps.get(PROP_DEEPCOPY);
      repoProps.put(CopyActionExecuter.PARAM_DEEP_COPY, deepCopy);
   }

   public void prepareForEdit(Map<String, Serializable> actionProps,
         Map<String, Serializable> repoProps)
   {
      NodeRef destNodeRef = (NodeRef)repoProps.get(CopyActionExecuter.PARAM_DESTINATION_FOLDER);
      actionProps.put(PROP_DESTINATION, destNodeRef);
      
      Boolean deepCopy = (Boolean)repoProps.get(CopyActionExecuter.PARAM_DEEP_COPY);
      actionProps.put(PROP_DEEPCOPY, deepCopy);
   }

   public String generateSummary(FacesContext context, IWizardBean wizard,
         Map<String, Serializable> actionProps)
   {
      NodeRef space = (NodeRef)actionProps.get(PROP_DESTINATION);
      String spaceName = Repository.getNameForNode(
            Repository.getServiceRegistry(context).getNodeService(), space);
      
      return MessageFormat.format(Application.getMessage(context, "action_copy"),
            new Object[] {spaceName});
   }
}
