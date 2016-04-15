/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.web.scripts.rule.ruleset;

import org.alfresco.service.cmr.model.FileInfo;
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

    private FileInfo owningFileInfo;

    private Rule rule;

    public RuleRef(Rule rule, FileInfo owningFileInfo)
    {
        this.rule = rule;
        this.owningFileInfo = owningFileInfo;
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
     * Set the owning file info reference for rule
     * 
     * @param owningFileInfo the owning file info reference to set
     */
    public void setOwningFileInfo(FileInfo owningFileInfo)
    {
        this.owningFileInfo = owningFileInfo;
    }

    /**
     * Returns the owning file info reference for a rule.
     * 
     * @return the owning file info reference
     */
    public FileInfo getOwningFileInfo()
    {
        return owningFileInfo;
    }
}
