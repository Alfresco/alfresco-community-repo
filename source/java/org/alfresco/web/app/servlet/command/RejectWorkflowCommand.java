package org.alfresco.web.app.servlet.command;

import java.util.Map;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.bean.workflow.WorkflowUtil;

/**
 * Reject Workflow command implementation
 * 
 * @author Kevin Roast
 */
public final class RejectWorkflowCommand implements Command
{
   public static final String PROP_TARGET = "target";
   
   private static final String[] PROPERTIES = new String[] {PROP_TARGET};
   
   /**
    * @see org.alfresco.web.app.servlet.command.Command#getPropertyNames()
    */
   public String[] getPropertyNames()
   {
      return PROPERTIES;
   }
   
   /**
    * @see org.alfresco.web.app.servlet.command.Command#execute(org.alfresco.service.ServiceRegistry, java.util.Map)
    */
   public Object execute(ServiceRegistry serviceRegistry, Map<String, Object> properties)
   {
      // get the target Node for the command
      NodeRef nodeRef = (NodeRef)properties.get(PROP_TARGET);
      if (nodeRef == null)
      {
         throw new IllegalArgumentException(
               "Unable to execute RejectCommand - mandatory parameter not supplied: " + PROP_TARGET);
      }
      
      WorkflowUtil.reject(nodeRef, serviceRegistry.getNodeService(), serviceRegistry.getCopyService());
      
      return true;
   }
}
