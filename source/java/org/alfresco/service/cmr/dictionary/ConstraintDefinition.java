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
 * Property constraint definition
 * 
 * @author Derek Hulley
 */
public interface ConstraintDefinition
{
    /**
     * @return defining model 
     */
    public ModelDefinition getModel();
    
    /**
     * @return Returns the qualified name of the constraint
     */
    public QName getName();
    
    /**
     * @return Returns the constraint implementation
     */
    public Constraint getConstraint();
}
