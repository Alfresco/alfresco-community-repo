
package org.alfresco.repo.workflow;

import java.util.Arrays;
import java.util.Set;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
public class WorkflowTestHelper
{
    private final WorkflowAdminServiceImpl workflowAdminService;
    private final String engineId;
    private final Set<String> enabledEngines;
    private final Set<String> visibleEngines;
    
    public WorkflowTestHelper(WorkflowAdminServiceImpl workflowAdminService, String engineId, boolean enableEngineOnly)
    {
        this.workflowAdminService = workflowAdminService;
        this.engineId = engineId;
        this.enabledEngines = workflowAdminService.getEnabledEngines();
        this.visibleEngines = workflowAdminService.getVisibleEngines();
        if(enableEngineOnly)
        {
            enableThisEngineOnly();
        }
    }

    public void enableThisEngineOnly()
    {
        workflowAdminService.setEnabledEngines(Arrays.asList(engineId));
        workflowAdminService.setVisibleEngines(Arrays.asList(engineId));
    }
    
    public void tearDown()
    {
        workflowAdminService.setEnabledEngines(enabledEngines);
        workflowAdminService.setVisibleEngines(visibleEngines);
    }

    public void setVisible(boolean isVisible)
    {
        workflowAdminService.setEngineVisibility(engineId, isVisible);
    }
    
    public void setEnabled(boolean isEnabled)
    {
        workflowAdminService.setEngineEnabled(engineId, isEnabled);
    }
    
}
