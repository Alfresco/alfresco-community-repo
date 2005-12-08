/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
