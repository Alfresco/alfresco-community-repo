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
package org.alfresco.service.cmr.lock;

import java.text.MessageFormat;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Roy Wetherall
 */
public class UnableToAquireLockException extends RuntimeException
{
    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 3258689892710889781L;
    
    /**
     * Error message
     */
    private final static String ERROR_MESSAGE = "The node (id: {0})could not be locked since it" +
            " is already locked by antoher user.";

    /**
     * Constructor
     */
    public UnableToAquireLockException(NodeRef nodeRef)
    {
        super(MessageFormat.format(ERROR_MESSAGE, new Object[]{nodeRef.getId()}));
    }
}
