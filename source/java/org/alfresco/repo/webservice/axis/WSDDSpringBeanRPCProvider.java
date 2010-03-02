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
package org.alfresco.repo.webservice.axis;

import org.apache.axis.EngineConfiguration;
import org.apache.axis.Handler;
import org.apache.axis.deployment.wsdd.WSDDProvider;
import org.apache.axis.deployment.wsdd.WSDDService;

/**
 * Provider class loaded by Axis, used to identify and 
 * create an instance of our SpringRPC provider which in
 * turn loads service endpoints from Spring configured beans
 * 
 * @see org.alfresco.repo.webservice.axis.SpringBeanRPCProvider
 * @author gavinc
 */
public class WSDDSpringBeanRPCProvider extends WSDDProvider
{
   private static final String PROVIDER_NAME = "SpringRPC"; 
   
   /**
    * @see org.apache.axis.deployment.wsdd.WSDDProvider#newProviderInstance(org.apache.axis.deployment.wsdd.WSDDService, org.apache.axis.EngineConfiguration)
    */
   @Override
   public Handler newProviderInstance(WSDDService service, EngineConfiguration registry) 
      throws Exception
   {
      return new SpringBeanRPCProvider();
   }

   /**
    * @see org.apache.axis.deployment.wsdd.WSDDProvider#getName()
    */
   @Override
   public String getName()
   {
      return PROVIDER_NAME;
   }

}
