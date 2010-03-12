/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.dictionary;  

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * Compiled Model Difference
 * 
 * @author JanV
 *
 */
public class M2ModelDiff
{
	public static final String DIFF_CREATED     = "created";
	public static final String DIFF_UPDATED     = "updated";
	public static final String DIFF_UPDATED_INC = "updated_inc"; // incremental update
	public static final String DIFF_DELETED     = "deleted";
	public static final String DIFF_UNCHANGED   = "unchanged";
	 
	public static final String TYPE_TYPE = "TYPE";
	public static final String TYPE_ASPECT = "ASPECT";
	public static final String TYPE_DEFAULT_ASPECT = "DEFAULT_ASPECT";
	public static final String TYPE_PROPERTY = "PROPERTY";
	public static final String TYPE_ASSOCIATION = "ASSOCIATION";
	public static final String TYPE_CONSTRAINT = "TYPE_CONSTRAINT";
	
	private QName elementName;   
	private String elementType;  
	private String diffType;
		
	public M2ModelDiff(QName elementName, String elementType, String diffType)
    {
         // Check that all the passed values are not null        
         ParameterCheck.mandatory("elementName", elementName);
         ParameterCheck.mandatoryString("elementType", elementType);
         ParameterCheck.mandatoryString("diffType", diffType);
        
         if ((!elementType.equals(TYPE_TYPE)) &&
             (!elementType.equals(TYPE_ASPECT)) &&
             (!elementType.equals(TYPE_DEFAULT_ASPECT)) &&
             (!elementType.equals(TYPE_PROPERTY)) &&
             (!elementType.equals(TYPE_ASSOCIATION)) &&
             (!elementType.equals(TYPE_CONSTRAINT))
             )
         {
             throw new AlfrescoRuntimeException("Unknown element type = " + elementType);
         }
        
         if ((! diffType.equals(DIFF_CREATED)) && 
             (! diffType.equals(DIFF_UPDATED)) && 
             (! diffType.equals(DIFF_UPDATED_INC)) && 
             (! diffType.equals(DIFF_DELETED)) && 
             (! diffType.equals(DIFF_UNCHANGED)))
         {
             throw new AlfrescoRuntimeException("Unknown diff type = " + diffType);
         }
        
         this.elementName = elementName;
         this.elementType = elementType;
         this.diffType = diffType;
     }

     public QName getElementName()
     {
         return elementName;
     }

     public String getElementType()
     {
         return elementType;
     }

     public String getDiffType()
     {
         return diffType;
     }

     public String toString()
     {
         return new String(elementType + " " + elementName + " " + diffType);
     }
 }