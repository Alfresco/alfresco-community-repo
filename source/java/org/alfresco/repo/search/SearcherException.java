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
package org.alfresco.repo.search;

/**
 * Searcher related exceptions
 * 
 * @author andyh
 * 
 */
public class SearcherException extends RuntimeException
{

    /**
     * 
     */
    private static final long serialVersionUID = 3905522713513899318L;

    public SearcherException()
    {
        super();
    }

    public SearcherException(String message)
    {
        super(message);
    }

    public SearcherException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public SearcherException(Throwable cause)
    {
        super(cause);
    }

}
