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
package org.alfresco.jcr.query;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import org.alfresco.jcr.dictionary.JCRNamespace;
import org.alfresco.jcr.session.SessionImpl;
import org.alfresco.service.namespace.QName;


/**
 * Alfresco implementation of JCR Query Manager
 * 
 * @author David Caruana
 */
public class QueryManagerImpl implements QueryManager
{
    public static QName JCRPATH_COLUMN = QName.createQName(JCRNamespace.JCR_URI, "path");
    public static QName JCRSCORE_COLUMN = QName.createQName(JCRNamespace.JCR_URI, "score");
    
    /** supported query languages */    
    private static Map<String, Class<? extends QueryImpl>> supportedLanguages = new HashMap<String, Class<? extends QueryImpl>>();
    static
    {
        supportedLanguages.put(Query.XPATH, XPathQueryImpl.class);
    }

    private SessionImpl session;
    
    /**
     * Construct
     * 
     * @param session  session
     */
    public QueryManagerImpl(SessionImpl session)
    {
        this.session = session;
    }
    
    /* (non-Javadoc)
     * @see javax.jcr.query.QueryManager#createQuery(java.lang.String, java.lang.String)
     */
    public Query createQuery(String statement, String language) throws InvalidQueryException, RepositoryException
    {
        // is the language known?
        if (!isSupportedLanguage(language))
        {
            throw new InvalidQueryException("Query language " + language + " is not supported");
        }

        // construct the query
        Class<? extends QueryImpl> queryClass = supportedLanguages.get(language);
        try
        {
            Constructor<? extends QueryImpl> constructor = queryClass.getConstructor(new Class[] { SessionImpl.class, String.class } );
            QueryImpl queryImpl = constructor.newInstance(new Object[] { session, statement } );
            queryImpl.isValidStatement();
            return queryImpl.getProxy();
        }
        catch (InstantiationException e)
        {
            throw new RepositoryException("Failed to create query " + statement + " (language: " + language + ")");
        }
        catch (IllegalAccessException e)
        {
            throw new RepositoryException("Failed to create query " + statement + " (language: " + language + ")");
        }
        catch (Exception e)
        {
            throw new RepositoryException("Failed to create query " + statement + " (language: " + language + ")");
        }
    }

    /* (non-Javadoc)
     * @see javax.jcr.query.QueryManager#getQuery(javax.jcr.Node)
     */
    public Query getQuery(Node node) throws InvalidQueryException, RepositoryException
    {
        throw new InvalidQueryException("Persistent queries are not supported by the Repository.");
    }

    /* (non-Javadoc)
     * @see javax.jcr.query.QueryManager#getSupportedQueryLanguages()
     */
    public String[] getSupportedQueryLanguages() throws RepositoryException
    {
        return supportedLanguages.keySet().toArray(new String[supportedLanguages.size()]);
    }

    /**
     * Is supported language?
     * 
     * @param language  language to check
     * @return  true => supported
     */
    private boolean isSupportedLanguage(String language)
    {
        return supportedLanguages.containsKey(language);
    }
    
}
