/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.test.jscript;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.jscript.ClasspathScriptLocation;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.ScriptLocation;
import org.alfresco.service.cmr.repository.ScriptService;

/**
 * @author Roy Wetherall
 */
public class RMJScriptTest extends BaseRMTestCase
{
    private static String SCRIPT_PATH = "org/alfresco/module/org_alfresco_module_rm/test/jscript/";
    private static String CAPABILITIES_TEST = "CapabilitiesTest.js";
        
    private ScriptService scriptService;
    
    @Override
    protected void initServices()
    {
        super.initServices();
        this.scriptService = (ScriptService)this.applicationContext.getBean("ScriptService");
    }
    
    private NodeRef record;
    public void testCapabilities() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {        
                record = utils.createRecord(rmFolder, "testRecord.txt");
                return null;
            }           
        }); 
        
        doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run()
            {        
                utils.declareRecord(record);
                return record;
            }
            
            @Override
            public void test(NodeRef record) throws Exception
            {

                // Create a model to pass to the unit test scripts
                Map<String, Object> model = new HashMap<String, Object>(1);
                model.put("filePlan", filePlan);
                model.put("record", record);
                
                executeScript(CAPABILITIES_TEST, model);
            }            
        });        
    }
    
    private void executeScript(String script, Map<String, Object> model)
    {
        // Execute the unit test script
        ScriptLocation location = new ClasspathScriptLocation(SCRIPT_PATH + script);
        this.scriptService.executeScript(location, model);
    }
}
