/**
 * 
 */
package org.alfresco.repo.search.impl.lucene;

import java.io.IOException;
import java.io.Reader;

class MultiReader extends Reader
{
    Reader first;

    Reader second;

    boolean firstActive = true;

    MultiReader(Reader first, Reader second)
    {
        this.first = first;
        this.second = second;
    }

    @Override
    public void close() throws IOException
    {
        IOException ioe = null;
        try
        {
            first.close();
        }
        catch (IOException e)
        {
            ioe = e;
        }

        second.close();
        if (ioe != null)
        {
            throw ioe;
        }

    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException
    {
        synchronized (lock)
        {
            if ((off < 0) || (off > cbuf.length) || (len < 0) || ((off + len) > cbuf.length) || ((off + len) < 0))
            {
                throw new IndexOutOfBoundsException();
            }
            else if (len == 0)
            {
                return 0;
            }
            for(int i = 0; i < len; i++)
            {
                int c; 
                if(firstActive)
                {
                    c = first.read();
                    if(c == -1)
                    {
                        firstActive = false;
                        c = second.read();
                    }
                }
                else
                {
                    c = second.read();
                }
                if(c == -1)
                {
                    if(i == 0)
                    {
                        return -1; 
                    }
                    else
                    {
                        return i;
                    }
                }
                else
                {
                    cbuf[off+i] = (char)c;
                }
            }
            return len;
        }
    }

}