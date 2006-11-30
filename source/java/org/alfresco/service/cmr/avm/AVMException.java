/*
 * Copyright (C) 2006 Alfresco, Inc.
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

package org.alfresco.service.cmr.avm;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Class for generic AVM Exceptions.
 * @author britt
 */
public class AVMException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = -4894449240293309025L;

    /**
     * @param msgId
     */
    public AVMException(String msgId)
    {
        super(msgId);
    }

    /**
     * @param msgId
     * @param msgParams
     */
    public AVMException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }

    /**
     * @param msgId
     * @param cause
     */
    public AVMException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }

    /**
     * @param msgId
     * @param msgParams
     * @param cause
     */
    public AVMException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }
}
