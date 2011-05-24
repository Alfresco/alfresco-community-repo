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
package org.alfresco.opencmis;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;

public class CMISUtils
{
    @SuppressWarnings("unchecked")
    public static <T> T copy(T source)
    {
        T target = null;
        try
        {
            CopyOutputStream cos = new CopyOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(cos);
            out.writeObject(source);
            out.flush();
            out.close();

            ObjectInputStream in = new ObjectInputStream(cos.getInputStream());
            target = (T) in.readObject();
        } catch (Exception e)
        {
            throw new CmisRuntimeException("Object copy failed!", e);
        }

        return target;
    }

    private static class CopyOutputStream extends OutputStream
    {
        protected byte[] buf = null;
        protected int size = 0;

        public CopyOutputStream()
        {
            this(16 * 1024);
        }

        public CopyOutputStream(int initSize)
        {
            this.size = 0;
            this.buf = new byte[initSize];
        }

        private void verifyBufferSize(int sz)
        {
            if (sz > buf.length)
            {
                byte[] old = buf;
                buf = new byte[Math.max(sz, 2 * buf.length)];
                System.arraycopy(old, 0, buf, 0, old.length);
                old = null;
            }
        }

        public final void write(byte b[])
        {
            verifyBufferSize(size + b.length);
            System.arraycopy(b, 0, buf, size, b.length);
            size += b.length;
        }

        public final void write(byte b[], int off, int len)
        {
            verifyBufferSize(size + len);
            System.arraycopy(b, off, buf, size, len);
            size += len;
        }

        public final void write(int b)
        {
            verifyBufferSize(size + 1);
            buf[size++] = (byte) b;
        }

        public InputStream getInputStream()
        {
            return new CopyInputStream(buf, size);
        }
    }

    private static class CopyInputStream extends InputStream
    {
        protected byte[] buf = null;
        protected int count = 0;
        protected int pos = 0;

        public CopyInputStream(byte[] buf, int count)
        {
            this.buf = buf;
            this.count = count;
        }

        public final int available()
        {
            return count - pos;
        }

        public final int read()
        {
            return (pos < count) ? (buf[pos++] & 0xff) : -1;
        }

        public final int read(byte[] b, int off, int len)
        {
            if (pos >= count)
            {
                return -1;
            }

            if ((pos + len) > count)
            {
                len = (count - pos);
            }

            System.arraycopy(buf, pos, b, off, len);
            pos += len;

            return len;
        }

        public final long skip(long n)
        {
            if ((pos + n) > count)
            {
                n = count - pos;
            }

            if (n < 0)
            {
                return 0;
            }

            pos += n;

            return n;
        }
    }
}
