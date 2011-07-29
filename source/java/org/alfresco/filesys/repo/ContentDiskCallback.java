package org.alfresco.filesys.repo;

import java.io.IOException;

import org.alfresco.filesys.config.ServerConfigurationBean;
import org.alfresco.jlan.server.SrvSession;
import org.alfresco.jlan.server.core.DeviceContext;
import org.alfresco.jlan.server.core.DeviceContextException;
import org.alfresco.jlan.server.filesys.FileInfo;
import org.alfresco.jlan.server.filesys.FileOpenParams;
import org.alfresco.jlan.server.filesys.NetworkFile;
import org.alfresco.jlan.server.filesys.SearchContext;
import org.alfresco.jlan.server.filesys.TreeConnection;

/**
 * Called by the NonTransactionalContentDiskDriver to advise of operations completed 
 * by the ContentDiskInterface.
 */
public interface ContentDiskCallback
{
    public void getFileInformation(SrvSession sess, TreeConnection tree,
            String path, FileInfo info);
    
    public void fileExists(SrvSession sess, TreeConnection tree, String path, int fileExists);
  
    public void treeOpened(SrvSession sess, TreeConnection tree);

    public void treeClosed(SrvSession sess, TreeConnection tree);

    public void closeFile(SrvSession sess, TreeConnection tree,
            NetworkFile param);

    public void createDirectory(SrvSession sess, TreeConnection tree,
            FileOpenParams params);
 
    public void createFile(SrvSession sess, TreeConnection tree,
            FileOpenParams params, NetworkFile newFile);


    public void deleteDirectory(SrvSession sess, TreeConnection tree, String dir);

    public void deleteFile(SrvSession sess, TreeConnection tree, String name);

    public void flushFile(SrvSession sess, TreeConnection tree, NetworkFile file);

    public void isReadOnly(SrvSession sess, DeviceContext ctx, boolean isReadOnly);

    public void openFile(SrvSession sess, TreeConnection tree,
            FileOpenParams param, NetworkFile openFile);
  
    public void readFile(SrvSession sess, TreeConnection tree, NetworkFile file,
            byte[] buf, int bufPos, int siz, long filePos, int readSize);

    public void renameFile(SrvSession sess, TreeConnection tree,
            String oldPath, String newPath);

    public void seekFile(SrvSession sess, TreeConnection tree,
            NetworkFile file, long pos, int typ) throws IOException;

    public void setFileInformation(SrvSession sess, TreeConnection tree,
            String name, FileInfo info) throws IOException;

    public void startSearch(SrvSession sess, TreeConnection tree,
            String searchPath, int attrib, SearchContext context);

    public void truncateFile(SrvSession sess, TreeConnection tree,
            NetworkFile file, long siz);


    public void writeFile(SrvSession sess, TreeConnection tree,
            NetworkFile file, byte[] buf, int bufoff, int siz, long fileoff, int writeSize);

    public void registerContext(DeviceContext ctx, ServerConfigurationBean serverConfig)
            throws DeviceContextException;
   
}
