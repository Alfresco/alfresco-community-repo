
package org.alfresco.repo.workflow.jbpm;

import org.alfresco.repo.workflow.AbstractMultitenantWorkflowTest;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.junit.experimental.categories.Category;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
@Category(OwnJVMTestsCategory.class)
public class JbpmMultitenantWorkflowTest extends AbstractMultitenantWorkflowTest
{
    @Override
    protected String getEngine()
    {
        return JBPMEngine.ENGINE_ID;
    }

    @Override
    protected String getTestDefinitionPath()
    {
        return "jbpmresources/test_simple_processdefinition.xml";
    }

    @Override
    protected String getTestDefinitionKey()
    {
        return "jbpm$test";
    }

    protected String getAdhocDefinitionPath()
    {
        return "alfresco/workflow/adhoc_processdefinition.xml";
    }

    @Override
    protected String getAdhocDefinitionKey()
    {
        return "jbpm$wf:adhoc";
    }
    
    public void testSetup() throws Exception
    {
        // dummy test
    }
}
