package org.alfresco.repo.module.tool;

/**
 * I didn't want to create this.  The module management tool has an outputMessage method.  I needed an implentation-independent way of outputting
 * a message from a helper class without relying on a Logging framework or a Module Management Tool Class.
 * 
 * Hopefully this will be removed when the code is refactored.
 *
 * @author Gethin James
 */
public interface LogOutput
{

    public void info(Object message);
}
