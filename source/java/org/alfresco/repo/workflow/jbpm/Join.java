/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
    }

    /**
     * Constructor
     */
    public Join(String name)
    {
        super(name);
    }

    /* (non-Javadoc)
     * @see org.jbpm.jpdl.xml.Parsable#read(org.dom4j.Element, org.jbpm.jpdl.xml.JpdlXmlReader)
     */
    public void read(Element element, JpdlXmlReader jpdlReader)
    {
        // Add "on node leave" event handler which ends child tokens / tasks
        Delegation delegation = new Delegation(JoinEndForkedTokens.class.getName());
        Action action = new Action(delegation);
        Event event = new Event(Event.EVENTTYPE_NODE_LEAVE);
        event.addAction(action);
        addEvent(event);
    }

}
