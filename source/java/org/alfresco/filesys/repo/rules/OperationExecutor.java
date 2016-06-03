package org.alfresco.filesys.repo.rules;

/**
 * An operation executor is an implementation of how to execute an 
 * operation.  i.e. It is the thing that does stuff to the repo.
 */
public interface OperationExecutor
{
    public void execute(Operation operation);

}
