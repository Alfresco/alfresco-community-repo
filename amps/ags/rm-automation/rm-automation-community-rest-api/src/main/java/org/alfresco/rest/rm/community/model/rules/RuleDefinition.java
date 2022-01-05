/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.rm.community.model.rules;

import java.util.List;

/**
 * A class describing the rule
 */
public class RuleDefinition
{
    private String id = "";
    private String title;
    private String description = "";
    private boolean disabled = false;
    private boolean applyToChildren = false;
    private boolean runInBackground = false;
    private String ruleType = ConditionsOnRule.ADDED.getWhenConditionValue();
    private String path;
    private Boolean createRecordPath;
    private String contentTitle;
    private String contentDescription;
    private String rejectReason;
    private List<String> actions;

    /**
     * Creates a new object of type Rule Definition
     *
     * @return the object
     */
    public static RuleDefinition createNewRule()
    {
        return new RuleDefinition();
    }

    public String getId()
    {
        return id;
    }

    public RuleDefinition id(String id)
    {
        this.id = id;
        return this;
    }

    public String getTitle()
    {
        return title;
    }

    public RuleDefinition title(String title)
    {
        this.title = title;
        return this;
    }

    public String getDescription()
    {
        return description;
    }

    public RuleDefinition description(String description)
    {
        this.description = description;
        return this;
    }

    public boolean isDisabled()
    {
        return disabled;
    }

    public RuleDefinition disabled(boolean disabled)
    {
        this.disabled = disabled;
        return this;
    }

    public boolean isApplyToChildren()
    {
        return applyToChildren;
    }

    public RuleDefinition applyToChildren(boolean applyToChildren)
    {
        this.applyToChildren = applyToChildren;
        return this;
    }

    public boolean getRunInBackground()
    {
        return runInBackground;
    }

    public RuleDefinition runInBackground(boolean runInBackground)
    {
        this.runInBackground = runInBackground;
        return this;
    }

    public String getRuleType()
    {
        return ruleType;
    }

    public RuleDefinition ruleType(String ruleType)
    {
        this.ruleType = ruleType;
        return this;
    }

    public String getPath()
    {
        return path;
    }

    public RuleDefinition path(String path)
    {
        this.path = path;
        return this;
    }

    public Boolean getCreateRecordPath()
    {
        return createRecordPath;
    }

    public RuleDefinition createRecordPath(boolean createRecordPath)
    {
        this.createRecordPath = createRecordPath;
        return this;
    }

    public String getContentTitle()
    {
        return contentTitle;
    }

    public RuleDefinition contentTitle(String contentTitle)
    {
        this.contentTitle = contentTitle;
        return this;
    }

    public String getContentDescription()
    {
        return contentDescription;
    }

    public RuleDefinition contentDescription(String contentDescription)
    {
        this.contentDescription = contentDescription;
        return this;
    }

    public String getRejectReason()
    {
        return rejectReason;
    }

    public RuleDefinition rejectReason(String rejectReason)
    {
        this.rejectReason = rejectReason;
        return this;
    }

    public List<String> getActions()
    {
        return actions;
    }

    public RuleDefinition actions(List<String> actions)
    {
        this.actions = actions;
        return this;
    }
}

