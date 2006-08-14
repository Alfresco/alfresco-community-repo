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
package org.alfresco.repo.action;

import java.io.Serializable;

import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.namespace.QName;

/**
 * Parameter definition implementation class.
 * 
 * @author Roy Wetherall
 */
public class ParameterDefinitionImpl implements ParameterDefinition, Serializable
{
    /** 
     * Serial version UID
     */
    private static final long serialVersionUID = 3976741384558751799L;

    /**
     * The name of the parameter
     */
    private String name;
    
    /**
     * The type of the parameter
     */
    private QName type;
    
    /**
     * Is this a multi-valued parameter?
     */
    private boolean isMultiValued;
    
    /**
     * The display label
     */
    private String displayLabel;
	
	/**
	 * Indicates whether it is mandatory for the parameter to be set
	 */
	private boolean isMandatory = false;

    /**
     * Constructor
     * 
     * @param name          the name of the parameter
     * @param type          the type of the parameter
     * @param displayLabel  the display label
     */
    public ParameterDefinitionImpl(
            String name, 
            QName type,
            boolean isMandatory,
            String displayLabel)
    {
        this.name = name;
        this.type = type;
        this.displayLabel = displayLabel;
		this.isMandatory = isMandatory;
        this.isMultiValued = false;
    }

    /**
     * Constructor
     * 
     * @param name          the name of the parameter
     * @param type          the type of the parameter
     * @param displayLabel  the display label
     */
    public ParameterDefinitionImpl(
            String name, 
            QName type,
            boolean isMandatory,
            String displayLabel,
            boolean isMultiValued)
    {
        this.name = name;
        this.type = type;
        this.displayLabel = displayLabel;
        this.isMandatory = isMandatory;
        this.isMultiValued = isMultiValued;
    }

    /**
     * @see org.alfresco.service.cmr.action.ParameterDefinition#getName()
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @see org.alfresco.service.cmr.action.ParameterDefinition#getType()
     */
    public QName getType()
    {
        return this.type;
    }
	
	/**
	 * @see org.alfresco.service.cmr.action.ParameterDefinition#isMandatory()
	 */
	public boolean isMandatory() 
	{
		return this.isMandatory;
	}

    /**
     * @see org.alfresco.service.cmr.action.ParameterDefinition#isMultiValued()
     */
    public boolean isMultiValued()
    {
        return this.isMultiValued;
    }
    
    /**
     * @see org.alfresco.service.cmr.action.ParameterDefinition#getDisplayLabel()
     */
    public String getDisplayLabel()
    {
        return this.displayLabel;
    }
}
