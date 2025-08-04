/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2025 Alfresco Software Limited
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

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Map;

public class AuditRecord
{
    private final boolean inTransaction;
    private final String auditApplicationId;
    private final ZonedDateTime createdAt;
    private final Map<String, Serializable> auditData;

    public AuditRecord(Builder builder)
    {
        this.auditApplicationId = builder.auditRecordType;
        this.inTransaction = builder.inTransaction;
        this.auditData = builder.auditRecordData;
        this.createdAt = ZonedDateTime.now();
    }

    public boolean isInTransaction()
    {
        return inTransaction;
    }

    public String getAuditApplicationId()
    {
        return auditApplicationId;
    }

    public ZonedDateTime getCreatedAt()
    {
        return createdAt;
    }

    public Map<String, Serializable> getAuditData()
    {
        return auditData;
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static class Builder
    {
        private String auditRecordType;
        private boolean inTransaction;
        private Map<String, Serializable> auditRecordData;

        public Builder setAuditRecordType(String auditRecordType)
        {
            this.auditRecordType = auditRecordType;
            return this;
        }

        public Builder setInTransaction(boolean inTransaction)
        {
            this.inTransaction = inTransaction;
            return this;
        }

        public Builder setAuditRecordData(Map<String, Serializable> auditRecordData)
        {
            this.auditRecordData = auditRecordData;
            return this;
        }

        public AuditRecord build()
        {
            return new AuditRecord(this);
        }
    }

}
