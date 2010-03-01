/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
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