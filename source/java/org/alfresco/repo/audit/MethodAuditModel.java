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
