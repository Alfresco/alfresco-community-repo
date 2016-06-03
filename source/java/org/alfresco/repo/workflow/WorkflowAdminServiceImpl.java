package org.alfresco.repo.workflow;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.alfresco.service.cmr.workflow.WorkflowAdminService;

/**
 * Default implementation of the workflow admin service.
 *
 * @author Gavin Cornwell
 * @author Nick Smith
 * @since 4.0
 */
public class WorkflowAdminServiceImpl implements WorkflowAdminService
{
    public static final String NAME = "workflowAdminService";
    public static final String ENGINE = "engineId";
    public static final String ENABLED = "enabled";
    public static final String VISIBLE = "visible";
    
    private Set<String> enabledEngines = new HashSet<String>();
    private Set<String> visibleEngines = new HashSet<String>();
    
     /**
     * {@inheritDoc}
     */
     public boolean isEngineEnabled(String engineId)
     {
         return enabledEngines.contains(engineId);
     }
     
     /**
     * {@inheritDoc}
     */
     public void setEngineEnabled(String engineId, boolean isEnabled)
     {
         if(isEnabled)
         {
             enabledEngines.add(engineId);
         }
         else
         {
             enabledEngines.remove(engineId);
         }
     }
     
     /**
     * {@inheritDoc}
     */
     public boolean isEngineVisible(String engineId)
     {
         return isEngineEnabled(engineId) && visibleEngines.contains(engineId);
     }
     
     /**
     * {@inheritDoc}
     */
     public void setEngineVisibility(String engineId, boolean isVisible)
     {
         if(isVisible)
         {
             visibleEngines.add(engineId);
         }
         else
         {
             visibleEngines.remove(engineId);
         }
     }
     
     /**
      * Setter for Spring
      * @param engines All engine Ids to enable.
      */
     public void setEnabledEngines(Collection<String> engines)
     {
         if(false == enabledEngines.isEmpty())
         {
             enabledEngines.clear();
         }
         enabledEngines.addAll(engines);
     }
     
     /**
     * @return the enabledEngines
     */
    public Set<String> getEnabledEngines()
    {
        return new HashSet<String>(enabledEngines);
    }
    
     /**
      * Setter for Spring.
      * @param engines All engineIds to set visible.
      */
     public void setVisibleEngines(Collection<String> engines)
     {
         if(false == visibleEngines.isEmpty())
         {
             visibleEngines.clear();
         }
         visibleEngines.addAll(engines);
     }
     
     /**
     * @return the visibleEngines
     */
    public Set<String> getVisibleEngines()
    {
        return new HashSet<String>(visibleEngines);
    }

    public void setWorkflowEngineConfigurations(List<Properties> props)
    {
        for (Properties prop : props)
        {
            String engineId = prop.getProperty(ENGINE);
            String isEnabled = (String) prop.get(ENABLED);
            if(isEnabled!=null)
            {
                setEngineEnabled(engineId, Boolean.valueOf(isEnabled));
            }
            String isVisible = (String) prop.get(VISIBLE);
            if(isVisible!=null)
            {
                setEngineVisibility(engineId, Boolean.valueOf(isVisible));
            }
        }
    }
}
