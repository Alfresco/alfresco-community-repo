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

import java.io.IOException;
import java.util.HashMap;

import org.alfresco.repo.search.impl.lucene.query.LeafScorer.Counter;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermPositions;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.search.Weight;

public class PathScorer extends Scorer
{
    Scorer scorer;
  
    PathScorer(Similarity similarity, Scorer scorer)
    {
        super(similarity);
        this.scorer = scorer;
    }
  

    public static PathScorer createPathScorer(Similarity similarity, PathQuery pathQuery, IndexReader reader, Weight weight, DictionaryService dictionarySertvice, boolean repeat) throws IOException
    {
        Scorer selfScorer = null;
        HashMap<String, Counter> selfIds = null;
        
        StructuredFieldPosition last = null;
        if(pathQuery.getQNameStructuredFieldPositions().size() > 0)
        {
           last = pathQuery.getQNameStructuredFieldPositions().get(pathQuery.getQNameStructuredFieldPositions().size() - 1);
        }
        if ((last != null) && last.linkSelf())
        {
            PathQuery selfQuery = new PathQuery(dictionarySertvice);
            selfQuery.setQuery(pathQuery.getPathStructuredFieldPositions(), pathQuery.getQNameStructuredFieldPositions());
            selfQuery.removeDescendantAndSelf();
            if (!selfQuery.isEmpty())
            {
               selfIds = new HashMap<String, Counter>();
               selfScorer = PathScorer.createPathScorer(similarity, selfQuery, reader, weight, dictionarySertvice, repeat);
               selfIds.clear();
               while (selfScorer.next())
               {
                   int doc = selfScorer.doc();
                   Document document = reader.document(doc);
                   Field id = document.getField("ID");
                   Counter counter = selfIds.get(id.stringValue());
                   if (counter == null)
                   {
                       counter = new Counter();
                       selfIds.put(id.stringValue(), counter);
                   }
                   counter.count++;
               }
            }
        }
        
        
        if ((pathQuery.getPathStructuredFieldPositions().size() + pathQuery.getQNameStructuredFieldPositions().size()) == 0) // optimize
            // zero-term
            // case
            return null;

        
        for (StructuredFieldPosition sfp : pathQuery.getPathStructuredFieldPositions())
        {
            if (sfp.getTermText() != null)
            {
                TermPositions p = reader.termPositions(new Term(pathQuery.getPathField(), sfp.getTermText()));
                if (p == null)
                    return null;
                CachingTermPositions ctp = new CachingTermPositions(p);
                sfp.setCachingTermPositions(ctp);
            }
        }

        for (StructuredFieldPosition sfp : pathQuery.getQNameStructuredFieldPositions())
        {
            if (sfp.getTermText() != null)
            {
                TermPositions p = reader.termPositions(new Term(pathQuery.getQnameField(), sfp.getTermText()));
                if (p == null)
                    return null;
                CachingTermPositions ctp = new CachingTermPositions(p);
                sfp.setCachingTermPositions(ctp);
            }
        }

        TermPositions rootContainerPositions = null;
        if (pathQuery.getPathRootTerm() != null)
        {
            rootContainerPositions = reader.termPositions(pathQuery.getPathRootTerm());
        }
        
        TermPositions rootLeafPositions = null;
        if (pathQuery.getQNameRootTerm() != null)
        {
            rootLeafPositions = reader.termPositions(pathQuery.getQNameRootTerm());
        }


        TermPositions tp = reader.termPositions();

        ContainerScorer cs = null;

        TermPositions level0 = null;

        TermPositions nodePositions = reader.termPositions(new Term("ISNODE", "T"));

        // StructuredFieldPosition[] test =
        // (StructuredFieldPosition[])structuredFieldPositions.toArray(new
        // StructuredFieldPosition[]{});
        if (pathQuery.getPathStructuredFieldPositions().size() > 0)
        {
            TermPositions containerPositions = reader.termPositions(new Term("ISCONTAINER", "T"));
            cs = new ContainerScorer(weight, rootContainerPositions, (StructuredFieldPosition[]) pathQuery.getPathStructuredFieldPositions().toArray(new StructuredFieldPosition[] {}),
                    containerPositions, similarity, reader.norms(pathQuery.getPathField()));
        }
        else
        {
            level0 = reader.termPositions(new Term("ISROOT", "T"));
        }

        LeafScorer ls = new LeafScorer(weight, rootLeafPositions, level0, cs, (StructuredFieldPosition[]) pathQuery.getQNameStructuredFieldPositions().toArray(new StructuredFieldPosition[] {}), nodePositions,
                selfIds, reader, similarity, reader.norms(pathQuery.getQnameField()), dictionarySertvice, repeat, tp);

        return new PathScorer(similarity, ls);
    }

    @Override
    public boolean next() throws IOException
    {
        return scorer.next();
    }

    @Override
    public int doc()
    {
        return scorer.doc();
    }

    @Override
    public float score() throws IOException
    {
        return scorer.score();
    }

    @Override
    public boolean skipTo(int position) throws IOException
    {
        return scorer.skipTo(position);
    }

    @Override
    public Explanation explain(int position) throws IOException
    {
        return scorer.explain(position);
    }

}
