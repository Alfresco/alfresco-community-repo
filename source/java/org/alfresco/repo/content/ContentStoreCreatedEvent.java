package org.alfresco.repo.content;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.content.filestore.FileContentStore;
import org.springframework.context.ApplicationEvent;

/**
 * A class of event that notifies the listener of the existence of a {@link FileContentStore}. Useful for Monitoring
 * purposes.
 * 
 * @author dward
 * @since 3.1
 */
public class ContentStoreCreatedEvent extends ApplicationEvent
{
    private static final long serialVersionUID = 7090069096441126707L;
    protected transient Map<String, Serializable> extendedEventParams;

    /**
     * The Constructor.
     * 
     * @param source
     *            the source content store
     * @param extendedEventParams Map<String, Serializable>
     */
    public ContentStoreCreatedEvent(ContentStore source, Map<String, Serializable> extendedEventParams)
    {
        super(source);
        this.extendedEventParams = extendedEventParams;
    }
    
    /**
     * @return      Returns the source {@link ContentStore}
     */
    public ContentStore getContentStore()
    {
        return (ContentStore) getSource();
    }
    
    public Map<String, Serializable> getExtendedEventParams()
    {
    	return extendedEventParams;
    }
}
