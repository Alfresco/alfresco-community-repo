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
package org.alfresco.service.cmr.repository;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * @author Kevin Roast
 */
public class TemplateException extends AlfrescoRuntimeException
{
    /**
     * @param msgId
     */
    public TemplateException(String msgId)
    {
        super(msgId);
    }

    /**
     * @param msgId
     * @param cause
     */
    public TemplateException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }
    
    /**
     * @param msgId
     * @param params
     */
    public TemplateException(String msgId, Object[] params)
    {
        super(msgId, params);
    }
    
    /**
     * @param msgId
     * @param msgParams
     * @param cause
     */
    public TemplateException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }
}
