package org.alfresco.repo.action;

import java.io.Serializable;

import org.alfresco.service.cmr.action.ParameterConstraint;
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
	
    /** Parameter constraint name */
    private String parameterConstraintName;
    
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
     * Constructor
     * 
     * @param name String
     * @param type QName
     * @param isMandatory boolean
     * @param displayLabel String
     * @param isMultiValued boolean
     * @param parameterConstraintName String
     */
    public ParameterDefinitionImpl(
            String name, 
            QName type,
            boolean isMandatory,
            String displayLabel,
            boolean isMultiValued,
            String parameterConstraintName)
    {
        this(name, type, isMandatory, displayLabel, isMultiValued);
        this.parameterConstraintName = parameterConstraintName;
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
    
    /**
     * @see org.alfresco.service.cmr.action.ParameterDefinition#getParameterConstraintName()
     */
    public String getParameterConstraintName()
    {
        return this.parameterConstraintName;
    }
}
