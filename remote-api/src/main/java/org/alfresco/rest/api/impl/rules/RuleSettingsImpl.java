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
package org.alfresco.rest.api.impl.rules;

import static org.alfresco.repo.rule.RuleModel.ASPECT_IGNORE_INHERITED_RULES;
import static org.alfresco.rest.api.model.rules.RuleSetting.IS_INHERITANCE_ENABLED_KEY;

import java.util.Collections;

import org.alfresco.rest.api.RuleSettings;
import org.alfresco.rest.api.model.rules.RuleSetting;
import org.alfresco.rest.framework.core.exceptions.NotFoundException;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

@Experimental
public class RuleSettingsImpl implements RuleSettings
{
    private NodeValidator validator;
    private NodeService nodeService;

    @Override
    public RuleSetting getRuleSetting(String folderId, String ruleSettingKey)
    {
        NodeRef folderNode = validator.validateFolderNode(folderId, false);
        switch (ruleSettingKey)
        {
            case IS_INHERITANCE_ENABLED_KEY:
                return getIsInheritanceEnabled(folderNode);
            default:
                throw new NotFoundException("Unrecognised rule setting key " + ruleSettingKey);
        }
    }

    private RuleSetting getIsInheritanceEnabled(NodeRef folderNode)
    {
        boolean inheritanceDisabled = nodeService.hasAspect(folderNode, ASPECT_IGNORE_INHERITED_RULES);
        return RuleSetting.builder().key(IS_INHERITANCE_ENABLED_KEY).value(!inheritanceDisabled).create();
    }

    @Override
    public RuleSetting setRuleSetting(String folderId, RuleSetting ruleSetting)
    {
        NodeRef folderNode = validator.validateFolderNode(folderId, true);

        switch (ruleSetting.getKey())
        {
            case IS_INHERITANCE_ENABLED_KEY:
                return updateIsInheritanceEnabled(folderNode, ruleSetting.getValue());
            default:
                throw new NotFoundException("Unrecognised rule setting key " + ruleSetting.getKey());
        }
    }

    private RuleSetting updateIsInheritanceEnabled(NodeRef folderNode, Object value)
    {
        if (!(value instanceof Boolean))
        {
            throw new IllegalArgumentException("Rule setting " + IS_INHERITANCE_ENABLED_KEY + " requires a boolean value.");
        }

        if ((boolean) value)
        {
            nodeService.removeAspect(folderNode, ASPECT_IGNORE_INHERITED_RULES);
        }
        else
        {
            nodeService.addAspect(folderNode, ASPECT_IGNORE_INHERITED_RULES, Collections.emptyMap());
        }

        return RuleSetting.builder().key(IS_INHERITANCE_ENABLED_KEY).value(value).create();
    }

    public void setValidator(NodeValidator validator)
    {
        this.validator = validator;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
}
