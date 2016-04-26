package org.alfresco.service.cmr.replication;

public class DisabledReplicationJobException extends ReplicationServiceException
{

    private static final long serialVersionUID = 1L;

    public DisabledReplicationJobException(String message)
    {
        super(message);
    }
    
    public DisabledReplicationJobException(String message, Throwable source)
    {
        super(message, source);
    }
}
