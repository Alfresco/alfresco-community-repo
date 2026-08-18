/*
 * Copyright 2005 - 2026 Alfresco Software Limited.
 *
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of the paid license agreement will prevail.
 * Otherwise, the software is provided under the following open source license terms:
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
package org.alfresco.slingshot.web.scripts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;

/**
 * Unit tests for the "tag" filter query building logic in the slingshot doclist v2 web script library ({@code documentlibrary-v2/filters.lib.js}).
 * <p>
 * The test loads the <em>actual</em> {@code filters.lib.js} resource and executes {@code Filters.getFilterParams("tag", ...)} directly in the embedded Rhino JavaScript engine, injecting lightweight mock collaborators for the {@code args}, {@code search} and {@code logger} root objects. It therefore isolates and asserts the exact query string produced for a tag, with no database, Spring context or search index required.
 * <p>
 * Covers MNT-25799: a tag whose value contains a space (e.g. {@code "long tag"}) must be ISO9075-encoded when locating the tag node, and the resulting query must be a well-formed {@code +=cm:taggable:"<nodeRef>"} membership query rather than a broken multi-word query.
 *
 * @author GitHub Copilot
 */
public class FiltersLibTest
{
    /** Classpath location of the library under test (packaged as a web script resource). */
    private static final String FILTERS_LIB = "alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary-v2/filters.lib.js";

    /** NodeRef that the mocked tag lookup ({@code search.luceneSearch}) resolves to. */
    private static final String TAG_NODEREF = "workspace://SpacesStore/00000000-0000-0000-0000-000000000001";

    /**
     * Mock root objects and capture hooks, evaluated before the library so that the JavaScript globals referenced by {@code getFilterParams} (args, search, logger) are available. {@code __lastLuceneQuery} captures the path query passed to the tag lookup so the test can assert the tag value was correctly ISO9075-encoded.
     */
    private static final String MOCKS = "var __lastLuceneQuery = null;\n"
            + "var __returnEmpty = false;\n"
            + "var search = {\n"
            + "   ISO9075Encode: function(s) { return String(s).replace(/ /g, '_x0020_'); },\n"
            + "   luceneSearch: function(q) {\n"
            + "      __lastLuceneQuery = q;\n"
            + "      return __returnEmpty ? [] : [ { nodeRef: { toString: function() { return '" + TAG_NODEREF + "'; } } } ];\n"
            + "   }\n"
            + "};\n"
            + "var logger = {\n"
            + "   isDebugLoggingEnabled: function() { return false; }, debug: function() {},\n"
            + "   isWarnLoggingEnabled: function() { return false; }, warn: function() {},\n"
            + "   isLoggingEnabled: function() { return false; }\n"
            + "};\n";

    private Context cx;
    private Scriptable scope;

    @Before
    public void setUp() throws Exception
    {
        cx = Context.enter();
        // Interpretive mode - no bytecode generation required for a simple script evaluation
        cx.setOptimizationLevel(-1);
        scope = cx.initStandardObjects();

        cx.evaluateString(scope, MOCKS, "mocks", 1, null);
        cx.evaluateString(scope, readResource(FILTERS_LIB), "filters.lib.js", 1, null);
    }

    @After
    public void tearDown()
    {
        Context.exit();
    }

    /**
     * Baseline: a single-word tag resolves the tag node and produces a well-formed {@code +=cm:taggable:"<nodeRef>"} membership query using the fts-alfresco language.
     */
    @Test
    public void tagQueryForSingleWordTag()
    {
        Scriptable result = runTagFilter("mytag", false);

        assertEquals("fts-alfresco", getString(result, "language"));
        assertEquals("+=cm\\:taggable:\"" + TAG_NODEREF + "\"", getString(result, "query").trim());
        // The tag value is looked up under the category root, ISO9075-encoded
        assertEquals("+PATH:\"/cm:categoryRoot/cm:taggable//cm:mytag\"", getLastLuceneQuery());
    }

    /**
     * MNT-25799: a tag containing a space must be ISO9075-encoded (space -> _x0020_) when the tag node is located, and must still yield a valid membership query.
     */
    @Test
    public void tagQueryForTagWithSpaceIsEncoded()
    {
        Scriptable result = runTagFilter("long tag", false);

        assertEquals("fts-alfresco", getString(result, "language"));
        assertEquals("+=cm\\:taggable:\"" + TAG_NODEREF + "\"", getString(result, "query").trim());
        // The crux of the fix: the space is ISO9075-encoded rather than breaking the query
        assertEquals("+PATH:\"/cm:categoryRoot/cm:taggable//cm:long_x0020_tag\"", getLastLuceneQuery());
    }

    /**
     * The tag value is normalised before lookup: a trailing slash is stripped and the value is lower-cased, so mixed-case / trailing-slash input resolves to the same encoded path.
     */
    @Test
    public void tagValueIsNormalisedBeforeLookup()
    {
        runTagFilter("Long Tag/", false);
        assertEquals("+PATH:\"/cm:categoryRoot/cm:taggable//cm:long_x0020_tag\"", getLastLuceneQuery());
    }

    /**
     * Safety net: when the tag cannot be resolved to a node, the filter must return a null query (i.e. no results) rather than an unbounded query that would return every document.
     */
    @Test
    public void unknownTagReturnsNoResults()
    {
        Scriptable result = runTagFilter("does not exist", true);

        assertNull("Unknown tag must produce a null query (no results)", getRaw(result, "query"));
        assertEquals("fts-alfresco", getString(result, "language"));
        assertEquals(0.0, Context.toNumber(getRaw(result, "limitResults")), 0.0);
    }

    // ---------------------------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------------------------

    /**
     * Invokes {@code Filters.getFilterParams("tag", parsedArgs, {})} with the given tag value and lookup behaviour, returning the resulting parameters object.
     *
     * @param filterData
     *            the raw tag value supplied as the {@code filterData} argument
     * @param returnEmpty
     *            when {@code true}, the mocked tag lookup returns no matching node
     * @return the JavaScript filter parameters object produced by the library
     */
    private Scriptable runTagFilter(String filterData, boolean returnEmpty)
    {
        String setup = "args = { filterData: " + jsString(filterData) + ", sortAsc: null, sortField: null, max: null, days: null };\n"
                + "parsedArgs = { pathNode: { qnamePath: '/app:company_home' }, type: '' };\n"
                + "__returnEmpty = " + returnEmpty + ";\n"
                + "__result = Filters.getFilterParams('tag', parsedArgs, {});\n";
        cx.evaluateString(scope, setup, "tag-filter-invocation", 1, null);

        Object result = scope.get("__result", scope);
        assertNotNull("getFilterParams should return an object", result);
        assertTrue("getFilterParams should return a JavaScript object", result instanceof Scriptable);
        return (Scriptable) result;
    }

    private String getLastLuceneQuery()
    {
        return Context.toString(scope.get("__lastLuceneQuery", scope));
    }

    private static Object getRaw(Scriptable obj, String property)
    {
        Object value = ScriptableObject.getProperty(obj, property);
        if (value == Scriptable.NOT_FOUND || value instanceof Undefined)
        {
            return null;
        }
        return value;
    }

    private static String getString(Scriptable obj, String property)
    {
        Object value = getRaw(obj, property);
        return value == null ? null : Context.toString(value);
    }

    /** Renders a Java string as a safe single-quoted JavaScript string literal. */
    private static String jsString(String value)
    {
        return "'" + value.replace("\\", "\\\\").replace("'", "\\'") + "'";
    }

    private String readResource(String path) throws Exception
    {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path))
        {
            assertNotNull("Could not find '" + path + "' on the test classpath", is);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
