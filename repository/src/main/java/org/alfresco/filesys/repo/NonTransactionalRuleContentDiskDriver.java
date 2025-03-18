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
package org.alfresco.filesys.repo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.config.ConfigElement;

import org.alfresco.filesys.alfresco.ExtendedDiskInterface;
import org.alfresco.filesys.alfresco.RepositoryDiskInterface;
import org.alfresco.filesys.repo.rules.Command;
import org.alfresco.filesys.repo.rules.EvaluatorContext;
import org.alfresco.filesys.repo.rules.Operation;
import org.alfresco.filesys.repo.rules.RuleEvaluator;
import org.alfresco.filesys.repo.rules.operations.CloseFileOperation;
import org.alfresco.filesys.repo.rules.operations.CreateFileOperation;
import org.alfresco.filesys.repo.rules.operations.DeleteFileOperation;
import org.alfresco.filesys.repo.rules.operations.MoveFileOperation;
import org.alfresco.filesys.repo.rules.operations.OpenFileOperation;
import org.alfresco.filesys.repo.rules.operations.RenameFileOperation;
import org.alfresco.jlan.server.SrvSession;
import org.alfresco.jlan.server.core.DeviceContext;
import org.alfresco.jlan.server.core.DeviceContextException;
import org.alfresco.jlan.server.filesys.AccessDeniedException;
import org.alfresco.jlan.server.filesys.FileAttribute;
import org.alfresco.jlan.server.filesys.FileInfo;
import org.alfresco.jlan.server.filesys.FileName;
import org.alfresco.jlan.server.filesys.FileOpenParams;
import org.alfresco.jlan.server.filesys.NetworkFile;
import org.alfresco.jlan.server.filesys.SearchContext;
import org.alfresco.jlan.server.filesys.TreeConnection;
import org.alfresco.jlan.smb.SharingMode;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.FileFilterMode;
import org.alfresco.util.PropertyCheck;

/**
 * Non Transactional DiskDriver with rules engine.
 * <p>
 * Provides a DiskInterface that deals with "shuffles". Shuffles are implemented by the Rules Engine.
 * <p>
 * Sits on top of the repository and is non-retryable and non-transactional. It is, however thread safe and multiple callers may call in parallel.
 */
public class NonTransactionalRuleContentDiskDriver implements ExtendedDiskInterface
{
    /**
     * The Driver State. Contained within the JLAN SrvSession.
     */
    private class DriverState
    {
        /**
         * key, value pair storage for the session
         */
        Map<String, Object> sessionState = new ConcurrentHashMap<String, Object>();

        /**
         * Map of folderName to Evaluator Context.
         */
        Map<String, EvaluatorContext> contextMap = new ConcurrentHashMap<String, EvaluatorContext>();
    }

    private static final Log logger = LogFactory.getLog(NonTransactionalRuleContentDiskDriver.class);

    private ExtendedDiskInterface diskInterface;
    private RuleEvaluator ruleEvaluator;
    private RepositoryDiskInterface repositoryDiskInterface;
    private CommandExecutor commandExecutor;

    public void init()
    {
        PropertyCheck.mandatory(this, "diskInterface", diskInterface);
        PropertyCheck.mandatory(this, "ruleEvaluator", getRuleEvaluator());
        PropertyCheck.mandatory(this, "repositoryDiskInterface", getRepositoryDiskInterface());
        PropertyCheck.mandatory(this, "commandExecutor", getCommandExecutor());
    }

    @Override
    public FileInfo getFileInformation(SrvSession sess, TreeConnection tree,
            String path) throws IOException
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("getFileInformation:" + path);
        }
        FileFilterMode.setClient(ClientHelper.getClient(sess));
        try
        {
            FileInfo info = diskInterface.getFileInformation(sess, tree, path);
            return info;
        }
        finally
        {
            FileFilterMode.clearClient();

        }
    }

    @Override
    public int fileExists(SrvSession sess, TreeConnection tree, String path)
    {
        int fileExists = diskInterface.fileExists(sess, tree, path);

        return fileExists;
    }

    @Override
    public DeviceContext createContext(String shareName, ConfigElement args)
            throws DeviceContextException
    {
        return diskInterface.createContext(shareName, args);
    }

    @Override
    public void treeOpened(SrvSession sess, TreeConnection tree)
    {
        diskInterface.treeOpened(sess, tree);
    }

    @Override
    public void treeClosed(SrvSession sess, TreeConnection tree)
    {
        diskInterface.treeClosed(sess, tree);

    }

    @Override
    public void closeFile(SrvSession sess, TreeConnection tree,
            NetworkFile param) throws IOException
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("closeFile:" + param.getFullName());
        }

        ContentContext tctx = (ContentContext) tree.getContext();
        NodeRef rootNode = tctx.getRootNode();

        DriverState driverState = getDriverState(sess);

        String[] paths = FileName.splitPath(param.getFullName());
        String folder = paths[0];
        String file = paths[1];

        try
        {
            EvaluatorContext ctx = getEvaluatorContext(driverState, folder);

            Operation o = new CloseFileOperation(file, param, rootNode, param.getFullName(), param.hasDeleteOnClose(), param.isForce());
            Command c = ruleEvaluator.evaluate(ctx, o);

            commandExecutor.execute(sess, tree, c);

            releaseEvaluatorContextIfEmpty(driverState, ctx, folder);
        }
        catch (org.alfresco.repo.security.permissions.AccessDeniedException ade)
        {
            throw new AccessDeniedException("Unable to close file " + param.getFullName(), ade);
        }

    }

    @Override
    public void createDirectory(SrvSession sess, TreeConnection tree,
            FileOpenParams params) throws IOException
    {
        try
        {
            FileFilterMode.setClient(ClientHelper.getClient(sess));
            try
            {
                diskInterface.createDirectory(sess, tree, params);
            }
            finally
            {
                FileFilterMode.clearClient();
            }
        }
        catch (org.alfresco.repo.security.permissions.AccessDeniedException ade)
        {
            throw new AccessDeniedException("Unable to create directory " + params.getPath(), ade);
        }
    }

    @Override
    public NetworkFile createFile(SrvSession sess, TreeConnection tree,
            FileOpenParams params) throws IOException
    {
        try
        {
            int attr = params.getAttributes();
            if (logger.isDebugEnabled())
            {
                int sharedAccess = params.getSharedAccess();
                String strSharedAccess = SharingMode.getSharingModeAsString(sharedAccess);

                logger.debug("createFile:" + params.getPath()
                        + ", isDirectory: " + params.isDirectory()
                        + ", isStream: " + params.isStream()
                        + ", readOnlyAccess: " + params.isReadOnlyAccess()
                        + ", readWriteAccess: " + params.isReadWriteAccess()
                        + ", writeOnlyAccess:" + params.isWriteOnlyAccess()
                        + ", attributesOnlyAccess:" + params.isAttributesOnlyAccess()
                        + ", sequentialAccessOnly:" + params.isSequentialAccessOnly()
                        + ", requestBatchOpLock:" + params.requestBatchOpLock()
                        + ", requestExclusiveOpLock:" + params.requestExclusiveOpLock()
                        + ", isDeleteOnClose:" + params.isDeleteOnClose()
                        + ", sharedAccess: " + strSharedAccess
                        + ", allocationSize: " + params.getAllocationSize()
                        + ", isHidden:" + FileAttribute.isHidden(attr)
                        + ", isSystem:" + FileAttribute.isSystem(attr));
            }

            long creationDateTime = params.getCreationDateTime();
            if (creationDateTime != 0)
            {
                logger.debug("creationDateTime is set:" + new Date(creationDateTime));
            }

            ContentContext tctx = (ContentContext) tree.getContext();
            NodeRef rootNode = tctx.getRootNode();

            String[] paths = FileName.splitPath(params.getPath());
            String folder = paths[0];
            String file = paths[1];

            DriverState driverState = getDriverState(sess);
            EvaluatorContext ctx = getEvaluatorContext(driverState, folder);

            Operation o = new CreateFileOperation(file, rootNode, params.getPath(), params.getAllocationSize(), FileAttribute.isHidden(attr));
            Command c = ruleEvaluator.evaluate(ctx, o);

            Object ret = commandExecutor.execute(sess, tree, c);

            if (ret != null && ret instanceof NetworkFile)
            {
                return (NetworkFile) ret;
            }
            else
            {
                // Error - contact broken
                logger.error("contract broken - NetworkFile not returned. " + ret == null ? "Return value is null" : ret);
                return null;
            }
        }
        catch (org.alfresco.repo.security.permissions.AccessDeniedException ade)
        {
            throw new AccessDeniedException("Unable to create file " + params.getPath(), ade);
        }
    }

    @Override
    public void deleteDirectory(SrvSession sess, TreeConnection tree, String dir)
            throws IOException
    {
        try
        {
            diskInterface.deleteDirectory(sess, tree, dir);
        }
        catch (org.alfresco.repo.security.permissions.AccessDeniedException ade)
        {
            throw new AccessDeniedException("Unable to delete directory " + dir, ade);
        }
    }

    @Override
    public void deleteFile(SrvSession sess, TreeConnection tree, String name)
            throws IOException
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("deleteFile name:" + name);
        }
        try
        {
            ContentContext tctx = (ContentContext) tree.getContext();
            NodeRef rootNode = tctx.getRootNode();

            DriverState driverState = getDriverState(sess);

            String[] paths = FileName.splitPath(name);
            String folder = paths[0];
            String file = paths[1];

            EvaluatorContext ctx = getEvaluatorContext(driverState, folder);

            Operation o = new DeleteFileOperation(file, rootNode, name);
            Command c = ruleEvaluator.evaluate(ctx, o);
            commandExecutor.execute(sess, tree, c);

            releaseEvaluatorContextIfEmpty(driverState, ctx, folder);
        }
        catch (org.alfresco.repo.security.permissions.AccessDeniedException ade)
        {
            throw new AccessDeniedException("Unable to delete file " + name, ade);
        }

    } // End of deleteFile

    @Override
    public void flushFile(SrvSession sess, TreeConnection tree, NetworkFile file)
            throws IOException
    {
        diskInterface.flushFile(sess, tree, file);

    }

    @Override
    public boolean isReadOnly(SrvSession sess, DeviceContext ctx)
            throws IOException
    {
        boolean isReadOnly = diskInterface.isReadOnly(sess, ctx);

        return isReadOnly;
    }

    @Override
    public NetworkFile openFile(SrvSession sess, TreeConnection tree,
            FileOpenParams param) throws IOException
    {
        String path = param.getPath();

        boolean truncate = param.isOverwrite();

        if (logger.isDebugEnabled())
        {
            int sharedAccess = param.getSharedAccess();
            String strSharedAccess = SharingMode.getSharingModeAsString(sharedAccess);

            logger.debug("openFile:" + path
                    + ", isDirectory: " + param.isDirectory()
                    + ", isStream: " + param.isStream()
                    + ", readOnlyAccess: " + param.isReadOnlyAccess()
                    + ", readWriteAccess: " + param.isReadWriteAccess()
                    + ", writeOnlyAccess:" + param.isWriteOnlyAccess()
                    + ", attributesOnlyAccess:" + param.isAttributesOnlyAccess()
                    + ", sequentialAccessOnly:" + param.isSequentialAccessOnly()
                    + ", writeThrough:" + param.isWriteThrough()
                    + ", truncate:" + truncate
                    + ", requestBatchOpLock:" + param.requestBatchOpLock()
                    + ", requestExclusiveOpLock:" + param.requestExclusiveOpLock()
                    + ", isDeleteOnClose:" + param.isDeleteOnClose()
                    + ", allocationSize:" + param.getAllocationSize()
                    + ", sharedAccess: " + strSharedAccess
                    + ", openAction: " + param.getOpenAction()
                    + param);
        }

        ContentContext tctx = (ContentContext) tree.getContext();
        NodeRef rootNode = tctx.getRootNode();

        DriverState driverState = getDriverState(sess);

        String[] paths = FileName.splitPath(path);
        String folder = paths[0];
        String file = paths[1];

        EvaluatorContext ctx = getEvaluatorContext(driverState, folder);

        OpenFileMode openMode = OpenFileMode.READ_ONLY;

        if (param.isAttributesOnlyAccess())
        {
            openMode = OpenFileMode.ATTRIBUTES_ONLY;
        }
        else if (param.isReadWriteAccess())
        {
            openMode = OpenFileMode.READ_WRITE;
        }
        else if (param.isWriteOnlyAccess())
        {
            openMode = OpenFileMode.WRITE_ONLY;
        }
        else if (param.isReadOnlyAccess())
        {
            openMode = OpenFileMode.READ_ONLY;
        }
        else if (param.isDeleteOnClose())
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("open file has delete on close");
            }
            openMode = OpenFileMode.DELETE;
        }

        try
        {
            Operation o = new OpenFileOperation(file, openMode, truncate, rootNode, path);
            Command c = ruleEvaluator.evaluate(ctx, o);
            Object ret = commandExecutor.execute(sess, tree, c);

            if (ret != null && ret instanceof NetworkFile)
            {
                NetworkFile x = (NetworkFile) ret;

                if (logger.isDebugEnabled())
                {
                    logger.debug("returning open file: for path:" + path + ", ret:" + ret);
                }
                return x;
            }
            else
            {
                // Error - contact broken
                logger.error("contract broken - NetworkFile not returned. " + ret == null ? "Return value is null" : ret);
                return null;
            }
        }
        catch (org.alfresco.repo.security.permissions.AccessDeniedException ade)
        {
            throw new AccessDeniedException("Unable to open file " + param.getPath(), ade);
        }

        // return diskInterface.openFile(sess, tree, params);
    } // End of OpenFile

    @Override
    public int readFile(SrvSession sess, TreeConnection tree, NetworkFile file,
            byte[] buf, int bufPos, int siz, long filePos) throws IOException
    {
        int readSize = diskInterface.readFile(sess, tree, file, buf, bufPos, siz, filePos);
        return readSize;
    }

    @Override
    public void renameFile(SrvSession sess, TreeConnection tree,
            String oldPath, String newPath) throws IOException
    {
        ContentContext tctx = (ContentContext) tree.getContext();
        NodeRef rootNode = tctx.getRootNode();

        if (logger.isDebugEnabled())
        {
            logger.debug("renameFile oldPath:" + oldPath + ", newPath:" + newPath);
        }

        DriverState driverState = getDriverState(sess);

        // Is this a rename within the same folder or a move between folders?

        String[] paths = FileName.splitPath(oldPath);
        String oldFolder = paths[0];
        String oldFile = paths[1];

        paths = FileName.splitPath(newPath);
        String newFolder = paths[0];
        String newFile = paths[1];

        try
        {
            if (oldFolder.equalsIgnoreCase(newFolder))
            {
                logger.debug("renameFileCommand - is a rename within the same folder");

                EvaluatorContext ctx = getEvaluatorContext(driverState, oldFolder);

                Operation o = new RenameFileOperation(oldFile, newFile, oldPath, newPath, rootNode);
                Command c = ruleEvaluator.evaluate(ctx, o);
                commandExecutor.execute(sess, tree, c);

                ruleEvaluator.notifyRename(ctx, o, c);

                releaseEvaluatorContextIfEmpty(driverState, ctx, oldFolder);

            }
            else
            {
                logger.debug("moveFileCommand - move between folders");

                Operation o = new MoveFileOperation(oldFile, newFile, oldPath, newPath, rootNode);

                /* Note: At the moment we only have move scenarios for the destination folder - so we only need to evaluate against a single (destination) context/folder. This will require re-design as and when we need to have scenarios for the source/folder */

                // EvaluatorContext ctx1 = getEvaluatorContext(driverState, oldFolder);
                EvaluatorContext ctx2 = getEvaluatorContext(driverState, newFolder);

                Command c = ruleEvaluator.evaluate(ctx2, o);

                commandExecutor.execute(sess, tree, c);

                releaseEvaluatorContextIfEmpty(driverState, ctx2, newFolder);

                // diskInterface.renameFile(sess, tree, oldPath, newPath);

            }
        }
        catch (org.alfresco.repo.security.permissions.AccessDeniedException ade)
        {
            throw new AccessDeniedException("Unable to rename file file " + oldPath, ade);
        }

    }

    @Override
    public long seekFile(SrvSession sess, TreeConnection tree,
            NetworkFile file, long pos, int typ) throws IOException
    {
        long ret = diskInterface.seekFile(sess, tree, file, pos, typ);

        return ret;
    }

    @Override
    public void setFileInformation(SrvSession sess, TreeConnection tree,
            String name, FileInfo info) throws IOException
    {
        diskInterface.setFileInformation(sess, tree, name, info);

    }

    @Override
    public SearchContext startSearch(SrvSession sess, TreeConnection tree,
            String searchPath, int attrib) throws FileNotFoundException
    {
        FileFilterMode.setClient(ClientHelper.getClient(sess));
        try
        {
            SearchContext context = diskInterface.startSearch(sess, tree, searchPath, attrib);
            return context;
        }
        finally
        {
            FileFilterMode.clearClient();
        }
    }

    @Override
    public void truncateFile(SrvSession sess, TreeConnection tree,
            NetworkFile file, long siz) throws IOException
    {
        diskInterface.truncateFile(sess, tree, file, siz);

    }

    @Override
    public int writeFile(SrvSession sess, TreeConnection tree,
            NetworkFile file, byte[] buf, int bufoff, int siz, long fileoff)
            throws IOException
    {
        int writeSize = diskInterface.writeFile(sess, tree, file, buf, bufoff, siz, fileoff);

        return writeSize;
    }

    public void setDiskInterface(ExtendedDiskInterface diskInterface)
    {
        this.diskInterface = diskInterface;
    }

    public ExtendedDiskInterface getDiskInterface()
    {
        return diskInterface;
    }

    public void setRuleEvaluator(RuleEvaluator ruleEvaluator)
    {
        this.ruleEvaluator = ruleEvaluator;
    }

    public RuleEvaluator getRuleEvaluator()
    {
        return ruleEvaluator;
    }

    @Override
    public void registerContext(DeviceContext ctx)
            throws DeviceContextException
    {
        diskInterface.registerContext(ctx);
    }

    public void setRepositoryDiskInterface(RepositoryDiskInterface repositoryDiskInterface)
    {
        this.repositoryDiskInterface = repositoryDiskInterface;
    }

    public RepositoryDiskInterface getRepositoryDiskInterface()
    {
        return repositoryDiskInterface;
    }

    public void setCommandExecutor(CommandExecutor commandExecutor)
    {
        this.commandExecutor = commandExecutor;
    }

    public CommandExecutor getCommandExecutor()
    {
        return commandExecutor;
    }

    /**
     * Get the driver state from the session.
     * 
     * @param sess
     *            SrvSession
     * @return the driver state.
     */
    private DriverState getDriverState(SrvSession sess)
    {
        synchronized (sess)
        {
            // Get the driver state
            Object state = sess.getDriverState();
            if (state == null)
            {
                state = new DriverState();
                sess.setDriverState(state);
                if (logger.isDebugEnabled())
                {
                    logger.debug("new driver state created");
                }

            }
            DriverState driverState = (DriverState) state;
            return driverState;
        }
    }

    /**
     * Get the evaluator context from the state and the folder.
     * 
     * @param driverState
     *            DriverState
     * @param folder
     *            String
     * @return EvaluatorContext
     */
    private EvaluatorContext getEvaluatorContext(DriverState driverState, String folder)
    {
        synchronized (driverState.contextMap)
        {
            EvaluatorContext ctx = driverState.contextMap.get(folder);
            if (ctx == null)
            {
                ctx = ruleEvaluator.createContext(driverState.sessionState);
                driverState.contextMap.put(folder, ctx);
                if (logger.isDebugEnabled())
                {
                    logger.debug("new driver context: " + folder);
                }
            }
            return ctx;
        }
    }

    /**
     * Release the evaluator context if there are no active scenarios.
     * 
     * @param driverState
     *            DriverState
     * @param ctx
     *            EvaluatorContext
     * @param folder
     *            String
     */
    private void releaseEvaluatorContextIfEmpty(DriverState driverState, EvaluatorContext ctx, String folder)
    {
        synchronized (driverState.contextMap)
        {
            if (ctx != null)
            {
                if (ctx.getScenarioInstances().size() > 0)
                {}
                else
                {
                    driverState.contextMap.remove(folder);
                }
            }

        }
    }

}
