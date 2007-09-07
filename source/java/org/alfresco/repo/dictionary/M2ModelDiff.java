/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;

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
             (!elementType.equals(TYPE_ASSOCIATION)))
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