/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.test.integration.classification;

import java.util.Collections;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.classification.model.ClassifiedContentModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Classification level integration test
 *
 * @author Roy Wetherall
 * @since 3.0
 */
public class ClassifyTest extends BaseRMTestCase
{
	/** test data */
	private static final String CLASSIFICATION_LEVEL = "level1";
	private static final String CLASSIFICATION_REASON = "Test Reason 1";
	private static final String CLASSIFICATION_AUTHORITY = "classification authority";
	private static final String RECORD_NAME = "recordname.txt";
    
    /**
     * Given that a record is complete
     * And unclassified
     * Then I can successfully set the initial classification
     */
    public void testClassifyCompleteRecord() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
        	private NodeRef record;
        	
            public void given() throws Exception
            {
            	record = utils.createRecord(rmFolder, RECORD_NAME);
            	utils.completeRecord(record);
            }

            public void when() throws Exception
            {
            	classificationService.classifyContent(
            			CLASSIFICATION_LEVEL, 
            			CLASSIFICATION_AUTHORITY, 
            			Collections.singleton(CLASSIFICATION_REASON), 
            			record);
            }

            @SuppressWarnings("unchecked")
			public void then() throws Exception
            {
            	assertTrue(nodeService.hasAspect(record, ClassifiedContentModel.ASPECT_CLASSIFIED));
            	assertEquals(CLASSIFICATION_LEVEL, (String)nodeService.getProperty(record, ClassifiedContentModel.PROP_INITIAL_CLASSIFICATION));
            	assertEquals(CLASSIFICATION_LEVEL, (String)nodeService.getProperty(record, ClassifiedContentModel.PROP_CURRENT_CLASSIFICATION));
            	assertEquals(CLASSIFICATION_AUTHORITY, (String)nodeService.getProperty(record, ClassifiedContentModel.PROP_CLASSIFICATION_AUTHORITY));
            	assertEquals(Collections.singletonList(CLASSIFICATION_REASON), (List<String>)nodeService.getProperty(record, ClassifiedContentModel.PROP_CLASSIFICATION_REASONS));            
            }
        });
    }
}
