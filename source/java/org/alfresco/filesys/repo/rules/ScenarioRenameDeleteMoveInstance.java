/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.filesys.repo.rules;

import java.util.ArrayList;
import java.util.Date;

import org.alfresco.filesys.repo.ResultCallback;
import org.alfresco.filesys.repo.rules.commands.CloseFileCommand;
import org.alfresco.filesys.repo.rules.commands.CompoundCommand;
import org.alfresco.filesys.repo.rules.commands.CopyContentCommand;
import org.alfresco.filesys.repo.rules.commands.DeleteFileCommand;
import org.alfresco.filesys.repo.rules.commands.RenameFileCommand;
import org.alfresco.filesys.repo.rules.commands.RestoreFileCommand;
import org.alfresco.filesys.repo.rules.operations.CloseFileOperation;
import org.alfresco.filesys.repo.rules.operations.DeleteFileOperation;
import org.alfresco.filesys.repo.rules.operations.MoveFileOperation;
import org.alfresco.filesys.repo.rules.operations.RenameFileOperation;
import org.alfresco.jlan.server.filesys.FileName;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is an instance of a rename, delete, move scenario triggered by a rename of a 
 * file matching a specified pattern.
 * <p>
 * a) Original file is renamed.   Typically with an obscure name.
 * b) Renamed file is deleted via delete command or via deleteOnClose flag and close operation.
 * c) Temp file is moved into original file location.
 * 
 * <p>
 * If this filter is active then this is what happens.
 * a) Original file is renamed:
 *    - File is renamed.
 * b) Renamed file is deleted via delete command or via deleteOnClose flag and close operation: 
 *    - File is deleted.
 * c) Temp file is moved into original file location - Scenario fires 
 *    - Deleted file is restored.
 *    - Restored file is renamed to it's original name.
 *    - Content from file that must be moved is copied to restored file.
 *    - File that must be moved is deleted.
 */
public class ScenarioRenameDeleteMoveInstance implements ScenarioInstance
{
    private static Log logger = LogFactory.getLog(ScenarioRenameDeleteMoveInstance.class);

    enum InternalState
    {
        NONE, DELETE, MOVE
    }

    InternalState internalState = InternalState.NONE;

    private Date startTime = new Date();

    private String fileMiddle;
    private String fileFrom;
    private String fileEnd;

    private Ranking ranking;
    private boolean deleteBackup;

    /**
     * Timeout in ms. Default 30 seconds.
     */
    private long timeout = 30000;

    private boolean isComplete;
    private String folderMiddle;
    private String folderEnd;
    private NodeRef originalNodeRef;

    /**
     * Evaluate the next operation
     * 
     * @param operation
     */
    public Command evaluate(Operation operation)
    {

        /**
         * Anti-pattern : timeout
         */
        Date now = new Date();
        if (now.getTime() > startTime.getTime() + getTimeout())
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Instance timed out");

            }
            isComplete = true;
            return null;
        }

        switch (internalState)
        {

        case NONE:

            if (operation instanceof RenameFileOperation)
            {
                RenameFileOperation r = (RenameFileOperation) operation;
                fileMiddle = r.getFrom();
                fileEnd = r.getTo();

                String[] paths = FileName.splitPath(r.getFromPath());
                folderMiddle = paths[0];

                String[] paths2 = FileName.splitPath(r.getToPath());
                folderEnd = paths2[0];

                internalState = InternalState.DELETE;
            }
            else
            {
                // anything else bomb out
                if (logger.isDebugEnabled())
                {
                    logger.debug("State error, expected a RENAME");
                }
                isComplete = true;
            }

        case DELETE:

            if (operation instanceof DeleteFileOperation)
            {
                internalState = InternalState.MOVE;
                DeleteFileOperation d = (DeleteFileOperation) operation;
                if (d.getName().equalsIgnoreCase(fileEnd))
                {
                    ArrayList<Command> commands = new ArrayList<Command>();
                    ArrayList<Command> postCommitCommands = new ArrayList<Command>();
                    ArrayList<Command> postErrorCommands = new ArrayList<Command>();
                    // Rename node to remove "hidden". In this case node will be moved to the archive store and can be restored later.
                    // This can be replaced with command that removes hidden aspect in future(when ContentDiskDriver2.setFileInformation() method will support hidden attribute)
                    RenameFileCommand r1 = new RenameFileCommand(fileEnd, "tmp" + fileEnd, d.getRootNodeRef(), folderEnd + "\\" + fileEnd, folderEnd + "\\" + "tmp" + fileEnd);
                    fileEnd = "tmp" + fileEnd;
                    commands.add(r1);
                    commands.add(new DeleteFileCommand(fileEnd, d.getRootNodeRef(), folderEnd + "\\" + fileEnd));
                    postCommitCommands.add(newDeleteFileCallbackCommand());
                    return new CompoundCommand(commands, postCommitCommands, postErrorCommands);
                }
            }
            if (operation instanceof CloseFileOperation)
            {
                CloseFileOperation c = (CloseFileOperation) operation;
                if (c.getNetworkFile().hasDeleteOnClose() && c.getName().equalsIgnoreCase(fileEnd))
                {
                    internalState = InternalState.MOVE;
                    ArrayList<Command> commands = new ArrayList<Command>();
                    ArrayList<Command> postCommitCommands = new ArrayList<Command>();
                    ArrayList<Command> postErrorCommands = new ArrayList<Command>();
                    // Rename node to remove "hidden". In this case node will be moved to the archive store and can be restored later.
                    RenameFileCommand r1 = new RenameFileCommand(fileEnd, "tmp" + fileEnd, c.getRootNodeRef(), folderEnd + "\\" + fileEnd, folderEnd + "\\" + "tmp" + fileEnd);
                    fileEnd = "tmp" + fileEnd;
                    commands.add(r1);
                    commands.add(new CloseFileCommand(fileEnd, c.getNetworkFile(), c.getRootNodeRef(), folderEnd + "\\" + fileEnd));
                    postCommitCommands.add(newDeleteFileCallbackCommand());
                    return new CompoundCommand(commands, postCommitCommands, postErrorCommands);
                }

            }

            break;

        case MOVE:

            if (operation instanceof MoveFileOperation && originalNodeRef != null)
            {
                if (logger.isDebugEnabled())
                {
                    logger.info("Tracking rename: " + operation);
                }
                MoveFileOperation m = (MoveFileOperation) operation;

                if (fileMiddle.equalsIgnoreCase(m.getTo()))
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Got second rename");
                    }

                    fileFrom = m.getFrom();

                    String[] paths = FileName.splitPath(m.getFromPath());
                    String oldFolder = paths[0];

                    ArrayList<Command> commands = new ArrayList<Command>();

                    RestoreFileCommand rest1 = new RestoreFileCommand(fileEnd, m.getRootNodeRef(), folderEnd, 0, originalNodeRef);
                    RenameFileCommand r1 = new RenameFileCommand(fileEnd, fileMiddle, m.getRootNodeRef(), folderEnd + "\\" + fileEnd, folderMiddle + "\\" + fileMiddle);
                    commands.add(rest1);
                    commands.add(r1);
                    CopyContentCommand copyContent = new CopyContentCommand(fileFrom, fileMiddle, m.getRootNodeRef(), oldFolder + "\\" + fileFrom, folderMiddle + "\\" + fileMiddle);
                    commands.add(copyContent);
                    DeleteFileCommand d1 = new DeleteFileCommand(oldFolder, m.getRootNodeRef(), oldFolder + "\\" + fileFrom);
                    commands.add(d1);

                    isComplete = true;
                    return new CompoundCommand(commands);
                }
            }

            break;
        }

        return null;
    }

    @Override
    public boolean isComplete()
    {
        return isComplete;
    }

    @Override
    public Ranking getRanking()
    {
        return ranking;
    }

    public void setRanking(Ranking ranking)
    {
        this.ranking = ranking;
    }

    public String toString()
    {
        return "ScenarioRenameDeleteMove:" + fileMiddle;
    }

    public void setTimeout(long timeout)
    {
        this.timeout = timeout;
    }

    public long getTimeout()
    {
        return timeout;
    }

    public void setDeleteBackup(boolean deleteBackup)
    {
        this.deleteBackup = deleteBackup;
    }

    public boolean isDeleteBackup()
    {
        return deleteBackup;
    }

    /**
     * Called for delete file.
     */
    private ResultCallback newDeleteFileCallbackCommand()
    {
        return new ResultCallback()
        {
            @Override
            public void execute(Object result)
            {
                if (result instanceof NodeRef)
                {
                    logger.debug("got node ref of deleted node");
                    originalNodeRef = (NodeRef) result;
                }
            }

            @Override
            public TxnReadState getTransactionRequired()
            {
                return TxnReadState.TXN_NONE;
            }
        };
    }
}
