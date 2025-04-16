/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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
package org.alfresco.ibatis;

import static org.springframework.util.Assert.notNull;
import static org.springframework.util.ObjectUtils.isEmpty;
import static org.springframework.util.StringUtils.hasLength;
import static org.springframework.util.StringUtils.tokenizeToStringArray;

import java.io.IOException;
import java.util.Properties;
import javax.sql.DataSource;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.type.TypeHandler;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

import org.alfresco.metrics.db.DBMetricsReporter;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.resource.HierarchicalResourceLoader;

/**
 * Extends the MyBatis-Spring support by allowing a choice of {@link org.springframework.core.io.ResourceLoader}. The {@link #setResourceLoader(HierarchicalResourceLoader) ResourceLoader} will be used to load the <b>SqlMapConfig</b> file and use a {@link HierarchicalXMLConfigBuilder} to read the individual MyBatis (3.x) resources.
 * <p/>
 * Pending a better way to extend/override, much of the implementation is a direct copy of the MyBatis-Spring {@link SqlSessionFactoryBean}; some of the <tt>protected</tt> methods do not have access to the object's state and can therefore not be overridden successfully.
 * <p/>
 * This is equivalent to HierarchicalSqlMapClientFactoryBean which extended iBatis (2.x). See also: <a href=https://issues.apache.org/jira/browse/IBATIS-589>IBATIS-589</a> and: <a href=http://code.google.com/p/mybatis/issues/detail?id=21</a>
 *
 * @author Derek Hulley, janv
 * @since 4.0
 */
// note: effectively replaces SqlSessionFactoryBean to use hierarchical resource loader
public class HierarchicalSqlSessionFactoryBean extends SqlSessionFactoryBean
{

    private HierarchicalResourceLoader resourceLoader;

    private final Log logger = LogFactory.getLog(getClass());

    private Resource configLocation;

    private Resource[] mapperLocations;

    private DataSource dataSource;

    private TransactionFactory transactionFactory;

    private Properties configurationProperties;

    private SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new AlfrescoSqlSessionFactoryBuilder();

    private SqlSessionFactory sqlSessionFactory;

    private String environment = SqlSessionFactoryBean.class.getSimpleName(); // EnvironmentAware requires spring 3.1

    private boolean failFast;

    private Interceptor[] plugins;

    private TypeHandler<?>[] typeHandlers;

    private String typeHandlersPackage;

    private Class<?>[] typeAliases;

    private String typeAliasesPackage;

    private Class<?> typeAliasesSuperType;

    private DatabaseIdProvider databaseIdProvider = new VendorDatabaseIdProvider();

    private ObjectFactory objectFactory;

    private ObjectWrapperFactory objectWrapperFactory;

    private DBMetricsReporter dbMetricsReporter;

    /**
     * Default constructor
     */
    public HierarchicalSqlSessionFactoryBean()
    {}

    public DBMetricsReporter getDbMetricsReporter()
    {
        return dbMetricsReporter;
    }

    public void setDbMetricsReporter(DBMetricsReporter dbMetricsReporter)
    {
        this.dbMetricsReporter = dbMetricsReporter;
    }

    /**
     * Set the resource loader to use. To use the <b>&#35;resource.dialect&#35</b> placeholder, use the {@link HierarchicalResourceLoader}.
     *
     * @param resourceLoader
     *            the resource loader to use
     */
    public void setResourceLoader(HierarchicalResourceLoader resourceLoader)
    {
        this.resourceLoader = resourceLoader;
    }

    /**
     * Sets the ObjectFactory.
     *
     * @since 1.1.2
     * @param objectFactory
     *            ObjectFactory
     */
    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    /**
     * Sets the ObjectWrapperFactory.
     *
     * @since 1.1.2
     * @param objectWrapperFactory
     *            ObjectWrapperFactory
     */
    public void setObjectWrapperFactory(ObjectWrapperFactory objectWrapperFactory)
    {
        this.objectWrapperFactory = objectWrapperFactory;
    }

    /**
     * Sets the DatabaseIdProvider.
     *
     * @since 1.1.0
     * @return DatabaseIdProvider
     */
    public DatabaseIdProvider getDatabaseIdProvider()
    {
        return databaseIdProvider;
    }

    /**
     * Gets the DatabaseIdProvider
     *
     * @since 1.1.0
     * @param databaseIdProvider
     *            DatabaseIdProvider
     */
    public void setDatabaseIdProvider(DatabaseIdProvider databaseIdProvider)
    {
        this.databaseIdProvider = databaseIdProvider;
    }

    /**
     * Mybatis plugin list.
     *
     * @since 1.0.1
     *
     * @param plugins
     *            list of plugins
     *
     */
    public void setPlugins(Interceptor... plugins)
    {
        this.plugins = plugins;
    }

    /**
     * Packages to search for type aliases.
     *
     * @since 1.0.1
     *
     * @param typeAliasesPackage
     *            package to scan for domain objects
     *
     */
    public void setTypeAliasesPackage(String typeAliasesPackage)
    {
        this.typeAliasesPackage = typeAliasesPackage;
    }

    /**
     * Super class which domain objects have to extend to have a type alias created. No effect if there is no package to scan configured.
     *
     * @since 1.1.2
     *
     * @param typeAliasesSuperType
     *            super class for domain objects
     *
     */
    public void setTypeAliasesSuperType(Class<?> typeAliasesSuperType)
    {
        this.typeAliasesSuperType = typeAliasesSuperType;
    }

    /**
     * Packages to search for type handlers.
     *
     * @since 1.0.1
     *
     * @param typeHandlersPackage
     *            package to scan for type handlers
     *
     */
    public void setTypeHandlersPackage(String typeHandlersPackage)
    {
        this.typeHandlersPackage = typeHandlersPackage;
    }

    /**
     * Set type handlers. They must be annotated with {@code MappedTypes} and optionally with {@code MappedJdbcTypes}
     *
     * @since 1.0.1
     *
     * @param typeHandlers
     *            Type handler list
     */
    public void setTypeHandlers(TypeHandler<?>... typeHandlers)
    {
        this.typeHandlers = typeHandlers;
    }

    /**
     * List of type aliases to register. They can be annotated with {@code Alias}
     *
     * @since 1.0.1
     *
     * @param typeAliases
     *            Type aliases list
     */
    public void setTypeAliases(Class<?>... typeAliases)
    {
        this.typeAliases = typeAliases;
    }

    /**
     * If true, a final check is done on Configuration to assure that all mapped statements are fully loaded and there is no one still pending to resolve includes. Defaults to false.
     *
     * @since 1.0.1
     *
     * @param failFast
     *            enable failFast
     */
    public void setFailFast(boolean failFast)
    {
        this.failFast = failFast;
    }

    /**
     * Set the location of the MyBatis {@code SqlSessionFactory} config file. A typical value is "WEB-INF/mybatis-configuration.xml".
     */
    public void setConfigLocation(Resource configLocation)
    {
        this.configLocation = configLocation;
    }

    /**
     * Set locations of MyBatis mapper files that are going to be merged into the {@code SqlSessionFactory} configuration at runtime.
     *
     * This is an alternative to specifying "&lt;sqlmapper&gt;" entries in an MyBatis config file. This property being based on Spring's resource abstraction also allows for specifying resource patterns here: e.g. "classpath*:sqlmap/*-mapper.xml".
     */
    public void setMapperLocations(Resource... mapperLocations)
    {
        this.mapperLocations = mapperLocations;
    }

    /**
     * Set optional properties to be passed into the SqlSession configuration, as alternative to a {@code &lt;properties&gt;} tag in the configuration xml file. This will be used to resolve placeholders in the config file.
     */
    public void setConfigurationProperties(Properties sqlSessionFactoryProperties)
    {
        this.configurationProperties = sqlSessionFactoryProperties;
    }

    /**
     * Set the JDBC {@code DataSource} that this instance should manage transactions for. The {@code DataSource} should match the one used by the {@code SqlSessionFactory}: for example, you could specify the same JNDI DataSource for both.
     *
     * A transactional JDBC {@code Connection} for this {@code DataSource} will be provided to application code accessing this {@code DataSource} directly via {@code DataSourceUtils} or {@code DataSourceTransactionManager}.
     *
     * The {@code DataSource} specified here should be the target {@code DataSource} to manage transactions for, not a {@code TransactionAwareDataSourceProxy}. Only data access code may work with {@code TransactionAwareDataSourceProxy}, while the transaction manager needs to work on the underlying target {@code DataSource}. If there's nevertheless a {@code TransactionAwareDataSourceProxy} passed in, it will be unwrapped to extract its target {@code DataSource}.
     *
     */
    public void setDataSource(DataSource dataSource)
    {
        if (dataSource instanceof TransactionAwareDataSourceProxy)
        {
            // If we got a TransactionAwareDataSourceProxy, we need to perform
            // transactions for its underlying target DataSource, else data
            // access code won't see properly exposed transactions (i.e.
            // transactions for the target DataSource).
            this.dataSource = ((TransactionAwareDataSourceProxy) dataSource).getTargetDataSource();
        }
        else
        {
            this.dataSource = dataSource;
        }
    }

    /**
     * Sets the {@code SqlSessionFactoryBuilder} to use when creating the {@code SqlSessionFactory}.
     *
     * This is mainly meant for testing so that mock SqlSessionFactory classes can be injected. By default, {@code SqlSessionFactoryBuilder} creates {@code DefaultSqlSessionFactory} instances.
     *
     */
    public void setSqlSessionFactoryBuilder(SqlSessionFactoryBuilder sqlSessionFactoryBuilder)
    {
        this.sqlSessionFactoryBuilder = sqlSessionFactoryBuilder;
    }

    /**
     * Set the MyBatis TransactionFactory to use. Default is {@code SpringManagedTransactionFactory}
     *
     * The default {@code SpringManagedTransactionFactory} should be appropriate for all cases: be it Spring transaction management, EJB CMT or plain JTA. If there is no active transaction, SqlSession operations will execute SQL statements non-transactionally.
     *
     * <b>It is strongly recommended to use the default {@code TransactionFactory}.</b> If not used, any attempt at getting an SqlSession through Spring's MyBatis framework will throw an exception if a transaction is active.
     *
     * @see SpringManagedTransactionFactory
     * @param transactionFactory
     *            the MyBatis TransactionFactory
     */
    public void setTransactionFactory(TransactionFactory transactionFactory)
    {
        this.transactionFactory = transactionFactory;
    }

    /**
     * <b>NOTE:</b> This class <em>overrides</em> any {@code Environment} you have set in the MyBatis config file. This is used only as a placeholder name. The default value is {@code SqlSessionFactoryBean.class.getSimpleName()}.
     *
     * @param environment
     *            the environment name
     */
    public void setEnvironment(String environment)
    {
        this.environment = environment;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {

        PropertyCheck.mandatory(this, "resourceLoader", resourceLoader);

        notNull(dataSource, "Property 'dataSource' is required");
        notNull(sqlSessionFactoryBuilder, "Property 'sqlSessionFactoryBuilder' is required");
        if (sqlSessionFactoryBuilder instanceof AlfrescoSqlSessionFactoryBuilder)
        {
            ((AlfrescoSqlSessionFactoryBuilder) sqlSessionFactoryBuilder).setDbMetricsReporter(this.dbMetricsReporter);
        }
        this.sqlSessionFactory = buildSqlSessionFactory();
    }

    /**
     * Build a {@code SqlSessionFactory} instance.
     * <p/>
     * The default implementation uses the standard MyBatis {@code XMLConfigBuilder} API to build a {@code SqlSessionFactory} instance based on an Reader.
     *
     * @return SqlSessionFactory
     * @throws IOException
     *             if loading the config file failed
     */
    protected SqlSessionFactory buildSqlSessionFactory() throws IOException
    {

        Configuration configuration;

        HierarchicalXMLConfigBuilder xmlConfigBuilder = null;
        if (this.configLocation != null)
        {
            try
            {
                xmlConfigBuilder = new HierarchicalXMLConfigBuilder(resourceLoader, this.configLocation.getInputStream(), null, this.configurationProperties);
                configuration = xmlConfigBuilder.getConfiguration();
            }
            catch (Exception ex)
            {
                throw new IOException("Failed to parse config resource: " + this.configLocation, ex);
            }
            finally
            {
                ErrorContext.instance().reset();
            }
        }
        else
        {
            if (this.logger.isDebugEnabled())
            {
                this.logger.debug("Property 'configLocation' not specified, using default MyBatis Configuration");
            }
            configuration = new Configuration();
            configuration.setVariables(this.configurationProperties);
        }

        if (this.objectFactory != null)
        {
            configuration.setObjectFactory(this.objectFactory);
        }

        if (this.objectWrapperFactory != null)
        {
            configuration.setObjectWrapperFactory(this.objectWrapperFactory);
        }

        if (hasLength(this.typeAliasesPackage))
        {
            String[] typeAliasPackageArray = tokenizeToStringArray(this.typeAliasesPackage,
                    ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
            for (String packageToScan : typeAliasPackageArray)
            {
                configuration.getTypeAliasRegistry().registerAliases(packageToScan,
                        typeAliasesSuperType == null ? Object.class : typeAliasesSuperType);
                if (this.logger.isDebugEnabled())
                {
                    this.logger.debug("Scanned package: '" + packageToScan + "' for aliases");
                }
            }
        }

        if (!isEmpty(this.typeAliases))
        {
            for (Class<?> typeAlias : this.typeAliases)
            {
                configuration.getTypeAliasRegistry().registerAlias(typeAlias);
                if (this.logger.isDebugEnabled())
                {
                    this.logger.debug("Registered type alias: '" + typeAlias + "'");
                }
            }
        }

        if (!isEmpty(this.plugins))
        {
            for (Interceptor plugin : this.plugins)
            {
                configuration.addInterceptor(plugin);
                if (this.logger.isDebugEnabled())
                {
                    this.logger.debug("Registered plugin: '" + plugin + "'");
                }
            }
        }

        if (hasLength(this.typeHandlersPackage))
        {
            String[] typeHandlersPackageArray = tokenizeToStringArray(this.typeHandlersPackage,
                    ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
            for (String packageToScan : typeHandlersPackageArray)
            {
                configuration.getTypeHandlerRegistry().register(packageToScan);
                if (this.logger.isDebugEnabled())
                {
                    this.logger.debug("Scanned package: '" + packageToScan + "' for type handlers");
                }
            }
        }

        if (!isEmpty(this.typeHandlers))
        {
            for (TypeHandler<?> typeHandler : this.typeHandlers)
            {
                configuration.getTypeHandlerRegistry().register(typeHandler);
                if (this.logger.isDebugEnabled())
                {
                    this.logger.debug("Registered type handler: '" + typeHandler + "'");
                }
            }
        }

        if (xmlConfigBuilder != null)
        {
            try
            {
                xmlConfigBuilder.parse();

                if (this.logger.isDebugEnabled())
                {
                    this.logger.debug("Parsed configuration file: '" + this.configLocation + "'");
                }
            }
            catch (Exception ex)
            {
                throw new IOException("Failed to parse config resource: " + this.configLocation, ex);
            }
            finally
            {
                ErrorContext.instance().reset();
            }
        }

        if (this.transactionFactory == null)
        {
            this.transactionFactory = new SpringManagedTransactionFactory();
        }

        Environment environment = new Environment(this.environment, this.transactionFactory, this.dataSource);
        configuration.setEnvironment(environment);

        // Commented out to be able to use dummy dataSource in tests.
        /* if (this.databaseIdProvider != null) { try { configuration.setDatabaseId(this.databaseIdProvider.getDatabaseId(this.dataSource)); } catch (SQLException e) { throw new IOException("Failed getting a databaseId", e); } } */

        if (!isEmpty(this.mapperLocations))
        {
            for (Resource mapperLocation : this.mapperLocations)
            {
                if (mapperLocation == null)
                {
                    continue;
                }

                try
                {
                    XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(mapperLocation.getInputStream(),
                            configuration, mapperLocation.toString(), configuration.getSqlFragments());
                    xmlMapperBuilder.parse();
                }
                catch (Exception e)
                {
                    throw new IOException("Failed to parse mapping resource: '" + mapperLocation + "'", e);
                }
                finally
                {
                    ErrorContext.instance().reset();
                }

                if (this.logger.isDebugEnabled())
                {
                    this.logger.debug("Parsed mapper file: '" + mapperLocation + "'");
                }
            }
        }
        else
        {
            if (this.logger.isDebugEnabled())
            {
                this.logger.debug("Property 'mapperLocations' was not specified, only MyBatis mapper files specified in the config xml were loaded");
            }
        }

        return this.sqlSessionFactoryBuilder.build(configuration);
    }

    /**
     * {@inheritDoc}
     */
    public SqlSessionFactory getObject() throws Exception
    {
        if (this.sqlSessionFactory == null)
        {
            afterPropertiesSet();
        }

        return this.sqlSessionFactory;
    }

    /**
     * {@inheritDoc}
     */
    public Class<? extends SqlSessionFactory> getObjectType()
    {
        return this.sqlSessionFactory == null ? SqlSessionFactory.class : this.sqlSessionFactory.getClass();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSingleton()
    {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public void onApplicationEvent(ContextRefreshedEvent event)
    {
        if (failFast)
        {
            // fail-fast -> check all statements are completed
            this.sqlSessionFactory.getConfiguration().getMappedStatementNames();
        }
    }
}
