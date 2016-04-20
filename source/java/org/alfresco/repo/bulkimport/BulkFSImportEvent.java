package org.alfresco.repo.bulkimport;

import org.springframework.context.ApplicationEvent;

/**
 * A class of event that notifies the listener of a significant event relating to a bulk filesystem
 * import. Useful for Monitoring purposes.
 * 
 * @since 4.0
 */
public class BulkFSImportEvent extends ApplicationEvent
{
	private static final long serialVersionUID = 6249867689460133967L;

	/**
     * The Constructor.
     * 
     * @param source
     *            the source index monitor
     */
    public BulkFSImportEvent(BulkFilesystemImporter source)
    {
        super(source);
    }
    
    public BulkFilesystemImporter getBulkFilesystemImporter()
    {
    	return (BulkFilesystemImporter)source;
    }
}
