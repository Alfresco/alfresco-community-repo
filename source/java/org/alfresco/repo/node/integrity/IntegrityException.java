package org.alfresco.repo.node.integrity;

import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Thrown when an integrity check fails
 * 
 * @author Derek Hulley
 */
public class IntegrityException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = -5036557255854195669L;

    private List<IntegrityRecord> records;
    
    public IntegrityException(List<IntegrityRecord> records)
    {
        super("Integrity failure");
        this.records = records;
    }

    public IntegrityException(String msg, List<IntegrityRecord> records)
    {
        super(msg);
        this.records = records;
    }

    /**
     * @return Returns a list of all the integrity violations
     */
    public List<IntegrityRecord> getRecords()
    {
        return records;
    }
}
