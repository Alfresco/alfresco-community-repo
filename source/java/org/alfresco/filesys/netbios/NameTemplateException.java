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
package org.alfresco.filesys.netbios;

/**
 * Name Template Exception Class
 * <p>
 * Thrown when a NetBIOS name template contains invalid characters or is too long.
 */
public class NameTemplateException extends Exception
{
    private static final long serialVersionUID = 3256439188231762230L;

    /**
     * Default constructor.
     */
    public NameTemplateException()
    {
        super();
    }

    /**
     * Class constructor
     * 
     * @param s java.lang.String
     */
    public NameTemplateException(String s)
    {
        super(s);
    }
}
