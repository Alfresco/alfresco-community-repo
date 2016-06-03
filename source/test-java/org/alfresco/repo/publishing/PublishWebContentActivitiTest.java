
package org.alfresco.repo.publishing;

import java.util.List;

import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.junit.experimental.categories.Category;


/**
 * @author Nick Smith
 * @author Frederik Heremans
 * @since 4.0
 */
@Category(OwnJVMTestsCategory.class)
public class PublishWebContentActivitiTest extends PublishWebContentProcessTest
{
    private static final String DEF_NAME = "activiti$publishWebContent";
    
    @Override
    protected String getWorkflowDefinitionName()
    {
        return DEF_NAME;
    }
    
    /**
     * Activiti has 2 paths: a timer-scope-path and the main execution-path
     */
    protected void checkNode(String expNode)
    {
        List<WorkflowPath> paths = workflowService.getWorkflowPaths(instanceId);
        assertEquals(2, paths.size());
        WorkflowPath path = paths.get(0);
        assertEquals(expNode, path.getNode().getName());
    }
}
