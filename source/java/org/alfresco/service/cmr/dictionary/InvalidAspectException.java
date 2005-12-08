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
 * Thrown when a reference to an <b>aspect</b> is incorrect.
 * 
 * @author Derek Hulley
 */
public class InvalidAspectException extends InvalidClassException
{
    private static final long serialVersionUID = 3257290240330051893L;

    public InvalidAspectException(QName aspectName)
    {
        super(null, aspectName);
    }

    public InvalidAspectException(String msg, QName aspectName)
    {
        super(msg, aspectName);
    }

    /**
     * @return Returns the offending aspect name
     */
    public QName getAspectName()
    {
        return getClassName();
    }
}
