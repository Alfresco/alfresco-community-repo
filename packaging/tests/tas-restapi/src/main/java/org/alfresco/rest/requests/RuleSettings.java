/*-
 * #%L
 * alfresco-tas-restapi
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
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
/*
 * Copyright 2022 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package org.alfresco.rest.requests;

import org.alfresco.rest.core.RestRequest;
import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.model.RestRuleSettingsModel;
import org.springframework.http.HttpMethod;

public class RuleSettings extends ModelRequest<FolderRules>
{
    public static final String IS_INHERITANCE_ENABLED = "-isInheritanceEnabled-";
    private static final String BASE_PATH = "nodes/{nodeId}/rule-settings/{ruleSettingKey}";

    private String nodeId;
    private String ruleSettingKey;

    public RuleSettings(RestWrapper restWrapper)
    {
        super(restWrapper);
    }

    public RuleSettings withNodeId(String nodeId)
    {
        this.nodeId = nodeId;
        return this;
    }

    public RuleSettings withRuleSettingKey(String ruleSettingKey)
    {
        this.ruleSettingKey = ruleSettingKey;
        return this;
    }

    public RestRuleSettingsModel retrieveSetting()
    {
        RestRequest request = RestRequest.simpleRequest(HttpMethod.GET, BASE_PATH, nodeId, ruleSettingKey);
        return restWrapper.processModel(RestRuleSettingsModel.class, request);
    }

    public RestRuleSettingsModel updateSetting(RestRuleSettingsModel ruleSettingsModel)
    {
        RestRequest request = RestRequest.requestWithBody(HttpMethod.PUT, ruleSettingsModel.toJson(), BASE_PATH, nodeId, ruleSettingKey);
        return restWrapper.processModel(RestRuleSettingsModel.class, request);
    }
}
