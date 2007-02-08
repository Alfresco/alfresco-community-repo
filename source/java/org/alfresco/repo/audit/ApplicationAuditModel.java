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
package org.alfresco.repo.audit;

import org.alfresco.service.cmr.repository.NodeRef;

public interface ApplicationAuditModel
{
    /**
     * Report if audit behaviour can be determined before the method call
     * 
     * @param auditState,
     * @param mi
     * @return
     */
    public AuditMode beforeExecution(AuditMode auditMode, String application, String description,
            NodeRef key, Object... args);

    /**
     * Report if audit behaviour can be determined after the method call
     * 
     * @param auditState,
     * @param mi
     * @return
     */
    public AuditMode afterExecution(AuditMode auditMode, String application,  String description,
            NodeRef key, Object... args);

    /**
     * Report if audit behaviour should be invoked on error. It could be we look at the error and filter - this is not supported at the moment.
     * 
     * @param auditState,
     * @param mi
     * @return
     */
    public AuditMode onError(AuditMode auditMode, String application,  String description,
            NodeRef key, Object... args);

    /**
     * Get the optional parameters that are to be recorded
     * 
     * @param mi
     * @return
     */
    public RecordOptions getAuditRecordOptions(String application);
}
