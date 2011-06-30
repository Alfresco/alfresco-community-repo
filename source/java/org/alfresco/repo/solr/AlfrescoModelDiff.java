package org.alfresco.repo.solr;

import org.alfresco.service.namespace.QName;

/**
 * Represents a diff between the set of current repository Alfresco models and the set maintained in SOLR.
 * The diff can represent a new, changed or removed Alfresco model. For a new model the newChecksum is
 * populated; for a changed model both checksums are populated; for a removed model neither checksum is populated.
 * 
 * @since 4.0
 */
public class AlfrescoModelDiff
{
    public static enum TYPE
    {
        NEW, CHANGED, REMOVED;
    };
    
    private String modelName;
    private TYPE type;
    private Long oldChecksum;
    private Long newChecksum;

    /**
     * use full model name or it will be converted to the prefix form - as we are requesting the model it may not be on the other side - so the namespace is unknown.
     * @param modelName
     * @param type
     * @param oldChecksum
     * @param newChecksum
     */
    public AlfrescoModelDiff(String modelName, TYPE type, Long oldChecksum, Long newChecksum)
    {
        super();
        this.modelName = modelName;
        this.type = type;
        this.oldChecksum = oldChecksum;
        this.newChecksum = newChecksum;
    }
    
    public AlfrescoModelDiff(QName modelName, TYPE type, Long oldChecksum, Long newChecksum)
    {
       this(modelName.toString(), type, oldChecksum, newChecksum);
    }

    public String getModelName()
    {
        return modelName;
    }

    public TYPE getType()
    {
        return type;
    }

    public Long getOldChecksum()
    {
        return oldChecksum;
    }

    public Long getNewChecksum()
    {
        return newChecksum;
    }
}
