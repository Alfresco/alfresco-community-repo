package org.alfresco.repo.importer;

import org.dom4j.io.XMLWriter;

public interface ExportSource
{
    /**
     * Generate XML suitable for use with the importer.
     * 
     * @param writer XMLWriter
     */
    public void generateExport(XMLWriter writer);
}
