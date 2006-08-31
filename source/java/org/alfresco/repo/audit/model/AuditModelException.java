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

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Exceptions from the audit model package.
 * 
 * @author Andy Hind
 */
public class AuditModelException extends AlfrescoRuntimeException
{

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -2527034441058184109L;

    public AuditModelException(String msgId)
    {
        super(msgId);
    }

    public AuditModelException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }

    public AuditModelException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }

    public AuditModelException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }

}
