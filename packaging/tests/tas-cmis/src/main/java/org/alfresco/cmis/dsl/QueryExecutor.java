package org.alfresco.cmis.dsl;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import static org.alfresco.utility.Utility.checkObjectIsInitialized;
import static org.alfresco.utility.report.log.Step.STEP;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Streams;

import org.alfresco.cmis.CmisWrapper;
import org.alfresco.utility.LogFactory;
import org.alfresco.utility.data.provider.XMLTestData;
import org.alfresco.utility.exception.TestConfigurationException;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.TestModel;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;
import org.slf4j.Logger;
import org.testng.Assert;

/**
 * DSL for CMIS Queries
 * This will also handle execution of CMIS queries
 */
public class QueryExecutor
{
    static Logger LOG = LogFactory.getLogger();

    CmisWrapper cmisWrapper;
    private long returnedResults = -1;
    private String currentQuery = "";
    private ItemIterable<QueryResult> results;

    public QueryExecutor(CmisWrapper cmisWrapper, String query)
    {
        this.cmisWrapper = cmisWrapper;
        currentQuery = query;
    }

    public QueryResultAssertion assertResultsCount()
    {
        returnedResults = executeQuery(currentQuery).getPageNumItems();
        return new QueryResultAssertion();
    }

    public QueryResultAssertion assertColumnIsOrdered()
    {
        results = executeQuery(currentQuery);
        return new QueryResultAssertion();
    }

    public QueryResultAssertion assertColumnValuesRange()
    {
        results = executeQuery(currentQuery);
        return new QueryResultAssertion();
    }

    public QueryResultAssertion assertValues()
    {
        results = executeQuery(currentQuery);
        return new QueryResultAssertion();
    }

    private ItemIterable<QueryResult> executeQuery(String query)
    {
        Session session = cmisWrapper.getSession();
        checkObjectIsInitialized(session, "You need to authenticate first using <cmisWrapper.authenticateUser(UserModel userModel)>");

        return session.query(query, false);
    }

    /**
     * Call getNodeRef on each test data item used in test and replace that with NODE_REF keywords in your Query
     *
     * @param testData
     * @return
     */
    public QueryExecutor applyNodeRefsFrom(XMLTestData testData)
    {
        List<String> dataItems = extractKeywords("NODE_REF");
        if (dataItems.isEmpty())
            return this;

        List<String> nodeRefs = new ArrayList<String>();
        for (String dataItemName : dataItems)
        {
            currentQuery = currentQuery.replace(String.format("NODE_REF[%s]", dataItemName), "%s");
            TestModel model = testData.getTestDataItemWithId(dataItemName).getModel();
            if (model == null)
                throw new TestConfigurationException("No TestData with ID: " + dataItemName + " found in your XML file.");

            if (model instanceof SiteModel)
            {
                nodeRefs.add(cmisWrapper.getDataContentService().usingAdmin().usingSite((SiteModel) model).getNodeRef());
            }
            else if (model instanceof FolderModel)
            {
                nodeRefs.add(((FolderModel) model).getNodeRef());
            }
            else if (model instanceof FileModel)
            {
                nodeRefs.add(((FileModel) model).getNodeRef());
            }
        }

        try
        {
            currentQuery = String.format(currentQuery, nodeRefs.toArray());
            LOG.info("Injecting nodeRef IDs \n\tQuery: [{}]", currentQuery);
        }
        catch (Exception e)
        {
            throw new TestConfigurationException(
                    "You passed multiple keywords to your search query, please re-analyze your query search format: " + e.getMessage());
        }
        return this;
    }

    /**
     * if you have in your search 'SELECT * from cmis:document where workspace://SpacesStore/NODE_REF[site1] or workspace://SpacesStore/NODE_REF[site2]'
     * and pass key="NODE_REF" this method will get "site1" and "site2" as values
     *
     * @param key
     * @return
     * @throws TestConfigurationException
     */
    private List<String> extractKeywords(String key) throws TestConfigurationException
    {
        String[] lines = currentQuery.split(key);
        List<String> keywords = new ArrayList<String>();

        for (int i = 0; i < lines.length; i++)
        {
            if (lines[i].startsWith("["))
            {
                String keyValue = "";
                for (int j = 1; j < lines[i].length() - 1; j++)
                {
                    String tmp = Character.toString(lines[i].charAt(j));
                    if (tmp.equals("]"))
                        break;
                    keyValue += tmp;
                }
                keywords.add(keyValue);
            }
        }
        return keywords;
    }

    public class QueryResultAssertion
    {
        public QueryResultAssertion equals(long expectedValue)
        {
            STEP(String.format("Verify that query: '%s' has %d results count returned", currentQuery, expectedValue));
            Assert.assertEquals(returnedResults, expectedValue, showErrorMessage());
            return this;
        }

        public QueryResultAssertion isGreaterThan(long expectedValue)
        {
            STEP(String.format("Verify that query: '%s' has more than %d results count returned", currentQuery, expectedValue));
            if (expectedValue <= returnedResults)
                Assert.fail(String.format("%s expected to have more than %d results, but found %d", showErrorMessage(), expectedValue, returnedResults));

            return this;
        }

        public QueryResultAssertion isLowerThan(long expectedValue)
        {
            STEP(String.format("Verify that query: '%s' has more than %d results count returned", currentQuery, expectedValue));
            if (returnedResults >= expectedValue)
                Assert.fail(String.format("%s expected to have less than %d results, but found %d", showErrorMessage(), expectedValue, returnedResults));

            return this;
        }

        public QueryResultAssertion isOrderedAsc(String queryName)
        {
            STEP(String.format("Verify that query: '%s' is returning ascending ordered values for column %s", currentQuery, queryName));
            List<Object> columnValues = new ArrayList<>();
            results.forEach((r) -> {
                columnValues.add(r.getPropertyValueByQueryName(queryName));
            });
            List<Object> orderedColumnValues = columnValues.stream().sorted().collect(Collectors.toList());
            Assert.assertEquals(columnValues, orderedColumnValues,
                    String.format("%s column values expected to be in ascendent order, but found %s", queryName, columnValues.toString()));

            return this;

        }

        public QueryResultAssertion isOrderedDesc(String queryName)
        {
            STEP(String.format("Verify that query: '%s' is returning descending ordered values for column %s", currentQuery, queryName));
            List<Object> columnValues = new ArrayList<>();
            results.forEach((r) -> {
                columnValues.add(r.getPropertyValueByQueryName(queryName));
            });
            List<Object> reverseOrderedColumnValues = columnValues.stream().sorted(Collections.reverseOrder()).collect(Collectors.toList());
            Assert.assertEquals(columnValues, reverseOrderedColumnValues,
                    String.format("%s column values expected to be in descendent order, but found %s", queryName, columnValues.toString()));

            return this;

        }

        public QueryResultAssertion isReturningValuesInRange(String queryName, BigDecimal minValue, BigDecimal maxValue)
        {
            STEP(String.format("Verify that query: '%s' is returning values for column %s in range from %.4f to %.4f", currentQuery, queryName, minValue, maxValue));
            results.forEach((r) -> {
                BigDecimal value = (BigDecimal) r.getPropertyValueByQueryName(queryName);
                if (value.compareTo(minValue) < 0 || value.compareTo(maxValue) > 0)
                {
                    Assert.fail(String.format("%s column values expected to be in range from %.4f to %.4f, but found %.4f", queryName, minValue, maxValue, value));
                }
            });

            return this;
        }

        public <T> QueryResultAssertion isReturningValues(String queryName, Set<T> values)
        {
            STEP(String.format("Verify that query: '%s' returns the values from %s for column %s", currentQuery, values, queryName));
            Set<T> resultSet = Streams.stream(results).map(r -> (T) r.getPropertyValueByQueryName(queryName)).collect(toSet());
            Assert.assertEquals(resultSet, values, "Values did not match");

            return this;
        }

        public <T> QueryResultAssertion isReturningOrderedValues(String queryName, List<T> values)
        {
            STEP(String.format("Verify that query: '%s' returns the values from %s for column %s", currentQuery, values, queryName));
            List<T> resultList = Streams.stream(results).map(r -> (T) r.getPropertyValueByQueryName(queryName)).collect(toList());
            // Include both lists in assertion message as TestNG does not provide this information.
            Assert.assertEquals(resultList, values, "Values did not match expected " + values + " but found " + resultList);

            return this;
        }

        private String showErrorMessage()
        {
            return String.format("Returned results count of Query [%s] is not the expected one:", currentQuery);
        }
    }
}
