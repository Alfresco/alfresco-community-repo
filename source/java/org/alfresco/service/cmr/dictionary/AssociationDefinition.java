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
 * Read-only definition of an Association.
 *  
 * @author David Caruana
 *
 */
public interface AssociationDefinition
{
    
    /**
     * @return  defining model
     */
    public ModelDefinition getModel();
    
    /**
     * @return  the qualified name
     */
    public QName getName();

    /**
     * @return the human-readable title 
     */
    public String getTitle();
    
    /**
     * @return the human-readable description 
     */
    public String getDescription();
    
    /**
     * Is this a child association?
     * 
     * @return true => child,  false => general relationship
     */
    public boolean isChild();
    
    /**
     * Is this association maintained by the Repository?
     * 
     * @return true => system maintained, false => client may maintain 
     */
    public boolean isProtected();

    /**
     * @return the source class
     */
    public ClassDefinition getSourceClass();

    /**
     * @return the role of the source class in this association? 
     */
    public QName getSourceRoleName();
    
    /**
     * Is the source class optional in this association?
     *  
     * @return true => cardinality > 0
     */
    public boolean isSourceMandatory();

    /**
     * Can there be many source class instances in this association? 
     * 
     * @return true => cardinality > 1, false => cardinality of 0 or 1
     */
    public boolean isSourceMany();

    /**
     * @return the target class  
     */
    public ClassDefinition getTargetClass();
    
    /**
     * @return the role of the target class in this association? 
     */
    public QName getTargetRoleName();
    
    /**
     * Is the target class optional in this association?
     *  
     * @return true => cardinality > 0
     */
    public boolean isTargetMandatory();
    
    /**
     * Is the target class is mandatory, it is enforced?
     *  
     * @return true => enforced
     */
    public boolean isTargetMandatoryEnforced();

    /**
     * Can there be many target class instances in this association? 
     * 
     * @return true => cardinality > 1, false => cardinality of 0 or 1
     */
    public boolean isTargetMany();

}
