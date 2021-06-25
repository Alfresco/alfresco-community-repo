package org.alfresco.repo.search.impl.parsers;

import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_TEXT;
import static org.junit.Assert.assertEquals;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AlfrescoFunctionEvaluationContextTest
{

    @Mock
    private NamespacePrefixResolver namespacePrefixResolve;

    @Mock
    private DictionaryService dictionaryService;

    private AlfrescoFunctionEvaluationContext alfrescoFunctionEvaluationContext;

    @Before
    public void setup()
    {
        alfrescoFunctionEvaluationContext = new AlfrescoFunctionEvaluationContext(namespacePrefixResolve, dictionaryService, null);
    }

    @Test
    public void shouldReturnValidFieldNameWhenFieldNameIsLowercase()
    {
        assertEquals(FIELD_TEXT, alfrescoFunctionEvaluationContext.getLuceneFieldName("text"));
        assertEquals("content", alfrescoFunctionEvaluationContext.getLuceneFieldName("content"));
        assertEquals("name", alfrescoFunctionEvaluationContext.getLuceneFieldName("name"));
        assertEquals("description", alfrescoFunctionEvaluationContext.getLuceneFieldName("description"));
        assertEquals("score", alfrescoFunctionEvaluationContext.getLuceneFieldName("score"));
    }

    @Test
    public void shouldReturnValidFieldNameWhenFieldNameIsUpperCase()
    {
        assertEquals(FIELD_TEXT, alfrescoFunctionEvaluationContext.getLuceneFieldName("TEXT"));
        assertEquals("content", alfrescoFunctionEvaluationContext.getLuceneFieldName("CONTENT"));
        assertEquals("name", alfrescoFunctionEvaluationContext.getLuceneFieldName("NAME"));
        assertEquals("description", alfrescoFunctionEvaluationContext.getLuceneFieldName("DESCRIPTION"));
        assertEquals("score", alfrescoFunctionEvaluationContext.getLuceneFieldName("SCORE"));
    }

    @Test
    public void shouldReturnValidFieldNameWhenFieldNameIsMixedCase()
    {
        assertEquals(FIELD_TEXT, alfrescoFunctionEvaluationContext.getLuceneFieldName("tExT"));
        assertEquals("content", alfrescoFunctionEvaluationContext.getLuceneFieldName("cOnTeNt"));
        assertEquals("name", alfrescoFunctionEvaluationContext.getLuceneFieldName("NaMe"));
        assertEquals("description", alfrescoFunctionEvaluationContext.getLuceneFieldName("DeScRiPtIoN"));
        assertEquals("score", alfrescoFunctionEvaluationContext.getLuceneFieldName("sCoRe"));
    }

}