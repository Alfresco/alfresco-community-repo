/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.util.schemacomp;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.util.ParameterCheck;
import org.alfresco.util.TempFileProvider;
import org.springframework.context.ApplicationContext;

/**
 * Given a set of database object prefixes (e.g. "alf_", "jbpm_") and
 * a file name template (e.g. "AlfrescoSchema-MySQL-{0}-") will produce a set of files,
 * one per database object prefix of the form:
 * <pre>
 *   AlfrescoSchema-MySQL-alf_-2334829.xml
 * </pre>
 * Where the database object prefix is substituted for parameter {0} and the random number
 * is produced by the File.createTempFile() method. The suffix .xml is always used.
 * 
 * @author Matt Ward
 */
public class MultiFileDumper
{
    private final String[] dbPrefixes;
    private final File directory;
    private final String fileNameTemplate;
    private final DbToXMLFactory dbToXMLFactory;
    private final static String fileNameSuffix = ".xml";
    public final static String[] DEFAULT_PREFIXES = new String[] { "alf_", "jbpm_", "act_" };
    private String defaultSchemaName;
    
    
    /**
     * Constructor with all available arguments.
     * 
     * @param dbPrefixes
     * @param directory
     * @param fileNameTemplate
     * @param dbToXMLFactory
     * @param defaultSchemaName
     */
    public MultiFileDumper(String[] dbPrefixes, File directory, String fileNameTemplate, DbToXMLFactory dbToXMLFactory, String defaultSchemaName)
    {
        ParameterCheck.mandatory("dbPrefixes", dbPrefixes);
        ParameterCheck.mandatory("directory", directory);
        ParameterCheck.mandatory("fileNameTemplate", fileNameTemplate);
        ParameterCheck.mandatory("dbToXMLFactory", dbToXMLFactory);
        if (dbPrefixes.length == 0)
        {
            throw new IllegalArgumentException("At least one database object prefix is required.");
        }
        
        this.dbPrefixes = dbPrefixes;
        this.directory = directory;
        this.fileNameTemplate = fileNameTemplate;
        this.dbToXMLFactory = dbToXMLFactory;
        this.defaultSchemaName = defaultSchemaName;
    }
    
    
    
    /**
     * Construct a MultiFileDumper with the {@link MultiFileDumper#DEFAULT_PREFIXES}.
     * 
     * @param directory
     * @param fileNameTemplate
     * @param dbToXMLFactory
     * @param defaultSchemaName can be null
     */
    public MultiFileDumper(File directory, String fileNameTemplate, DbToXMLFactory dbToXMLFactory, String defaultSchemaName)
    {
        this(DEFAULT_PREFIXES, directory, fileNameTemplate, dbToXMLFactory, defaultSchemaName);
    }


    public List<File> dumpFiles()
    {
        List<File> files = new ArrayList<File>(dbPrefixes.length);
        
        for (String dbPrefix : dbPrefixes)
        {
            String fileNamePrefix = getFileNamePrefix(dbPrefix);
            File outputFile = TempFileProvider.createTempFile(fileNamePrefix, fileNameSuffix, directory);
            files.add(outputFile);
            DbToXML dbToXML = dbToXMLFactory.create(outputFile, dbPrefix);
            dbToXML.setDbSchemaName(defaultSchemaName);
            dbToXML.execute();
        }
        
        return files;
    }
    
    
    private String getFileNamePrefix(String dbPrefix)
    {
        MessageFormat formatter = new MessageFormat(fileNameTemplate);
        return formatter.format(new Object[] { dbPrefix });
    }

    public interface DbToXMLFactory
    {
        DbToXML create(File outputFile, String dbPrefix);
    }

    public static class DbToXMLFactoryImpl implements DbToXMLFactory
    {
        private ApplicationContext ctx;

        public DbToXMLFactoryImpl(ApplicationContext ctx)
        {
            this.ctx = ctx;
        }

        @Override
        public DbToXML create(File outputFile, String dbPrefix)
        {
            return new DbToXML(ctx, outputFile, dbPrefix);
        }
    }
}
