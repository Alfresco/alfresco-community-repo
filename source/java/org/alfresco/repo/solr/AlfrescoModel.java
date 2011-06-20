package org.alfresco.repo.solr;

import org.alfresco.service.cmr.dictionary.ModelDefinition;

/**
 * Represents an alfresco model and checksum.
 * 
 * @since 4.0
 */
public class AlfrescoModel
{
    private ModelDefinition modelDef;
    private long checksum;

    protected AlfrescoModel(ModelDefinition modelDef)
    {
        this.modelDef = modelDef;
        this.checksum = modelDef.getChecksum(ModelDefinition.XMLBindingType.SOLR);
    }

    public ModelDefinition getModelDef()
    {
        return modelDef;
    }

    public long getChecksum()
    {
        return checksum;
    }
    
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }

        if(!(other instanceof AlfrescoModel))
        {
            return false;
        }

        AlfrescoModel model = (AlfrescoModel)other;
        return (modelDef.getName().equals(model.getModelDef().getName()) &&
        		checksum == model.getChecksum());
    }

    public int hashcode()
    {
    	int result = 17;
        result = 31 * result + modelDef.hashCode();
        result = 31 * result + Long.valueOf(checksum).hashCode();
        return result;
    }
}
