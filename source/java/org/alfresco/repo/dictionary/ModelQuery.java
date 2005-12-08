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
package org.alfresco.repo.dictionary;

import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.QName;


/**
 * Access to model items.
 * 
 * @author David Caruana
 *
 */
/*package*/ interface ModelQuery
{
    /**
     * Gets the specified data type
     * 
     * @param name  name of the data type
     * @return  data type definition
     */
    public DataTypeDefinition getDataType(QName name);

    /**
     * Gets the data type for the specified Java Class
     * 
     * @param javaClass   the java class
     * @return  the data type definition (or null, if mapping is not available)
     */
    public DataTypeDefinition getDataType(Class javaClass);
    
    /**
     * Gets the specified type
     * 
     * @param name  name of the type
     * @return  type definition
     */
    public TypeDefinition getType(QName name);
    
    /**
     * Gets the specified aspect
     * 
     * @param name  name of the aspect
     * @return  aspect definition
     */
    public AspectDefinition getAspect(QName name);
    
    /**
     * Gets the specified class
     * 
     * @param name  name of the class
     * @return  class definition
     */
    public ClassDefinition getClass(QName name);
    
    /**
     * Gets the specified property
     * 
     * @param name  name of the property
     * @return  property definition
     */
    public PropertyDefinition getProperty(QName name);
    
    /**
     * Gets the specified association
     * 
     * @param name  name of the association
     * @return  association definition
     */
    public AssociationDefinition getAssociation(QName name);
    
}
