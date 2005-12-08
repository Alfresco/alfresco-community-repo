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
package org.alfresco.filesys.server.filesys;

/**
 * <p>
 * This error is generated when a tree connection has no free file slots. The new file open request
 * will be rejected by the server.
 */
public class TooManyFilesException extends Exception
{
    private static final long serialVersionUID = 4051332218943060273L;

    /**
     * TooManyFilesException constructor.
     */
    public TooManyFilesException()
    {
        super();
    }

    /**
     * TooManyFilesException constructor.
     * 
     * @param s java.lang.String
     */
    public TooManyFilesException(String s)
    {
        super(s);
    }
}