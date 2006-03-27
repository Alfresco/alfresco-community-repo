package org.alfresco.web.bean.dialog;

import javax.faces.context.FacesContext;

import org.alfresco.web.app.Application;
import org.alfresco.web.app.context.UIContextService;

/**
 * Base class for all dialog beans providing common functionality
 * 
 * @author gavinc
 */
public abstract class BaseDialogBean implements IDialogBean
{
   protected static final String DIALOG_CLOSE = "dialog:close";

   public abstract String finish();
   
   public void init()
   {
      // tell any beans to update themselves so the UI gets refreshed
      UIContextService.getInstance(FacesContext.getCurrentInstance()).notifyBeans();
   }
   
   public String cancel()
   {
      return DIALOG_CLOSE;
   }

   public boolean getFinishButtonDisabled()
   {
      return true;
   }

   public String getFinishButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "ok");
   }
}
