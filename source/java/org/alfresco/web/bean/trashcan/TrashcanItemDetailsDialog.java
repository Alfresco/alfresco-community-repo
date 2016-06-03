package org.alfresco.web.bean.trashcan;

import javax.faces.context.FacesContext;

import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;

public class TrashcanItemDetailsDialog extends TrashcanDialog
{
   private static final long serialVersionUID = 1767515883530860417L;
   
   private static final String MSG_DETAILS_OF = "details_of";
   private static final String MSG_CLOSE = "close";
   private final static String MSG_LEFT_QUOTE = "left_qoute";
   private final static String MSG_RIGHT_QUOTE = "right_quote";
   
   transient private PermissionService permissionService;

   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      return null;
   }

   @Override
   public String getContainerTitle()
   {
       FacesContext fc = FacesContext.getCurrentInstance();
       return Application.getMessage(fc, MSG_DETAILS_OF) + " " + Application.getMessage(fc, MSG_LEFT_QUOTE) + 
             property.getItem().getName() + Application.getMessage(fc, MSG_RIGHT_QUOTE);
   }

   @Override
   public String getCancelButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), MSG_CLOSE);
   }
   
   @Override
   public Object getActionsContext()
   {
      return getItem();
   }

   public Node getItem()
   {
      return property.getItem();
   }
   
   public void setPermissionService(PermissionService permissionService)
   {
      this.permissionService = permissionService;
   }
   
   /**
    *@return permissionService
    */
   protected PermissionService getPermissionService()
   {
    //check for null for cluster environment
      if (permissionService == null)
      {
         permissionService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getPermissionService();
      }
      return permissionService;
   }
}
