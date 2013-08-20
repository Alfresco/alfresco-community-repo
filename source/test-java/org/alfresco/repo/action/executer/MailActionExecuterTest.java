/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.action.executer;

import org.alfresco.util.test.junitrules.AlfrescoPerson;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.rules.RuleChain;
import org.springframework.context.ApplicationContext;

/**
 * Provides tests for the MailActionExecuter class. The logic is now in AbstractMailActionExecuterTest. See the Javadoc for AbstractMailActionExecuterTest. The setupRuleChain()
 * method is very important as it really setup the class including creating the users.
 */
public class MailActionExecuterTest extends AbstractMailActionExecuterTest
{

    // Tie them together in a static Rule Chain
    @ClassRule
    public static RuleChain ruleChain = setupRuleChain();

    @BeforeClass
    public static void setup()
    {
        ApplicationContext appCtx = APP_CONTEXT_INIT.getApplicationContext();
        setupTests(appCtx);
    }

    @AfterClass
    public static void tearDown()
    {
        tearDownTests();
    }

    /**
     * Sets up both users and the RuleChain.
     * 
     * @return RuleChain
     */
    private static RuleChain setupRuleChain()
    {
        BRITISH_USER = new AlfrescoPerson(APP_CONTEXT_INIT, "englishuser@test.com");
        FRENCH_USER = new AlfrescoPerson(APP_CONTEXT_INIT, "frenchuser@test.com");
        AUSTRALIAN_USER = new AlfrescoPerson(APP_CONTEXT_INIT, "australianuser@test.com");

        return RuleChain.outerRule(APP_CONTEXT_INIT).around(AUSTRALIAN_USER).around(BRITISH_USER).around(FRENCH_USER);
    }

}
