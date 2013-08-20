package org.alfresco.rest.framework.tests.core;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Runs all the tests for the Public Rest Framework
 * @author Gethin James
 */
@RunWith(Suite.class)
@SuiteClasses({ InspectorTests.class, JsonJacksonTests.class, ParamsExtractorTests.class,
            ResourceLocatorTests.class, ResourceWebScriptHelperTests.class, SerializeTests.class, WhereTests.class })
public class AllRestFrameworkTest
{

}
