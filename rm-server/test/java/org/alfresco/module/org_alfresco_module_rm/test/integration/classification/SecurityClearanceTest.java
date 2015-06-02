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

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationLevel;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationException.LevelIdNotFound;
import org.alfresco.module.org_alfresco_module_rm.classification.ClearanceLevel;
import org.alfresco.module.org_alfresco_module_rm.classification.SecurityClearance;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;

/**
 * Application context tests for the security clearance service.
 *
 * @author tpage
 */
public class SecurityClearanceTest extends BaseRMTestCase
{
    @Override
    protected boolean isUserTest()
    {
        return true;
    };

    /**
     * Given I am admin
     * When I try to give a user maximum clearance
     * Then I am successful.
     */
    public void testGiveClearance() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private SecurityClearance securityClearance;

            public void when() throws Exception
            {
                securityClearance = securityClearanceService.setUserSecurityClearance(userName, "level1");
            }

            public void then() throws Exception
            {
                ClearanceLevel clearanceLevel = securityClearance.getClearanceLevel();
                ClassificationLevel highestClassificationLevel = clearanceLevel.getHighestClassificationLevel();
                assertEquals("level1", highestClassificationLevel.getId());
            }
        });
    }

    /**
     * Given I am a user with level2 access
     * And I try to give another user level1 access
     * Then an exception is thrown.
     */
    public void testCantGiveClearance() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(LevelIdNotFound.class)
        {
            public void given() throws Exception
            {
                securityClearanceService.setUserSecurityClearance(userName, "level2");
            }

            public void when() throws Exception
            {
                doTestInTransaction(new Test<Void>()
                {
                    @Override
                    public Void run()
                    {
                        securityClearanceService.setUserSecurityClearance(rmUserName, "level1");
                        return null;
                    }
                }, userName);
            }
        });
    }
}
