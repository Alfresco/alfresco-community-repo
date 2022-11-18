package org.alfresco.cmis.search;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import static org.alfresco.utility.report.log.Step.STEP;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.alfresco.cmis.CmisProperties;
import org.alfresco.cmis.dsl.QueryExecutor.QueryResultAssertion;
import org.alfresco.utility.Utility;
import org.alfresco.utility.model.ContentModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

@ContextConfiguration("classpath:alfresco-cmis-context.xml")
@Component
@Scope(value = "prototype")
public abstract class AbstractCmisE2ETest extends AbstractE2EFunctionalTest
{
    private static Logger LOGGER = LoggerFactory.getLogger(AbstractCmisE2ETest.class);

    @Autowired
    protected CmisProperties cmisProperties;

    public String documentContent = "CMIS document content";

    @BeforeMethod(alwaysRun = true)
    public void showStartTestInfo(Method method)
    {
        LOGGER.info(String.format("*** STARTING Test: [%s] ***", method.getName()));
    }

    @AfterMethod(alwaysRun = true)
    public void showEndTestInfo(Method method)
    {
        LOGGER.info(String.format("*** ENDING Test: [%s] ***", method.getName()));
    }

    public Integer getElasticWaitTimeInSeconds()
    {
        return cmisProperties.envProperty().getSolrWaitTimeInSeconds();
    }

    /**
     * Repeat Elastic Query till results count returns expectedCountResults
     * @param query CMIS Query to be executed
     * @param expectedResultsCount Number of results expected
     * @return true when results count is equals to expectedCountResults
     */
    protected boolean waitForIndexing(String query, long expectedResultsCount)
    {
        try
        {
            waitForIndexing(query, execution -> execution.hasLength(expectedResultsCount));
            return true;
        }
        catch (AssertionError ae)
        {
            STEP("Received assertion error for query '" + query + "': " + ae);
            return false;
        }
    }

    /**
     * Repeat Elastic Query until we get the expected results or we hit the retry limit.
     *
     * @param query CMIS Query to be executed
     * @param expectedResults The expected results (unordered).
     */
    protected void waitForIndexing(String query, ContentModel... expectedResults)
    {
        Set<String> expectedNames = Arrays.stream(expectedResults).map(ContentModel::getName).collect(toSet());
        waitForIndexing(query, execution -> execution.isReturningValues("cmis:name", expectedNames));
    }

    /**
     * Repeat Elastic Query until we get the expected results in the given order or we hit the retry limit.
     *
     * @param query CMIS Query to be executed
     * @param expectedResults The expected results (ordered).
     */
    protected void waitForIndexingOrdered(String query, ContentModel... expectedResults)
    {
        List<String> expectedNames = Arrays.stream(expectedResults).map(ContentModel::getName).collect(toList());
        waitForIndexing(query, execution -> execution.isReturningOrderedValues("cmis:name", expectedNames));
    }

    /**
     * Repeat Elastic Query until we get the expected results or we hit the retry limit.
     *
     * @param query CMIS Query to be executed
     * @param assertionMethod A method that will be called to check the response and which will throw an AssertionError if they aren't what we want.
     */
    protected void waitForIndexing(String query, Consumer<QueryResultAssertion> assertionMethod)
    {
        int searchCount = 0;
        while (true)
        {
            try
            {
                assertionMethod.accept(cmisApi.withQuery(query).assertValues());
                return;
            }
            catch (AssertionError ae)
            {
                searchCount++;
                if (searchCount < SEARCH_MAX_ATTEMPTS)
                {
                    LOGGER.info(String.format("WaitForIndexing in Progress: %s", ae));
                    Utility.waitToLoopTime(getElasticWaitTimeInSeconds(), "Wait For Indexing");
                }
                else
                {
                    throw ae;
                }
            }
        }
    }
}
