/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.tools;

import java.io.File;
import java.io.InputStream;
import java.util.Locale;

import org.alfresco.repo.exporter.ACPExportPackageHandler;
import org.alfresco.repo.exporter.FileExportPackageHandler;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.view.ExportPackageHandler;
import org.alfresco.service.cmr.view.Exporter;
import org.alfresco.service.cmr.view.ExporterContext;
import org.alfresco.service.cmr.view.ExporterCrawlerParameters;
import org.alfresco.service.cmr.view.ExporterException;
import org.alfresco.service.cmr.view.ExporterService;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.service.namespace.QName;

/**
 * Alfresco Repository Export Tool
 * 
 * @author David Caruana
 */
public final class Export extends Tool
{
    /** Export Context */
    private ExportContext context;

    /**
     * Entry Point
     * 
     * @param args
     */
    public static void main(String[] args)
    {
        Tool tool = new Export();
        tool.start(args);
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.tools.Tool#getToolName() */
    protected @Override String getToolName()
    {
        return "Alfresco Repository Exporter";
    }

    /**
     * Process Export Tool command line arguments
     * 
     * @param args
     *            the arguments
     * @return the export context
     */
    protected @Override
    /* package */ ToolContext processArgs(String[] args)
    {
        context = new ExportContext();
        context.setLogin(true);

        int i = 0;
        while (i < args.length)
        {
            if (args[i].equals("-h") || args[i].equals("-help"))
            {
                context.setHelp(true);
                break;
            }
            else if (args[i].equals("-s") || args[i].equals("-store"))
            {
                i++;
                if (i == args.length || args[i].length() == 0)
                {
                    throw new ToolArgumentException("The value <store> for the parameter -store must be specified");
                }
                context.storeRef = new StoreRef(args[i]);
            }
            else if (args[i].equals("-p") || args[i].equals("-path"))
            {
                i++;
                if (i == args.length || args[i].length() == 0)
                {
                    throw new ToolArgumentException("The value <path> for the parameter -path must be specified");
                }
                context.path = args[i];
            }
            else if (args[i].equals("-d") || args[i].equals("-dir"))
            {
                i++;
                if (i == args.length || args[i].length() == 0)
                {
                    throw new ToolArgumentException("The value <dir> for the parameter -dir must be specified");
                }
                context.destDir = args[i];
            }
            else if (args[i].equals("-packagedir"))
            {
                i++;
                if (i == args.length || args[i].length() == 0)
                {
                    throw new ToolArgumentException("The value <packagedir> for the parameter -packagedir must be specified");
                }
                context.packageDir = args[i];
            }
            else if (args[i].equals("-user"))
            {
                i++;
                if (i == args.length || args[i].length() == 0)
                {
                    throw new ToolArgumentException("The value <user> for the option -user must be specified");
                }
                context.setUsername(args[i]);
            }
            else if (args[i].equals("-pwd"))
            {
                i++;
                if (i == args.length || args[i].length() == 0)
                {
                    throw new ToolArgumentException("The value <password> for the option -pwd must be specified");
                }
                context.setPassword(args[i]);
            }
            else if (args[i].equals("-root"))
            {
                context.self = true;
            }
            else if (args[i].equals("-nochildren"))
            {
                context.children = false;
            }
            else if (args[i].equals("-zip"))
            {
                context.zipped = true;
            }
            else if (args[i].equals("-overwrite"))
            {
                context.overwrite = true;
            }
            else if (args[i].equals("-quiet"))
            {
                context.setQuiet(true);
            }
            else if (args[i].equals("-verbose"))
            {
                context.setVerbose(true);
            }
            else if (i == (args.length - 1))
            {
                context.packageName = args[i];
            }
            else
            {
                throw new ToolArgumentException("Unknown option " + args[i]);
            }

            // next argument
            i++;
        }

        return context;
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.tools.Tool#displayHelp() */
    protected @Override
    /* package */ void displayHelp()
    {
        logError("Usage: export -user username -s[tore] store [options] packagename");
        logError("");
        logError("username: username for login");
        logError("store: the store to extract from in the form of scheme://store_name");
        logError("packagename: the filename to export to (with or without extension)");
        logError("");
        logError("Options:");
        logError(" -h[elp] display this help");
        logError(" -p[ath] the path within the store to extract from (default: /)");
        logError(" -d[ir] the destination directory to export to (default: current directory)");
        logError(" -pwd password for login");
        logError(" -packagedir the directory to place extracted content (default: dir/<packagename>)");
        logError(" -root extract the item located at export path");
        logError(" -nochildren do not extract children of the item at export path");
        logError(" -overwrite force overwrite of existing export package if it already exists");
        logError(" -quiet do not display any messages during export");
        logError(" -verbose report export progress");
        logError(" -zip export in zip format");
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.tools.Tool#execute() */
    protected @Override
    /* package */ int execute()
            throws ToolException
    {
        ExporterService exporter = getServiceRegistry().getExporterService();
        MimetypeService mimetypeService = getServiceRegistry().getMimetypeService();

        // create export package handler
        ExportPackageHandler exportHandler = null;
        if (context.zipped)
        {
            exportHandler = new ZipHandler(context.getDestDir(), context.getZipFile(), context.getPackageFile(), context.getPackageDir(), context.overwrite, mimetypeService);
        }
        else
        {
            exportHandler = new FileHandler(context.getDestDir(), context.getPackageFile(), context.getPackageDir(), context.overwrite, mimetypeService);
        }

        // export Repository content to export package
        ExporterCrawlerParameters parameters = new ExporterCrawlerParameters();
        parameters.setExportFrom(context.getLocation());
        parameters.setCrawlSelf(context.self);
        parameters.setCrawlChildNodes(context.children);

        try
        {
            exporter.exportView(exportHandler, parameters, new ExportProgress());
        }
        catch (ExporterException e)
        {
            throw new ToolException("Failed to export", e);
        }

        return 0;
    }

    /**
     * Handler for exporting Repository content streams to file system files
     * 
     * @author David Caruana
     */
    private class FileHandler extends FileExportPackageHandler
    {
        /**
         * Construct
         * 
         * @param destDir
         * @param dataFile
         * @param contentDir
         * @param overwrite
         */
        public FileHandler(File destDir, File dataFile, File contentDir, boolean overwrite, MimetypeService mimetypeService)
        {
            super(destDir, dataFile, contentDir, overwrite, mimetypeService);
        }

        /**
         * Log Export Message
         * 
         * @param message
         *            message to log
         */
        protected void log(String message)
        {
            Export.this.logInfo(message);
        }
    }

    /**
     * Handler for exporting Repository content streams to zip file
     * 
     * @author David Caruana
     */
    private class ZipHandler extends ACPExportPackageHandler
    {
        /**
         * Construct
         * 
         * @param destDir
         * @param zipFile
         * @param dataFile
         * @param contentDir
         */
        public ZipHandler(File destDir, File zipFile, File dataFile, File contentDir, boolean overwrite, MimetypeService mimetypeService)
        {
            super(destDir, zipFile, dataFile, contentDir, overwrite, mimetypeService);
        }

        /**
         * Log Export Message
         * 
         * @param message
         *            message to log
         */
        protected void log(String message)
        {
            Export.this.logInfo(message);
        }
    }

    /**
     * Export Tool Context
     * 
     * @author David Caruana
     */
    private class ExportContext extends ToolContext
    {
        /** Store Reference to export from */
        private StoreRef storeRef;
        /** Path to export from */
        private String path;
        /** Destination directory to export to */
        private String destDir;
        /** The package directory within the destination directory to export to */
        private String packageDir;
        /** The package name to export to */
        private String packageName;
        /** Export children */
        private boolean children = true;
        /** Export self */
        private boolean self = false;
        /** Force overwrite of existing package */
        private boolean overwrite = false;
        /** Zipped? */
        private boolean zipped = false;

        /* (non-Javadoc)
         * 
         * @see org.alfresco.tools.ToolContext#validate() */
        @Override
        /* package */ void validate()
        {
            super.validate();

            if (storeRef == null)
            {
                throw new ToolArgumentException("Store to export from has not been specified.");
            }
            if (packageName == null)
            {
                throw new ToolArgumentException("Package name has not been specified.");
            }
            if (destDir != null)
            {
                File fileDestDir = new File(destDir);
                if (fileDestDir.exists() == false)
                {
                    throw new ToolArgumentException("Destination directory " + fileDestDir.getAbsolutePath() + " does not exist.");
                }
            }
        }

        /**
         * Get the location within the Repository to export from
         * 
         * @return the location
         */
        private Location getLocation()
        {
            Location location = new Location(storeRef);
            location.setPath(path);
            return location;
        }

        /**
         * Get the destination directory
         * 
         * @return the destination directory (or null if current directory)
         */
        private File getDestDir()
        {
            File dir = (destDir == null) ? null : new File(destDir);
            return dir;
        }

        /**
         * Get the package directory
         * 
         * @return the package directory within the destination directory
         */
        private File getPackageDir()
        {
            File dir = null;
            if (packageDir != null)
            {
                dir = new File(packageDir);
            }
            else if (packageName.indexOf('.') != -1)
            {
                dir = new File(packageName.substring(0, packageName.indexOf('.')));
            }
            else
            {
                dir = new File(packageName);
            }
            return dir;
        }

        /**
         * Get the xml export file
         * 
         * @return the package file
         */
        private File getPackageFile()
        {
            String packageFile = (packageName.indexOf('.') != -1) ? packageName : packageName + ".xml";
            File file = new File(packageFile);
            return file;
        }

        /**
         * Get the zip file
         * 
         * @return the zip file
         */
        private File getZipFile()
        {
            int iExt = packageName.indexOf('.');
            String zipFile = ((iExt != -1) ? packageName.substring(0, iExt) : packageName) + ".acp";
            return new File(zipFile);
        }
    }

    /**
     * Report Export Progress
     * 
     * @author David Caruana
     */
    private class ExportProgress
            implements Exporter
    {
        /* (non-Javadoc)
         * 
         * @see org.alfresco.service.cmr.view.Exporter#start() */
        public void start(ExporterContext exportNodeRef)
        {}

        /* (non-Javadoc)
         * 
         * @see org.alfresco.service.cmr.view.Exporter#startNamespace(java.lang.String, java.lang.String) */
        public void startNamespace(String prefix, String uri)
        {
            logVerbose("Exporting namespace " + uri + " (prefix: " + prefix + ")");
        }

        /* (non-Javadoc)
         * 
         * @see org.alfresco.service.cmr.view.Exporter#endNamespace(java.lang.String) */
        public void endNamespace(String prefix)
        {}

        /* (non-Javadoc)
         * 
         * @see org.alfresco.service.cmr.view.Exporter#startNode(org.alfresco.service.cmr.repository.NodeRef) */
        public void startNode(NodeRef nodeRef)
        {
            logVerbose("Exporting node " + nodeRef.toString());
        }

        /* (non-Javadoc)
         * 
         * @see org.alfresco.service.cmr.view.Exporter#endNode(org.alfresco.service.cmr.repository.NodeRef) */
        public void endNode(NodeRef nodeRef)
        {}

        /* (non-Javadoc)
         * 
         * @see org.alfresco.service.cmr.view.Exporter#startAspects(org.alfresco.service.cmr.repository.NodeRef) */
        public void startAspects(NodeRef nodeRef)
        {}

        /* (non-Javadoc)
         * 
         * @see org.alfresco.service.cmr.view.Exporter#endAspects(org.alfresco.service.cmr.repository.NodeRef) */
        public void endAspects(NodeRef nodeRef)
        {}

        /* (non-Javadoc)
         * 
         * @see org.alfresco.service.cmr.view.Exporter#startAspect(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName) */
        public void startAspect(NodeRef nodeRef, QName aspect)
        {}

        /* (non-Javadoc)
         * 
         * @see org.alfresco.service.cmr.view.Exporter#endAspect(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName) */
        public void endAspect(NodeRef nodeRef, QName aspect)
        {}

        /* (non-Javadoc)
         * 
         * @see org.alfresco.service.cmr.view.Exporter#startACL(org.alfresco.service.cmr.repository.NodeRef) */
        public void startACL(NodeRef nodeRef)
        {}

        /* (non-Javadoc)
         * 
         * @see org.alfresco.service.cmr.view.Exporter#permission(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.security.AccessPermission) */
        public void permission(NodeRef nodeRef, AccessPermission permission)
        {}

        /* (non-Javadoc)
         * 
         * @see org.alfresco.service.cmr.view.Exporter#endACL(org.alfresco.service.cmr.repository.NodeRef) */
        public void endACL(NodeRef nodeRef)
        {}

        /* (non-Javadoc)
         * 
         * @see org.alfresco.service.cmr.view.Exporter#startProperties(org.alfresco.service.cmr.repository.NodeRef) */
        public void startProperties(NodeRef nodeRef)
        {}

        /* (non-Javadoc)
         * 
         * @see org.alfresco.service.cmr.view.Exporter#endProperties(org.alfresco.service.cmr.repository.NodeRef) */
        public void endProperties(NodeRef nodeRef)
        {}

        /* (non-Javadoc)
         * 
         * @see org.alfresco.service.cmr.view.Exporter#startProperty(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName) */
        public void startProperty(NodeRef nodeRef, QName property)
        {}

        /* (non-Javadoc)
         * 
         * @see org.alfresco.service.cmr.view.Exporter#endProperty(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName) */
        public void endProperty(NodeRef nodeRef, QName property)
        {}

        /* (non-Javadoc)
         * 
         * @see org.alfresco.service.cmr.view.Exporter#startValueCollection(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName) */
        public void startValueCollection(NodeRef nodeRef, QName property)
        {}

        /* (non-Javadoc)
         * 
         * @see org.alfresco.service.cmr.view.Exporter#endValueCollection(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName) */
        public void endValueCollection(NodeRef nodeRef, QName property)
        {}

        /* (non-Javadoc)
         * 
         * @see org.alfresco.service.cmr.view.Exporter#value(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, java.io.Serializable) */
        public void value(NodeRef nodeRef, QName property, Object value, int index)
        {}

        /* (non-Javadoc)
         * 
         * @see org.alfresco.service.cmr.view.Exporter#content(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, java.io.InputStream) */
        public void content(NodeRef nodeRef, QName property, InputStream content, ContentData contentData, int index)
        {}

        /* (non-Javadoc)
         * 
         * @see org.alfresco.service.cmr.view.Exporter#startAssoc(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName) */
        public void startAssoc(NodeRef nodeRef, QName assoc)
        {}

        /* (non-Javadoc)
         * 
         * @see org.alfresco.service.cmr.view.Exporter#endAssoc(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName) */
        public void endAssoc(NodeRef nodeRef, QName assoc)
        {}

        /* (non-Javadoc)
         * 
         * @see org.alfresco.service.cmr.view.Exporter#startAssocs(org.alfresco.service.cmr.repository.NodeRef) */
        public void startAssocs(NodeRef nodeRef)
        {}

        /* (non-Javadoc)
         * 
         * @see org.alfresco.service.cmr.view.Exporter#endAssocs(org.alfresco.service.cmr.repository.NodeRef) */
        public void endAssocs(NodeRef nodeRef)
        {}

        /* (non-Javadoc)
         * 
         * @see org.alfresco.service.cmr.view.Exporter#warning(java.lang.String) */
        public void warning(String warning)
        {
            logInfo("Warning: " + warning);
        }

        /* (non-Javadoc)
         * 
         * @see org.alfresco.service.cmr.view.Exporter#end() */
        public void end()
        {}

        /* (non-Javadoc)
         * 
         * @see org.alfresco.service.cmr.view.Exporter#startReference(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName) */
        public void startReference(NodeRef nodeRef, QName childName)
        {}

        /* (non-Javadoc)
         * 
         * @see org.alfresco.service.cmr.view.Exporter#endReference(org.alfresco.service.cmr.repository.NodeRef) */
        public void endReference(NodeRef nodeRef)
        {}

        public void endValueMLText(NodeRef nodeRef)
        {}

        public void startValueMLText(NodeRef nodeRef, Locale locale, boolean isNull)
        {}
    }

}
