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
package org.alfresco.service.cmr.dictionary;

import org.alfresco.service.namespace.QName;

/**
 * Thrown when an operation cannot be performed because a type is not recognised
 * by the data dictionary
 * 
 * @author Derek Hulley
 */
public class InvalidTypeException extends InvalidClassException
{
    private static final long serialVersionUID = 3256722870754293558L;

    public InvalidTypeException(QName typeName)
    {
        super(null, typeName);
    }

    public InvalidTypeException(String msg, QName typeName)
    {
        super(msg, typeName);
    }

    /**
     * @return Returns the offending type name
     */
    public QName getTypeName()
    {
        return getClassName();
    }
}
