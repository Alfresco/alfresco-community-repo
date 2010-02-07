/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.web.scripts.rule.ruleset;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.Rule;



/**
 * Rule object for REST API
 * 
 * @author unknown
 *
 */
public class RuleRef
{

    /** Serial version UID */
    private static final long serialVersionUID = -923276130307938661L;
    
    private NodeRef owningNodeRef;
    
    private Rule rule;
    
    public RuleRef(Rule rule, NodeRef owningNodeRef)
    {
        this.rule = rule;
        this.owningNodeRef = owningNodeRef;
    }
    
    /**
     * Set the rule
     * 
     * @param rule the rule to set
     */
    public void setRule(Rule rule)
    {
        this.rule = rule;
    }
    
    /**
     * Return the rule
     * 
     * @return rule
     */
    public Rule getRule()
    {
        return rule;
    }
    
    /**
     * Set the owning node reference for rule
     * 
     * @param owningNodeRef the owning node reference to set
     */
    public void setOwningNodeRef(NodeRef owningNodeRef)
    {
        this.owningNodeRef = owningNodeRef;
    }
    
    /**
     * Returns the owning node reference for a rule.
     * 
     * @return the owning node reference
     */
    public NodeRef getOwningNodeRef()
    {
        return owningNodeRef;
    }
}
