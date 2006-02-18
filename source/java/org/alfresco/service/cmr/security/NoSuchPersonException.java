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
package org.alfresco.service.cmr.security;

import org.alfresco.repo.security.person.PersonException;

/**
 * Thrown when a person doesn't exist and can't be created.
 * 
 * @author Derek Hulley
 */
public class NoSuchPersonException extends PersonException
{
    private static final long serialVersionUID = -8514361120995433997L;

    private final String userName;
    
    public NoSuchPersonException(String userName)
    {
        super(String.format("User does not exist and could not be created: %s", userName));
        this.userName = userName;
    }

    public String getUserName()
    {
        return userName;
    }
}
