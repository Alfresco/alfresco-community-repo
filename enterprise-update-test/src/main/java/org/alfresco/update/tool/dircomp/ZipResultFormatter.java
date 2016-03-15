package org.alfresco.update.tool.dircomp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Format the org.alfresco.update.tool.dircomp.ResultSet as a set of files inside a zip file.
 * 
 * Files that are equal are not added to the zip file.
 * 
 * @author mrogers
 */
public class ZipResultFormatter implements ResultFormatter
{
    @Override
    public void format(ResultSet resultSet, OutputStream out)
    {
        ZipOutputStream zos = (ZipOutputStream)out;
        
        for(Result result : resultSet.results)
        {
            if(!result.equal)
            {
                try
                {
                    putFile(result.p1, zos);
                    putFile(result.p2, zos);
                }
                catch (IOException ie)
                {
                    // Do nothing
                }
            }
        }
    }
    
    private void putFile(Path path, ZipOutputStream zos) throws IOException
    {
        
        if(path != null)
        {
            byte[] buffer = new byte[1024];
            File file = path.toFile();
            if(file.isFile())
            {
                ZipEntry zipEntry = new ZipEntry(getEntryName(path));
                zipEntry.setTime(file.lastModified());
                try (FileInputStream ins = new FileInputStream(file))
                {   
                    zos.putNextEntry(zipEntry);
                    int len;
                    while ((len = ins.read(buffer)) > 0) 
                    {
                        zos.write(buffer, 0, len);
                    }
                    zos.closeEntry();
                }
            }
        }
    }
    
    private String getEntryName(Path path)
    {
       // eg differences/xml-data/foo/bar
       return "differences" + path.normalize().toString().replace('\\', '/').trim();
    }

}
