package org.alfresco.web.bean;

import javax.faces.context.FacesContext;

import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Repository;

/**
 * Simple backing bean used by the about page to display the version.
 * 
 * @author gavinc
 */
public class AboutBean extends BaseDialogBean
{
   private static final long serialVersionUID = -3777479360531145878L;
   
   private final static String MSG_VERSION = "version";
   private final static String MSG_CLOSE = "close";
   
   transient private DescriptorService descriptorService;
   
   /**
    * Retrieves the version of the repository.
    * 
    * @return The version string
    */
   public String getVersion()
   {
      return this.getDescriptorService().getServerDescriptor().getVersion();
   }
   
   /**
    * Retrieves the edition of the repository.
    * 
    * @return The edition
    */
   public String getEdition()
   {
      return this.getDescriptorService().getServerDescriptor().getEdition();
   }
   
   /**
    * Sets the DescriptorService.
    * 
    * @param descriptorService The DescriptorService
    */
   public void setDescriptorService(DescriptorService descriptorService)
   {
      this.descriptorService = descriptorService;
   }

   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      return null;
   }
   
   @Override
   public String getContainerDescription()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), MSG_VERSION) + " :" + getEdition() + " - v" + getVersion();
   }

   DescriptorService getDescriptorService()
   {
      //check for null in cluster environment  
      if (descriptorService == null)
      {
         descriptorService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getDescriptorService();
      }
      return descriptorService;
   }
   
   @Override
   public String getCancelButtonLabel()
   {
    
      return Application.getMessage(FacesContext.getCurrentInstance(), MSG_CLOSE);
   }
   
   @Override
   protected String getDefaultCancelOutcome() 
   {
	  return "dialog:close:browse";
   }
}
