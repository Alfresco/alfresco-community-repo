package org.alfresco.filesys.repo.rules.commands;

import java.util.List;

import org.alfresco.filesys.repo.rules.Command;

/**
 * The post commit command is executed after a successful completion of a command.
 * @author mrogers
 *
 */
public class PostCommitCommand
{
    List<Command> commands;
}
