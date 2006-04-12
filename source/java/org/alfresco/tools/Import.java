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
package org.alfresco.tools;

import java.io.File;
import java.io.Serializable;
import java.nio.charset.Charset;

import org.alfresco.repo.importer.ACPImportPackageHandler;
import org.alfresco.repo.importer.FileImportPackageHandler;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.view.ImportPackageHandler;
import org.alfresco.service.cmr.view.ImporterBinding;
import org.alfresco.service.cmr.view.ImporterException;
import org.alfresco.service.cmr.view.ImporterProgress;
import org.alfresco.service.cmr.view.ImporterService;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.service.cmr.view.ImporterBinding.UUID_BINDING;
import org.alfresco.service.namespace.QName;



/**
 * Import Tool.
 * 
 * @author David Caruana
 */
public class Import extends Tool
{
    /** Import Tool Context */
    private ImportContext context;
    
    
    /**
     * Entry Point
     * 
     * @param args
     */
    public static void main(String[] args)
    {
        Tool tool = new Import();
        tool.start(args);
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.tools.Tool#processArgs(java.lang.String[])
     */
    @Override
    /*package*/ ToolContext processArgs(String[] args)
    {
        context = new ImportContext();
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
                    throw new ToolException("The value <store> for the option -store must be specified");
                }
                context.storeRef = new StoreRef(args[i]);
            }
            else if (args[i].equals("-p") || args[i].equals("-path"))
            {
                i++;
                if (i == args.length || args[i].length() == 0)
                {
                    throw new ToolException("The value <path> for the option -path must be specified");
                }
                context.path = args[i];
            }
            else if (args[i].equals("-d") || args[i].equals("-dir"))
            {
                i++;
                if (i == args.length || args[i].length() == 0)
                {
                    throw new ToolException("The value <dir> for the option -dir must be specified");
                }
                context.sourceDir = args[i];
            }
            else if (args[i].equals("-user"))
            {
                i++;
                if (i == args.length || args[i].length() == 0)
                {
                    throw new ToolException("The value <user> for the option -user must be specified");
                }
                context.setUsername(args[i]);
            }
            else if (args[i].equals("-pwd"))
            {
                i++;
                if (i == args.length || args[i].length() == 0)
                {
                    throw new ToolException("The value <password> for the option -pwd must be specified");
                }
                context.setPassword(args[i]);
            }
            else if (args[i].equals("-encoding"))
            {
                i++;
                if (i == args.length || args[i].length() == 0)
                {
                    throw new ToolException("The value <encoding> for the option -encoding must be specified");
                }
                context.encoding = args[i];
            }
            else if (args[i].equals("-uuidBinding"))
            {
                i++;
                try
                {
                    context.uuidBinding = UUID_BINDING.valueOf(UUID_BINDING.class, args[i]);
                }
                catch(IllegalArgumentException e)
                {
                    throw new ToolException("The value " + args[i] + " is an invalid uuidBinding");
                }
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
                throw new ToolException("Unknown option " + args[i]);
            }

            // next argument
            i++;
        }

        return context;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.tools.Tool#displayHelp()
     */
    @Override
    /*package*/ void displayHelp()
    {
        System.out.println("Usage: import -user username -s[tore] store [options] packagename");
        System.out.println("");
        System.out.println("username: username for login");
        System.out.println("store: the store to import into the form of scheme://store_name");
        System.out.println("packagename: the filename to import from (with or without extension)");
        System.out.println("");
        System.out.println("Options:");
        System.out.println(" -h[elp] display this help");
        System.out.println(" -p[ath] the path within the store to extract into (default: /)");
        System.out.println(" -d[ir] the source directory to import from (default: current directory)");
        System.out.println(" -pwd password for login");
        System.out.println(" -encoding package file encoding (default: " + Charset.defaultCharset() + ")");
        System.out.println(" -uuidBinding CREATE_NEW, REMOVE_EXISTING, REPLACE_EXISTING, UPDATE_EXISTING, THROW_ON_COLLISION (default: CREATE_NEW)");
        System.out.println(" -quiet do not display any messages during import");
        System.out.println(" -verbose report import progress");
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.tools.Tool#getToolName()
     */
    @Override
    /*package*/ String getToolName()
    {
        return "Alfresco Repository Importer";
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.tools.Tool#execute()
     */
    @Override
    /*package*/ void execute() throws ToolException
    {
        ImporterService importer = getServiceRegistry().getImporterService();
        
        // determine type of import (from zip or file system)
        ImportPackageHandler importHandler;
        if (context.zipFile)
        {
            importHandler = new ZipHandler(context.getSourceDir(), context.getPackageFile(), context.encoding);
        }
        else
        {
            importHandler = new FileHandler(context.getSourceDir(), context.getPackageFile(), context.encoding);
        }
        
        try
        {
            ImportBinding binding = new ImportBinding(context.uuidBinding);
            importer.importView(importHandler, context.getLocation(), binding, new ImportProgress());
        }
        catch(ImporterException e)
        {
            throw new ToolException("Failed to import package due to " + e.getMessage(), e);
        }
    }

    /**
     * Handler for importing Repository content from zip package
     * 
     * @author David Caruana
     */
    private class ZipHandler extends ACPImportPackageHandler
    {
        /**
         * Construct
         * 
         * @param sourceDir
         * @param dataFile
         * @param dataFileEncoding
         */
        public ZipHandler(File sourceDir, File dataFile, String dataFileEncoding)
        {
            super(new File(sourceDir, dataFile.getPath()), dataFileEncoding);
        }

        /**
         * Log Export Message
         * 
         * @param message  message to log
         */
        protected void log(String message)
        {
            Import.this.log(message);
        }
    }
    
    /**
     * Handler for importing Repository content from file system files
     * 
     * @author David Caruana
     */
    private class FileHandler extends FileImportPackageHandler
    {
        /**
         * Construct
         * 
         * @param sourceDir
         * @param dataFile
         * @param dataFileEncoding
         */
        public FileHandler(File sourceDir, File dataFile, String dataFileEncoding)
        {
            super(sourceDir, dataFile, dataFileEncoding);
        }

        /**
         * Log Export Message
         * 
         * @param message  message to log
         */
        protected void log(String message)
        {
            Import.this.log(message);
        }
    }
    
    /**
     * Report Import Progress
     * 
     * @author David Caruana
     */
    private class ImportProgress implements ImporterProgress
    {
        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ImporterProgress#nodeCreated(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, org.alfresco.service.namespace.QName)
         */
        public void nodeCreated(NodeRef nodeRef, NodeRef parentRef, QName assocName, QName childName)
        {
            logVerbose("Imported node " + nodeRef + " (parent=" + parentRef + ", childname=" + childName + ", association=" + assocName + ")");            
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ImporterProgress#nodeLinked(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, org.alfresco.service.namespace.QName)
         */
        public void nodeLinked(NodeRef nodeRef, NodeRef parentRef, QName assocName, QName childName)
        {
        }
        
        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ImporterProgress#contentCreated(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
         */
        public void contentCreated(NodeRef nodeRef, String sourceUrl)
        {
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ImporterProgress#propertySet(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, java.io.Serializable)
         */
        public void propertySet(NodeRef nodeRef, QName property, Serializable value)
        {
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ImporterProgress#permissionSet(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.security.AccessPermission)
         */
        public void permissionSet(NodeRef nodeRef, AccessPermission permission)
        {
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ImporterProgress#aspectAdded(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
         */
        public void aspectAdded(NodeRef nodeRef, QName aspect)
        {
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ImporterProgress#started()
         */
        public void started()
        {
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ImporterProgress#completed()
         */
        public void completed()
        {
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ImporterProgress#error(java.lang.Throwable)
         */
        public void error(Throwable e)
        {
        }
    }
    
    /**
     * Import Tool Context
     * 
     * @author David Caruana
     */
    private class ImportContext extends ToolContext
    {
        /** Store Reference to import into */
        private StoreRef storeRef;
        /** Path to import into */
        private String path;
        /** Source directory to import from */
        private String sourceDir;
        /** The package name to import */
        private String packageName;
        /** The package encoding */
        private String encoding = null;
        /** The UUID Binding */
        private UUID_BINDING uuidBinding = UUID_BINDING.CREATE_NEW;
        /** Zip Package? */
        private boolean zipFile = false;

        /* (non-Javadoc)
         * @see org.alfresco.tools.ToolContext#validate()
         */
        @Override
        /*package*/ void validate()
        {
            super.validate();
            
            if (storeRef == null)
            {
                throw new ToolException("Store to import into has not been specified.");
            }
            if (packageName == null)
            {
                throw new ToolException("Package name has not been specified.");
            }
            if (sourceDir != null)
            {
                File fileSourceDir = getSourceDir();
                if (fileSourceDir.exists() == false)
                {
                    throw new ToolException("Source directory " + fileSourceDir.getAbsolutePath() + " does not exist.");
                }
            }
            if (packageName.endsWith(".acp"))
            {
                File packageFile = new File(getSourceDir(), packageName);
                if (!packageFile.exists())
                {
                    throw new ToolException("Package zip file " + packageFile.getAbsolutePath() + " does not exist.");
                }
                zipFile = true;
            }
            else
            {
                File packageFile = new File(getSourceDir(), getDataFile().getPath());
                if (!packageFile.exists())
                {
                    throw new ToolException("Package file " + packageFile.getAbsolutePath() + " does not exist.");
                }
            }
        }

        /**
         * Get the location within the Repository to import into
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
         * Get the source directory
         * 
         * @return the source directory (or null if current directory)
         */
        private File getSourceDir()
        {
            File dir = (sourceDir == null) ? null : new File(sourceDir); 
            return dir;
        }

        /**
         * Get the xml import file
         * 
         * @return the package file
         */
        private File getDataFile()
        {
            String dataFile = (packageName.indexOf('.') != -1) ? packageName : packageName + ".xml";
            File file = new File(dataFile); 
            return file;
        }
        
        /**
         * Get the zip import file (.acp - alfresco content package)
         * 
         * @return the zip package file
         */
        private File getPackageFile()
        {
            return (zipFile) ? new File(packageName) : getDataFile();
        }        
    }
 

    /**
     * Import Tool Binding
     * 
     * @author davidc
     */
    private class ImportBinding implements ImporterBinding
    {
        private UUID_BINDING uuidBinding = null;
    
        /**
         * Construct
         * 
         * @param uuidBinding
         */
        public ImportBinding(UUID_BINDING uuidBinding)
        {
            this.uuidBinding = uuidBinding;
        }
        
        /*
         *  (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ImporterBinding#getUUIDBinding()
         */
        public UUID_BINDING getUUIDBinding()
        {
            return uuidBinding;
        }

        /*
         *  (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ImporterBinding#allowReferenceWithinTransaction()
         */
        public boolean allowReferenceWithinTransaction()
        {
            return false;
        }

        /*
         *  (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ImporterBinding#getValue(java.lang.String)
         */
        public String getValue(String key)
        {
            return null;
        }

        public QName[] getExcludedClasses()
        {
            return new QName[] {};
        }
    }
}
