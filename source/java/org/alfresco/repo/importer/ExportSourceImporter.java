/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.importer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.view.ImporterBinding;
import org.alfresco.service.cmr.view.ImporterService;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.util.TempFileProvider;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

public class ExportSourceImporter implements ImporterJobSPI
{
    private ImporterService importerService;

    private ExportSource exportSource;

    private StoreRef storeRef;

    private String path;

    public ExportSourceImporter()
    {
        super();
    }

    public void setImporterService(ImporterService importerService)
    {
        this.importerService = importerService;
    }

    public void setExportSource(ExportSource exportSource)
    {
        this.exportSource = exportSource;
    }

    public void doImport()
    {
        try
        {
            File tempFile = TempFileProvider.createTempFile("ExportSourceImporter-", ".xml");
            Writer writer = new BufferedWriter(new FileWriter(tempFile));
            XMLWriter xmlWriter = createXMLExporter(writer);
            exportSource.generateExport(xmlWriter);
            xmlWriter.close();

            Reader reader = new BufferedReader(new FileReader(tempFile));

            Location location = new Location(storeRef);
            location.setPath(path);

            importerService.importView(reader, location, REPLACE_BINDING, null);
            reader.close();
        }
        catch (IOException io)
        {
            throw new ExportSourceImporterException("Failed to import", io);
        }
    }

    private XMLWriter createXMLExporter(Writer writer)
    {
        // Define output format
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setNewLineAfterDeclaration(false);
        format.setIndentSize(3);
        format.setEncoding("UTF-8");

        // Construct an XML Exporter

        XMLWriter xmlWriter = new XMLWriter(writer, format);
        return xmlWriter;
    }

    private static ImporterBinding REPLACE_BINDING = new ImporterBinding()
    {

        public UUID_BINDING getUUIDBinding()
        {
            return UUID_BINDING.REPLACE_EXISTING;
        }

        public String getValue(String key)
        {
            return null;
        }

    };

}
