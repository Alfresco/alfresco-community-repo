/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.web.bean.dialog;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.context.UIContextService;
import org.alfresco.web.bean.BrowseBean;
import org.alfresco.web.bean.NavigationBean;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.config.DialogsConfigElement.DialogButtonConfig;
import org.alfresco.web.ui.common.ReportedException;
import org.alfresco.web.ui.common.Utils;

/**
 * Base class for all dialog beans providing common functionality
 * 
 * @author gavinc
 */
@SuppressWarnings("serial")
public abstract class BaseDialogBean implements IDialogBean, Serializable
{
   protected Map<String, String> parameters;
   protected boolean isFinished = false;
   
   // services common to most dialogs
   protected BrowseBean browseBean;
   protected NavigationBean navigator;
   
   transient private TransactionService transactionService;
   transient private NodeService nodeService;
   transient private CheckOutCheckInService checkOutCheckInService;
   transient private FileFolderService fileFolderService;
   transient private SearchService searchService;
   transient private DictionaryService dictionaryService;
   transient private NamespaceService namespaceService;
   transient private RuleService ruleService;
   
   public void init(Map<String, String> parameters)
   {
      // tell any beans to update themselves so the UI gets refreshed
      UIContextService.getInstance(FacesContext.getCurrentInstance()).notifyBeans();
      
      // store the parameters, create empty map if necessary
      this.parameters = parameters;
      
      if (this.parameters == null)
      {
         this.parameters = Collections.<String, String>emptyMap();
      }
      
      // reset the isFinished flag
      this.isFinished = false;
   }
   
   public void restored()
   {
      // do nothing by default, subclasses can override if necessary
   }
   
   public String cancel()
   {
      // remove container variable
      FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove(
                AlfrescoNavigationHandler.EXTERNAL_CONTAINER_SESSION);
      
      return getDefaultCancelOutcome();
   }
   
   public String finish()
   {
      final FacesContext context = FacesContext.getCurrentInstance();
      final String defaultOutcome = getDefaultFinishOutcome();
      String outcome = null;
      
      // check the isFinished flag to stop the finish button
      // being pressed multiple times
      if (this.isFinished == false)
      {
         this.isFinished = true;
      
         RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(context);
         RetryingTransactionCallback<String> callback = new RetryingTransactionCallback<String>()
         {
            public String execute() throws Throwable
            {
               // call the actual implementation
               return finishImpl(context, defaultOutcome);
            }
         };
         try
         {
            // Execute
            outcome = txnHelper.doInTransaction(callback, false, true);
            
            // allow any subclasses to perform post commit processing 
            // i.e. resetting state or setting status messages
            outcome = doPostCommitProcessing(context, outcome);
            
            // remove container variable
            context.getExternalContext().getSessionMap().remove(
                    AlfrescoNavigationHandler.EXTERNAL_CONTAINER_SESSION);
         }
         catch (Throwable e)
         {
            // reset the flag so we can re-attempt the operation
            isFinished = false;
            outcome = getErrorOutcome(e);
            if (e instanceof ReportedException == false)
            {
                Utils.addErrorMessage(formatErrorMessage(e), e);
            }
            ReportedException.throwIfNecessary(e);
         }
      }
      else
      {
         Utils.addErrorMessage(Application.getMessage(context, "error_wizard_completed_already"));
      }
      
      return outcome;
   }
   
   public boolean isFinished()
   {
      return isFinished;
   }
   
   public List<DialogButtonConfig> getAdditionalButtons()
   {
      // none by default, subclasses can override if necessary
      
      return null;
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

   public String getContainerTitle()
   {
      // nothing by default, subclasses can override if necessary
      
      return null;
   }
   
   public String getContainerSubTitle()
   {
      // nothing by default, subclasses can override if necessary
      
      return null;
   }
   
   public String getContainerDescription()
   {
      // nothing by default, subclasses can override if necessary
      
      return null;
   }
   
   public Object getActionsContext()
   {
      // return the current node as the context for actions be default
      // dialog implementations can override this method to return the
      // appropriate object for their use case
      
      if (this.navigator == null)
      {
         throw new AlfrescoRuntimeException("To use actions in the dialog the 'navigator' " +
                  "property must be injected with an instance of NavigationBean!");
      }
      
      return this.navigator.getCurrentNode();
   }

   public String getActionsConfigId()
   {
      // nothing by default, subclasses can override if necessary
      
      return null;
   }

   public String getMoreActionsConfigId()
   {
      // nothing by default, subclasses can override if necessary
      
      return null;
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
   
   protected TransactionService getTransactionService()
   {
      if (this.transactionService == null)
      {
         this.transactionService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getTransactionService();
      }
      return this.transactionService;
   }
   
   /**
    * @param nodeService The nodeService to set.
    */
   public void setNodeService(NodeService nodeService)
   {
      this.nodeService = nodeService;
   }
   
   protected NodeService getNodeService()
   {
      if (this.nodeService == null)
      {
         this.nodeService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getNodeService();
      }
      return this.nodeService;
   }
   
   protected CheckOutCheckInService getCheckOutCheckInService()
   {
      if (this.checkOutCheckInService == null)
      {
         this.checkOutCheckInService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getCheckOutCheckInService();
      }
      return this.checkOutCheckInService;
   }
   
   /**
    * Get the rule service
    * @return	RuleService	rule service
    */
   protected RuleService getRuleService()
   {
	   if (ruleService == null)
	   {
		   ruleService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getRuleService();
	   }
	   return ruleService;
   }
   
   /**
    * @param fileFolderService used to manipulate folder/folder model nodes
    */
   public void setFileFolderService(FileFolderService fileFolderService)
   {
      this.fileFolderService = fileFolderService;
   }
   
   protected FileFolderService getFileFolderService()
   {
      if (this.fileFolderService == null)
      {
         this.fileFolderService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getFileFolderService();
      }
      return this.fileFolderService;
   }

   /**
    * @param searchService the service used to find nodes
    */
   public void setSearchService(SearchService searchService)
   {
      this.searchService = searchService;
   }
   
   protected SearchService getSearchService()
   {
      if (this.searchService == null)
      {
         this.searchService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getSearchService();
      }
      return this.searchService;
   }
   
   /**
    * Sets the dictionary service
    * 
    * @param dictionaryService  the dictionary service
    */
   public void setDictionaryService(DictionaryService dictionaryService)
   {
      this.dictionaryService = dictionaryService;
   }
   
   protected DictionaryService getDictionaryService()
   {
      if (this.dictionaryService == null)
      {
         this.dictionaryService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getDictionaryService();
      }
      return this.dictionaryService;
   }
   
   /**
    * @param namespaceService The NamespaceService
    */
   public void setNamespaceService(NamespaceService namespaceService)
   {
      this.namespaceService = namespaceService;
   }
   
   protected NamespaceService getNamespaceService()
   {
      if (this.namespaceService == null)
      {
         this.namespaceService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getNamespaceService();
      }
      return this.namespaceService;
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
      throws Throwable;

   /**
    * Performs any post commit processing subclasses may want to provide
    * 
    * @param context FacesContext
    * @param outcome The default outcome
    * @return The outcome
    */
   protected String doPostCommitProcessing(FacesContext context, String outcome)
   {
      // do nothing by default, subclasses can override if necessary
      
      return outcome;
   }
   
   /**
    * The default message id to use in error messages
    * 
    * @return The error message lookup id
    */
   protected String getErrorMessageId()
   {
      return Repository.ERROR_GENERIC;
   }
   
   /**
    * The outcome to return if the given exception occurs
    * 
    * @param exception The exception that got thrown
    * @return The error outcome, null by default
    */
   protected String getErrorOutcome(Throwable exception)
   {
      return null;
   }
   
   /**
    * Returns a formatted exception string for the given exception
    * 
    * @param exception The exception that got thrown
    * @return The formatted message
    */
   protected String formatErrorMessage(Throwable exception)
   {
      return MessageFormat.format(Application.getMessage(
            FacesContext.getCurrentInstance(), getErrorMessageId()), 
            exception.getMessage());
   }

}
