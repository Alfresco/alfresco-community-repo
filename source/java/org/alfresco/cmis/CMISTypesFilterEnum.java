package org.alfresco.cmis;

/**
 * CMIS Types Filter Enum
 *  
 * @author davidc
 */
public enum CMISTypesFilterEnum implements EnumLabel
{
    DOCUMENTS("documents"),
    FOLDERS("folders"),
    POLICIES("policies"),
    ANY("any");
    
    
    private String label;
    
    /**
     * Construct
     * 
     * @param label
     */
    CMISTypesFilterEnum(String label)
    {
        this.label = label;
    }

    /* (non-Javadoc)
     * @see org.alfresco.cmis.EnumLabel#label()
     */
    public String getLabel()
    {
        return label;
    }

    public static EnumFactory<CMISTypesFilterEnum> FACTORY = new EnumFactory<CMISTypesFilterEnum>(CMISTypesFilterEnum.class, ANY); 
}