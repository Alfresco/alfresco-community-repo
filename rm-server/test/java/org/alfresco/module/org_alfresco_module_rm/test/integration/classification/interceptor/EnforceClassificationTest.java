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
package org.alfresco.module.org_alfresco_module_rm.test.integration.classification.interceptor;

import org.alfresco.module.org_alfresco_module_rm.classification.ContentClassificationService;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;

/**
 * Enforce classification integration test
 *
 * @author Roy Wetherall
 * @since 3.0
 */
public class EnforceClassificationTest extends BaseRMTestCase
{
	/** test data */
//	private static final String CLASSIFICATION_LEVEL1 = "level1";
//	private static final String CLASSIFICATION_LEVEL2 = "level2";
//	private static final String CLASSIFICATION_LEVEL3 = "level3";
//	private static final String CLASSIFICATION_LEVEL4 = "level4";
//
//	private static final String CLASSIFICATION_REASON = "Test Reason 1";
//	private static final String CLASSIFICATION_AUTHORITY = "classification.authority";
//	private static final String RECORD_NAME = "recordname.txt";
//
//	private ContentClassificationService contentClassificationService;

	@Override
	protected void initServices()
	{
	    super.initServices();
	    contentClassificationService = (ContentClassificationService)applicationContext.getBean("contentClassificationService");
	}

	@Override
	protected boolean isCollaborationSiteTest()
	{
	    return true;
	}

	/**
	 *
	 */
    public void testUserNotClearedDocument() throws Exception
    {
//        doBehaviourDrivenTest(new BehaviourDrivenTest(AccessDeniedException.class)
//        {
//        	private String userName;
//
//            public void given() throws Exception
//            {
//                // create test person and assign read permission to document
//                userName = GUID.generate();
//                createPerson(userName, true);
//                permissionService.setPermission(dmDocument, userName , PermissionService.READ, true);
//
//                // assign security clearance
//                securityClearanceService.setUserSecurityClearance(userName, CLASSIFICATION_LEVEL3);
//
//                // classify document
//                contentClassificationService.classifyContent(
//                            CLASSIFICATION_LEVEL1,
//                            CLASSIFICATION_AUTHORITY,
//                            Collections.singleton(CLASSIFICATION_REASON),
//                            dmDocument);
//
//            }
//
//            public void when() throws Exception
//            {
//                AuthenticationUtil.runAs(new RunAsWork<Void>()
//                {
//                    public Void doWork() throws Exception
//                    {
//                        nodeService.getAspects(dmDocument);
//
//                        return null;
//                    }
//                }, userName);
//            }
//        });
    }
}
