/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.jcr.item;

import java.util.StringTokenizer;

import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;

/**
 * JCR Path Helper
 * 
 * @author David Caruana
 */
public class JCRPath
{
    private Path path;
    
    /**
     * Constuct path from string representation of path
     * 
     * @param strPath
     */
    public JCRPath(NamespacePrefixResolver resolver, String strPath)
    {
        // TODO: replace this simple parse for full path syntax
        boolean root = false;
        int pos = 0;
        path = new Path();
        StringTokenizer tokenizer = new StringTokenizer(strPath, "/", true);
        while (tokenizer.hasMoreTokens())
        {
            String token = tokenizer.nextToken();
            if (pos == 0 && token.equals("/"))
            {
                root = true;
            }
            else if (!token.equals("/"))
            {
                if (root)
                {
                    path.append(new RootSimpleElement(resolver, token));
                    root = false;
                }
                else
                {
                    path.append(new SimpleElement(resolver, token));
                }
            }
            pos++;
        }
    }
    
    /**
     * Get the Path
     * 
     * @return  the underling path
     */
    public Path getPath()
    {
        return path;
    }

    
    @Override
    public String toString()
    {
        return path.toString();
    }    

    /**
     * Simple Path Element used for building JCR Paths
     * 
     * @author David Caruana
     */
    public static class SimpleElement extends Path.Element
    {
        private static final long serialVersionUID = -6510331182652872996L;
        private QName path;
        
        /**
         * @param resolver namespace prefix resolver
         * @param path  path element name
         */
        public SimpleElement(QName path)
        {
            this.path = path;
        }

        /**
         * @param path  path element name
         */
        public SimpleElement(NamespacePrefixResolver resolver, String path)
        {
            this.path = QName.createQName(path, resolver);
        }

        /**
         * Get the QName representation of Path
         */
        public QName getQName()
        {
            return path;
        }
        
        @Override
        public String getElementString()
        {
            return path.toString();
        }

        @Override
        public String getPrefixedString(NamespacePrefixResolver resolver)
        {
            return path.toPrefixString(resolver);
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object o)
        {
            if(o == this)
            {
                return true;
            }
            if(!(o instanceof SimpleElement))
            {
                return false;
            }
            SimpleElement other = (SimpleElement)o;
            return this.path.equals(other.path);
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        public int hashCode()
        {
            return path.hashCode();
        }

    }

    /**
     * Root Path Element
     * 
     * @author David Caruana
     */
    public static class RootSimpleElement extends SimpleElement
    {
        private static final long serialVersionUID = -4827016063963328324L;

        /**
         * Construct
         * 
         * @param path
         */
        public RootSimpleElement(NamespacePrefixResolver resolver, String path)
        {
            super(resolver, path);
        }
        
        @Override
        public String getElementString()
        {
            return "/" + super.getElementString();
        }
        
        @Override
        public String getPrefixedString(NamespacePrefixResolver resolver)
        {
            return "/" + super.getPrefixedString(resolver);
        }
    }
    
}
