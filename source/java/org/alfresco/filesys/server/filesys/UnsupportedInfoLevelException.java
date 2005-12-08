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
 * This error is generated when a request is made for an information level that is not currently
 * supported by the SMB server.
 */
public class UnsupportedInfoLevelException extends Exception
{
    private static final long serialVersionUID = 3762538905790395444L;

    /**
     * Class constructor.
     */
    public UnsupportedInfoLevelException()
    {
        super();
    }

    /**
     * Class constructor.
     * 
     * @param str java.lang.String
     */
    public UnsupportedInfoLevelException(String str)
    {
        super(str);
    }
}