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
package org.alfresco.scripts;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * @author Kevin Roast
 */
public class ScriptResourceHelper
{
    private static final String SCRIPT_ROOT = "_root";
    private static final String IMPORT_PREFIX = "<import";
    private static final String IMPORT_RESOURCE = "resource=\"";
    
    /**
     * Resolve the import directives in the specified script. The implementation of the supplied
     * ScriptResourceLoader instance is responsible for handling the resource retrieval. 
     * <p>
     * Multiple includes of the same resource are dealt with correctly and nested includes of scripts
     * is fully supported.
     * <p>
     * Note that for performance reasons the script import directive syntax and placement in the file
     * is very strict. The import lines <i>must</i> always be first in the file - even before any comments.
     * Immediately that the script service detects a non-import line it will assume the rest of the
     * file is executable script and no longer attempt to search for any further import directives. Therefore
     * all imports should be at the top of the script, one following the other, in the correct syntax and
     * with no comments present - the only separators valid between import directives is white space.
     * 
     * @param script        The script content to resolve imports in
     * 
     * @return a valid script with all nested includes resolved into a single script instance 
     */
    public static String resolveScriptImports(String script, ScriptResourceLoader loader, Log logger)
    {
        // use a linked hashmap to preserve order of includes - the key in the collection is used
        // to resolve multiple includes of the same scripts and therefore cyclic includes also
        Map<String, String> scriptlets = new LinkedHashMap<String, String>(8, 1.0f);
        
        // perform a recursive resolve of all script imports
        recurseScriptImports(SCRIPT_ROOT, script, loader, scriptlets, logger);
        
        if (scriptlets.size() == 1)
        {
            // quick exit for single script with no includes
            if (logger.isTraceEnabled())
                logger.trace("Script content resolved to:\r\n" + script);
            
            return script;
        }
        else
        {
            // calculate total size of buffer required for the script and all includes
            int length = 0;
            for (String scriptlet : scriptlets.values())
            {
                length += scriptlet.length();
            }
            // append the scripts together to make a single script
            StringBuilder result = new StringBuilder(length);
            for (String scriptlet : scriptlets.values())
            {
                result.append(scriptlet);
            }
            
            if (logger.isTraceEnabled())
                logger.trace("Script content resolved to:\r\n" + result.toString());
            
            return result.toString();
        }
    }
    
    /**
     * Recursively resolve imports in the specified scripts, adding the imports to the
     * specific list of scriplets to combine later.
     * 
     * @param location      Script location - used to ensure duplicates are not added
     * @param script        The script to recursively resolve imports for
     * @param scripts       The collection of scriplets to execute with imports resolved and removed
     */
    private static void recurseScriptImports(
          String location, String script, ScriptResourceLoader loader, Map<String, String> scripts, Log logger)
    {
        int index = 0;
        // skip any initial whitespace
        for (; index<script.length(); index++)
        {
            if (Character.isWhitespace(script.charAt(index)) == false)
            {
                break;
            }
        }
        // look for the "<import" directive marker
        if (script.startsWith(IMPORT_PREFIX, index))
        {
            // skip whitespace between "<import" and "resource"
            boolean afterWhitespace = false;
            index += IMPORT_PREFIX.length() + 1;
            for (; index<script.length(); index++)
            {
                if (Character.isWhitespace(script.charAt(index)) == false)
                {
                    afterWhitespace = true;
                    break;
                }
            }
            if (afterWhitespace == true && script.startsWith(IMPORT_RESOURCE, index))
            {
                // found an import line!
                index += IMPORT_RESOURCE.length();
                int resourceStart = index;
                for (; index<script.length(); index++)
                {
                    if (script.charAt(index) == '"' && script.charAt(index + 1) == '>')
                    {
                        // found end of import line - so we have a resource path
                        String resource = script.substring(resourceStart, index);
                        
                        if (logger.isDebugEnabled())
                            logger.debug("Found script resource import: " + resource);
                        
                        if (scripts.containsKey(resource) == false)
                        {
                            // load the script resource (and parse any recursive includes...)
                            String includedScript = loader.loadScriptResource(resource);
                            if (includedScript != null)
                            {
                                if (logger.isDebugEnabled())
                                    logger.debug("Succesfully located script '" + resource + "'");
                                recurseScriptImports(resource, includedScript, loader, scripts, logger);
                            }
                        }
                        else
                        {
                            if (logger.isDebugEnabled())
                                logger.debug("Note: already imported resource: " + resource);
                        }
                        
                        // continue scanning this script for additional includes
                        // skip the last two characters of the import directive
                        for (index += 2; index<script.length(); index++)
                        {
                            if (Character.isWhitespace(script.charAt(index)) == false)
                            {
                                break;
                            }
                        }
                        recurseScriptImports(location, script.substring(index), loader, scripts, logger);
                        return;
                    }
                }
                // if we get here, we failed to find the end of an import line
                throw new ScriptException(
                        "Malformed 'import' line - must be first in file, no comments and strictly of the form:" +
                        "\r\n<import resource=\"...\">");
            }
            else
            {
                throw new ScriptException(
                        "Malformed 'import' line - must be first in file, no comments and strictly of the form:" +
                        "\r\n<import resource=\"...\">");
            }
        }
        else
        {
            // no (further) includes found - include the original script content
            if (logger.isDebugEnabled())
                logger.debug("Imports resolved, adding resource '" + location);
            if (logger.isTraceEnabled())
                logger.trace(script);
            scripts.put(location, script);
        }
    }
}
