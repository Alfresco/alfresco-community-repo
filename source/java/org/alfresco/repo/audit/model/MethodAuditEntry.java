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

}
