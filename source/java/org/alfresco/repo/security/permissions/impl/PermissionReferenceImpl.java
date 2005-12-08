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
 * A simple permission reference (not persisted).
 * 
 * A permission is identified by name for a given type, which is identified by its qualified name.
 * 
 * @author andyh
 */
public class PermissionReferenceImpl extends AbstractPermissionReference
{
    private QName qName;
    
    private String name;

    public PermissionReferenceImpl(QName qName, String name)
    {
        this.qName = qName;
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public QName getQName()
    {
        return qName;
    }
    
    

}
