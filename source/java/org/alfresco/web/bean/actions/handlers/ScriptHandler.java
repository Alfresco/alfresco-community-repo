package org.alfresco.web.bean.actions.handlers;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.repo.action.executer.ScriptActionExecutor;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.NavigationBean;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wizard.IWizardBean;

/**
 * Action handler for the "script" action.
 * 
 * @author gavinc
 */
public class ScriptHandler extends BaseActionHandler
{
   protected static final String PROP_SCRIPT = "script";
   
   public String getJSPPath()
   {
      return getJSPPath(ScriptActionExecutor.NAME);
   }

   public void prepareForSave(Map<String, Serializable> actionProps,
         Map<String, Serializable> repoProps)
   {
      // add the selected script noderef to the action properties
      String id = (String)actionProps.get(PROP_SCRIPT);
      NodeRef scriptRef = new NodeRef(Repository.getStoreRef(), id);
      repoProps.put(ScriptActionExecutor.PARAM_SCRIPTREF, scriptRef);
      
      NavigationBean navBean = (NavigationBean)FacesHelper.getManagedBean(
            FacesContext.getCurrentInstance(), "NavigationBean");
      repoProps.put(ScriptActionExecutor.PARAM_SPACEREF, 
            navBean.getCurrentNode().getNodeRef());
   }

   public void prepareForEdit(Map<String, Serializable> actionProps,
         Map<String, Serializable> repoProps)
   {
      NodeRef scriptRef = (NodeRef)repoProps.get(ScriptActionExecutor.PARAM_SCRIPTREF);
      actionProps.put(PROP_SCRIPT, scriptRef.getId());
   }

   public String generateSummary(FacesContext context, IWizardBean wizard,
         Map<String, Serializable> actionProps)
   {
      String id = (String)actionProps.get(PROP_SCRIPT);
      NodeRef scriptRef = new NodeRef(Repository.getStoreRef(), id);
      String scriptName = Repository.getNameForNode(
            Repository.getServiceRegistry(context).getNodeService(), scriptRef);
      
      return MessageFormat.format(Application.getMessage(context, "action_script"),
            new Object[] {scriptName});
   }
}
