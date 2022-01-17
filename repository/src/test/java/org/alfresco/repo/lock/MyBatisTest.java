/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.lock;

//import static org.apache.ibatis.reflection.ParamNameResolver.wrapToMapIfCollection;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputFilter.Config;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import junit.framework.TestCase;
import org.alfresco.ibatis.HackableConfiguration;
import org.alfresco.ibatis.HackableConfiguration.RemoveFactory;
import org.alfresco.ibatis.IdsEntity;
import org.alfresco.repo.calendar.CalendarModel;
import org.alfresco.repo.calendar.cannedqueries.CalendarEntity;
import org.alfresco.repo.calendar.cannedqueries.GetCalendarEntriesCannedQueryParams;
import org.alfresco.repo.domain.activities.ActivityFeedEntity;
import org.alfresco.repo.domain.activities.ActivityFeedQueryEntity;
import org.alfresco.repo.domain.activities.FeedControlEntity;
import org.alfresco.repo.domain.contentdata.EncryptedKey;
import org.alfresco.repo.domain.node.ChildAssocEntity;
import org.alfresco.repo.domain.node.ChildPropertyEntity;
import org.alfresco.repo.domain.node.NodePropertyValue;
import org.alfresco.repo.domain.node.NodeRangeEntity;
import org.alfresco.repo.domain.node.TransactionQueryEntity;
import org.alfresco.repo.domain.node.ibatis.NodeBatchLoadEntity;
import org.alfresco.repo.domain.propval.PropertyClassEntity;
import org.alfresco.repo.domain.propval.PropertyDoubleValueEntity;
import org.alfresco.repo.domain.propval.PropertyStringQueryEntity;
import org.alfresco.repo.domain.qname.NamespaceEntity;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.download.cannedquery.GetDownloadsCannedQueryParams;
import org.alfresco.repo.node.archive.ArchivedNodeEntity;
import org.alfresco.repo.node.archive.GetArchivedNodesCannedQueryParams;
import org.alfresco.repo.search.impl.querymodel.Argument;
import org.alfresco.repo.search.impl.querymodel.Column;
import org.alfresco.repo.search.impl.querymodel.Function;
import org.alfresco.repo.search.impl.querymodel.Query;
import org.alfresco.repo.search.impl.querymodel.Selector;
import org.alfresco.repo.search.impl.querymodel.impl.db.DBQuery;
import org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryModelFactory;
import org.alfresco.repo.search.impl.querymodel.impl.functions.PropertyAccessor;
import org.alfresco.repo.security.person.FilterSortPersonEntity;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.ReflectionHelper;
import org.alfresco.util.testing.category.DBTests;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.SimpleExecutor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.resultset.DefaultResultSetHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMap;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.defaults.DefaultSqlSession;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.TransactionFactory;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ReflectionUtils;

@Category ({ OwnJVMTestsCategory.class, DBTests.class })
public class MyBatisTest extends TestCase
{
    private SqlSessionFactory sessionFactory;
    private Configuration myBatisConfiguration;
    //private TestExecutor columnExtractingExecutor;
    private QNameDAO qNameDAO;

    @Override
    public void setUp() throws Exception
    {
        final ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
        sessionFactory = ctx.getBean("repoSqlSessionFactory", SqlSessionFactory.class);
        assertNotNull(sessionFactory);
        myBatisConfiguration = sessionFactory.getConfiguration();
        assertNotNull(myBatisConfiguration);
        assertTrue(myBatisConfiguration instanceof HackableConfiguration);
//        Environment env = myBatisConfiguration.getEnvironment();
//        TransactionFactory txFactory = env.getTransactionFactory();
//        Transaction tx = txFactory.newTransaction(env.getDataSource(), null, false);
//        columnExtractingExecutor = new TestExecutor(myBatisConfiguration, tx);
        qNameDAO = (QNameDAO) ctx.getBean("qnameDAOImpl");
        assertNotNull(qNameDAO);
    }

    public static Set<String> ALL_TYPES = new HashSet<>();

//    static class TestExecutor extends SimpleExecutor
//    {
//        public TestExecutor(Configuration configuration, Transaction transaction)
//        {
//            super(configuration, transaction);
//        }
//
//        public Collection<String> getColumnNames(MappedStatement ms)
//        {
//            return getColumnNames(ms, null);
//        }
//
//        public Set<String> getColumnNames(MappedStatement ms, Object parameter)
//        {
//            Object param = wrapCollection(parameter);
//            //Object param = wrapToMapIfCollection(parameter, null);
//            BoundSql boundSql = ms.getBoundSql(param);
//            PreparedStatement stmt = null;
//            try
//            {
//                Configuration configuration = ms.getConfiguration();
//                StatementHandler handler = configuration.newStatementHandler(wrapper, ms, param, RowBounds.DEFAULT, NO_RESULT_HANDLER, boundSql);
//                stmt = prepareStmt(handler, ms.getStatementLog());
//                stmt.execute();
//                try (ResultSet rs = stmt.getResultSet())
//                {
//                    ResultSetMetaData meta = rs.getMetaData();
//                    HashSet<String> result = new HashSet<>();
//                    for (int i = 1; i <= meta.getColumnCount(); i++)
//                    {
//                        ALL_TYPES.add(meta.getColumnClassName(i));
//                        result.add(meta.getColumnName(i).toUpperCase(Locale.ENGLISH) + ": " +  meta.getColumnClassName(i));
//                    }
//                    return result;
//                }
//            } catch (SQLException e)
//            {
////                System.err.println();
////                System.err.println(stmt);
//                return Set.of();
//            } finally
//            {
//                closeStatement(stmt);
//            }
//        }
//
//        private Object wrapCollection(Object object) {
//            DefaultSqlSession.StrictMap map;
//            if (object instanceof Collection) {
//                map = new DefaultSqlSession.StrictMap();
//                map.put("collection", object);
//                if (object instanceof List) {
//                    map.put("list", object);
//                }
//
//                return map;
//            } else if (object != null && object.getClass().isArray()) {
//                map = new DefaultSqlSession.StrictMap();
//                map.put("array", object);
//                return map;
//            } else {
//                return object;
//            }
//        }
//
//        private PreparedStatement prepareStmt(StatementHandler handler, Log statementLog) throws SQLException
//        {
//            Statement stmt;
//            Connection connection = getConnection(statementLog);
//            //stmt = handler.prepare(connection, transaction.getTimeout());
//            stmt = handler.prepare(connection);
//            handler.parameterize(stmt);
//            return (PreparedStatement) stmt;
//        }
//    }

    private final Set<String> columnNames = new HashSet<>();

    private ResultSetHandler newResultSetHandler(Executor executor, MappedStatement mappedStatement, ParameterHandler parameterHandler, ResultHandler<?> resultHandler, BoundSql boundSql, RowBounds rowBounds)
    {
        return new DefaultResultSetHandler(executor, mappedStatement, parameterHandler, resultHandler, boundSql, rowBounds){
            @Override
            public List<Object> handleResultSets(Statement stmt) throws SQLException
            {
                ResultSet rs = stmt.getResultSet();
                assertNotNull(rs);

                ResultSetMetaData md = rs.getMetaData();
                columnNames.clear();
                Set<String> columns = new HashSet<>(md.getColumnCount());
                for (int i = 1; i <= md.getColumnCount(); i++)
                {
                    columns.add(md.getColumnName(i).toLowerCase(Locale.ROOT));
                }
                columnNames.addAll(columns);

                return super.handleResultSets(wrapStatement(stmt, columns));
            }

            private Statement wrapStatement(final Statement stm, final Set<String> columns)
            {
                return (Statement) Proxy.newProxyInstance(Statement.class.getClassLoader(), new Class[] {Statement.class}, new InvocationHandler()
                {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
                    {
                        if ("getResultSet".equals(method.getName()))
                        {
                            ResultSet rs = (ResultSet) method.invoke(stm, args);
                            return wrapResultSet(rs, columns);
                        }
                        return method.invoke(stm, args);
                    }
                });
            }

            private ResultSet wrapResultSet(ResultSet rs, Set<String> columns)
            {
                return (ResultSet) Proxy.newProxyInstance(ResultSet.class.getClassLoader(), new Class[] {ResultSet.class}, new InvocationHandler()
                {
                    private boolean hasNext = true;
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
                    {
                        if ("next".equals(method.getName()))
                        {
                            return handleNext();
                        }
                        if (args != null && args.length == 1 && args[0] instanceof String && columns.contains(((String)args[0]).toLowerCase(Locale.ROOT)) && method.getName().startsWith("get"))
                        {
                            return getDefaultValue(method.getName());
                        }
                        //System.err.println("!!!!@@@@@@@@@########## " + method.getName() + "(" + (args == null ? "" : Arrays.toString(args)) + ") -> " + columns);
                        return method.invoke(rs, args);
                    }

                    private boolean handleNext()
                    {
                        if (hasNext)
                        {
                            hasNext = false;
                            return true;
                        }
                        return false;
                    }
                });
            }

            private Object getDefaultValue(String methodName)
            {
                switch (methodName)
                {
                    case "getShort":
                        return (short) 1;
                    case "getLong":
                        return 2L;
                    case "getString":
                        return "three";
                    case "getTimestamp":
                        return Timestamp.valueOf(LocalDateTime.of(2022, 4, 4, 4, 4));
                    case "getInt":
                        return 5;
                    case "getBoolean":
                        return true;
                    case "getDouble":
                        return 7d;
                    case "getBinaryStream":
                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                        try(ObjectOutputStream oos = new ObjectOutputStream(bytes))
                        {
                            oos.writeObject("888");
                        } catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                        return new ByteArrayInputStream(bytes.toByteArray());
                    case "getBytes":
                        return new byte[] {9, 9, 9};
                    case "getFloat":
                        return Float.valueOf(10);
                    default:
                        throw new UnsupportedOperationException("Unsupported type " + methodName);
                }
            }
        };
    }

    @Test
    public void testAllSelectStatements() throws IOException, URISyntaxException
    {
        long start = System.currentTimeMillis();
        final String myBatisVersion;
        try (InputStream mybatisPomProps = Configuration.class.getResourceAsStream("/META-INF/maven/org.mybatis/mybatis/pom.properties"))
        {
            Properties p = new Properties();
            p.load(mybatisPomProps);
            myBatisVersion = (String) p.get("version");
        }
//        Properties props = new Properties();
//        props.load();
        try (RemoveFactory remove = HackableConfiguration.useFactory(this::newResultSetHandler))
        {
//            SqlSession s = sessionFactory.openSession();
//            ActivityFeedQueryEntity q = new ActivityFeedQueryEntity();
//            q.setFeedUserId("user");
//            List<ActivityFeedEntity> feed = s.selectList("alfresco.activities.select.select_activity_feed_for_feeduser", q);
//

            System.out.println("-------------------------------------------");
            final StringBuilder report = new StringBuilder(20 * 1024 * 1024);
            for (String statementId : allStatementsSorted())
            {
                //if (!statementId.contains("DynamicQuery")) continue;
                System.out.println(statementId);
                report.append(statementId);
                Optional<MappedStatement> possibleStatement = getMappedStatement(statementId);
                if (possibleStatement.isEmpty())
                {
                    report.append(" - SKIPPING - no mapped statement.").append(System.lineSeparator());
                    continue;
                }
                final MappedStatement ms = possibleStatement.get();
                if (!SqlCommandType.SELECT.equals(ms.getSqlCommandType()))
                {
                    report.append(" - SKIPPING - no SELECT statement (`" + ms.getSqlCommandType() + "`).").append(System.lineSeparator());
                    continue;
                }
                assertEquals(ms.getResultMaps().size(), 1);
                final ParameterMap param = ms.getParameterMap();
                final ResultMap result = ms.getResultMaps().get(0);
                assertNotNull(result);
                final Class<?> resultType = result.getType();
                assertNotNull(resultType);

                report.append("    IN: `" + param.getType() + "`    OUT: `" + resultType + "`");
//                if (isScalarType(resultType) && result.getMappedColumns().isEmpty())
//                {
//                    report.append(" - SKIPPING - scalar result (`" + resultType + "`) without mapped columns").append(System.lineSeparator());
//                    continue;
//                }

//                Collection<String> columnNames = getPossibleColumnsSorted(statementId);
//                if (columnNames.isEmpty())
//                {
//                    report.append(" - !!!!!!!!!!!!!!!! no columns.").append(System.lineSeparator());
//                    continue;
//                }
//                report.append(System.lineSeparator());
//                columnNames.stream().forEach(c -> report.append("  ").append(c).append(System.lineSeparator()));

                Set<Map> results = getAllPossibleResults(statementId);
                assertNotNull(results);
                assertFalse(results.isEmpty());
                Map combined = combineAll(results);
                appendParams(report, combined, 1);
                report.append(System.lineSeparator());
                //report.append(System.lineSeparator()).append("  Results: " + combined).append(System.lineSeparator());
            }
            System.out.println("++++++++++++++++++++++++++++++++++++");
            System.out.println(report);
            long stop = System.currentTimeMillis();
            Files.writeString(Path.of(System.getProperty("user.home"), "mybatis-" + myBatisVersion + "-" + System.currentTimeMillis() + "-" + (stop - start) + ".txt"), report, StandardOpenOption.CREATE);
            System.out.println("++++++++++++++++++++++++++++++++++++");
            System.out.println(ALL_TYPES);
            System.out.println("++++++++++++++++++++++++++++++++++++");
        }
    }

    private void appendParams(StringBuilder sb, Map m, int level)
    {
        String prefix = " ".repeat(level * 2);
        ArrayList sortedKeys = new ArrayList<>(m.keySet());
        Collections.sort(sortedKeys);
        sortedKeys.forEach(k -> {
            Object v = m.get(k);
            sb.append(System.lineSeparator()).append(prefix).append(k).append(":");
            if (v instanceof Map)
            {
                appendParams(sb, (Map) v, level + 1);
            }
            else
            {
                sb.append(v);
            }
        });
    }

    private Map combineAll(Set<Map> results)
    {
        Iterator<Map> i = results.iterator();
        if (results.size() == 1) return i.next();

        HashMap result = new HashMap<>(i.next());
        while (i.hasNext())
        {
            combine(result, i.next());
        }
        return result;
    }

    private void combine(Map map, Map toAdd)
    {
        toAdd.forEach((k, v) -> {
            Object existing = map.get(k);
            if (existing == null)
            {
                map.put(k, v);
            } else if (v != null)
            {
                if (existing instanceof Map)
                {
                    if (v instanceof Map)
                    {
                        combine((Map) existing, (Map) v);
                    } else if (v != null)
                    {
                        throw new IllegalArgumentException("Don't know how to combine key `" + k + "` of type `" + existing.getClass() + "` with type `" + v.getClass() + "`.");
                    }
                }
                if (!existing.equals(v))
                {
                    throw new IllegalArgumentException("Don't know how to combine key `" + k + "` of type `" + existing.getClass() + "` with value `" + v + "`.");
                }
            }
        });
    }

    private Collection<String> allStatementsSorted()
    {
        return myBatisConfiguration.getMappedStatementNames().stream().sorted().collect(Collectors.toUnmodifiableList());
    }

    private Optional<MappedStatement> getMappedStatement(String statementId)
    {
        if (!myBatisConfiguration.hasStatement(statementId)) return Optional.empty();
        try
        {
            MappedStatement ms = myBatisConfiguration.getMappedStatement(statementId);
            return Optional.of(ms);
        } catch (IllegalArgumentException e)
        {
            if (e.getMessage().contains(" ambiguous "))
            {
                return Optional.empty();
            }
            throw e;
        }
    }

    private boolean isScalarType(Class<?> resultType)
    {
        return resultType.isPrimitive() || Number.class.isAssignableFrom(resultType) || String.class == resultType;
    }

    private Set<Map> queryForAllPossibleValues(String statementId, Collection params)
    {
        Set result = new HashSet(params.size());
        for (Object p : params)
        {
            Object o = query(statementId, p);
            if (o != null) result.add(getAllProps(o));
        }
        return result;
    }

    private Set<Map> queryForAllPossibleValues(String statementId, Object param)
    {
        Object o = query(statementId, param);
        if (o == null) return Set.of();
        return Set.of(getAllProps(o));
    }

    private Object query(String statementId, Object param)
    {
        columnNames.clear();
        try(SqlSession sqlSession = sessionFactory.openSession(true);)
        {
            sqlSession.clearCache();
            List<Object> res = sqlSession.selectList(statementId, param);
            assertTrue(res.size() == 1);
            Object record = res.get(0);
            if (record == null)
            {
                columnNames.clear();
            }
            return record;
        } catch (Exception e)
        {
            for (Throwable t = e; t != null; t = t.getCause())
            {
                if (t instanceof SQLException)
                {
                    columnNames.clear();
                    return null;
                }
            }
            throw new RuntimeException(e);
        }
    }


    private Map getAllProps(Object o)
    {
        if (o instanceof Map) return (Map) o;
        Map<String, Object> result = new HashMap<>();

        ReflectionUtils.doWithFields(o.getClass(), f -> {
            ReflectionUtils.makeAccessible(f);
            Object v = ReflectionUtils.getField(f, o);
            if (v != null) result.put(f.getName(), getPropsValue(v));
        }, f -> (f.getModifiers() & Modifier.STATIC) == 0);

        return result;
    }

    private Object getPropsValue(Object v)
    {
        if (v == null) return null;
        Class<? extends Object> type = v.getClass();
        if (type.isPrimitive() || v instanceof Number || v instanceof String || v instanceof Date || v instanceof Boolean) return v;
        if (v instanceof Collection)
        {
            return ((Collection)v).stream().map(this::getPropsValue).collect(Collectors.toCollection(() -> {
                try
                {
                    return (Collection) v.getClass().getConstructor().newInstance();
                } catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }));
        }
        if (v instanceof Map)
        {
            Map newMap;
            try
            {
                newMap = (Map) v.getClass().getConstructor().newInstance();
            }catch (Exception e)
            {
                throw new RuntimeException(e);
            }
            ((Map)v).forEach((mapKey, mapValue) -> newMap.put(getPropsValue(mapKey), getPropsValue(mapValue)));
            return newMap;
        }
        if (type.isArray()) {
            if (v instanceof byte[]) return Arrays.toString((byte[]) v);
            throw new UnsupportedOperationException("Unsupported array type `" + v.getClass() + "`.");
        }
        return getAllProps(v);
    }

    private Set<Map> getAllPossibleResults(String statementId)
    {
        final MappedStatement ms = myBatisConfiguration.getMappedStatement(statementId);
        if ("alfresco.metadata.query.select_byDynamicQuery".equals(statementId) || "select_byDynamicQuery".equals(statementId))
        {
            return queryForAllPossibleValues(statementId, Map.of("joins", List.of(),"limit", 0));
        }

        Set<String> collectedColumnNames = new HashSet<>();
        ParameterMap param = ms.getParameterMap();
        Class<?> paramType = param.getType();
        if (paramType == null)
        {
            Optional<Object> knownParameter = getKnownParameter(statementId);
            if (knownParameter.isPresent())
                return queryForAllPossibleValues(statementId, knownParameter.get());
            else
                return queryForAllPossibleValues(statementId, (Object) null);
        }
        Optional<List<Object>> knownParameters = getKnownParameters(statementId);
        List<Object> paramsToCheck = knownParameters
                .or(() -> getPrimitiveParameters(param))
                .or(() -> getMapParameters(param))
                .orElseGet(() -> getBeanParameters(statementId, param));
        //assertFalse(paramsToCheck.isEmpty());

        if (paramsToCheck.size() > 1024)
        {
            ArrayList<Object> toShuffle = new ArrayList<>(paramsToCheck);
            Collections.shuffle(toShuffle);
            paramsToCheck = toShuffle.subList(0, 1024);
        }

        return queryForAllPossibleValues(statementId, paramsToCheck);
    }

    private Collection<String> getPossibleColumnsSorted(String statementId)
    {
        final MappedStatement ms = myBatisConfiguration.getMappedStatement(statementId);
        if ("alfresco.metadata.query.select_byDynamicQuery".equals(statementId) || "select_byDynamicQuery".equals(statementId))
        {
            query(statementId, Map.of("joins", List.of(),"limit", 0));
            return columnNames.stream()
                       .sorted()
                       .collect(Collectors.toUnmodifiableList());
        }

        Set<String> collectedColumnNames = new HashSet<>();
        ParameterMap param = ms.getParameterMap();
        Class<?> paramType = param.getType();
        if (paramType == null)
        {
            Optional<Object> knownParameter = getKnownParameter(statementId);
            if (knownParameter.isPresent())
                query(statementId, knownParameter.get());
            else
                query(statementId,null);
            return columnNames.stream()
                              .sorted()
                              .collect(Collectors.toUnmodifiableList());
        }
        Optional<List<Object>> knownParameters = getKnownParameters(statementId);
        List<Object> paramsToCheck = knownParameters
                .or(() -> getPrimitiveParameters(param))
                .or(() -> getMapParameters(param))
                .orElseGet(() -> getBeanParameters(statementId, param));
        //assertFalse(paramsToCheck.isEmpty());

        if (paramsToCheck.size() > 128)
        {
            ArrayList<Object> toShuffle = new ArrayList<>(paramsToCheck);
            Collections.shuffle(toShuffle);
            paramsToCheck = toShuffle.subList(0, 128);
        }

        for (Object p : paramsToCheck)
        {
            query(statementId, p);
            collectedColumnNames.addAll(columnNames);
        }

        return collectedColumnNames.stream().sorted().collect(Collectors.toUnmodifiableList());
    }

    private Optional<? extends List<Object>> getPrimitiveParameters(ParameterMap param)
    {
        Class<?> type = param.getType();
        if (!isScalarType(type)) return Optional.empty();
        return Optional.of(getPossibleValues(type));
    }

    private Optional<List<Object>> getMapParameters(ParameterMap param)
    {
        if (Map.class != param.getType()) return Optional.empty();
        if (!myBatisConfiguration.hasParameterMap(param.getId())) return Optional.of(List.of(Map.of()));

        List<ParameterMapping> mappings = myBatisConfiguration.getParameterMap(param.getId()).getParameterMappings();
        assertFalse(mappings.isEmpty());

        List<List<Object>> possibleValues = new ArrayList<>(mappings.size());
        for (ParameterMapping m : mappings)
        {
            possibleValues.add(getPossibleValues(m.getJavaType()));
        }

        List<Object> allMaps = allPermutations(possibleValues).stream().map(p -> {
            Map<String, Object> m = new HashMap<>();
            for (int i = 0; i < mappings.size(); i++) m.put(mappings.get(i).getProperty(), p.get(i));
            return m;
        }).collect(Collectors.toUnmodifiableList());

        return Optional.of(allMaps);
    }

    private List<Object> getBeanParameters(String statementId, ParameterMap param)
    {
        Class<?> type = param.getType();
        if (ArchivedNodeEntity.class == type)
        {
            type = GetArchivedNodesCannedQueryParams.class;
        }
        if (CalendarEntity.class == type)
        {
            type = GetCalendarEntriesCannedQueryParams.class;
        }

        assertNotNull(type);

        return getPossibleInstances(type);
    }

    AtomicInteger level = new AtomicInteger();
    private List<Object> getPossibleInstances(Class type)
    {
        if (PropertyDoubleValueEntity.class == type)
        {
            PropertyDoubleValueEntity e = new PropertyDoubleValueEntity();
            e.setDoubleValue(1d);
            e.setId(1l);
            return List.of(e);
        }
        if (org.alfresco.repo.domain.node.NodeEntity.class == type)
        {
            return List.of(new org.alfresco.repo.domain.node.NodeEntity());
        }
        if (org.alfresco.repo.domain.node.StoreEntity.class == type)
        {
            return List.of(new org.alfresco.repo.domain.node.StoreEntity());
        }
        if (ChildPropertyEntity.class == type)
        {
            ChildPropertyEntity e = new ChildPropertyEntity();
            e.setParentNodeId(1L);
            e.setPropertyQNameId(2L);
            e.setValue(new NodePropertyValue(DataTypeDefinition.TEXT, "v"));
            return List.of(e);
        }
        if (level.incrementAndGet() > 4)
        {
            System.out.println(level.get() + " -> " + type);
        }
        try
        {
            return getBeanParametersWithDefaultConstructor(type.getConstructor());
        } catch (NoSuchMethodException e)
        {
            return getBeanParametersWithoutDefaultConstructor(type);
        }
        finally
        {
            level.decrementAndGet();
        }
    }

    private List<Object> getBeanParametersWithDefaultConstructor(Constructor<?> ctor)
    {
        final BeanInfo info;
        try
        {
            info = Introspector.getBeanInfo(ctor.newInstance().getClass());
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        final List<Method> setters = Stream
                .of(info.getMethodDescriptors())
                .map(MethodDescriptor::getMethod)
                .filter(m -> m.getName().startsWith("set") && m.getParameterTypes().length == 1)
                .filter(this::isSupported)
                .collect(Collectors.toUnmodifiableList());

        List<List<Object>> possibleValues = new ArrayList<>(setters.size());
        for (Method m : setters)
        {
            final Class<?> paramType = m.getParameterTypes()[0];
            if (List.class.isAssignableFrom(paramType))
            {
                possibleValues.add(List.of(instantiateList(m.getGenericParameterTypes()[0])));
                continue;
            }
            if (Long.class == paramType && (m.getName().contains("MaxRecords")))
            {
                possibleValues.add(Arrays.asList(1L, 2L));
                continue;
            }
            if (String.class == paramType && m.getName().contains("ContentUrl"))
            {
                possibleValues.add(Arrays.asList("content"));
                continue;
            }
            if (Number.class.isAssignableFrom(paramType) && m.getName().equals("setLimit"))
            {
                possibleValues.add(Arrays.asList(0, 1));
                continue;
            }
            if (FeedControlEntity.class.isAssignableFrom(paramType) && m.getName().equals("setAppTool"))
            {
                possibleValues.add(Arrays.asList("a", "bb"));
                continue;
            }

            possibleValues.add(getPossibleValues(paramType));
        }

//        long count = possibleValues.stream().mapToLong(List::size).reduce(1, (a, b) -> a * b);
//        if (count > 1000000)
//        {
//            System.err.println(info.getBeanDescriptor().getBeanClass() + " -> " + count);
//            for (int i = 0; i < setters.size(); i++)
//            {
//                System.err.println("\t" + setters.get(i).getName() + " -> " + possibleValues.get(i).size());
//            }
//        }

        return allPermutations(possibleValues).stream().map(v -> {
            try
            {
                final Object instance = ctor.newInstance();
                for (int i = 0; i < setters.size(); i++)
                {
                    try
                    {
                        setters.get(i).invoke(instance, v.get(i));
                    } catch (InvocationTargetException e)
                    {
                        if (NullPointerException.class.isInstance(e.getCause()) || IllegalArgumentException.class.isInstance(e.getCause()))
                        {
                            return null;
                        }
                        //System.err.println("$$$ setting " + v.get(i) + " using " + setters.get(i));
                        throw e;
                    }
                }
                return instance;
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }).filter(o -> o != null) .collect(Collectors.toUnmodifiableList());
    }

    private boolean isSupported(Method method)
    {
        if ("setTypeQNameIds".equals(method.getName()) && method.getDeclaringClass() == ChildAssocEntity.class) return false;
        if ("setTypeQNameId".equals(method.getName()) && method.getDeclaringClass() == TransactionQueryEntity.class) return false;
        if ("setStoreId".equals(method.getName()) && method.getDeclaringClass() == TransactionQueryEntity.class) return false;
        if ("setJavaClass".equals(method.getName()) && method.getDeclaringClass() == PropertyClassEntity.class) return false;
        if ("setUriSafe".equals(method.getName()) && method.getDeclaringClass() == NamespaceEntity.class) return false;
        return true;
    }

    private Object instantiateList(Type paramType)
    {
        if ("java.util.List<java.lang.Long>".equals(paramType.getTypeName())) return List.of(0L, 1L, 2L);
        if ("java.util.List<java.lang.String>".equals(paramType.getTypeName())) return List.of("a", "bb");
        //System.err.println("!!@#$% " + paramType.getTypeName());
        return List.of(0L, 1L, 2L);
    }

    private List<Object> getBeanParametersWithoutDefaultConstructor(Class type)
    {
        if (EncryptedKey.class == type) return List.of(new EncryptedKey("a", "b", "c", ByteBuffer.allocate(1)));
        if (Set.class == type) return List.of(Set.of(1L));
        if (Serializable.class == type) return List.of("asd");
        if (QNameDAO.class == type) return List.of(qNameDAO);

        if (type.isEnum())
        {
            Object[] allValues = type.getEnumConstants();
            if (allValues.length < 2) return List.of(allValues[0]);
            return List.of(allValues[0], allValues[1]);
        }

        if (!type.isInterface() && type.getDeclaredConstructors().length > 0)
        {
            Optional<Constructor> possibleCtor = Stream
                    .of(type.getDeclaredConstructors())
                    .filter(c -> (c.getModifiers() & Modifier.PUBLIC) == Modifier.PUBLIC)
                    .sorted(Comparator.comparingInt(Constructor::getParameterCount))
                    .findFirst();
            if (possibleCtor.isEmpty())
            {
                throw new RuntimeException("Don't know what to proceed with `" + type + "`.");
            }
            Constructor ctor = possibleCtor.get();

            final List<Class> params = Stream
                    .of(ctor.getParameterTypes())
                    .collect(Collectors.toUnmodifiableList());

            List<List<Object>> possibleValues = new ArrayList<>(params.size());
            for (Class<?> paramType : params)
            {
                possibleValues.add(getPossibleValues(paramType));
            }

            return allPermutations(possibleValues).stream().map(v -> {
                try
                {
                    try
                    {
                        return ctor.newInstance(v.toArray());
                    } catch (InvocationTargetException e)
                    {
                        if (NullPointerException.class.isInstance(e.getCause()) || IllegalArgumentException.class.isInstance(e.getCause()))
                        {
                            return null;
                        }
                        throw e;
                    }
                } catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }).filter(o -> o != null) .collect(Collectors.toUnmodifiableList());
        }

        System.err.println("!@#$%!! " + type);
        return Arrays.asList(new Object[] {null});
//        throw new RuntimeException("Can't create " + type);
        //return List.of();
    }

    private List<Object> getPossibleValues(Class<?> type)
    {
        if (Long.class == type) return Arrays.asList(null, -1L, 0L, 1L);
        if (String.class == type) return Arrays.asList(null, "a", "bb");
        if (Boolean.class == type) return Arrays.asList(null, true, false);
        if (Integer.class == type) return Arrays.asList(null, -1, 0, 1);
        if (Double.class == type) return Arrays.asList(null, -1d, 0d, 1d);
        if (int.class == type) return Arrays.asList(-1, 0, 1);
        if (long.class == type) return Arrays.asList(-1L, 0L, 1L);
        if (boolean.class == type) return Arrays.asList(true, false);
        if (Date.class == type) return Arrays.asList(null, new Date());
        if (Short.class == type) return Arrays.asList(null, (short)-1, (short)0, (short)1);
        if (short.class == type) return Arrays.asList((short)-1, (short)0, (short)1);
        if (NodeRef.class == type)
            return Arrays.asList(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, UUID.randomUUID().toString()));
        if (NodePropertyValue.class == type)
            return List.of(new NodePropertyValue(DataTypeDefinition.TEXT, "abc"));

        if (byte[].class == type)
            return List.of(new byte[] {1, 2});
        if (Long[].class == type)
            return List.of(new Long[] {1L}, new Long[] {1L, 2L});

        List<Object> result = getPossibleInstances(type);
        if (result.size() <= 10) return result;

        ArrayList<Object> r = new ArrayList<>(result);
        Collections.shuffle(r);
        return r.subList(0, 10);

        //throw new UnsupportedOperationException("Unsupported type " + type);
    }

    private Optional<Object> getKnownParameter(String statementId)
    {
        if ("alfresco.lock.select_LockBySharedIds".equals(statementId)) return Optional.of(List.of(1L));
        if ("select_LockBySharedIds".equals(statementId)) return Optional.of(List.of(1L));
        return Optional.empty();
    }

    private Optional<List<Object>> getKnownParameters(String statementId)
    {
        return Optional.empty();
    }

    final Random rnd = new Random();
    List<List<Object>> allPermutations(List<List<Object>> possibleValues)
    {
        List<List<Object>> result = new ArrayList<>(2048);
        int size = possibleValues.size();
        final int[] idx = new int[size + 1];

        while (idx[size] == 0)
        {
            if (result.size() < 10_000 || rnd.nextInt(12345) == 17)
            {
                List permutation = new ArrayList(size);
                for (int i = 0; i < size; i++) permutation.add(possibleValues.get(i).get(idx[i]));

                result.add(permutation);
//                if (result.size() > 1000000 && result.size() % 1000 == 0)
//                {
//                    System.err.println(result.size());
//                }
            }

            for (int i = 0; i < idx.length; i++)
            {
                idx[i]++;
                if (i < possibleValues.size() && idx[i] >= possibleValues.get(i).size())
                {
                    idx[i] = 0;
                } else
                {
                    break;
                }
            }
        }

        return result;
    }

//    //@Test
//    public void alabama() throws IllegalAccessException, SQLException, InvocationTargetException, NoSuchMethodException, InstantiationException
//    {
//        assertNotNull(sessionFactory);
////        Environment env = myBatisConfiguration.getEnvironment();
////        TransactionFactory txFactory = env.getTransactionFactory();
////        Transaction tx = txFactory.newTransaction(env.getDataSource(), null, false);
//        //TestExecutor executor = new TestExecutor(myBatisConfiguration, tx);
//        //myBatisConfiguration.newExecutor(tx, ExecutorType.SIMPLE);
//
////        GetArchivedNodesCannedQueryParams params = new GetArchivedNodesCannedQueryParams(1L, 2L, "a", Boolean.TRUE, 3L, Boolean.FALSE);
////        sessionFactory.openSession().selectList("alfresco.query.archivednodes.select_GetArchivedNodesCannedQuery", params);
//
//        for (String stmtId : myBatisConfiguration.getMappedStatementNames())
//        {
//            final MappedStatement ms;
//            try
//            {
//                ms = myBatisConfiguration.getMappedStatement(stmtId);
//            } catch (IllegalArgumentException e)
//            {
//                System.err.println("Skipping `" + stmtId + "` because of `" + e.getMessage() + "`.");
//                continue;
//            }
//            if (DBQuery.class == ms.getParameterMap().getType())
//            {
//                System.err.println("Skipping `" + stmtId + "` because of `" + ms.getParameterMap().getType() + "`.");
//                continue;
//            }
//            if (!SqlCommandType.SELECT.equals(ms.getSqlCommandType())) continue;
//            final ResultMap resMap = ms.getResultMaps().get(0);
//            if (resMap.getMappedColumns().isEmpty() && (resMap.getType().isPrimitive() || Number.class.isAssignableFrom(resMap.getType()) || String.class == resMap.getType()))
//            {
//                System.err.println("Skipping `" + stmtId + "` because of empty column mapping for type `" + resMap.getType() + "`.");
//                continue;
//            }
////            Collection<Map<String, ?>> parametersToCheck = getParametersToCheck("alfresco.node.select_ChildAssocOfParentByName");
//            final ParameterMap param = ms.getParameterMap();
//            String paramId = param.getId();
//            Class<?> paramType = param.getType();
//            String paramTypeInfo = "" + paramType + (ms.getConfiguration().hasParameterMap(paramId) ? "(" + ms.getConfiguration().getParameterMap(paramId).getParameterMappings() + ")" : "(" + param.getParameterMappings() + ")");
//
//            System.out.println("Checking " + stmtId + "#" + paramTypeInfo);
//            Collection<Object> parametersToCheck = getParametersToCheck(stmtId);
//            for (Object p : parametersToCheck)
//            {
//                Set<String> columnNames = executor.getColumnNames(ms, p);
//                Set<String> resultColumns = new HashSet<>(resMap.getMappedColumns());
//                resultColumns.removeAll(columnNames);
//                if (!resultColumns.isEmpty())
//                {
//                    System.out.println(resultColumns);
//                }
//            }
//        }

//        session.selectList("alfresco.node.select_ChildAssocOfParentByName", Map.of("parentNode", Map.of("id", 1L), "typeQNameId", 2L, "childNodeName", "\';a", "childNodeNameCrc", 1L));
//
//        for (Object o : cfg.getMappedStatements())
//        {
//            if (!(o instanceof MappedStatement) || !SqlCommandType.SELECT.equals(((MappedStatement) o).getSqlCommandType()))
//                continue;
////            if (!(o instanceof MappedStatement)) continue;
//            final MappedStatement ms = (MappedStatement) o;
//            if (ms.getSqlSource() instanceof DynamicSqlSource) continue;
//            assertEquals(1, ms.getResultMaps().size());
//            final ResultMap resultMap = ms.getResultMaps().get(0);
//            assertNotNull(resultMap);
//            System.out.print(ms.getId() + " -> " + ms.getParameterMap().getType() + " ");
//            SqlSource sqlSource = ms.getSqlSource();
//            ReflectionUtils.findField(sqlSource.getClass(), "sqlSource");
//            System.out.println(ms.getBoundSql(null).getParameterMappings());
//        }

//        System.err.println(cfg.getAutoMappingBehavior());

//        for (Object o : cfg.getResultMaps())
//        {
//            if (!(o instanceof ResultMap)) continue;
//            final ResultMap resultMap = (ResultMap) o;
//            System.out.println(resultMap.getId() + " -> " + resultMap.getMappedColumns());
//            System.out.println(resultMap.getId() + " -> " + resultMap.getResultMappings());
//        }

//        SqlSession session = sessionFactory.openSession();
//
//        List<Object> tenants = session.selectList("alfresco.tenants.select_Tenant");

//        for (Object o : cfg.getMappedStatements())
//        {
//            if (!(o instanceof MappedStatement))
//            {
//                Field f = ReflectionUtils.findField(o.getClass(), "subject");
//                ReflectionUtils.makeAccessible(f);
//                System.err.println(ReflectionUtils.getField(f, o));
//                continue;
//            }
//            MappedStatement stmt = (MappedStatement) o;
//            if (!SqlCommandType.SELECT.equals(stmt.getSqlCommandType()))
//            {
//                continue;
//            }
//            System.out.print("\n" + stmt.getId() + " -> ");
//
//            session.select(stmt.getId(), new ResultHandler()
//            {
//                @Override
//                public void handleResult(ResultContext resultContext)
//                {
//                    System.out.print(resultContext.getResultObject());
//                }
//            });
//
//        }
//
//           .stream()
//           .filter(s -> SqlCommandType.SELECT.equals(s.getSqlCommandType()))
//           .forEach(this::check);
//    }

    private Collection<Object> getParametersToCheck(String statementId) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException
    {
        MappedStatement ms = myBatisConfiguration.getMappedStatement(statementId);

        Collection<Object> dynamicParams = getDynamicParameters(ms);
        if (dynamicParams != null) return dynamicParams;

        Collection<Object> knownParams = getKnownParameters(ms);
        if (knownParams != null) return knownParams;


        final List<ParameterMapping> params = getParamsMapping(ms);
        assertNotNull(params);
        if (params.isEmpty()) return List.of(Map.of());

        final List<String> paramNames = new ArrayList<>();
        final List<List<Object>> valuesToCheck = new ArrayList<>();
        for (ParameterMapping m : params)
        {
            paramNames.add(m.getProperty());
            valuesToCheck.add(getValuesToCheck(m));
        }
        final int[] idx = new int[paramNames.size()];
        final ArrayList<Object> result = new ArrayList<>();
        do
        {
            Map<String, Object> p = new LinkedHashMap<>();
            for (int i = 0; i < idx.length; i++)
            {
                String name = paramNames.get(i);
                String[] names = name.split("\\.");
                Object value = valuesToCheck.get(i).get(idx[i]);
                for (int n = names.length - 1; n > 0; n--)
                {
                    value = Collections.singletonMap(names[n], value);
                }
                p.put(names[0], value);
            }
            result.add(p);

            for (int i = 0; i < idx.length; i++)
            {
                if (++idx[i] >= valuesToCheck.get(i).size())
                {
                    idx[i] = 0;
                } else
                {
                    break;
                }
            }
        } while (IntStream.of(idx).sum() != 0);
        return result;
    }

    private Collection<Object> getKnownParameters(MappedStatement ms)
    {
        if (NodeBatchLoadEntity.class == ms.getParameterMap().getType())
        {
            NodeBatchLoadEntity e = new NodeBatchLoadEntity();
            e.setStoreId(1L);
            e.setIds(List.of(2L));
            e.setUuids(List.of("a"));
            return List.of(e);
        }
        if (ChildPropertyEntity.class == ms.getParameterMap().getType())
        {
            ChildPropertyEntity e = new ChildPropertyEntity();
            e.setParentNodeId(1L);
            e.setPropertyQNameId(2L);
            e.setValue(new NodePropertyValue(DataTypeDefinition.TEXT, "v"));
            return List.of(e);
        }
        if (IdsEntity.class == ms.getParameterMap().getType())
        {
            IdsEntity ids = new IdsEntity();
            ids.setIds(List.of(0L));
            ids.setIdOne(1L);
            ids.setIdTwo(2L);
            ids.setIdThree(3L);
            ids.setIdFour(4L);
            return List.of(ids);
        }
        if ("alfresco.node.select_TxnMaxTxIdInNodeIdRange".equals(ms.getId()))
        {
            NodeRangeEntity range = new NodeRangeEntity();
            range.setFromNodeId(1L);
            range.setToNodeId(2L);
            return List.of(range);
        }
        if ("alfresco.query.archivednodes.select_GetArchivedNodesCannedQuery".equals(ms.getId()))
        {
            return List.of(new GetArchivedNodesCannedQueryParams(1L, 2L, "a", Boolean.TRUE, 3L, Boolean.FALSE));
        }
        if ("alfresco.query.downloads.select_GetDownloadsBeforeQuery".equals(ms.getId()))
        {
            return List.of(new GetDownloadsCannedQueryParams(1L, 2L, 3L, new Date()));
        }
        if ("alfresco.query.calendar.select_GetCalendarEntriesCannedQuery".equals(ms.getId()))
        {
            GetCalendarEntriesCannedQueryParams e = new GetCalendarEntriesCannedQueryParams(new Long[] { 1L }, 2L, 3L, 4L, 5L, 6L, 7L, new Date(), new Date());
            return List.of(e);
        }
        if (FilterSortPersonEntity.class == ms.getParameterMap().getType())
        {
            FilterSortPersonEntity e = new FilterSortPersonEntity();
            e.setPattern("asd");
            return List.of(e);
        }
        if (DBQuery.class == ms.getParameterMap().getType())
        {
            DBQueryModelFactory factory = new DBQueryModelFactory();
            Function function = factory.getFunction(PropertyAccessor.NAME);
            Argument arg = factory.createPropertyArgument(PropertyAccessor.ARG_PROPERTY, true, true, "", "asd");
            Map<String, Argument> functionArguments = new LinkedHashMap<String, Argument>();
            functionArguments.put(arg.getName(), arg);
            Column column = factory.createColumn(function, functionArguments, "asd");
            Selector selector = factory.createSelector(CalendarModel.ASPECT_DOC_FOLDERED, "asd");
            Query q = factory.createQuery(List.of(column), selector, null, null);
            return List.of(q);
        }
        return null;
    }

    private Collection<Object> getDynamicParameters(MappedStatement ms)
    {
        if (ms.getParameterMap().getType() == null && ms.getSqlSource() instanceof DynamicSqlSource)
        {
            try
            {
                ms.getBoundSql(null);
            } catch (BuilderException e)
            {
                if (e.getMessage().contains("'list' evaluated to a null"))
                {
                    return List.of(List.of(1L));
                } else
                {
                    throw e;
                }
            }
        }
        return null;
    }

    private List<ParameterMapping> getParamsMapping(MappedStatement ms) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException
    {
        Class<?> type = ms.getParameterMap().getType();
        Object paramInstance;
        if (type == null)
        {
            paramInstance = null;
        } else
        {
            assertNotNull(type);
            if (Map.class == type)
            {
                //ms.getConfiguration().getParameterMap(ms.getParameterMap().getId())
                paramInstance = null;
            } else if (Long.class == type)
            {
                paramInstance = 1L;
            } else
            {
                paramInstance = type.getDeclaredConstructor().newInstance();
            }
        }
        return ms.getBoundSql(paramInstance).getParameterMappings();
    }

    private List<Object> getValuesToCheck(ParameterMapping m)
    {
        Class<?> clazz = m.getJavaType();
        assertNotNull(clazz);
        assertTrue(ParameterMode.IN.equals(m.getMode()) || m.getMode() == null);

        if (clazz == Object.class && m.getProperty().toLowerCase(Locale.ROOT).endsWith("id"))
        {
            clazz = Long.class;
        }

        if (clazz == Long.class) return Arrays.asList(null, 0L, 1L);
        if (clazz == String.class) return Arrays.asList(null, "a", "bb");
        if (clazz == double.class) return Arrays.asList(-1.0d, 0.0d, 1.0d);
        if (clazz == Double.class) return Arrays.asList(null, 0.0d, 1.0d);
        if (clazz == int.class) return Arrays.asList(-1, 0, 1);
        if (clazz == long.class) return Arrays.asList(-1L, 0L, 1L);
        if (clazz == Boolean.class) return Arrays.asList(true, false, null);
        if (clazz == Integer.class) return Arrays.asList(null, 0, 1);
        if (clazz == Short.class) return Arrays.asList(null, (short) 0, (short) 1);

        throw new UnsupportedOperationException("Unknown type `" + clazz + "` for property `" + m.getProperty() + "`.");
    }
}
