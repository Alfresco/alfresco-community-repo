/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.workflow.jbpm;

import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import org.dom4j.Element;
import org.jbpm.graph.def.Action;
import org.jbpm.graph.def.Event;
import org.jbpm.graph.def.Node;
import org.jbpm.instantiation.Delegation;
import org.jbpm.jpdl.xml.JpdlXmlReader;
import org.jbpm.jpdl.xml.Problem;
import org.jbpm.jpdl.xml.ProblemListener;
import org.jbpm.scheduler.def.CancelTimerAction;
import org.jbpm.scheduler.def.CreateTimerAction;
import org.jbpm.taskmgmt.def.Task;
import org.xml.sax.InputSource;

/**
 * Extended JpdlXmlReader with access to problems encountered during compile.
 * 
 * Provides extension to Timers (to allow for absolute date). 
 * 
 * @author davidc
 */
public class JBPMJpdlXmlReader extends JpdlXmlReader
{
    private static final long serialVersionUID = -753730152120696221L;

    /*
     * Construct
     */
    public JBPMJpdlXmlReader(InputStream inputStream)
    {
        super(new InputSource(inputStream));
    }

    /*
     * Construct
     */
    public JBPMJpdlXmlReader(InputSource inputSource, ProblemListener problemListener)
    {
        super(inputSource, problemListener);
    }

    /*
     * Construct
     */
    public JBPMJpdlXmlReader(InputSource inputSource)
    {
        super(inputSource);
    }

    /*
     * Construct
     */
    public JBPMJpdlXmlReader(Reader reader)
    {
        super(reader);
    }

    /**
     * {@inheritDoc}
      */
    @Override
    protected void readNodeTimer(Element timerElement, Node node)
    {
        // NOTE: This method implementation is a copy from the JpdlXmlReader class
        //       with the difference of constructing an AlfrescoCreateTimerAction.
        //       It may need to be updated whenever a jbpm library upgrade is performed.
        
        String name = timerElement.attributeValue("name", node.getName());
        
        CreateTimerAction createTimerAction = new AlfrescoCreateTimerAction();
        createTimerAction.read(timerElement, this);
        createTimerAction.setTimerName(name);
        createTimerAction.setTimerAction(readSingleAction(timerElement));
        addAction(node, Event.EVENTTYPE_NODE_ENTER, createTimerAction);
        
        CancelTimerAction cancelTimerAction = new CancelTimerAction();
        cancelTimerAction.setTimerName(name);
        addAction(node, Event.EVENTTYPE_NODE_LEAVE, cancelTimerAction);
    }

    /**
     * {@inheritDoc}
      */
    @Override
    protected void readTaskTimer(Element timerElement, Task task)
    {
        // NOTE: This method implementation is a copy from the JpdlXmlReader class
        //       with the difference of constructing an AlfrescoCreateTimerAction.
        //       It may need to be updated whenever a jbpm library upgrade is performed.
        
        String name = timerElement.attributeValue("name", task.getName());
        if (name == null)
            name = "timer-for-task-" + task.getId();

        CreateTimerAction createTimerAction = new AlfrescoCreateTimerAction();
        createTimerAction.read(timerElement, this);
        createTimerAction.setTimerName(name);
        Action action = null;
        if ("timer".equals(timerElement.getName()))
        {
            action = readSingleAction(timerElement);
        }
        else
        {
            Delegation delegation = createMailDelegation("task-reminder", null, null, null, null);
            action = new Action(delegation);
        }
        createTimerAction.setTimerAction(action);
        addAction(task, Event.EVENTTYPE_TASK_CREATE, createTimerAction);

        // read the cancel-event types
        Collection<String> cancelEventTypes = new ArrayList<String>();

        String cancelEventTypeText = timerElement.attributeValue("cancel-event");
        if (cancelEventTypeText != null)
        {
            // cancel-event is a comma separated list of events
            StringTokenizer tokenizer = new StringTokenizer(cancelEventTypeText, ",");
            while (tokenizer.hasMoreTokens())
            {
                cancelEventTypes.add(tokenizer.nextToken().trim());
            }
        }
        else
        {
            // set the default
            cancelEventTypes.add(Event.EVENTTYPE_TASK_END);
        }
        for (String cancelEventType : cancelEventTypes)
        {
            CancelTimerAction cancelTimerAction = new CancelTimerAction();
            cancelTimerAction.setTimerName(name);
            addAction(task, cancelEventType, cancelTimerAction);
        }
    }

    /**
     * Gets the problems
     * 
     * @return  problems
     */
    @SuppressWarnings("unchecked")
    public List<Problem> getProblems()
    {
        return problems;
    }
}
