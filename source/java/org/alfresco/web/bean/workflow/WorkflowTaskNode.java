package org.alfresco.web.bean.workflow;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.cmr.workflow.WorkflowTransition;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.TransientMapNode;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Wrapper around a {@link WorkflowTask} to allow it to be approached as a {@link Node}.
 * Can hold additional properties, on top of the properties in the actual {@link WorkflowTask}.
 *  
 * @author Frederik Heremans
 */
public class WorkflowTaskNode extends TransientMapNode {

	private static final long serialVersionUID = 1L;
	private Map<String, Object> propertyWrapper;
	
	private WorkflowTask workflowTask;
	
	public WorkflowTaskNode(WorkflowTask workflowTask) {
		super(workflowTask.getDefinition().getMetadata().getName(), workflowTask.getTitle(), null);
		this.workflowTask = workflowTask;
		
		propertyWrapper = new WorkflowTaskPropertyBackedMap();
		
		propertyWrapper.put(ContentModel.PROP_NAME.toString(), workflowTask.getTitle());
		propertyWrapper.put("type", type.toString());
		propertyWrapper.put("id", workflowTask.getId());
		
		// add extra properties for completed tasks
	    if (workflowTask.getState().equals(WorkflowTaskState.COMPLETED))
	    {
	       // add the outcome label for any completed task
	       String outcome = null;
	       String transition = (String)workflowTask.getProperties().get(WorkflowModel.PROP_OUTCOME);
	       if (transition != null)
	       {
	          for (WorkflowTransition trans : workflowTask.getDefinition().getNode().getTransitions())
	          {
	             if (trans.getId().equals(transition))
	             {
	                outcome = trans.getTitle();
	                break;
	             }
	          }
	          
	          if(outcome == null)
	          {
	          	outcome = transition;
	          }
	          if (outcome != null)
	          {
	        	  propertyWrapper.put("outcome", outcome);
	          }
	       }
	         
	       // add the workflow instance id and name this taks belongs to
	       propertyWrapper.put("workflowInstanceId", workflowTask.getPath().getInstance().getId());
	       
	       // add the task itself as a property
	       propertyWrapper.put("workflowTask", workflowTask);
	    }
	    
        // Add an additional property, containing a human-friendly representation of the priority
        Integer priority = (Integer) workflowTask.getProperties().get(WorkflowModel.PROP_PRIORITY);
        String priorityMessage = "";
        if (priority != null)
        {
            priorityMessage = I18NUtil.getMessage(getPriorityMessageKey(priority), I18NUtil.getLocale());
        }
        propertyWrapper.put("priorityMessage", priorityMessage);
	    
	}
	
	@Override
	public Map<String, Object> getProperties() {
		return propertyWrapper;
	}
	
	/**
	 * Map which can contain local values (added by put and putAll). Get-operation reverts
	 * to {@link WorkflowTask} properties when not found in the local values.
	 *  
	 * @author Frederik Heremans
	 */
	private class WorkflowTaskPropertyBackedMap extends HashMap<String, Object>
	{
		private static final long serialVersionUID = 1L;

		@Override
		public Object get(Object key) {
			String fullKey = QName.resolveToQNameString(WorkflowTaskNode.this.getNamespacePrefixResolver(), key.toString());
			Object value =  super.get(fullKey);
			if(value == null)
			{
				value = workflowTask.getProperties().get(QName.resolveToQName(WorkflowTaskNode.this.getNamespacePrefixResolver(), fullKey));
			}
			return value;
		}
		
		@Override
		public boolean containsKey(Object key) {
			String fullKey = QName.resolveToQNameString(WorkflowTaskNode.this.getNamespacePrefixResolver(), key.toString());
			boolean contains =  super.containsKey(fullKey);
			if(!contains)
			{
				contains = workflowTask.getProperties().containsKey(QName.resolveToQName(WorkflowTaskNode.this.getNamespacePrefixResolver(), fullKey));
			}
			return contains;
		}
		
		@Override
		public Object put(String key, Object value) {
			return super.put(QName.resolveToQNameString(WorkflowTaskNode.this.getNamespacePrefixResolver(), key.toString()), value);
		}
	}

    protected String getPriorityMessageKey(int priority)
    {
        return "listconstraint.bpm_allowedPriority." + priority;
    }
}
