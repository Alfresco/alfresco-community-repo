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
package org.alfresco.repo.security.permissions.impl;

import org.alfresco.service.namespace.QName;

/**
 * Store and read the definition of a required permission.
 * 
 * @author andyh
 */
public class RequiredPermission extends PermissionReferenceImpl
{
    public enum On {
        PARENT, NODE, CHILDREN
    };

    private On on;

    boolean implies;

    public RequiredPermission(QName qName, String name, On on, boolean implies)
    {
        super(qName, name);
        this.on = on;
        this.implies = implies;
    }

    public boolean isImplies()
    {
        return implies;
    }

    public On getOn()
    {
        return on;
    }

}
