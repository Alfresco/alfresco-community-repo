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
package org.alfresco.repo.audit;

import org.alfresco.repo.event.v1.model.UserInfo;

import java.time.ZonedDateTime;
import java.util.Map;

public class AuditRecordBuilder
{
    public static final String BASIC_ACTION = "basic_action";
    public static final String LOGIN = "login";
    public static final String DOWNLOAD_CONTENT = "download-content";
    public static final String API_CALL = "api-call";

    private boolean inTransaction;
    private String auditedActionType;
    private UserInfo userInfo;
    private Map<String, ?> auditData;
    private ZonedDateTime createdAt;

    public AuditRecordBuilder setInTransaction(boolean inTransaction)
    {
        this.inTransaction = inTransaction;
        return this;
    }

    public AuditRecordBuilder setAuditedActionType(String auditedActionType)
    {
        this.auditedActionType = auditedActionType;
        return this;
    }

    public AuditRecordBuilder setUserInfo(UserInfo userInfo)
    {
        this.userInfo = userInfo;
        return this;
    }

    public AuditRecordBuilder setAuditData(Map<String, ?> auditData)
    {
        this.auditData = auditData;
        return this;
    }

    public AuditRecordBuilder setCreatedAt(ZonedDateTime createdAt)
    {
        this.createdAt = createdAt;
        return this;
    }

    public AuditRecord build()
    {
        return new AuditRecord(inTransaction, auditedActionType, userInfo, auditData, createdAt);
    }
}