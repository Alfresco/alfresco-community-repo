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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.audit.model;

import org.alfresco.repo.audit.ApplicationAuditModel;
import org.alfresco.repo.audit.AuditMode;
import org.alfresco.repo.audit.RecordOptions;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ApplicationAuditEntry extends AbstractNamedAuditEntry implements ApplicationAuditModel
{
    private static Log s_logger = LogFactory.getLog(ApplicationAuditEntry.class);

    public ApplicationAuditEntry()
    {
        super();
    }

    public AuditMode beforeExecution(AuditMode auditMode, String application, String description, NodeRef key, Object... args)
    {
        if(s_logger.isDebugEnabled())
        {
            s_logger.debug("Evaluating if application is audited ..."+application);
        }
        return getEffectiveAuditMode();
    }

    public AuditMode afterExecution(AuditMode auditMode, String application, String description, NodeRef key, Object... args)
    {
        throw new UnsupportedOperationException();
    }

    public AuditMode onError(AuditMode auditMode, String application, String description, NodeRef key, Object... args)
    {
        throw new UnsupportedOperationException();
    }

    public RecordOptions getAuditRecordOptions(String application)
    {
        throw new UnsupportedOperationException();
    }

  
}
