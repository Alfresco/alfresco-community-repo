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
        EXTERNAL_USER = new AlfrescoPerson(APP_CONTEXT_INIT, "externaluser@externaldomain.com");

        return RuleChain.outerRule(APP_CONTEXT_INIT).around(AUSTRALIAN_USER).around(BRITISH_USER).around(FRENCH_USER).around(EXTERNAL_USER);
    }

}
