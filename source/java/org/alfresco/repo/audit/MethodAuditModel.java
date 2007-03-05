/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.audit;

import org.alfresco.repo.audit.model.TrueFalseUnset;
import org.aopalliance.intercept.MethodInvocation;

/**
 * The audit model used to audit method calls.
 * 
 * @author andyh
 */
public interface MethodAuditModel
{
    /**
     * Report if audit behaviour can be determined before the method call
     * 
     * @param auditMode
     * @param mi
     * @return - the audit mode
     */
    public AuditMode beforeExecution(AuditMode auditMode, MethodInvocation mi);

    /**
     * Report if audit behaviour can be determined after the method call
     * 
     * @param auditMode
     * @param mi
     * @return - the audit mode
     */
    public AuditMode afterExecution(AuditMode auditMode, MethodInvocation mi);

    /**
     * Report if audit behaviour should be invoked on error. It could be we look at the error and filter - this filter is not
     * supported at the moment.
     * 
     * @param auditMode
     * @param mi
     * @return - the audit mode
     */
    public AuditMode onError(AuditMode auditMode, MethodInvocation mi);

    /**
     * Get the optional parameters that are to be recorded
     * 
     * @param mi
     * @return - what to record 
     */
    public RecordOptions getAuditRecordOptions(MethodInvocation mi);

    /**
     * Deteine if internal calls to public service shoud be audited
     * @param mi
     * @return - mode
     */
    public TrueFalseUnset getAuditInternalServiceMethods(MethodInvocation mi);
}
