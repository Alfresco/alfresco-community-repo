package org.alfresco.repo.scheduler;

import org.quartz.spi.TriggerFiredBundle;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

/**
 * A special Job Factory that is based on the usual {@link SpringBeanJobFactory},
 *  but also handles {@link ApplicationContextAware} job beans.
 * 
 * @author Nick Burch
 */
public class AlfrescoJobFactory extends SpringBeanJobFactory implements ApplicationContextAware
{
   private ApplicationContext context;

   @Override
   public void setApplicationContext(ApplicationContext applicationContext) 
   {
      this.context = applicationContext;
   }

   @Override
   protected Object createJobInstance(TriggerFiredBundle bundle)
         throws Exception {
      Object job = super.createJobInstance(bundle);
      if(job instanceof ApplicationContextAware)
      {
         ((ApplicationContextAware)job).setApplicationContext(context);
      }
      return job;
   }
   
}
