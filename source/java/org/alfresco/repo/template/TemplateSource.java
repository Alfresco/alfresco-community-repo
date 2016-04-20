
package org.alfresco.repo.template;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public interface TemplateSource
{
    Reader getReader(String encoding) throws IOException;
    
    void close() throws IOException;
    
    long lastModified();
    
    InputStream getResource(String name);
}
