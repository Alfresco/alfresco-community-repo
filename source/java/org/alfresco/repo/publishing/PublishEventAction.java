
package org.alfresco.repo.publishing;

import java.util.List;

import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * This ActionExecuter adds a publish event to the publish event queue.
 * 
 * @author Nick Smith
 * @since 4.0
 */
public class PublishEventAction extends ActionExecuterAbstractBase 
{
    private PublishingEventProcessor processor;
    
    /**
    * {@inheritDoc}
    */
    @Override
    protected void executeImpl(Action action, NodeRef eventNode)
    {
        processor.processEventNode(eventNode);
    }

    /**
    * {@inheritDoc}
    */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        //NOOP
    }
    
    /**
     * @param processor the processor to set
     */
    public void setPublishingEventProcessor(PublishingEventProcessor processor)
    {
        this.processor = processor;
    }
}
