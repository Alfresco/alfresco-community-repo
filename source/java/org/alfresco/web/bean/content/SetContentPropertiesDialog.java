package org.alfresco.web.bean.content;

import org.alfresco.web.app.AlfrescoNavigationHandler;

/**
 * Bean implementation of the "Set Content Properties" dialog.
 * 
 * @author gavinc
 */
public class SetContentPropertiesDialog extends EditContentPropertiesDialog
{
   private static final long serialVersionUID = -7705362669371767349L;

   @Override
   protected String getDefaultCancelOutcome()
   {
      return super.getDefaultCancelOutcome() + 
             AlfrescoNavigationHandler.OUTCOME_SEPARATOR + 
             "browse";
   }

   @Override
   protected String getDefaultFinishOutcome()
   {
      return super.getDefaultFinishOutcome() + 
             AlfrescoNavigationHandler.OUTCOME_SEPARATOR + 
             "browse";
   }
}
