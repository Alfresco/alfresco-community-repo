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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.SearcherException;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.QName;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermPositions;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.search.Weight;

public class LeafScorer extends Scorer
{
    static class Counter
    {
        int count = 0;

        public String toString()
        {
            return "count = " + count;
        }
    }

    private int counter;

    private int countInCounter;

    int min = 0;

    int max = 0;

    boolean more = true;

    Scorer containerScorer;

    StructuredFieldPosition[] sfps;

    float freq = 0.0f;

    HashMap<String, Counter> parentIds = new HashMap<String, Counter>();

    HashMap<String, List<String>> categories = new HashMap<String, List<String>>();

    HashMap<String, Counter> selfIds = null;

    boolean hasSelfScorer;

    IndexReader reader;

    private TermPositions allNodes;

    TermPositions level0;

    HashSet<String> selfLinks = new HashSet<String>();

    BitSet selfDocs = new BitSet();

    private TermPositions root;

    private int rootDoc;

    private boolean repeat;

    private DictionaryService dictionaryService;

    private int[] parents;

    private int[] self;

    private int[] cats;

    private TermPositions tp;

    public LeafScorer(Weight weight, TermPositions root, TermPositions level0, ContainerScorer containerScorer,
            StructuredFieldPosition[] sfps, TermPositions allNodes, HashMap<String, Counter> selfIds,
            IndexReader reader, Similarity similarity, byte[] norms, DictionaryService dictionaryService,
            boolean repeat, TermPositions tp)
    {
        super(similarity);
        this.root = root;
        this.containerScorer = containerScorer;
        this.sfps = sfps;
        this.allNodes = allNodes;
        this.tp = tp;
        if (selfIds == null)
        {
            this.selfIds = new HashMap<String, Counter>();
            hasSelfScorer = false;
        }
        else
        {
            this.selfIds = selfIds;
            hasSelfScorer = true;
        }
        this.reader = reader;
        this.level0 = level0;
        this.dictionaryService = dictionaryService;
        this.repeat = repeat;
        try
        {
            initialise();
        }
        catch (IOException e)
        {
            throw new SearcherException(e);
        }

    }

    private void initialise() throws IOException
    {
        if (containerScorer != null)
        {
            parentIds.clear();
            while (containerScorer.next())
            {
                int doc = containerScorer.doc();
                Document document = reader.document(doc);
                Field id = document.getField("ID");
                Counter counter = parentIds.get(id.stringValue());
                if (counter == null)
                {
                    counter = new Counter();
                    parentIds.put(id.stringValue(), counter);
                }
                counter.count++;

                if (!hasSelfScorer)
                {
                    counter = selfIds.get(id.stringValue());
                    if (counter == null)
                    {
                        counter = new Counter();
                        selfIds.put(id.stringValue(), counter);
                    }
                    counter.count++;
                }

                Field isCategory = document.getField("ISCATEGORY");
                if (isCategory != null)
                {
                    Field path = document.getField("PATH");
                    String pathString = path.stringValue();
                    if ((pathString.length() > 0) && (pathString.charAt(0) == '/'))
                    {
                        pathString = pathString.substring(1);
                    }
                    List<String> list = categories.get(id.stringValue());
                    if (list == null)
                    {
                        list = new ArrayList<String>();
                        categories.put(id.stringValue(), list);
                    }
                    list.add(pathString);
                }
            }
        }
        else if (level0 != null)
        {
            parentIds.clear();
            while (level0.next())
            {
                int doc = level0.doc();
                Document document = reader.document(doc);
                Field id = document.getField("ID");
                if (id != null)
                {
                    Counter counter = parentIds.get(id.stringValue());
                    if (counter == null)
                    {
                        counter = new Counter();
                        parentIds.put(id.stringValue(), counter);
                    }
                    counter.count++;

                    counter = selfIds.get(id.stringValue());
                    if (counter == null)
                    {
                        counter = new Counter();
                        selfIds.put(id.stringValue(), counter);
                    }
                    counter.count++;
                }
            }
            if (parentIds.size() != 1)
            {
                throw new SearcherException("More than one root node? " + parentIds.size());
            }
        }

        if (allNodes())
        {
            int position = 0;
            parents = new int[10000];
            for (String parent : parentIds.keySet())
            {
                Counter counter = parentIds.get(parent);
                tp.seek(new Term("PARENT", parent));
                while (tp.next())
                {
                    for (int i = 0, l = tp.freq(); i < l; i++)
                    {
                        for(int j = 0; j < counter.count; j++)
                        {
                           parents[position++] = tp.doc();
                           if (position == parents.length)
                           {
                               int[] old = parents;
                               parents = new int[old.length * 2];
                               System.arraycopy(old, 0, parents, 0, old.length);
                           }
                        }
                       
                    }
                }
                
            }
            int[] old = parents;
            parents = new int[position];
            System.arraycopy(old, 0, parents, 0, position);
            Arrays.sort(parents);

            position = 0;
            self = new int[10000];
            for (String id : selfIds.keySet())
            {
                tp.seek(new Term("ID", id));
                while (tp.next())
                {
                    Counter counter = selfIds.get(id);
                    for(int i = 0; i < counter.count; i++)
                    {
                       self[position++] = tp.doc();
                       if (position == self.length)
                       {
                           old = self;
                           self = new int[old.length * 2];
                           System.arraycopy(old, 0, self, 0, old.length);
                       }
                    }
                }
                
            }
            old = self;
            self = new int[position];
            System.arraycopy(old, 0, self, 0, position);
            Arrays.sort(self);

            position = 0;
            cats = new int[10000];
            for (String catid : categories.keySet())
            {
                for (QName apsectQName : dictionaryService.getAllAspects())
                {
                    AspectDefinition aspDef = dictionaryService.getAspect(apsectQName);
                    if (isCategorised(aspDef))
                    {
                        for (PropertyDefinition propDef : aspDef.getProperties().values())
                        {
                            if (propDef.getDataType().getName().equals(DataTypeDefinition.CATEGORY))
                            {
                                tp.seek(new Term("@" + propDef.getName().toString(), catid));
                                while (tp.next())
                                {
                                    for (int i = 0, l = tp.freq(); i < l; i++)
                                    {
                                        cats[position++] = tp.doc();
                                        if (position == cats.length)
                                        {
                                            old = cats;
                                            cats = new int[old.length * 2];
                                            System.arraycopy(old, 0, cats, 0, old.length);
                                        }
                                    }
                                }
                               
                            }
                        }
                    }
                }

            }
            old = cats;
            cats = new int[position];
            System.arraycopy(old, 0, cats, 0, position);
            Arrays.sort(cats);
        }
    }

    public boolean next() throws IOException
    {

        if (repeat && (countInCounter < counter))
        {
            countInCounter++;
            return true;
        }
        else
        {
            countInCounter = 1;
            counter = 0;
        }

        if (allNodes())
        {
            while (more)
            {
                if (allNodes.next() && root.next())
                {
                    if (check())
                    {
                        return true;
                    }
                }
                else
                {
                    more = false;
                    return false;
                }
            }
        }

        if (!more)
        {
            // One of the search terms has no more docuements
            return false;
        }

        if (max == 0)
        {
            // We need to initialise
            // Just do a next on all terms and check if the first doc matches
            doNextOnAll();
            if (found())
            {
                return true;
            }
        }

        return findNext();
    }

    private boolean allNodes()
    {
        if (sfps.length == 0)
        {
            return true;
        }
        for (StructuredFieldPosition sfp : sfps)
        {
            if (sfp.getCachingTermPositions() != null)
            {
                return false;
            }
        }
        return true;
    }

    private boolean findNext() throws IOException
    {
        // Move to the next document

        while (more)
        {
            move(); // may set more to false
            if (found())
            {
                return true;
            }
        }

        // If we get here we must have no more documents
        return false;
    }

    private void skipToMax() throws IOException
    {
        // Do the terms
        int current;
        for (int i = 0, l = sfps.length; i < l; i++)
        {
            if (i == 0)
            {
                min = max;
            }
            if (sfps[i].getCachingTermPositions() != null)
            {
                if (sfps[i].getCachingTermPositions().doc() < max)
                {
                    if (sfps[i].getCachingTermPositions().skipTo(max))
                    {
                        current = sfps[i].getCachingTermPositions().doc();
                        adjustMinMax(current, false);
                    }
                    else
                    {
                        more = false;
                        return;
                    }
                }
            }
        }

        // Do the root
        if (root.doc() < max)
        {
            if (root.skipTo(max))
            {
                rootDoc = root.doc();
            }
            else
            {
                more = false;
                return;
            }
        }
    }

    private void move() throws IOException
    {
        if (min == max)
        {
            // If we were at a match just do next on all terms
            doNextOnAll();
        }
        else
        {
            // We are in a range - try and skip to the max position on all terms
            skipToMax();
        }
    }

    private void doNextOnAll() throws IOException
    {
        // Do the terms
        int current;
        boolean first = true;
        for (int i = 0, l = sfps.length; i < l; i++)
        {
            if (sfps[i].getCachingTermPositions() != null)
            {
                if (sfps[i].getCachingTermPositions().next())
                {
                    current = sfps[i].getCachingTermPositions().doc();
                    adjustMinMax(current, first);
                    first = false;
                }
                else
                {
                    more = false;
                    return;
                }
            }
        }

        // Do the root term
        if (root.next())
        {
            rootDoc = root.doc();
        }
        else
        {
            more = false;
            return;
        }
        if (root.doc() < max)
        {
            if (root.skipTo(max))
            {
                rootDoc = root.doc();
            }
            else
            {
                more = false;
                return;
            }
        }
    }

    private void adjustMinMax(int doc, boolean setMin)
    {

        if (max < doc)
        {
            max = doc;
        }

        if (setMin)
        {
            min = doc;
        }
        else if (min > doc)
        {
            min = doc;
        }
    }

    private boolean found() throws IOException
    {
        if (sfps.length == 0)
        {
            return true;
        }

        // no more documents - no match
        if (!more)
        {
            return false;
        }

        // min and max must point to the same document
        if (min != max)
        {
            return false;
        }

        if (rootDoc != max)
        {
            return false;
        }

        return check();
    }

    private boolean check() throws IOException
    {
        if (allNodes())
        {
            this.counter = 0;
            int position;

            StructuredFieldPosition last = sfps[sfps.length - 1];

            if (last.linkSelf())
            {
                if ((self != null) && sfps[1].linkSelf() && ((position = Arrays.binarySearch(self, doc())) >= 0))
                {
                    if (!selfDocs.get(doc()))
                    {
                        selfDocs.set(doc());
                        while (position > -1 && self[position] == doc())
                        {
                            position--;
                        }
                        for (int i = position + 1, l = self.length; ((i < l) && (self[i] == doc())); i++)
                        {
                            this.counter++;
                        }
                    }
                }
            }
            if (!selfDocs.get(doc()) && last.linkParent())
            {
                if ((parents != null) && ((position = Arrays.binarySearch(parents, doc())) >= 0))
                {
                    while (position > -1 && parents[position] == doc())
                    {
                        position--;
                    }
                    for (int i = position + 1, l = parents.length; ((i < l) && (parents[i] == doc())); i++)
                    {
                        this.counter++;
                    }
                }

                if ((cats != null) && ((position = Arrays.binarySearch(cats, doc())) >= 0))
                {
                    while (position > -1 && cats[position] == doc())
                    {
                        position--;
                    }
                    for (int i = position + 1, l = cats.length; ((i < l) && (cats[i] == doc())); i++)
                    {
                        this.counter++;
                    }
                }
            }
            return counter > 0;
        }

        // String name = reader.document(doc()).getField("QNAME").stringValue();
        // We have duplicate entries
        // The match must be in a known term range
        int count = root.freq();
        int start = 0;
        int end = -1;
        for (int i = 0; i < count; i++)
        {
            if (i == 0)
            {
                // First starts at zero
                start = 0;
                end = root.nextPosition();
            }
            else
            {
                start = end + 1;
                end = root.nextPosition();
            }

            check(start, end, i);

        }
        // We had checks to do and they all failed.
        return this.counter > 0;
    }

    private void check(int start, int end, int position) throws IOException
    {
        int offset = 0;
        for (int i = 0, l = sfps.length; i < l; i++)
        {
            offset = sfps[i].matches(start, end, offset);
            if (offset == -1)
            {
                return;
            }
        }
        // Last match may fail
        if (offset == -1)
        {
            return;
        }
        else
        {
            if ((sfps[sfps.length - 1].isTerminal()) && (offset != 2))
            {
                return;
            }
        }

        Document doc = reader.document(doc());
        Field[] parentFields = doc.getFields("PARENT");
        Field[] linkFields = doc.getFields("LINKASPECT");

        String parentID = null;
        String linkAspect = null;
        if ((parentFields != null) && (parentFields.length > position) && (parentFields[position] != null))
        {
            parentID = parentFields[position].stringValue();
        }
        if ((linkFields != null) && (linkFields.length > position) && (linkFields[position] != null))
        {
            linkAspect = linkFields[position].stringValue();
        }

        containersIncludeCurrent(doc, parentID, linkAspect);

    }

    private void containersIncludeCurrent(Document document, String parentID, String aspectQName) throws IOException
    {
        if ((containerScorer != null) || (level0 != null))
        {
            if (sfps.length == 0)
            {
                return;
            }
            String id = document.getField("ID").stringValue();
            StructuredFieldPosition last = sfps[sfps.length - 1];
            if ((last.linkSelf() && selfIds.containsKey(id)))
            {
                Counter counter = selfIds.get(id);
                if (counter != null)
                {
                    if (!selfLinks.contains(id))
                    {
                        this.counter += counter.count;
                        selfLinks.add(id);
                        return;
                    }
                }
            }
            if ((parentID != null) && (parentID.length() > 0) && last.linkParent())
            {
                if (!selfLinks.contains(id))
                {
                    if (categories.containsKey(parentID))
                    {
                        Field typeField = document.getField("TYPE");
                        if ((typeField != null) && (typeField.stringValue() != null))
                        {
                            QName typeRef = QName.createQName(typeField.stringValue());
                            if (isCategory(typeRef))
                            {
                                Counter counter = parentIds.get(parentID);
                                if (counter != null)
                                {
                                    this.counter += counter.count;
                                    return;
                                }
                            }
                        }

                        if (aspectQName != null)
                        {
                            QName classRef = QName.createQName(aspectQName);
                            AspectDefinition aspDef = dictionaryService.getAspect(classRef);
                            if (isCategorised(aspDef))
                            {
                                for (PropertyDefinition propDef : aspDef.getProperties().values())
                                {
                                    if (propDef.getDataType().getName().equals(DataTypeDefinition.CATEGORY))
                                    {
                                        // get field and compare to ID
                                        // Check in path as QName
                                        // somewhere
                                        Field[] categoryFields = document.getFields("@" + propDef.getName());
                                        if (categoryFields != null)
                                        {
                                            for (Field categoryField : categoryFields)
                                            {
                                                if ((categoryField != null) && (categoryField.stringValue() != null))
                                                {
                                                    if (categoryField.stringValue().endsWith(parentID))
                                                    {
                                                        int count = 0;
                                                        List<String> paths = categories.get(parentID);
                                                        if (paths != null)
                                                        {
                                                            for (String path : paths)
                                                            {
                                                                if (path.indexOf(aspectQName) != -1)
                                                                {
                                                                    count++;
                                                                }
                                                            }
                                                        }
                                                        this.counter += count;
                                                        return;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }
                    else
                    {
                        Counter counter = parentIds.get(parentID);
                        if (counter != null)
                        {
                            this.counter += counter.count;
                            return;
                        }
                    }

                }
            }

            return;
        }
        else
        {
            return;
        }
    }

    private boolean isCategory(QName classRef)
    {
        if (classRef == null)
        {
            return false;
        }
        TypeDefinition current = dictionaryService.getType(classRef);
        while (current != null)
        {
            if (current.getName().equals(ContentModel.TYPE_CATEGORY))
            {
                return true;
            }
            else
            {
                QName parentName = current.getParentName();
                if (parentName == null)
                {
                    break;
                }
                current = dictionaryService.getType(parentName);
            }
        }
        return false;
    }

    private boolean isCategorised(AspectDefinition aspDef)
    {
        AspectDefinition current = aspDef;
        while (current != null)
        {
            if (current.getName().equals(ContentModel.ASPECT_CLASSIFIABLE))
            {
                return true;
            }
            else
            {
                QName parentName = current.getParentName();
                if (parentName == null)
                {
                    break;
                }
                current = dictionaryService.getAspect(parentName);
            }
        }
        return false;
    }

    public int doc()
    {
        if (allNodes())
        {
            return allNodes.doc();
        }
        return max;
    }

    public float score() throws IOException
    {
        return repeat ? 1.0f : counter;
    }

    public boolean skipTo(int target) throws IOException
    {

        countInCounter = 1;
        counter = 0;

        if (allNodes())
        {
            allNodes.skipTo(target);
            root.skipTo(allNodes.doc()); // must match
            if (check())
            {
                return true;
            }
            while (more)
            {
                if (allNodes.next() && root.next())
                {
                    if (check())
                    {
                        return true;
                    }
                }
                else
                {
                    more = false;
                    return false;
                }
            }
        }

        max = target;
        return findNext();
    }

    public Explanation explain(int doc) throws IOException
    {
        Explanation tfExplanation = new Explanation();

        while (next() && doc() < doc)
        {
        }

        float phraseFreq = (doc() == doc) ? freq : 0.0f;
        tfExplanation.setValue(getSimilarity().tf(phraseFreq));
        tfExplanation.setDescription("tf(phraseFreq=" + phraseFreq + ")");

        return tfExplanation;
    }

}
