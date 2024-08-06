/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2024 Alfresco Software Limited
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
package org.alfresco.repo.audit2;

import org.alfresco.repo.event.v1.model.UserInfo;

import java.time.ZonedDateTime;
import java.util.Map;

public class AuditRecord
{
    AuditedActionType auditedActionType;
    ZonedDateTime createdAt;
    UserInfo userInfo;
    Map<String, ?> auditData;

    public AuditRecord(AuditedActionType auditedActionType, UserInfo userInfo, Map<String, ?> auditData, ZonedDateTime createdAt)
    {
        this.auditedActionType = auditedActionType;
        this.userInfo = userInfo;
        this.auditData = auditData;
        this.createdAt = createdAt;
    }

    public AuditedActionType getAuditedActionType()
    {
        return auditedActionType;
    }

    public ZonedDateTime getCreatedAt()
    {
        return createdAt;
    }

    public UserInfo getUserInfo()
    {
        return userInfo;
    }

    public Map<String, ?> getAuditData()
    {
        return auditData;
    }


    public enum AuditedActionType
    {
        BASIC_ACTION("basic_action"),
        LOGIN("login"),
        DOWNLOAD_CONTENT("download-content"),
        API_CALL("api-call");

        private final String auditedAction;

        AuditedActionType(String auditedAction)
        {
            this.auditedAction = auditedAction;
        }

        public String getAuditedAction()
        {
            return auditedAction;
        }
    }
}

