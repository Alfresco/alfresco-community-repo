/*
 * Copyright (C) 2006 Alfresco, Inc.
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

package org.alfresco.repo.avm;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.avm.SuperRepository;
import org.alfresco.repo.avm.hibernate.HibernateHelper;
import org.alfresco.repo.avm.hibernate.HibernateTxn;
import org.alfresco.repo.avm.hibernate.HibernateTxnCallback;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.tool.hbm2ddl.SchemaExport;

/**
 * Implements the AVMService.  Stub.
 * @author britt
 */
public class AVMServiceImpl implements AVMService
{
    /**
     * The Hibernate SessionFactory.
     */
    private SessionFactory fSessionFactory;
    
    /**
     * The HibernateTxn.
     */
    private HibernateTxn fTransaction;
    
    /**
     * The SuperRepository for each service thread.
     */
    private SuperRepository fSuperRepository;
    
    /**
     * The storage directory.
     */
    private String fStorage;
    
    /**
     * The node id issuer.
     */
    private Issuer fNodeIssuer;
    
    /**
     * The content id issuer.
     */
    private Issuer fContentIssuer;
    
    /**
     * The layer id issuer.
     */
    private Issuer fLayerIssuer;
    
    /**
     * Basic constructor for the service.
     * @param createTables Flag for whether tables should be created.
     */
    public AVMServiceImpl()
    {
        fSessionFactory = HibernateHelper.GetSessionFactory();
        fTransaction = new HibernateTxn(fSessionFactory);
    }
    
    /**
     * Final initialization of the service.  Must be called only on a 
     * fully initialized instance.
     * @param createTables Whether we should create tables, and a default
     * repository.
     */
    public void init(boolean createTables)
    {
        if (createTables)
        {
            SchemaExport se = new SchemaExport(HibernateHelper.GetConfiguration());
            se.drop(false, true);
            se.create(false, true);
            File storage = new File(fStorage);
            storage.mkdirs();
            fNodeIssuer = new Issuer(fStorage + File.separator + "node", 0L);
            fContentIssuer = new Issuer(fStorage + File.separator + "content", 0L);
            fLayerIssuer = new Issuer(fStorage + File.separator + "layer", 0L);
            fSuperRepository = new SuperRepository(fNodeIssuer,
                                                       fContentIssuer,
                                                       fLayerIssuer,
                                                       fStorage);
            createRepository("main");
        }       
        else
        {
            try
            {
                fNodeIssuer = new Issuer(fStorage + File.separator + "node");
                fContentIssuer = new Issuer(fStorage + File.separator + "content");
                fLayerIssuer = new Issuer(fStorage + File.separator + "layer");
                fSuperRepository = new SuperRepository(fNodeIssuer,
                                                           fContentIssuer,
                                                           fLayerIssuer,
                                                           fStorage);
            }
            catch (Exception e)
            {
                // TODO Log this and abort in some useful way.
            }
        }
    }
    
    /**
     * Set the location of file storage.
     * @param storage
     */
    public void setStorage(String storage)
    {
        fStorage = storage;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMService#getFileInputStream(int, java.lang.String)
     */
    public InputStream getFileInputStream(final int version, final String path)
    {
        if (path == null)
        {
            throw new AVMBadArgumentException("Null path.");
        }
        class HTxnCallback implements HibernateTxnCallback
        {
            public InputStream in = null;
            
            public void perform(Session session)
            {
                fSuperRepository.setSession(session);
                in = fSuperRepository.getInputStream(version, path);
            }
        };
        HTxnCallback doit = new HTxnCallback();
        fTransaction.perform(doit, false);
        return doit.in;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMService#getFileOutputStream(java.lang.String)
     */
    public OutputStream getFileOutputStream(final String path)
    {
        if (path == null)
        {
            throw new AVMBadArgumentException("Null path.");
        }
        class HTxnCallback implements HibernateTxnCallback
        {
            public OutputStream out = null;
            
            public void perform(Session session)
            {
                fSuperRepository.setSession(session);
                out = fSuperRepository.getOutputStream(path);
            }
        };
        HTxnCallback doit = new HTxnCallback();
        fTransaction.perform(doit, true);
        return doit.out;
    }

    /**
     * Get a random access file to the given file.
     * @param version The version to look for (read-only)
     * @param path The path to the file.
     * @param access The access mode for RandomAccessFile
     * @return A Random Access File.
     */
    public RandomAccessFile getRandomAccess(final int version, final String path, final String access)
    {
        if (path == null || access == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        class HTxnCallback implements HibernateTxnCallback
        {
            public RandomAccessFile file;
            
            public void perform(Session session)
            {
                fSuperRepository.setSession(session);
                file = fSuperRepository.getRandomAccess(version, path, access);
            }
        }
        HTxnCallback doit = new HTxnCallback();
        fTransaction.perform(doit, true);
        return doit.file;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMService#getFolderListing(int, java.lang.String)
     */
    public List<FolderEntry> getDirectoryListing(final int version, final String path)
    {
        if (path == null)
        {
            throw new AVMBadArgumentException("Null path.");
        }
        class HTxnCallback implements HibernateTxnCallback
        {
            public List<FolderEntry> listing;
            
            public void perform(Session session)
            {
                fSuperRepository.setSession(session);
                listing = fSuperRepository.getListing(version, path);
            }
        }
        HTxnCallback doit = new HTxnCallback();
        fTransaction.perform(doit, false);
        return doit.listing;
    }

    /**
     * Get a directory listing from a node descriptor.
     * @param dir The directory node descriptor.
     * @return A Map of names to node descriptors.
     */
    public Map<String, AVMNodeDescriptor> getDirectoryListing(final AVMNodeDescriptor dir)
    {
        if (dir == null)
        {
            throw new AVMBadArgumentException("Null descriptor.");
        }
        class HTxnCallback implements HibernateTxnCallback
        {
            public Map<String, AVMNodeDescriptor> listing;
            
            public void perform(Session session)
            {
                fSuperRepository.setSession(session);
                listing = fSuperRepository.getListing(dir);
            }
        }
        HTxnCallback doit = new HTxnCallback();
        fTransaction.perform(doit, false);
        return doit.listing;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMService#createFile(java.lang.String, java.lang.String)
     */
    public OutputStream createFile(final String path, final String name)
    {
        if (path == null || name == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        class HTxnCallback implements HibernateTxnCallback
        {
            public OutputStream out;
            
            public void perform(Session session)
            {
                fSuperRepository.setSession(session);
                out = fSuperRepository.createFile(path, name);
            }
        }
        HTxnCallback doit = new HTxnCallback();
        fTransaction.perform(doit, true);
        return doit.out;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMService#createFolder(java.lang.String, java.lang.String)
     */
    public void createDirectory(final String path, final String name)
    {
        if (path == null || name == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        class HTxnCallback implements HibernateTxnCallback
        {
            public void perform(Session session)
            {
                fSuperRepository.setSession(session);
                fSuperRepository.createDirectory(path, name);
            }
        }
        HTxnCallback doit = new HTxnCallback();
        fTransaction.perform(doit, true);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMService#createLayeredFile(java.lang.String, java.lang.String, java.lang.String)
     */
    public void createLayeredFile(final String srcPath, final String parent, final String name)
    {
        if (srcPath == null || parent == null || name == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        class HTxnCallback implements HibernateTxnCallback
        {
            public void perform(Session session)
            {
                fSuperRepository.setSession(session);
                fSuperRepository.createLayeredFile(srcPath, parent, name);
            }
        }
        HTxnCallback doit = new HTxnCallback();
        fTransaction.perform(doit, true);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMService#createLayeredFolder(java.lang.String, java.lang.String, java.lang.String)
     */
    public void createLayeredDirectory(final String srcPath, final String parent, final String name)
    {
        if (srcPath == null || parent == null || name == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        class HTxnCallback implements HibernateTxnCallback
        {
            public void perform(Session session)
            {
                fSuperRepository.setSession(session);
                fSuperRepository.createLayeredDirectory(srcPath, parent, name);
            }
        }
        HTxnCallback doit = new HTxnCallback();
        fTransaction.perform(doit, true);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMService#createRepository(java.lang.String)
     */
    public void createRepository(final String name)
    {
        if (name == null)
        {
            throw new AVMBadArgumentException("Name is null.");
        }
        class HTxnCallback implements HibernateTxnCallback
        {
            public void perform(Session session)
            {
                fSuperRepository.setSession(session);
                fSuperRepository.createRepository(name);
            }
        }
        HTxnCallback doit = new HTxnCallback();
        fTransaction.perform(doit, true);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMService#createBranch(int, java.lang.String, java.lang.String, java.lang.String)
     */
    public void createBranch(final int version, final String srcPath, final String dstPath,
            final String name)
    {
        if (srcPath == null || dstPath == null || name == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        class HTxnCallback implements HibernateTxnCallback
        {
            public void perform(Session session)
            {
                fSuperRepository.setSession(session);
                fSuperRepository.createBranch(version, srcPath, dstPath, name);
            }
        }
        HTxnCallback doit = new HTxnCallback();
        fTransaction.perform(doit, true);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMService#removeNode(java.lang.String, java.lang.String)
     */
    public void removeNode(final String parent, final String name)
    {
        if (parent == null || name == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        class HTxnCallback implements HibernateTxnCallback
        {
            public void perform(Session session)
            {
                fSuperRepository.setSession(session);
                fSuperRepository.remove(parent, name);
            }
        }
        HTxnCallback doit = new HTxnCallback();
        fTransaction.perform(doit, true);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMService#rename(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void rename(final String srcParent, final String srcName, final String dstParent,
            final String dstName)
    {
        if (srcParent == null || srcName == null || dstParent == null || dstName == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        class HTxnCallback implements HibernateTxnCallback
        {
            public void perform(Session session)
            {
                fSuperRepository.setSession(session);
                fSuperRepository.rename(srcParent, srcName, dstParent, dstName);
            }
        }
        HTxnCallback doit = new HTxnCallback();
        fTransaction.perform(doit, true);
    }

    /**
     * Uncover a deleted name in a layered directory.
     * @param dirPath The path to the layered directory.
     * @param name The name to uncover.
     */
    public void uncover(final String dirPath, final String name)
    {
        if (dirPath == null || name == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        class HTxnCallback implements HibernateTxnCallback
        {
            public void perform(Session session)
            {
                fSuperRepository.setSession(session);
                fSuperRepository.uncover(dirPath, name);
            }
        }
        HTxnCallback doit = new HTxnCallback();
        fTransaction.perform(doit, true);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMService#getLatestVersionID(java.lang.String)
     */
    public int getLatestVersionID(final String repName)
    {
        if (repName == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        class HTxnCallback implements HibernateTxnCallback
        {
            public int latestVersionID;
            
            public void perform(Session session)
            {
                fSuperRepository.setSession(session);
                latestVersionID = fSuperRepository.getLatestVersionID(repName);
            }
        }
        HTxnCallback doit = new HTxnCallback();
        fTransaction.perform(doit, false);
        return doit.latestVersionID;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMService#createSnapshot(java.util.List)
     */
    public void createSnapshot(final List<String> repositories)
    {
        if (repositories == null)
        {
            throw new AVMBadArgumentException("Repositories is null.");
        }
        class HTxnCallback implements HibernateTxnCallback
        {
            public void perform(Session session)
            {
                fSuperRepository.setSession(session);
                fSuperRepository.createSnapshot(repositories);
            }
        }
        HTxnCallback doit = new HTxnCallback();
        fTransaction.perform(doit, true);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMService#createSnapshot(java.lang.String)
     */
    public void createSnapshot(final String repository)
    {
        if (repository == null)
        {
            throw new AVMBadArgumentException("Repository is null.");
        }
        class HTxnCallback implements HibernateTxnCallback
        {
            public void perform(Session session)
            {
                fSuperRepository.setSession(session);
                fSuperRepository.createSnapshot(repository);
            }
        }
        HTxnCallback doit = new HTxnCallback();
        fTransaction.perform(doit, true);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMService#lookup(int, java.lang.String)
     */
    public AVMNodeDescriptor lookup(final int version, final String path)
    {
        if (path == null)
        {
            throw new AVMBadArgumentException("Path is null.");
        }
        class HTxnCallback implements HibernateTxnCallback
        {
            public AVMNodeDescriptor descriptor;
            
            public void perform(Session session)
            {
                fSuperRepository.setSession(session);
                Lookup lookup = fSuperRepository.lookup(version, path);
                descriptor = lookup.getCurrentNode().getDescriptor(lookup);
            }
        }
        HTxnCallback doit = new HTxnCallback();
        fTransaction.perform(doit, false);
        return doit.descriptor;
    }

    /**
     * Lookup a node descriptor from a directory node descriptor.
     * @param dir The node descriptor of the directory.
     * @param name The name to lookup.
     * @return The node descriptor of the child.
     */
    public AVMNodeDescriptor lookup(final AVMNodeDescriptor dir, final String name)
    {
        if (dir == null || name == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        class HTxnCallback implements HibernateTxnCallback
        {
            public AVMNodeDescriptor child;
            
            public void perform(Session session)
            {
                fSuperRepository.setSession(session);
                child = fSuperRepository.lookup(dir, name);
            }
        }
        HTxnCallback doit = new HTxnCallback();
        fTransaction.perform(doit, false);
        return doit.child;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMService#destroyRepository(java.lang.String)
     */
    public void purgeRepository(final String name)
    {
        if (name == null)
        {
            throw new AVMBadArgumentException("Name is null.");
        }
        class HTxnCallback implements HibernateTxnCallback
        {
            public void perform(Session session)
            {
                fSuperRepository.setSession(session);
                fSuperRepository.purgeRepository(name);
            }
        }
        HTxnCallback doit = new HTxnCallback();
        fTransaction.perform(doit, true);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMService#purgeVersion(int, java.lang.String)
     */
    public void purgeVersion(final int version, final String name)
    {
        if (name == null)
        {
            throw new AVMBadArgumentException("Name is null.");
        }
        class HTxnCallback implements HibernateTxnCallback
        {
            public void perform(Session session)
            {
                fSuperRepository.setSession(session);
                fSuperRepository.purgeVersion(name, version);
            }
        }
        HTxnCallback doit = new HTxnCallback();
        fTransaction.perform(doit, true);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMService#getIndirectionPath(java.lang.String)
     */
    public String getIndirectionPath(final int version, final String path)
    {
        if (path == null)
        {
            throw new AVMBadArgumentException("Path is null.");
        }
        class HTxnCallback implements HibernateTxnCallback
        {
            public String indirectionPath;
            
            public void perform(Session session)
            {
                fSuperRepository.setSession(session);
                indirectionPath = fSuperRepository.getIndirectionPath(version, path);
            }
        }
        HTxnCallback doit = new HTxnCallback();
        fTransaction.perform(doit, false);
        return doit.indirectionPath;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMService#getRepositoryVersions(java.lang.String)
     */
    public List<VersionDescriptor> getRepositoryVersions(final String name)
    {
        if (name == null)
        {
            throw new AVMBadArgumentException("Name is null.");
        }
        class HTxnCallback implements HibernateTxnCallback
        {
            public List<VersionDescriptor> versions;
            
            public void perform(Session session)
            {
                fSuperRepository.setSession(session);
                versions = fSuperRepository.getRepositoryVersions(name);
            }
        }
        HTxnCallback doit = new HTxnCallback();
        fTransaction.perform(doit, false);
        return doit.versions;
    }

    /**
     * Get version IDs by creation date.  From or to may be null but not
     * both.
     * @param name The name of the repository to search.
     * @param from The earliest versions to return.
     * @param to The latest versions to return.
     * @return The Set of matching version IDs.
     */
    public List<VersionDescriptor> getRepositoryVersions(final String name, final Date from, final Date to)
    {
        if (name == null || (from == null && to == null))
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        class HTxnCallback implements HibernateTxnCallback
        {
            public List<VersionDescriptor> versions;
            
            public void perform(Session session)
            {
                fSuperRepository.setSession(session);
                versions = fSuperRepository.getRepositoryVersions(name, from, to);
            }
        }
        HTxnCallback doit = new HTxnCallback();
        fTransaction.perform(doit, false);
        return doit.versions;
    }

    /**
     * Change what a layered directory points to.
     */
    public void retargetLayeredDirectory(final String path, final String target)
    {
        if (path == null || target == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        class HTxnCallback implements HibernateTxnCallback
        {
            public void perform(Session session)
            {
                fSuperRepository.setSession(session);
                fSuperRepository.retargetLayeredDirectory(path, target);
            }
        }
        HTxnCallback doit = new HTxnCallback();
        fTransaction.perform(doit, true);
    }

    /**
     * Make the indicated directory a primary indirection.
     * @param path The absolute path.
     */
    public void makePrimary(final String path)
    {
        if (path == null)
        {
            throw new AVMBadArgumentException("Path is null.");
        }
        class HTxnCallback implements HibernateTxnCallback
        {
            public void perform(Session session)
            {
                fSuperRepository.setSession(session);
                fSuperRepository.makePrimary(path);
            }
        }
        HTxnCallback doit = new HTxnCallback();
        fTransaction.perform(doit, true);
    }

    public List<String> getRepositoryNames()
    {
        class HTxnCallback implements HibernateTxnCallback
        {
            public List<String> names;
            
            public void perform(Session session)
            {
                fSuperRepository.setSession(session);
                names = fSuperRepository.getRepositoryNames();
            }
        }
        HTxnCallback doit = new HTxnCallback();
        fTransaction.perform(doit, false);
        return doit.names;
    }

    /**
     * Get a descriptor for the specified repository root.
     * @param version The version to get.
     * @param name The name of the repository.
     * @return The root descriptor.
     */
    public AVMNodeDescriptor getRepositoryRoot(final int version, final String name)
    {
        if (name == null)
        {
            throw new AVMBadArgumentException("Name is null.");
        }
        class HTxnCallback implements HibernateTxnCallback
        {
            public AVMNodeDescriptor root;
            
            public void perform(Session session)
            {
                fSuperRepository.setSession(session);
                root = fSuperRepository.getRepositoryRoot(version, name);
            }
        }
        HTxnCallback doit = new HTxnCallback();
        fTransaction.perform(doit, false);
        return doit.root;
    }
 }
