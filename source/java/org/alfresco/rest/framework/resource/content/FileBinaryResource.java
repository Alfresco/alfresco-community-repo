
package org.alfresco.rest.framework.resource.content;

import java.io.File;

/**
 * A binary resource based on a File.
 * 
 * @author Gethin James
 */
public class FileBinaryResource implements BinaryResource
{
    final File file;

    public FileBinaryResource(File file)
    {
        super();
        this.file = file;
    }

    public File getFile()
    {
        return this.file;
    }
}
