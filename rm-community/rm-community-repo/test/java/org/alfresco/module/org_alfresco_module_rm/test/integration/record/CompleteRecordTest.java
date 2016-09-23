/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.test.integration.record;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionResult;
import org.alfresco.module.org_alfresco_module_rm.action.impl.DeclareRecordAction;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Complete record tests.
 * 
 * @author Roy Wetherall
 * @since 2.2.1
 */
public class CompleteRecordTest extends BaseRMTestCase
{    
	private static final QName ASPECT_TEST = QName.createQName("http://www.alfresco.org/model/rmtest/1.0", "recordMetaDataWithProperty");
	private static final QName PROP_TEST = QName.createQName("http://www.alfresco.org/model/rmtest/1.0", "customMandatoryProperty");
	
	/** complete record action */
	private DeclareRecordAction action;
	
	/**
	 * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#initServices()
	 */
	@Override
	protected void initServices() 
	{
		super.initServices();
		
		// get the action
		action = (DeclareRecordAction)applicationContext.getBean("declareRecord");
	}
	
	/**
	 * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#tearDownImpl()
	 */
	@Override
	protected void tearDownImpl() 
	{
		super.tearDownImpl();
		
		// ensure action is returned to original state
    	action.setCheckMandatoryPropertiesEnabled(true);
	}
	
	/**
	 * Given the the application is configured to check for mandatory values before complete
	 * And a filed record is missing mandatory values
	 * When I try to complete the record
	 * Then the missing properties parameter of the action will be populated 
	 * And the record will not be complete
	 */	
    public void testCheckForMandatoryValuesMissing() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {            
        	private NodeRef record;
        	private RecordsManagementActionResult result; 
        	
            public void given()
            {
            	// enable mandatory parameter check
            	action.setCheckMandatoryPropertiesEnabled(true);
            	
            	// create a record
            	record = utils.createRecord(rmFolder, "record.txt", "title");
            	
            	// add the record aspect (that has a mandatory property)
            	nodeService.addAspect(record, ASPECT_TEST, null);
            }
            
            public void when()
            {
            	// complete record     
            	result = rmActionService.executeRecordsManagementAction(record, "declareRecord");
            }
            
            public void then() 
            {
            	assertNotNull(result);
            	assertNotNull(result.getValue());            	
            	assertFalse(nodeService.hasAspect(record, ASPECT_DECLARED_RECORD));
            }
        });           
    }
    
    /**
	 * Given the the application is configured to check for mandatory values before complete
	 * And a filed record has all mandatory values
	 * When I try to complete the record
	 * Then the record is completed
	 */
    public void testCheckForMandatoryValuePresent() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {            
        	private NodeRef record;
        	private RecordsManagementActionResult result; 
        	
            public void given()
            {
            	// enable mandatory parameter check
            	action.setCheckMandatoryPropertiesEnabled(true);
            	
            	// create a record
            	record = utils.createRecord(rmFolder, "record.txt", "title");
            	
            	// add the record aspect (that has a mandatory property)
            	Map<QName, Serializable> properties = new HashMap<QName, Serializable>(1);
            	properties.put(PROP_TEST, "something");
            	nodeService.addAspect(record, ASPECT_TEST, properties);
            }
            
            public void when()
            {
            	// complete record     
            	result = rmActionService.executeRecordsManagementAction(record, "declareRecord");
            }
            
            public void then() 
            {
            	assertNotNull(result);
            	assertNull(result.getValue());            	
            	assertTrue(nodeService.hasAspect(record, ASPECT_DECLARED_RECORD));
            }
        });           
    }
    
    /**
	 * Given the the application is configured not to check for mandatory values before complete
	 * And a filed record is missing mandatory values
	 * When I try to complete the record
	 * Then the record is completed
	 */	
    public void testDontCheckForMandatoryValuesMissing() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {            
        	private NodeRef record;
        	private RecordsManagementActionResult result; 
        	
            public void given()
            {
            	// disable mandatory parameter check
            	action.setCheckMandatoryPropertiesEnabled(false);
            	
            	// create a record
            	record = utils.createRecord(rmFolder, "record.txt", "title");
            	
            	// add the record aspect (that has a mandatory property)
            	nodeService.addAspect(record, ASPECT_TEST, null);
            }
            
            public void when()
            {
            	// complete record     
            	result = rmActionService.executeRecordsManagementAction(record, "declareRecord");
            }
            
            public void then() 
            {
            	assertNotNull(result);
            	assertNull(result.getValue());            	
            	assertTrue(nodeService.hasAspect(record, ASPECT_DECLARED_RECORD));
            }
        });           
    }
    
    /**
	 * Given the the application is configured to not to check for mandatory values before complete
	 * And a filed record has all mandatory values
	 * When I try to complete the record
	 * Then the record is completed
	 */
    public void testDontCheckForMandatoryValuePresent() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {            
        	private NodeRef record;
        	private RecordsManagementActionResult result; 
        	
            public void given()
            {
            	// enable mandatory parameter check
            	action.setCheckMandatoryPropertiesEnabled(false);
            	
            	// create a record
            	record = utils.createRecord(rmFolder, "record.txt", "title");
            	
            	// add the record aspect (that has a mandatory property)
            	Map<QName, Serializable> properties = new HashMap<QName, Serializable>(1);
            	properties.put(PROP_TEST, "something");
            	nodeService.addAspect(record, ASPECT_TEST, properties);
            }
            
            public void when()
            {
            	// complete record     
            	result = rmActionService.executeRecordsManagementAction(record, "declareRecord");
            }
            
            public void then() 
            {
            	assertNotNull(result);
            	assertNull(result.getValue());            	
            	assertTrue(nodeService.hasAspect(record, ASPECT_DECLARED_RECORD));
            }
        });           
    }
}
