package org.alfresco.repo.importer;

import java.io.InputStream;

import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;


public interface ImportContentHandler extends ContentHandler, ErrorHandler
{
    public void setImporter(Importer importer);

    public InputStream importStream(String content);

}
