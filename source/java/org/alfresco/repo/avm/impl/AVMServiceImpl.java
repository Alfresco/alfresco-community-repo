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

package org.alfresco.repo.avm.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

import org.alfresco.repo.avm.AVMNode;
import org.alfresco.repo.avm.AVMService;
import org.alfresco.repo.avm.FolderEntry;
import org.alfresco.repo.avm.Lookup;
import org.alfresco.repo.avm.SuperRepository;
import org.alfresco.repo.avm.hibernate.HibernateHelper;
import org.alfresco.repo.avm.hibernate.HibernateTxn;
import org.alfresco.repo.avm.hibernate.HibernateTxnCallback;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

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
    private ThreadLocal<SuperRepository> fSuperRepository;
    
    /**
     * The storage directory.
     */
    private String fStorage;
    
    /**
     * Basic constructor for the service.
     */
    public AVMServiceImpl()
    {
        fSessionFactory = HibernateHelper.GetSessionFactory();
        fTransaction = new HibernateTxn(fSessionFactory);
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
        class HTxnCallback implements HibernateTxnCallback
        {
            public InputStream in = null;
            
            public void perform(Session session)
            {
                fSuperRepository.set(new SuperRepositoryImpl(session, fStorage));
                in = fSuperRepository.get().getInputStream(version, path);
            }
        };
        HTxnCallback doit = new HTxnCallback();
        fTransaction.perform(doit);
        return doit.in;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMService#getFileOutputStream(java.lang.String)
     */
    public OutputStream getFileOutputStream(final String path)
    {
        class HTxnCallback implements HibernateTxnCallback
        {
            public OutputStream out = null;
            
            public void perform(Session session)
            {
                fSuperRepository.set(new SuperRepositoryImpl(session, fStorage));
                out = fSuperRepository.get().getOutputStream(path);
            }
        };
        HTxnCallback doit = new HTxnCallback();
        fTransaction.perform(doit);
        return doit.out;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMService#getFolderListing(int, java.lang.String)
     */
    public List<FolderEntry> getDirectoryListing(final int version, final String path)
    {
        class HTxnCallback implements HibernateTxnCallback
        {
            public List<FolderEntry> listing;
            
            public void perform(Session session)
            {
                fSuperRepository.set(new SuperRepositoryImpl(session, fStorage));
                listing = fSuperRepository.get().getListing(version, path);
            }
        }
        HTxnCallback doit = new HTxnCallback();
        fTransaction.perform(doit);
        return doit.listing;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMService#createFile(java.lang.String, java.lang.String)
     */
    public void createFile(final String path, final String name)
    {
        class HTxnCallback implements HibernateTxnCallback
        {
            public void perform(Session session)
            {
                fSuperRepository.set(new SuperRepositoryImpl(session, fStorage));
                fSuperRepository.get().createFile(path, name);
            }
        }
        HTxnCallback doit = new HTxnCallback();
        fTransaction.perform(doit);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMService#createFolder(java.lang.String, java.lang.String)
     */
    public void createDirectory(final String path, final String name)
    {
        class HTxnCallback implements HibernateTxnCallback
        {
            public void perform(Session session)
            {
                fSuperRepository.set(new SuperRepositoryImpl(session, fStorage));
                fSuperRepository.get().createDirectory(path, name);
            }
        }
        HTxnCallback doit = new HTxnCallback();
        fTransaction.perform(doit);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMService#createLayeredFile(java.lang.String, java.lang.String, java.lang.String)
     */
    public void createLayeredFile(final String srcPath, final String parent, final String name)
    {
        class HTxnCallback implements HibernateTxnCallback
        {
            public void perform(Session session)
            {
                fSuperRepository.set(new SuperRepositoryImpl(session, fStorage));
                fSuperRepository.get().createLayeredFile(srcPath, parent, name);
            }
        }
        HTxnCallback doit = new HTxnCallback();
        fTransaction.perform(doit);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMService#createLayeredFolder(java.lang.String, java.lang.String, java.lang.String)
     */
    public void createLayeredDirectory(final String srcPath, final String parent, final String name)
    {
        class HTxnCallback implements HibernateTxnCallback
        {
            public void perform(Session session)
            {
                fSuperRepository.set(new SuperRepositoryImpl(session, fStorage));
                fSuperRepository.get().createLayeredDirectory(srcPath, parent, name);
            }
        }
        HTxnCallback doit = new HTxnCallback();
        fTransaction.perform(doit);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMService#createRepository(java.lang.String)
     */
    public void createRepository(final String name)
    {
        class HTxnCallback implements HibernateTxnCallback
        {
            public void perform(Session session)
            {
                fSuperRepository.set(new SuperRepositoryImpl(session, fStorage));
                fSuperRepository.get().createRepository(name);
            }
        }
        HTxnCallback doit = new HTxnCallback();
        fTransaction.perform(doit);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMService#createBranch(int, java.lang.String, java.lang.String, java.lang.String)
     */
    public void createBranch(final int version, final String srcPath, final String dstPath,
            final String name)
    {
        class HTxnCallback implements HibernateTxnCallback
        {
            public void perform(Session session)
            {
                fSuperRepository.set(new SuperRepositoryImpl(session, fStorage));
                fSuperRepository.get().createBranch(version, srcPath, dstPath, name);
            }
        }
        HTxnCallback doit = new HTxnCallback();
        fTransaction.perform(doit);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMService#removeNode(java.lang.String, java.lang.String)
     */
    public void removeNode(final String parent, final String name)
    {
        class HTxnCallback implements HibernateTxnCallback
        {
            public void perform(Session session)
            {
                fSuperRepository.set(new SuperRepositoryImpl(session, fStorage));
                fSuperRepository.get().remove(parent, name);
            }
        }
        HTxnCallback doit = new HTxnCallback();
        fTransaction.perform(doit);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMService#rename(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void rename(final String srcParent, final String srcName, final String dstParent,
            final String dstName)
    {
        class HTxnCallback implements HibernateTxnCallback
        {
            public void perform(Session session)
            {
                fSuperRepository.set(new SuperRepositoryImpl(session, fStorage));
                fSuperRepository.get().rename(srcParent, srcName, dstParent, dstName);
            }
        }
        HTxnCallback doit = new HTxnCallback();
        fTransaction.perform(doit);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMService#slide(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void slide(final String srcParent, final String srcName, final String dstParent,
            final String dstName)
    {
        class HTxnCallback implements HibernateTxnCallback
        {
            public void perform(Session session)
            {
                fSuperRepository.set(new SuperRepositoryImpl(session, fStorage));
                fSuperRepository.get().slide(srcParent, srcName, dstParent, dstName);
            }
        }
        HTxnCallback doit = new HTxnCallback();
        fTransaction.perform(doit);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMService#getLatestVersionID(java.lang.String)
     */
    public long getLatestVersionID(final String repName)
    {
        class HTxnCallback implements HibernateTxnCallback
        {
            public long latestVersionID;
            
            public void perform(Session session)
            {
                fSuperRepository.set(new SuperRepositoryImpl(session, fStorage));
                latestVersionID = fSuperRepository.get().getLatestVersionID(repName);
            }
        }
        HTxnCallback doit = new HTxnCallback();
        fTransaction.perform(doit);
        return doit.latestVersionID;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMService#createSnapshot(java.util.List)
     */
    public void createSnapshot(final List<String> repositories)
    {
        class HTxnCallback implements HibernateTxnCallback
        {
            public void perform(Session session)
            {
                fSuperRepository.set(new SuperRepositoryImpl(session, fStorage));
                fSuperRepository.get().createSnapshot(repositories);
            }
        }
        HTxnCallback doit = new HTxnCallback();
        fTransaction.perform(doit);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMService#lookup(int, java.lang.String)
     */
    public AVMNode lookup(final int version, final String path)
    {
        class HTxnCallback implements HibernateTxnCallback
        {
            public Lookup lookup;
            
            public void perform(Session session)
            {
                fSuperRepository.set(new SuperRepositoryImpl(session, fStorage));
                lookup = fSuperRepository.get().lookup(version, path);
            }
        }
        HTxnCallback doit = new HTxnCallback();
        fTransaction.perform(doit);
        return doit.lookup.getCurrentNode();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMService#destroyRepository(java.lang.String)
     */
    public void destroyRepository(final String name)
    {
        class HTxnCallback implements HibernateTxnCallback
        {
            public void perform(Session session)
            {
                fSuperRepository.set(new SuperRepositoryImpl(session, fStorage));
                fSuperRepository.get().destroyRepository(name);
            }
        }
        HTxnCallback doit = new HTxnCallback();
        fTransaction.perform(doit);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMService#purgeVersion(int, java.lang.String)
     */
    public void purgeVersion(final int version, final String name)
    {
        class HTxnCallback implements HibernateTxnCallback
        {
            public void perform(Session session)
            {
                fSuperRepository.set(new SuperRepositoryImpl(session, fStorage));
                fSuperRepository.get().purgeVersion(name, version);
            }
        }
        HTxnCallback doit = new HTxnCallback();
        fTransaction.perform(doit);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMService#getIndirectionPath(java.lang.String)
     */
    public String getIndirectionPath(final int version, final String path)
    {
        class HTxnCallback implements HibernateTxnCallback
        {
            public String indirectionPath;
            
            public void perform(Session session)
            {
                fSuperRepository.set(new SuperRepositoryImpl(session, fStorage));
                indirectionPath = fSuperRepository.get().getIndirectionPath(version, path);
            }
        }
        HTxnCallback doit = new HTxnCallback();
        fTransaction.perform(doit);
        return doit.indirectionPath;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMService#getRepositoryVersions(java.lang.String)
     */
    public Set<Long> getRepositoryVersions(final String name)
    {
        class HTxnCallback implements HibernateTxnCallback
        {
            public Set<Long> versions;
            
            public void perform(Session session)
            {
                fSuperRepository.set(new SuperRepositoryImpl(session, fStorage));
                versions = fSuperRepository.get().getRepositoryVersions(name);
            }
        }
        HTxnCallback doit = new HTxnCallback();
        fTransaction.perform(doit);
        return doit.versions;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMService#retargetLayeredFolder(java.lang.String, java.lang.String)
     */
    public void retargetLayeredFolder(String path, String target)
    {
        // TODO Auto-generated method stub
        
    }
}
