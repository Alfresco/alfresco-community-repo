package org.alfresco.web.bean;

import org.alfresco.service.descriptor.DescriptorService;

/**
 * Simple backing bean used by the about page to display the version.
 * 
 * @author gavinc
 */
public class AboutBean
{
   DescriptorService descriptorService;
   
   /**
    * Retrieves the version of the repository.
    * 
    * @return The version string
    */
   public String getVersion()
   {
      return this.descriptorService.getServerDescriptor().getVersion();
   }
   
   /**
    * Retrieves the edition of the repository.
    * 
    * @return The edition
    */
   public String getEdition()
   {
      return this.descriptorService.getServerDescriptor().getEdition();
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
}
