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

import org.alfresco.filesys.repo.TempNetworkFile;
import org.alfresco.filesys.repo.rules.ScenarioInstance.Ranking;
import org.alfresco.filesys.repo.rules.commands.CloseFileCommand;
import org.alfresco.filesys.repo.rules.commands.CompoundCommand;
import org.alfresco.filesys.repo.rules.commands.CopyContentCommand;
import org.alfresco.filesys.repo.rules.commands.CreateFileCommand;
import org.alfresco.filesys.repo.rules.commands.DoNothingCommand;
import org.alfresco.filesys.repo.rules.commands.MoveFileCommand;
import org.alfresco.filesys.repo.rules.commands.DeleteFileCommand;
import org.alfresco.filesys.repo.rules.commands.OpenFileCommand;
import org.alfresco.filesys.repo.rules.commands.ReduceQuotaCommand;
import org.alfresco.filesys.repo.rules.commands.RemoveNoContentFileOnError;
import org.alfresco.filesys.repo.rules.commands.RemoveTempFileCommand;
import org.alfresco.filesys.repo.rules.commands.RenameFileCommand;
import org.alfresco.filesys.repo.rules.operations.CloseFileOperation;
import org.alfresco.filesys.repo.rules.operations.CreateFileOperation;
import org.alfresco.filesys.repo.rules.operations.DeleteFileOperation;
import org.alfresco.filesys.repo.rules.operations.MoveFileOperation;
import org.alfresco.filesys.repo.rules.operations.OpenFileOperation;
import org.alfresco.filesys.repo.rules.operations.RenameFileOperation;
import org.alfresco.jlan.server.filesys.NetworkFile;

/**
 * The Simple Standard Scenario is what will be done if no other 
 * scenario intervenes.
 */
public class ScenarioSimpleNonBufferedInstance implements ScenarioInstance
{
    private Ranking ranking = Ranking.LOW;
    
    @Override
    public Command evaluate(Operation operation)
    {
        if(operation instanceof CreateFileOperation)
        {
            CreateFileOperation c = (CreateFileOperation)operation;
            return new CreateFileCommand(c.getName(), c.getRootNodeRef(), c.getPath(), c.getAllocationSize());
        }
        else if(operation instanceof DeleteFileOperation)
        {
            DeleteFileOperation d = (DeleteFileOperation)operation;
            return new DeleteFileCommand(d.getName(), d.getRootNodeRef(), d.getPath());
        }
        else if(operation instanceof RenameFileOperation)
        {
            RenameFileOperation r = (RenameFileOperation)operation;
            return new RenameFileCommand(r.getFrom(), r.getTo(), r.getRootNodeRef(), r.getFromPath(), r.getToPath());
        }
        else if(operation instanceof MoveFileOperation)
        {
            MoveFileOperation m = (MoveFileOperation)operation;
            return new MoveFileCommand(m.getFrom(), m.getTo());
        }
        else if(operation instanceof OpenFileOperation)
        {
            OpenFileOperation o = (OpenFileOperation)operation;
            return new OpenFileCommand(o.getName(), o.getMode(), o.isTruncate(), o.getRootNodeRef(), o.getPath());
        }
        else if(operation instanceof CloseFileOperation)
        {
            CloseFileOperation c = (CloseFileOperation)operation;
            
            NetworkFile file = c.getNetworkFile();
            
            ArrayList<Command> commands = new ArrayList<Command>();
            ArrayList<Command> postCommitCommands = new ArrayList<Command>();
            ArrayList<Command> postErrorCommands = new ArrayList<Command>();
            
            commands.add(new CloseFileCommand(c.getName(), file, c.getRootNodeRef(), c.getPath()));
            
            // postErrorCommands.add(new RemoveNoContentFileOnError(c.getName(), c.getRootNodeRef(), c.getPath()));
            
            if(c.isDeleteOnClose())
            {
                postCommitCommands.add(new ReduceQuotaCommand(c.getName(), file, c.getRootNodeRef(), c.getPath()));
            }
            
            if (file instanceof TempNetworkFile)
            { 
                postCommitCommands.add(new RemoveTempFileCommand((TempNetworkFile)file));
            }

            return new CompoundCommand(commands, postCommitCommands, postErrorCommands);  
            
        }
        else return new DoNothingCommand();
    }

    @Override
    public boolean isComplete()
    {
        /** 
         * This instance is always complete
         */
        return true;
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
        return "ScenarioSimpleNonBuffered default instance";
    }
}
