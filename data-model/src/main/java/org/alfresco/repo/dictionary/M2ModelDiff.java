/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.dictionary;  

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.dictionary.NamespaceDefinition;
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
	public static final String TYPE_NAMESPACE = "TYPE_NAMESPACE";

    private static final String ERR_UNKNOWN_ELEMENT_TYPE = "d_dictionary.model_diff.element_type.unknown";
    private static final String ERR_UNKNOWN_DIFF_TYPE = "d_dictionary.model_diff.diff_type.unknown";

	private QName elementName;   
	private NamespaceDefinition namespace;
	private String elementType;  
	private String diffType;
		
	public M2ModelDiff(QName elementName, String elementType, String diffType)
    {
        initModelDiff(elementName, elementType, diffType);
    }

    public M2ModelDiff(QName elementName, NamespaceDefinition namespace, String elementType, String diffType)
    {
        initModelDiff(elementName, elementType, diffType);
        this.namespace = namespace;
    }

    private void initModelDiff(QName elementName, String elementType, String diffType)
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
            (!elementType.equals(TYPE_CONSTRAINT)) &&
            (!elementType.equals(TYPE_NAMESPACE))
            )
        {
            throw new AlfrescoRuntimeException(ERR_UNKNOWN_ELEMENT_TYPE, new Object[] { elementType });
        }

        if ((! diffType.equals(DIFF_CREATED)) &&
            (! diffType.equals(DIFF_UPDATED)) &&
            (! diffType.equals(DIFF_UPDATED_INC)) &&
            (! diffType.equals(DIFF_DELETED)) &&
            (! diffType.equals(DIFF_UNCHANGED)))
        {
            throw new AlfrescoRuntimeException(ERR_UNKNOWN_DIFF_TYPE, new Object[] { diffType });
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

     public NamespaceDefinition getNamespaceDefinition()
     {
        return namespace;
     }
     
     public String toString()
     {
         return new String(elementType + " " + elementName + " " + diffType);
     }
 }