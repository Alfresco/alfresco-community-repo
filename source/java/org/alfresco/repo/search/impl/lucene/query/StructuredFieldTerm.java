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
package org.alfresco.repo.search.impl.lucene.query;

import org.apache.lucene.index.Term;

/**
 * @author andyh
 */
public class StructuredFieldTerm
{

    private Term term;

    private StructuredFieldPosition sfp;

    /**
     * 
     */
    public StructuredFieldTerm(Term term, StructuredFieldPosition sfp)
    {
        this.term = term;
        this.sfp = sfp;
    }

    /**
     * @return Returns the sfp.
     */
    public StructuredFieldPosition getSfp()
    {
        return sfp;
    }

    /**
     * @return Returns the term.
     */
    public Term getTerm()
    {
        return term;
    }
}
