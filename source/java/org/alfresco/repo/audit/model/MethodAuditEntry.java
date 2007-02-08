/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.audit.model;

import org.alfresco.repo.audit.AuditMode;
import org.alfresco.repo.audit.MethodAuditModel;
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

    public RecordOptionsImpl getAuditRecordOptions(MethodInvocation mi)
    {
        throw new UnsupportedOperationException();
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
