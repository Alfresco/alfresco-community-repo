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
package org.alfresco.repo.transfer.script;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.jscript.ClasspathScriptLocation;
import org.alfresco.repo.transfer.TransferTargetImpl;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.ScriptLocation;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.transfer.TransferDefinition;
import org.alfresco.service.cmr.transfer.TransferException;
import org.alfresco.service.cmr.transfer.TransferService;
import org.alfresco.service.cmr.transfer.TransferTarget;
import org.alfresco.test_category.BaseSpringTestsCategory;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.BaseAlfrescoSpringTest;
import org.junit.experimental.categories.Category;
import org.mockito.Matchers;
import org.mockito.Mockito;

/**
 *  Script Transfer unit test
 * 
 * @author Mark Rogers
 */
@Category(BaseSpringTestsCategory.class)
public class ScriptTransferServiceTest extends BaseAlfrescoSpringTest 
{
    
//    String COMPANY_HOME_XPATH_QUERY = "/{http://www.alfresco.org/model/application/1.0}company_home";
//    String GUEST_HOME_XPATH_QUERY = "/{http://www.alfresco.org/model/application/1.0}company_home/{http://www.alfresco.org/model/application/1.0}guest_home";
    
    private ScriptService scriptService;
    private ScriptTransferService scriptTransferService;
   
    
    @SuppressWarnings("deprecation")
    protected void onSetUpInTransaction() throws Exception
    {
        super.onSetUpInTransaction();
        
        // Get the required services
        this.scriptService = (ScriptService)this.applicationContext.getBean("ScriptService");
        this.scriptTransferService = (ScriptTransferService)this.applicationContext.getBean("transferServiceScript");
     
    }   
    
  // == Test the JavaScript API ==
  
  public void testJSAPI() throws Exception
  {
      /**
       * Prepare some dummy data for tests
       */
      TransferTargetImpl dummyTarget = new TransferTargetImpl();
      dummyTarget.setName("dummyTarget");
      dummyTarget.setNodeRef(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "4"));
      
      Set<TransferTarget>dummyTargets = new HashSet<TransferTarget>();
      dummyTargets.add(dummyTarget);
      
      TransferService mockedTransferService = Mockito.mock(TransferService.class);
      scriptTransferService.setTransferService(mockedTransferService);
      
      // When the transfer method is called return a node ref - mocks a good call.
      // When the transfer method is called with a transfer name of exception - throw a transferException
      Mockito.when(mockedTransferService.transfer(Matchers.anyString(), Matchers.isA(TransferDefinition.class))).thenReturn(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "123"));
      Mockito.when(mockedTransferService.transfer(Matchers.eq("exception"), Matchers.isA(TransferDefinition.class))).thenThrow(new TransferException("mocked transfer exception"));
      
      // When getTransferTarget called return a TransferTarget      
      Mockito.when(mockedTransferService.getTransferTarget(Matchers.anyString())).thenReturn(dummyTarget);
      Mockito.when(mockedTransferService.getTransferTargets(Matchers.anyString())).thenReturn(dummyTargets);
      Mockito.when(mockedTransferService.getTransferTargets()).thenReturn(dummyTargets);
      
      // Execute the unit test script
      Map<String, Object> model = new HashMap<String, Object>(1);
      ScriptLocation location = new ClasspathScriptLocation("org/alfresco/repo/transfer/script/test_transferService.js");
      this.scriptService.executeScript(location, model);
          
   }
}
