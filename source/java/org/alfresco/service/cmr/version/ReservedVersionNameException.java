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
package org.alfresco.service.cmr.version;

import java.text.MessageFormat;

/**
 * @author Roy Wetherall
 */
public class ReservedVersionNameException extends RuntimeException
{
    /**
     * Serial verison UID
     */
    private static final long serialVersionUID = 3690478030330015795L;

    /**
     * Error message
     */
    private static final String MESSAGE = "The version property name {0} clashes with a reserved verison property name.";
    
    /**
     * Constructor
     * 
     * @param propertyName  the name of the property that clashes with
     *                      a reserved property name
     */
    public ReservedVersionNameException(String propertyName)
    {
        super(MessageFormat.format(MESSAGE, new Object[]{propertyName}));
    }
}
