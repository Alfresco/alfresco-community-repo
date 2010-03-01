/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.audit;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * The audit model used for application level auditing.
 * 
 * @author andyh
 */
public interface ApplicationAuditModel
{
    /**
     * Report if audit behaviour can be determined before the method call
     * 
     * @param auditMode
     * @param application
     * @param description
     * @param key
     * @param args
     * @return - the audit mode
     */
    public AuditMode beforeExecution(AuditMode auditMode, String application, String description, NodeRef key,
            Object... args);

    /**
     * Report if audit behaviour can be determined after the method call
     * 
     * @param auditMode
     * @param application
     * @param description
     * @param key
     * @param args
     * @return - the audit mode
     */
    public AuditMode afterExecution(AuditMode auditMode, String application, String description, NodeRef key,
            Object... args);

    /**
     * Report if audit behaviour should be invoked on error. It could be we look at the error and filter - this is not
     * supported at the moment.
     * 
     * @param auditMode
     * @param application
     * @param description
     * @param key
     * @param args
     * @return - the audit mode
     */
    public AuditMode onError(AuditMode auditMode, String application, String description, NodeRef key, Object... args);

   /**
    * Get the optional parameters that are to be recorded
    * 
    * @param application
    * @return - the audit mode
    */
    public RecordOptions getAuditRecordOptions(String application);
}
