package org.alfresco.web.app.servlet.command;

import java.util.Map;

import javax.servlet.ServletContext;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.web.bean.repository.Repository;

/**
 * Initial implementation of a Command Processor that is always passed enough URL elements
 * to construct a single NodeRef argument. The NodeRef is checked against READ permissions
 * for the current user during the validateArguments() call.
 * <p>
 * This class should be enough to form the base of Command Processor objects that only require
 * a single NodeRef passed on the URL. 
 * 
 * @author Kevin Roast
 */
public abstract class BaseNodeCommandProcessor implements CommandProcessor
{
   protected NodeRef targetRef;
   
   /**
    * @see org.alfresco.web.app.servlet.command.CommandProcessor#validateArguments(javax.servlet.ServletContext, java.lang.String, java.util.Map, java.lang.String[])
    */
   public boolean validateArguments(ServletContext sc, String command, Map<String, String> args, String[] urlElements)
   {
      if (urlElements.length < 3)
      {
         throw new IllegalArgumentException("Not enough URL arguments passed to command servlet.");
      }
      
      // get NodeRef to the node with the workflow attached to it
      StoreRef storeRef = new StoreRef(urlElements[0], urlElements[1]);
      this.targetRef = new NodeRef(storeRef, urlElements[2]);
      
      // get the services we need to execute the workflow command
      PermissionService permissionService = Repository.getServiceRegistry(sc).getPermissionService();
      
      // check that the user has at least READ access on the node - else redirect to the login page
      return (permissionService.hasPermission(this.targetRef, PermissionService.READ) == AccessStatus.ALLOWED);
   }
}
