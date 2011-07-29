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
package org.alfresco.filesys.repo.rules.commands;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.filesys.repo.rules.Command;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;

/**
 * A compound operation contains one or more other commands.
 */
public class CompoundCommand implements Command
{ 
    List<Command> commands;
    
    List<Command> postCommitCommands;
    
    List<Command> postErrorCommands;
    
    /**
     * New Compound Command containing the specified commands.
     * @param commands
     */
    public CompoundCommand(List<Command> commands)
    {
        this.commands = new ArrayList<Command>(commands);
    }
    
    /**
     * New Compound Command containing the specified commands.
     * @param commands
     */
    public CompoundCommand(List<Command> commands, List<Command> postCommitCommands)
    {
        this.commands = new ArrayList<Command>(commands);
        
        this.postCommitCommands = new ArrayList<Command>(postCommitCommands);
    }
    
    public CompoundCommand(List<Command> commands, List<Command> postCommitCommands, List<Command>postErrorCommands)
    {
        this.commands = new ArrayList<Command>(commands);
        
        this.postCommitCommands = new ArrayList<Command>(postCommitCommands);
        
        this.postErrorCommands = new ArrayList<Command>(postErrorCommands);
    }
    
    public List<Command> getCommands()
    {
        return commands;
    }
    
    public List<Command> getPostCommitCommands()
    {
        return postCommitCommands;
    }
    
    public List<Command> getPostErrorCommands()
    {
        return postErrorCommands;
    }

    @Override
    public TxnReadState getTransactionRequired()
    {
        TxnReadState readState = TxnReadState.TXN_NONE;
        for(Command command : commands)
        {
            TxnReadState x = command.getTransactionRequired();
            
            if(x != null && x.compareTo(readState) > 0)
            {
                readState = x;
            }
        }
        
        return readState;
    }
}
