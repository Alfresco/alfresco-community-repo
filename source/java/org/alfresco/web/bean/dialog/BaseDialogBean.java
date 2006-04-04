package org.alfresco.web.bean.dialog;

import java.text.MessageFormat;

import javax.faces.context.FacesContext;
import javax.transaction.UserTransaction;

import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.context.UIContextService;
import org.alfresco.web.bean.BrowseBean;
import org.alfresco.web.bean.NavigationBean;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;

/**
 * Base class for all dialog beans providing common functionality
 * 
 * @author gavinc
 */
public abstract class BaseDialogBean implements IDialogBean
{
   protected static final String ERROR_ID = "error_generic";
   
   // services common to most dialogs
   protected BrowseBean browseBean;
   protected NavigationBean navigator;
   protected NodeService nodeService;
   
   public void init()
   {
      // tell any beans to update themselves so the UI gets refreshed
      UIContextService.getInstance(FacesContext.getCurrentInstance()).notifyBeans();
   }
   
   public String cancel()
   {
      return getDefaultCancelOutcome();
   }
   
   public String finish()
   {
      String outcome = getDefaultFinishOutcome();
      
      UserTransaction tx = null;
   
      try
      {
         FacesContext context = FacesContext.getCurrentInstance();
         tx = Repository.getUserTransaction(context);
         tx.begin();
         
         // call the actual implementation
         outcome = finishImpl(context, outcome);
         
         tx.commit();
      }
      catch (Throwable e)
      {
         // rollback the transaction
         try { if (tx != null) {tx.rollback();} } catch (Exception ex) {}
         Utils.addErrorMessage(formatErrorMessage(e));
         outcome = null;
      }
      
      return outcome;
   }

   public String getCancelButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "cancel");
   }

   public String getFinishButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "ok");
   }
   
   public boolean getFinishButtonDisabled()
   {
      return true;
   }

   /**
    * @param browseBean The BrowseBean to set.
    */
   public void setBrowseBean(BrowseBean browseBean)
   {
      this.browseBean = browseBean;
   }
   
   /**
    * @param navigator The NavigationBean to set.
    */
   public void setNavigator(NavigationBean navigator)
   {
      this.navigator = navigator;
   }
   
   /**
    * @param nodeService The nodeService to set.
    */
   public void setNodeService(NodeService nodeService)
   {
      this.nodeService = nodeService;
   }
   
   /**
    * Returns the default cancel outcome
    * 
    * @return Default close outcome, dialog:close by default
    */
   protected String getDefaultCancelOutcome()
   {
      return AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME;
   }
   
   /**
    * Returns the default finish outcome
    * 
    * @return Default finish outcome, dialog:close by default
    */
   protected String getDefaultFinishOutcome()
   {
      return AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME;
   }
   
   /**
    * Performs the actual processing for the wizard.
    * NOTE: This method is called within the context of a transaction
    * so no transaction handling is required
    * 
    * @param context FacesContext
    * @param outcome The default outcome
    * @return The outcome
    */
   protected abstract String finishImpl(FacesContext context, String outcome)
      throws Exception;

   /**
    * Returns a formatted exception string for the given exception
    * 
    * @param exception The exception that got thrown
    * @return The formatted message
    */
   protected String formatErrorMessage(Throwable exception)
   {
      return MessageFormat.format(Application.getMessage(
            FacesContext.getCurrentInstance(), ERROR_ID), 
            exception.getMessage());
   }
}
