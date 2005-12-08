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
package org.alfresco.repo.security.permissions.impl.model;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Exceptions related to the permissions model
 * 
 * @author andyh
 */
public class PermissionModelException extends AlfrescoRuntimeException
{

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -5156253607792153538L;

    public PermissionModelException(String msg)
    {
        super(msg);
    }

    public PermissionModelException(String msg, Throwable cause)
    {
        super(msg, cause);
    }

}
