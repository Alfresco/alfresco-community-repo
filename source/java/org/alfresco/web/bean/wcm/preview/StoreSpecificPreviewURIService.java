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

package org.alfresco.web.bean.wcm.preview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.util.Pair;


/**
 * A PreviewURIService that proxies requests to underlying PreviewURIGenerator implementations based on the store that the Preview URI
 * is being generated for. 
 *
 * @author Peter Monks (peter.monks@alfresco.com)
 * 
 * @since 2.2.1
 * 
 * @deprecated see org.alfresco.wcm.preview.*
 */
public class StoreSpecificPreviewURIService implements PreviewURIService
{
    private final List<Pair<Pattern, PreviewURIService>> storeURIGenerators;
    private final PreviewURIService                      fallback;

    
    public StoreSpecificPreviewURIService(final List<Pair<String, PreviewURIService>> storeURIGenerators)
    {
        this(storeURIGenerators, null);
    }
    
    
    public StoreSpecificPreviewURIService(final List<Pair<String, PreviewURIService>> storeURIGenerators,
                                          final PreviewURIService                     fallback)
    {
        List<Pair<Pattern, PreviewURIService>> compiledStoreURIGenerators = null;
        
        // Iterate through the list, compiling all of the regex strings into Pattern objects
        if (storeURIGenerators != null)
        {
            Iterator<Pair<String, PreviewURIService>> iter = storeURIGenerators.iterator();

            compiledStoreURIGenerators = new ArrayList<Pair<Pattern, PreviewURIService>>(storeURIGenerators.size());
        
            while (iter.hasNext())
            {
                Pair<String, PreviewURIService> pair = iter.next();
                
                if (pair != null)
                {
                    String regex = pair.getFirst();
                
                    if (regex != null && regex.trim().length() > 0)
                    {
                        Pattern pattern = Pattern.compile(regex);
                        compiledStoreURIGenerators.add(new Pair<Pattern, PreviewURIService>(pattern, pair.getSecond()));
                    }
                }
            }
        }
        
        // We make the list unmodifiable to ensure correct behaviour in the presence of concurrent (multi-threaded) invocations.
        this.storeURIGenerators = Collections.unmodifiableList(compiledStoreURIGenerators);
        this.fallback           = fallback == null ? new VirtualisationServerPreviewURIService() : fallback;
    }
    

    /**
     * @see org.alfresco.web.bean.wcm.preview.PreviewURIService#getPreviewURI(java.lang.String, java.lang.String)
     */
    public String getPreviewURI(final String storeId, final String pathToAsset)
    {
        String   result      = null;
        boolean resultFound = false;
        
        if (storeURIGenerators != null)
        {
            Iterator<Pair<Pattern, PreviewURIService>> iter = storeURIGenerators.iterator();
            
            while (iter.hasNext())
            {
                Pair<Pattern, PreviewURIService> pair    = iter.next();
                Matcher                            matcher = pair.getFirst().matcher(storeId);
                
                if (matcher.matches())
                {
                    result      = pair.getSecond().getPreviewURI(storeId, pathToAsset);
                    resultFound = true;
                    break;
                }
            }
        }
        
        // We didn't find an impl for the given store, so use the fallback impl
        if (!resultFound)
        {
            result = fallback.getPreviewURI(storeId, pathToAsset);
        }
        
        return(result);
    }
}
