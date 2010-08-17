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
