/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.audit.model;

import org.alfresco.repo.audit.AuditMode;
import org.alfresco.repo.audit.MethodAuditModel;
import org.alfresco.repo.audit.RecordOptions;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MethodAuditEntry extends AbstractNamedAuditEntry implements MethodAuditModel
{
    private static Log s_logger = LogFactory.getLog(MethodAuditEntry.class);

    public MethodAuditEntry()
    {
        super();
        // TODO Auto-generated constructor stub
    }

    public AuditMode beforeExecution(AuditMode auditMode, MethodInvocation mi)
    {
        if(s_logger.isDebugEnabled())
        {
            s_logger.debug("Evaluating if method is audited ..."+((ServiceAuditEntry)getParent()).getName()+"."+getName());
        }
        return getEffectiveAuditMode();
    }

    public AuditMode afterExecution(AuditMode auditMode, MethodInvocation mi)
    {
        throw new UnsupportedOperationException();
    }

    public AuditMode onError(AuditMode auditMode, MethodInvocation mi)
    {
        throw new UnsupportedOperationException();
    }

    public RecordOptions getAuditRecordOptions(MethodInvocation mi)
    {
        return getEffectiveRecordOptions();
    }

    public TrueFalseUnset getAuditInternalServiceMethods(MethodInvocation mi)
    {
        if(s_logger.isDebugEnabled())
        {
            s_logger.debug("Evaluating if method is internally audited ..."+((ServiceAuditEntry)getParent()).getName()+"."+getName());
        }
        return getEffectiveAuditInternal();
    }

}
