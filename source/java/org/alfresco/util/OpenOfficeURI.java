package org.alfresco.util;

import java.io.File;
import java.io.IOException;

/**
 * A class that attempts to embody OpenOffice's rules for encoding file URIs which appear to differ from Java's. A
 * Windows style path is always prefixed "file:///" whereas a unix one is prefixed "file://".
 * 
 * @author dward
 */
public class OpenOfficeURI
{

    /** The source file. */
    private File source;

    /**
     * Instantiates a new open office URI.
     * 
     * @param source
     *            the source file name to convert to a URI
     * @throws IOException
     *             if the string cannot be resolved to a canonical file path
     */
    public OpenOfficeURI(String source) throws IOException
    {
        this.source = new File(source.replaceAll(" ", "%20")).getCanonicalFile();
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        String absolute = this.source.getAbsolutePath();
        if (File.separatorChar != '/')
        {
            absolute = absolute.replace(File.separatorChar, '/');
        }
        return (absolute.startsWith("/") ? "file://" : "file:///") + absolute;
    }

}
