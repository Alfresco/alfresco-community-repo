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
package org.alfresco.repo.search.transaction;

public class XidException extends RuntimeException
{

    /**
     * 
     */
    private static final long serialVersionUID = 3257847696969840185L;

    public XidException()
    {
        super();
    }

    public XidException(String message)
    {
        super(message);
    }

    public XidException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public XidException(Throwable cause)
    {
        super(cause);
    }

}
