/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.dictionary;

import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
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
     * Gets the specified property constraint
     * 
     * @param name the qualified name of the property constraint
     * @return
     */
    public ConstraintDefinition getConstraint(QName name);
    
    /**
     * Gets the specified association
     * 
     * @param name  name of the association
     * @return  association definition
     */
    public AssociationDefinition getAssociation(QName name);
    
}
