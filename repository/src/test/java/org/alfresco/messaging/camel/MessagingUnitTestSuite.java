package org.alfresco;

import org.junit.experimental.categories.Categories;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Categories.class)
@Suite.SuiteClasses({
        org.alfresco.messaging.camel.CamelRoutesTest.class,
        org.alfresco.messaging.camel.CamelComponentsTest.class
})
public class MessagingUnitTestSuite {
}
