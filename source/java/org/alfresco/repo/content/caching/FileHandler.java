package org.alfresco.repo.content.caching;

import java.io.File;

/**
 * Callback interface for file-based actions.
 * 
 * @author Matt Ward
 */
public interface FileHandler
{
    void handle(File file);
}
