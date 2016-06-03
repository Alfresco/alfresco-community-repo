package org.alfresco.repo.workflow.jbpm;

import org.dom4j.Element;
import org.jbpm.graph.def.Action;
import org.jbpm.graph.def.Event;
import org.jbpm.instantiation.Delegation;
import org.jbpm.jpdl.xml.JpdlXmlReader;


/**
 * Implementation of Join which ends child tokens / tasks for nOutM cases.
 * 
 * @author davidc
 *
 */
public class Join extends org.jbpm.graph.node.Join
{
    private static final long serialVersionUID = 6417483503439714897L;

    /**
     * Constructor
     */
    public Join()
    {
        super();
    }

    /**
     * Constructor
     */
    public Join(String name)
    {
        super(name);
    }

    /**
     * {@inheritDoc}
      */
    @Override
    public void read(Element element, JpdlXmlReader jpdlReader)
    {
        // Add "on node leave" event handler which ends child tokens / tasks
        Delegation delegation = new Delegation(JoinEndForkedTokens.class.getName());
        Action theAction = new Action(delegation);
        Event event = new Event(Event.EVENTTYPE_NODE_LEAVE);
        event.addAction(theAction);
        addEvent(event);
    }

}
