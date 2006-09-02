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
package org.alfresco.repo.audit;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Audit related exceptions.
 * 
 * @author Andy Hind
 */
public class AuditException extends AlfrescoRuntimeException
{

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -7947190775692164588L;

    public AuditException(String msgId)
    {
        super(msgId);
    }

    public AuditException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }

    public AuditException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }

    public AuditException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }

}
