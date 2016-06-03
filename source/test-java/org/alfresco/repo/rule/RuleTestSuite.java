package org.alfresco.repo.rule;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.alfresco.repo.rule.ruletrigger.RuleTriggerTest;


/**
 * Version test suite
 * 
 * @author Roy Wetherall
 */
public class RuleTestSuite extends TestSuite
{
    /**
     * Creates the test suite
     * 
     * @return  the test suite
     */
    public static Test suite() 
    {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(RuleTypeImplTest.class);
        suite.addTestSuite(RuleTriggerTest.class);
        suite.addTestSuite(RuleServiceImplTest.class);
        suite.addTestSuite(RuleServiceCoverageTest.class);
        return suite;
    }
}
