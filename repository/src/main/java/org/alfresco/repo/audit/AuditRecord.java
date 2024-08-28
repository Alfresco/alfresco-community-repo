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

public class AuditRecord
{
    private final boolean inTransaction;
    private final String auditedActionType;
    private final ZonedDateTime createdAt;
    private final UserInfo userInfo;
    private final Map<String, ?> auditData;

    public AuditRecord(boolean inTransaction,
                       String auditedActionType,
                       UserInfo userInfo,
                       Map<String, ?> auditData,
                       ZonedDateTime createdAt)
    {
        this.inTransaction = inTransaction;
        this.auditedActionType = auditedActionType;
        this.userInfo = userInfo;
        this.auditData = auditData;
        this.createdAt = createdAt;
    }

    public boolean isInTransaction()
    {
        return inTransaction;
    }

    public String getAuditedActionType()
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

}

