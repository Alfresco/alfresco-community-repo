/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
