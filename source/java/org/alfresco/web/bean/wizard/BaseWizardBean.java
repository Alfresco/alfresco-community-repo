package org.alfresco.web.bean.wizard;

import javax.faces.context.FacesContext;

import org.alfresco.web.app.Application;
import org.alfresco.web.app.context.UIContextService;


/**
 * Base class for all wizard beans providing common functionality
 * 
 * @author gavinc
 */
public abstract class BaseWizardBean implements IWizardBean
{
   protected static final String WIZARD_CLOSE = "wizard:close";
   
   public abstract String finish();
   
   public void init()
   {
      // tell any beans to update themselves so the UI gets refreshed
      UIContextService.getInstance(FacesContext.getCurrentInstance()).notifyBeans();
   }

   public boolean getNextButtonDisabled()
   {
      return true;
   }

   public boolean getBackButtonDisabled()
   {
      return true;
   }
   
   public boolean getFinishButtonDisabled()
   {
      return true;
   }
   
   public String getNextButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "next_button");
   }

   public String getBackButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "back_button");
   }

   public String getFinishButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "finish_button");
   }
   
   public String cancel()
   {
      return WIZARD_CLOSE;
   }
}
