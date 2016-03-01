 
package org.alfresco.module.org_alfresco_module_rm.content.cleanser;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

/**
 * Content cleanser base implementation.
 * 
 * @author Roy Wetherall
 * @since 2.4.a
 */
public abstract class ContentCleanser
{    
    /**
     * Cleanse file
     * 
     * @param file  file to cleanse
     */
    public abstract void cleanse(File file);
    
    /**
     * Overwrite files bytes with provided overwrite operation
     * 
     * @param file                  file 
     * @param overwriteOperation    overwrite operation
     */
    protected void overwrite(File file, OverwriteOperation overwriteOperation)
    {   
        // get the number of bytes
        long bytes = file.length();
        try
        {
            // get an output stream
            try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file)))
            {
                for (int i = 0; i < bytes; i++)
                {
                    // overwrite byte
                    overwriteOperation.operation(os);
                }
            }
        }
        catch (IOException ioException)
        {
            // re-throw
            throw new RuntimeException("Unable to overwrite file", ioException);
        }
    }
    
    /**
     * Overwrite operation 
     */
    protected abstract class OverwriteOperation
    {
        public abstract void operation(OutputStream os) throws IOException;
    }
    
    /**
     * Overwrite with zeros operation
     */
    protected OverwriteOperation overwriteZeros = new OverwriteOperation()
    {
        public void operation(OutputStream os) throws IOException
        {
            os.write(0);
        }
    };
    
    /**
     * Overwrite with ones operation
     */
    protected OverwriteOperation overwriteOnes = new OverwriteOperation()
    {
        public void operation(OutputStream os) throws IOException
        {
            os.write(0xff);
        }
    };
    
    /**
     * Overwrite with random operation
     */
    protected OverwriteOperation overwriteRandom = new OverwriteOperation()
    {
        private Random random = new Random();
        
        public void operation(OutputStream os) throws IOException
        {
            byte[] randomByte = new byte[1];
            random.nextBytes(randomByte);
            os.write(randomByte[0]);
        }
    };
}
