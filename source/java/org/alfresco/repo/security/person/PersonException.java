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
package org.alfresco.repo.security.person;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * All exceptions related to the person service.
 * 
 * @author Andy Hind
 */
public class PersonException extends AlfrescoRuntimeException
{

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 2802163127696444600L;

    public PersonException(String msgId)
    {
        super(msgId);
    }

    public PersonException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }

    public PersonException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }

    public PersonException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }

}
