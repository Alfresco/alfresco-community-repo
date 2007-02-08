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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
