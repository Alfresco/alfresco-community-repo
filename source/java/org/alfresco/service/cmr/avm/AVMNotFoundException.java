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


/**
 * This is the exception thrown when a node is not found.
 * @author britt
 */
public class AVMNotFoundException extends AVMException
{
    private static final long serialVersionUID = -8131080195448129281L;

    /**
     * @param msgId
     * @param msgParams
     * @param cause
     */
    public AVMNotFoundException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param msgId
     * @param msgParams
     */
    public AVMNotFoundException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param msgId
     * @param cause
     */
    public AVMNotFoundException(String msgId, Throwable cause)
    {
        super(msgId, cause);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param msgId
     */
    public AVMNotFoundException(String msgId)
    {
        super(msgId);
        // TODO Auto-generated constructor stub
    }
}
