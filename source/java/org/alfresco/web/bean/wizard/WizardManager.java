package org.alfresco.web.bean.wizard;

import javax.faces.context.FacesContext;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.config.WizardsConfigElement.WizardConfig;

/**
 * Bean that manages the wizard framework
 * 
 * @author gavinc
 */
public class WizardManager
{
   protected WizardConfig currentWizardConfig;
   protected IWizardBean currentWizard;
   
   /**
    * Sets the current wizard
    * 
    * @param config The configuration for the wizard to set
    */
   public void setCurrentWizard(WizardConfig config)
   {
      this.currentWizardConfig = config;
      
      String beanName = this.currentWizardConfig.getManagedBean();
      this.currentWizard = (IWizardBean)FacesHelper.getManagedBean(
            FacesContext.getCurrentInstance(), beanName);
      
      if (this.currentWizard == null)
      {
         throw new AlfrescoRuntimeException("Failed to find managed bean '" + beanName + "'");
      }
      
      // initialise the managed bean
      this.currentWizard.init();
   }
   
   /**
    * Returns the current wizard bean being managed
    * 
    * @return The current managed bean
    */
   public IWizardBean getBean()
   {
      return this.currentWizard;
   }
}
