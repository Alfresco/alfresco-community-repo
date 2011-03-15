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
