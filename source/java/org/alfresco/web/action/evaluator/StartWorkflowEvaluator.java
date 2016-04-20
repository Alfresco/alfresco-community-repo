package org.alfresco.web.action.evaluator;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.BPMEngineRegistry;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.NavigationBean;
import org.alfresco.web.bean.repository.Node;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.jsf.FacesContextUtils;

/**
 * UI Action Evaluator - Start workflow on a node.
 * 
 * @author gavinc
 */
public class StartWorkflowEvaluator extends BaseActionEvaluator
{
   private static final String BPM_ENGINE_BEAN_NAME = "bpm_engineRegistry";
   private static final long serialVersionUID = 3110333488835027710L;

   /**
    * @see org.alfresco.web.action.ActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
    */
   public boolean evaluate(Node node)
   {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      NavigationBean nav =
         (NavigationBean)FacesHelper.getManagedBean(facesContext, NavigationBean.BEAN_NAME);
      
      // determine whether the workflow services are active
      boolean workflowPresent = false;
      WebApplicationContext springContext = FacesContextUtils.getRequiredWebApplicationContext(facesContext);
      BPMEngineRegistry bpmReg = (BPMEngineRegistry)springContext.getBean(BPM_ENGINE_BEAN_NAME);
      if (bpmReg != null)
      {
         String[] components = bpmReg.getWorkflowComponents();
         workflowPresent = (components != null && components.length > 0);
      }
      
      return (workflowPresent && nav.getIsGuest() == false && 
              node.hasAspect(ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION) == false);
   }
}
