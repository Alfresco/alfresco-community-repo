/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.api.rules;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.rest.api.RuleSettings;
import org.alfresco.rest.api.model.rules.RuleSetting;
import org.alfresco.rest.api.nodes.NodesEntityResource;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.core.exceptions.RelationshipResourceNotFoundException;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.Experimental;
import org.alfresco.util.PropertyCheck;
import org.springframework.beans.factory.InitializingBean;

/**
 * Folder node rule settings (rule inheritance).
 */
@Experimental
@RelationshipResource (name = "rule-settings", entityResource = NodesEntityResource.class, title = "Folder rule settings")
public class NodeRuleSettingsRelation implements RelationshipResourceAction.ReadById<RuleSetting>,
                                                 RelationshipResourceAction.Update<RuleSetting>,
                                                 InitializingBean
{
    private RuleSettings ruleSettings;

    @Override
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "ruleSettings", ruleSettings);
    }

    /**
     * Get the given configuration value for the specified folder.
     * <p>
     * - GET /nodes/{folderId}/rule-settings/{ruleSettingKey}
     *
     * @param folderId The id of the folder.
     * @param ruleSettingKey The setting to retrieve.
     * @param parameters Unused.
     * @return {@link RuleSetting} The current value of the setting.
     */
    @WebApiDescription (
            title = "Get a folder node rule setting",
            description = "Returns the specified rule setting for the given folder",
            successStatus = HttpServletResponse.SC_OK
    )
    @Override
    public RuleSetting readById(String folderId, String ruleSettingKey, Parameters parameters) throws RelationshipResourceNotFoundException
    {
        return ruleSettings.getRuleSetting(folderId, ruleSettingKey);
    }

    /**
     * Set the value of a rule setting for the specified folder.
     * <p>
     * PUT /nodes/{folderId}/rule-settings/{ruleSettingKey}
     *
     * @param folderId The id of the folder.
     * @param ruleSetting The new value of the rule setting.
     * @param parameters Unused.
     * @return The updated rule setting.
     */
    @WebApiDescription (
            title = "Update folder node rule setting",
            description = "Update a rule setting for given node",
            successStatus = HttpServletResponse.SC_OK
    )
    @Override
    public RuleSetting update(String folderId, RuleSetting ruleSetting, Parameters parameters)
    {
        return ruleSettings.setRuleSetting(folderId, ruleSetting);
    }

    public void setRuleSettings(RuleSettings ruleSettings)
    {
        this.ruleSettings = ruleSettings;
    }
}
