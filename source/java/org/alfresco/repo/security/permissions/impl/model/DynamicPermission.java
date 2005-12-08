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
package org.alfresco.repo.security.permissions.impl.model;

import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.dom4j.Element;

/**
 * The definition of a required permission
 * 
 * @author andyh
 */
public class DynamicPermission extends AbstractPermission implements XMLModelInitialisable
{
    private static final String EVALUATOR = "evaluator";
    
    private String evaluatorFullyQualifiedClassName;
    
    public DynamicPermission(QName typeQName)
    {
        super(typeQName);
    }

    public void initialise(Element element, NamespacePrefixResolver nspr, PermissionModel permissionModel)
    {
        super.initialise(element, nspr, permissionModel);
        evaluatorFullyQualifiedClassName = element.attributeValue(EVALUATOR);   
    }

    public String getEvaluatorFullyQualifiedClassName()
    {
        return evaluatorFullyQualifiedClassName;
    }
}
