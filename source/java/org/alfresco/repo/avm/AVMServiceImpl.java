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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.alfresco.repo.avm.AVMRepository;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;

/**
 * Implements the AVMService.  Stub.
 * @author britt
 */
public class AVMServiceImpl implements AVMService
{
    private static Logger fgLogger = Logger.getLogger(AVMServiceImpl.class);
    
    /**
     * The RetryingTransaction.
     */
    private RetryingTransaction fTransaction;
    
    /**
     * The AVMRepository for each service thread.
     */
    private AVMRepository fAVMRepository;
    
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
     * Whether the tables should be dropped and created.
     */
    private boolean fInitialize;
    
    /**
     * Basic constructor for the service.
     */
    public AVMServiceImpl()
    {
    }
    
    /**
     * Final initialization of the service.  Must be called only on a 
     * fully initialized instance.
     */
    public void init()
    {
        try
        {
            fTransaction.perform(
            new RetryingTransactionCallback()
            {
                public void perform()
                {
                    IssuerDAO dao = AVMContext.fgInstance.fIssuerDAO;
                    Long val = dao.getNodeIssuerValue();
                    fNodeIssuer = new Issuer(val == null ? 0L : val + 1L);
                    val = dao.getContentIssuerValue();
                    fContentIssuer = new Issuer(val == null ? 0L : val + 1L);
                    val = dao.getLayerIssuerValue();
                    fLayerIssuer = new Issuer(val == null ? 0L : val + 1L);
                }
            }, false);
            fAVMRepository = new AVMRepository(fNodeIssuer,
                                                   fContentIssuer,
                                                   fLayerIssuer,
                                                   fStorage);
            fgLogger.info("Initialized AVMService and AVMRepository");
        }
        catch (Exception e)
        {
            fgLogger.fatal("Failed to initialize AVMService", e);
            // TODO Abort in some useful way.
        }
        if (fInitialize)
        {
            File storageDir = new File(fStorage);
            storageDir.mkdirs();
            createAVMStore("main");
            fgLogger.info("Created new main AVMStore");
        }
    }
    
    /**
     * Set the Retrying Transaction wrapper.
     * @param txn
     */
    public void setRetryingTransaction(RetryingTransaction txn)
    {
        fTransaction = txn;
    }
    
    /**
     * Set the location of file storage.
     * @param storage
     */
    public void setStorage(String storage)
    {
        fStorage = storage;
    }
    
    /**
     * Set whether we should create an initial AVMStore.
     * @param initialize
     */
    public void setInitialize(boolean initialize)
    {
        fInitialize = initialize;
    }

    /**
     * Get an InputStream from a file.
     * @param version The version to look under.
     * @param path The absolute path.
     * @return An InputStream
     * @throws AVMNotFoundException When the path is invalid.
     */
    public InputStream getFileInputStream(final int version, final String path)
    {
        if (path == null)
        {
            throw new AVMBadArgumentException("Null path.");
        }
        class TxnCallback implements RetryingTransactionCallback
        {
            public InputStream in = null;
            
            public void perform()
            {
                in = fAVMRepository.getInputStream(version, path);
            }
        };
        TxnCallback doit = new TxnCallback();
        fTransaction.perform(doit, false);
        return doit.in;
    }

    /**
     * Get an InputStream from a particular version of a file.
     * @param desc The node descriptor.
     * @return The InputStream.
     */
    public InputStream getFileInputStream(final AVMNodeDescriptor desc)
    {
        if (desc == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        class TxnCallback implements RetryingTransactionCallback
        {
            public InputStream in = null;
            
            public void perform()
            {
                in = fAVMRepository.getInputStream(desc);
            }
        }
        TxnCallback doit = new TxnCallback();
        fTransaction.perform(doit, false);
        return doit.in;
    }

    /**
     * Get an output stream to a file. Triggers versioning.
     */
    public OutputStream getFileOutputStream(final String path)
    {
        if (path == null)
        {
            throw new AVMBadArgumentException("Null path.");
        }
        class TxnCallback implements RetryingTransactionCallback
        {
            public OutputStream out = null;
            
            public void perform()
            {
                out = fAVMRepository.getOutputStream(path);
            }
        };
        TxnCallback doit = new TxnCallback();
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
        class TxnCallback implements RetryingTransactionCallback
        {
            public RandomAccessFile file;
            
            public void perform()
            {
                file = fAVMRepository.getRandomAccess(version, path, access);
            }
        }
        TxnCallback doit = new TxnCallback();
        fTransaction.perform(doit, true);
        return doit.file;
    }

    
    /**
     * Get a directory listing.
     * @param version The version id to lookup.
     * @param path The path to lookup.
     */
    public SortedMap<String, AVMNodeDescriptor> getDirectoryListing(final int version, final String path)
    {
        if (path == null)
        {
            throw new AVMBadArgumentException("Null path.");
        }
        class TxnCallback implements RetryingTransactionCallback
        {
            public SortedMap<String, AVMNodeDescriptor> listing;
            
            public void perform()
            {
                listing = fAVMRepository.getListing(version, path);
            }
        }
        TxnCallback doit = new TxnCallback();
        fTransaction.perform(doit, false);
        return doit.listing;
    }

    /**
     * Get a directory listing from a node descriptor.
     * @param dir The directory node descriptor.
     * @return A Map of names to node descriptors.
     */
    public SortedMap<String, AVMNodeDescriptor> getDirectoryListing(final AVMNodeDescriptor dir)
    {
        if (dir == null)
        {
            throw new AVMBadArgumentException("Null descriptor.");
        }
        class TxnCallback implements RetryingTransactionCallback
        {
            public SortedMap<String, AVMNodeDescriptor> listing;
            
            public void perform()
            {
                listing = fAVMRepository.getListing(dir);
            }
        }
        TxnCallback doit = new TxnCallback();
        fTransaction.perform(doit, false);
        return doit.listing;
    }

    /**
     * Create a new file. The file must not exist.
     * @param path The path to the containing directory.
     * @param name The name of the file.
     * @return An output stream to the file.
     */
    public OutputStream createFile(final String path, final String name)
    {
        if (path == null || name == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        class TxnCallback implements RetryingTransactionCallback
        {
            public OutputStream out;
            
            public void perform()
            {
                out = fAVMRepository.createFile(path, name);
            }
        }
        TxnCallback doit = new TxnCallback();
        fTransaction.perform(doit, true);
        return doit.out;
    }

    /**
     * Create a file with content specified by the InputStream.
     * Guaranteed to be created atomically.
     * @param path The path to the containing directory.
     * @param name The name to give the file.
     * @param in An InputStream containing data for file.
     */
    public void createFile(final String path, final String name, InputStream in)
    {
        if (path == null || name == null || in == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        // Save the contents to temp space.
        File dir = new File(fStorage);
        final File temp;
        try
        {
            temp = File.createTempFile("alf", "tmp", dir);
            OutputStream out = new FileOutputStream(temp);
            byte [] buff = new byte[8192];
            int read;
            while ((read = in.read(buff)) != -1)
            {
                out.write(buff, 0, read);
            }
            out.close();
            in.close();
        }
        catch (IOException ie)
        {
            throw new AVMException("I/O Error.");
        }
        class TxnCallback implements RetryingTransactionCallback
        {
            public void perform()
            {
                fAVMRepository.createFile(path, name, temp);
            }
        }
        TxnCallback doit = new TxnCallback();
        try
        {
            fTransaction.perform(doit, true);
        }
        finally
        {
            temp.delete();
        }
    }

    /**
     * Create a directory. The directory must not exist.
     * @param path The path to the containing directory.
     * @param name The name of the new directory.
     */
    public void createDirectory(final String path, final String name)
    {
        if (path == null || name == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        class TxnCallback implements RetryingTransactionCallback
        {
            public void perform()
            {
                fAVMRepository.createDirectory(path, name);
            }
        }
        TxnCallback doit = new TxnCallback();
        fTransaction.perform(doit, true);
    }

    /**
     * Create a new layered file.  It must not exist.
     * @param srcPath The src path.  Ie the target for the layering.
     * @param parent The path to the parent directory.
     * @param name The name to give the new file.
     */
    public void createLayeredFile(final String srcPath, final String parent, final String name)
    {
        if (srcPath == null || parent == null || name == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        class TxnCallback implements RetryingTransactionCallback
        {
            public void perform()
            {
                fAVMRepository.createLayeredFile(srcPath, parent, name);
            }
        }
        TxnCallback doit = new TxnCallback();
        fTransaction.perform(doit, true);
    }

    /**
     * Create a new layered directory.  It must not exist.
     * @param srcPath The src path. Ie the target for layering.
     * @param parent The path to the parent directory.
     * @param name The name for the new directory.
     */
    public void createLayeredDirectory(final String srcPath, final String parent, final String name)
    {
        if (srcPath == null || parent == null || name == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        class TxnCallback implements RetryingTransactionCallback
        {
            public void perform()
            {
                fAVMRepository.createLayeredDirectory(srcPath, parent, name);
            }
        }
        TxnCallback doit = new TxnCallback();
        fTransaction.perform(doit, true);
    }

    /**
     * Create an AVMStore with the given name.  It must not exist.
     * @param name The name to give the AVMStore.   
     */
    public void createAVMStore(final String name)
    {
        if (name == null)
        {
            throw new AVMBadArgumentException("Name is null.");
        }
        class TxnCallback implements RetryingTransactionCallback
        {
            public void perform()
            {
                fAVMRepository.createAVMStore(name);
            }
        }
        TxnCallback doit = new TxnCallback();
        fTransaction.perform(doit, true);
    }

    /**
     * Create a branch.
     * @param version The version to branch from.
     * @param srcPath The path to the thing to branch from.
     * @param dstPath The path to the destination containing directory.
     * @param name The name of the new branch.
     */
    public void createBranch(final int version, final String srcPath, final String dstPath,
            final String name)
    {
        if (srcPath == null || dstPath == null || name == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        class TxnCallback implements RetryingTransactionCallback
        {
            public void perform()
            {
                fAVMRepository.createBranch(version, srcPath, dstPath, name);
            }
        }
        TxnCallback doit = new TxnCallback();
        fTransaction.perform(doit, true);
    }

    /**
     * Remove a node. Beware, the node can be a directory and 
     * this acts recursively.
     * @param parent The path to the parent.
     * @param name The name of the node to remove.
     */
    public void removeNode(final String parent, final String name)
    {
        if (parent == null || name == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        class TxnCallback implements RetryingTransactionCallback
        {
            public void perform()
            {
                fAVMRepository.remove(parent, name);
            }
        }
        TxnCallback doit = new TxnCallback();
        fTransaction.perform(doit, true);
    }

    /**
     * Rename a node.
     * @param srcParent The path to the source parent.
     * @param srcName The name of the source node.
     * @param dstParent The path to the destination parent.
     * @param dstName The name to give the renamed node.
     */
    public void rename(final String srcParent, final String srcName, final String dstParent,
            final String dstName)
    {
        if (srcParent == null || srcName == null || dstParent == null || dstName == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        class TxnCallback implements RetryingTransactionCallback
        {
            public void perform()
            {
                fAVMRepository.rename(srcParent, srcName, dstParent, dstName);
            }
        }
        TxnCallback doit = new TxnCallback();
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
        class TxnCallback implements RetryingTransactionCallback
        {
            public void perform()
            {
                fAVMRepository.uncover(dirPath, name);
            }
        }
        TxnCallback doit = new TxnCallback();
        fTransaction.perform(doit, true);
    }

    /**
     * Get the Latest Version ID for an AVMStore.
     * @param repName The name of the AVMStore.
     * @return The Latest Version ID.
     */
    public int getLatestVersionID(final String repName)
    {
        if (repName == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        class TxnCallback implements RetryingTransactionCallback
        {
            public int latestVersionID;
            
            public void perform()
            {
                latestVersionID = fAVMRepository.getLatestVersionID(repName);
            }
        }
        TxnCallback doit = new TxnCallback();
        fTransaction.perform(doit, false);
        return doit.latestVersionID;
    }

    /**
     * Create snapshots of a group of AVMStores.
     * @param stores A List of AVMStore names.
     * @return A List of the new version ids.
     */
    public List<Integer> createSnapshot(final List<String> stores)
    {
        if (stores == null)
        {
            throw new AVMBadArgumentException("Stores is null.");
        }
        class TxnCallback implements RetryingTransactionCallback
        {
            public List<Integer> versionIDs;
            
            public void perform()
            {
                versionIDs = fAVMRepository.createSnapshot(stores);
            }
        }
        TxnCallback doit = new TxnCallback();
        fTransaction.perform(doit, true);
        return doit.versionIDs;
    }

    /**
     * Snapshot an AVMRepository.
     * @param store The name of the AVMStore.
     * @return The id of the new version.
     */
    public int createSnapshot(final String store)
    {
        if (store == null)
        {
            throw new AVMBadArgumentException("Store is null.");
        }
        class TxnCallback implements RetryingTransactionCallback
        {
            public int versionID;
            
            public void perform()
            {
                versionID = fAVMRepository.createSnapshot(store);
            }
        }
        TxnCallback doit = new TxnCallback();
        fTransaction.perform(doit, true);
        return doit.versionID;
    }

    /**
     * Look up information about a node.
     * @param version The version to look up.
     * @param path The path to look up.
     * @return A Descriptor.
     */
    public AVMNodeDescriptor lookup(final int version, final String path)
    {
        if (path == null)
        {
            throw new AVMBadArgumentException("Path is null.");
        }
        class TxnCallback implements RetryingTransactionCallback
        {
            public AVMNodeDescriptor descriptor;
            
            public void perform()
            {
                Lookup lookup = fAVMRepository.lookup(version, path);
                descriptor = lookup.getCurrentNode().getDescriptor(lookup);
            }
        }
        TxnCallback doit = new TxnCallback();
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
        class TxnCallback implements RetryingTransactionCallback
        {
            public AVMNodeDescriptor child;
            
            public void perform()
            {
                child = fAVMRepository.lookup(dir, name);
            }
        }
        TxnCallback doit = new TxnCallback();
        fTransaction.perform(doit, false);
        return doit.child;
    }

    /**
     * Purge an AVMStore.  Permanently delete everything that 
     * is only referenced in that AVMStore.
     * @param name The name of the AVMStore to purge.
     */
    public void purgeAVMStore(final String name)
    {
        if (name == null)
        {
            throw new AVMBadArgumentException("Name is null.");
        }
        class TxnCallback implements RetryingTransactionCallback
        {
            public void perform()
            {
                fAVMRepository.purgeAVMStore(name);
            }
        }
        TxnCallback doit = new TxnCallback();
        fTransaction.perform(doit, true);
    }

    /**
     * Purge a particular version from a repository.
     * @param version The id of the version to purge.
     * @param name The name of the repository.
     */
    public void purgeVersion(final int version, final String name)
    {
        if (name == null)
        {
            throw new AVMBadArgumentException("Name is null.");
        }
        class TxnCallback implements RetryingTransactionCallback
        {
            public void perform()
            {
                fAVMRepository.purgeVersion(name, version);
            }
        }
        TxnCallback doit = new TxnCallback();
        fTransaction.perform(doit, true);
    }

    /**
     * Get the indirection path of a layered node.
     * @param version The version to lookup.
     * @param path The path to lookup.
     * @return The indirection path (target) of the layered node.
     */
    public String getIndirectionPath(final int version, final String path)
    {
        if (path == null)
        {
            throw new AVMBadArgumentException("Path is null.");
        }
        class TxnCallback implements RetryingTransactionCallback
        {
            public String indirectionPath;
            
            public void perform()
            {
                indirectionPath = fAVMRepository.getIndirectionPath(version, path);
            }
        }
        TxnCallback doit = new TxnCallback();
        fTransaction.perform(doit, false);
        return doit.indirectionPath;
    }

    /**
     * Get the extant version ids for an AVMStore.
     * @param name The name of the AVMStore.
     * @return A List of VersionDescriptors.
     */
    public List<VersionDescriptor> getAVMStoreVersions(final String name)
    {
        if (name == null)
        {
            throw new AVMBadArgumentException("Name is null.");
        }
        class TxnCallback implements RetryingTransactionCallback
        {
            public List<VersionDescriptor> versions;
            
            public void perform()
            {
                versions = fAVMRepository.getAVMStoreVersions(name);
            }
        }
        TxnCallback doit = new TxnCallback();
        fTransaction.perform(doit, false);
        return doit.versions;
    }

    /**
     * Get version IDs by creation date.  From or to may be null but not
     * both.
     * @param name The name of the AVMStore to search.
     * @param from The earliest versions to return.
     * @param to The latest versions to return.
     * @return The Set of matching version IDs.
     */
    public List<VersionDescriptor> getAVMStoreVersions(final String name, final Date from, final Date to)
    {
        if (name == null || (from == null && to == null))
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        class TxnCallback implements RetryingTransactionCallback
        {
            public List<VersionDescriptor> versions;
            
            public void perform()
            {
                versions = fAVMRepository.getAVMStoreVersions(name, from, to);
            }
        }
        TxnCallback doit = new TxnCallback();
        fTransaction.perform(doit, false);
        return doit.versions;
    }

    /**
     * Change what a layered directory points to.
     * @param path The path to the layered directory.
     */
    public void retargetLayeredDirectory(final String path, final String target)
    {
        if (path == null || target == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        class TxnCallback implements RetryingTransactionCallback
        {
            public void perform()
            {
                fAVMRepository.retargetLayeredDirectory(path, target);
            }
        }
        TxnCallback doit = new TxnCallback();
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
        class TxnCallback implements RetryingTransactionCallback
        {
            public void perform()
            {
                fAVMRepository.makePrimary(path);
            }
        }
        TxnCallback doit = new TxnCallback();
        fTransaction.perform(doit, true);
    }

    /**
     * Get a list of all AVMStores.
     * @return The AVMStores.
     */
    public List<AVMStoreDescriptor> getAVMStores()
    {
        class TxnCallback implements RetryingTransactionCallback
        {
            public List<AVMStoreDescriptor> reps;
            
            public void perform()
            {
                reps = fAVMRepository.getAVMStores();
            }
        }
        TxnCallback doit = new TxnCallback();
        fTransaction.perform(doit, false);
        return doit.reps;
    }

    /**
     * Get a reposotory.
     * @param name The name of the AVMStore to get.
     * @return The AVMStore.
     */
    public AVMStoreDescriptor getAVMStore(final String name)
    {
        class TxnCallback implements RetryingTransactionCallback
        {
            public AVMStoreDescriptor desc;
            
            public void perform()
            {
                desc = fAVMRepository.getAVMStore(name);
            }
        }
        TxnCallback doit = new TxnCallback();
        fTransaction.perform(doit, false);
        return doit.desc;
    }

    /**
     * Get a descriptor for the specified AVMStore root.
     * @param version The version to get.
     * @param name The name of the AVMStore.
     * @return The root descriptor.
     */
    public AVMNodeDescriptor getAVMStoreRoot(final int version, final String name)
    {
        if (name == null)
        {
            throw new AVMBadArgumentException("Name is null.");
        }
        class TxnCallback implements RetryingTransactionCallback
        {
            public AVMNodeDescriptor root;
            
            public void perform()
            {
                root = fAVMRepository.getAVMStoreRoot(version, name);
            }
        }
        TxnCallback doit = new TxnCallback();
        fTransaction.perform(doit, false);
        return doit.root;
    }

    /**
     * Get the history of a node.
     * @param desc The node to get history from.
     * @param count The number of ancestors to fallow back. -1 means all.
     * @return A List of ancestors most recent first.
     */
    public List<AVMNodeDescriptor> getHistory(final AVMNodeDescriptor desc, final int count)
    {
        if (desc == null)
        {
            throw new AVMBadArgumentException("Null descriptor.");
        }
        class TxnCallback implements RetryingTransactionCallback
        {
            public List<AVMNodeDescriptor> history;
            
            public void perform()
            {
                history = fAVMRepository.getHistory(desc, count);
            }
        }
        TxnCallback doit = new TxnCallback();
        fTransaction.perform(doit, false);
        return doit.history;
    }

    /**
     * Set the opacity of a layered directory.  An opaque layer hides what
     * its indirection points to.
     * @param path The path to the layered directory.
     * @param opacity True is opaque false is not.
     */
    public void setOpacity(final String path, final boolean opacity)
    {
        if (path == null)
        {
            throw new AVMBadArgumentException("Null path.");
        }
        class TxnCallback implements RetryingTransactionCallback
        {
            public void perform()
            {
                fAVMRepository.setOpacity(path, opacity);
            }
        }
        TxnCallback doit = new TxnCallback();
        fTransaction.perform(doit, true);
    }

    /**
     * Get layering information about a path.
     * @param version The version to look under.
     * @param path The full AVM path.
     * @return A LayeringDescriptor.
     */
    public LayeringDescriptor getLayeringInfo(final int version, final String path)
    {
        if (path == null)
        {
            throw new AVMBadArgumentException("Null path: " + path);
        }
        class TxnCallback implements RetryingTransactionCallback
        {
            public LayeringDescriptor descriptor;
            
            public void perform()
            {
                descriptor = fAVMRepository.getLayeringInfo(version, path);
            }
        }
        TxnCallback doit = new TxnCallback();
        fTransaction.perform(doit, false);
        return doit.descriptor;
    }

    /**
     * Get the common ancestor of two nodes if one exists.
     * @param left The first node.
     * @param right The second node.
     * @return The common ancestor. There are four possible results. Null means
     * that there is no common ancestor.  Left returned means that left is strictly
     * an ancestor of right.  Right returned means that right is strictly an
     * ancestor of left.  Any other non null return is the common ancestor and
     * indicates that left and right are in conflict.
     */
    public AVMNodeDescriptor getCommonAncestor(final AVMNodeDescriptor left,
                                               final AVMNodeDescriptor right)
    {
        if (left == null || right == null)
        {
            throw new AVMBadArgumentException("Null node descriptor.");
        }
        class TxnCallback implements RetryingTransactionCallback
        {
            public AVMNodeDescriptor ancestor;
            
            public void perform()
            {
                ancestor = fAVMRepository.getCommonAncestor(left, right);
            }
        }
        TxnCallback doit = new TxnCallback();
        fTransaction.perform(doit, false);
        return doit.ancestor;
    }
    /**
     * Set a property on a node.
     * @param path The path to the node to set the property on. 
     * @param name The QName of the property.
     * @param value The property to set.
     */
    public void setProperty(final String path, final QName name, final PropertyValue value)
    {
        if (path == null || name == null || value == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        class TxnCallback implements RetryingTransactionCallback
        {
            public void perform()
            {
                fAVMRepository.setProperty(path, name, value);
            }
        }
        TxnCallback doit = new TxnCallback();
        fTransaction.perform(doit, true);
    }

    /**
     * Set a collection of properties on a node.
     * @param path The path to the node.
     * @param properties The Map of properties to set.
     */
    public void setProperties(final String path, final Map<QName, PropertyValue> properties)
    {
        if (path == null || properties == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        class TxnCallback implements RetryingTransactionCallback
        {
            public void perform()
            {
                fAVMRepository.setProperties(path, properties);
            }
        }
        TxnCallback doit = new TxnCallback();
        fTransaction.perform(doit, true);
    }

    /**
     * Get a property of a node by QName.
     * @param version The version to look under.
     * @param path The path to the node.
     * @param name The QName.
     * @return The PropertyValue or null if it doesn't exist.
     */
    public PropertyValue getProperty(final int version, final String path, final QName name)
    {
        if (path == null || name == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        class TxnCallback implements RetryingTransactionCallback
        {
            public PropertyValue value;
            
            public void perform()
            {
                value = fAVMRepository.getProperty(version, path, name);
            }
        }
        TxnCallback doit = new TxnCallback();
        fTransaction.perform(doit, false);
        return doit.value;
    }
    
    /**
     * Get all the properties associated with a node.
     * @param version The version to look under.
     * @param path The path to the node.
     * @return A List of AVMNodeProperties.
     */
    public Map<QName, PropertyValue> getProperties(final int version, final String path)
    {
        if (path == null)
        {
            throw new AVMBadArgumentException("Null path.");
        }
        class TxnCallback implements RetryingTransactionCallback
        {
            public Map<QName, PropertyValue> properties;
            
            public void perform()
            {
                properties = fAVMRepository.getProperties(version, path);
            }
        }
        TxnCallback doit = new TxnCallback();
        fTransaction.perform(doit, false);
        return doit.properties;
    }

    /**
     * Delete a property.
     * @param path The path to the node.
     * @param name The QName of the property to delete.
     */
    public void deleteProperty(final String path, final QName name)
    {
        if (path == null || name == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        class TxnCallback implements RetryingTransactionCallback
        {
            public void perform()
            {
                fAVMRepository.deleteProperty(path, name);
            }
        }
        TxnCallback doit = new TxnCallback();
        fTransaction.perform(doit, true);
    }
}
