package org.alfresco.repo.tenant;

import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;

/**
 * Experimental
 * 
 * @author janv
 * @since 4.2
 */
public class TenantBasicDataSource extends BasicDataSource
{
    public TenantBasicDataSource(BasicDataSource bds, String tenantUrl, int tenantMaxActive) throws SQLException
    {
        // tenant-specific
        this.setUrl(tenantUrl);
        this.setMaxActive(tenantMaxActive == -1 ? bds.getMaxActive() : tenantMaxActive);
        
        // defaults/overrides - see also 'baseDefaultDataSource' (core-services-context.xml + repository.properties)
        
        this.setDriverClassName(bds.getDriverClassName());
        this.setUsername(bds.getUsername());
        this.setPassword(bds.getPassword());
        
        this.setInitialSize(bds.getInitialSize());
        this.setMinIdle(bds.getMinIdle());
        this.setMaxIdle(bds.getMaxIdle());
        this.setDefaultAutoCommit(bds.getDefaultAutoCommit());
        this.setDefaultTransactionIsolation(bds.getDefaultTransactionIsolation());
        this.setMaxWait(bds.getMaxWait());
        this.setValidationQuery(bds.getValidationQuery());
        this.setTimeBetweenEvictionRunsMillis(bds.getTimeBetweenEvictionRunsMillis());
        this.setMinEvictableIdleTimeMillis(bds.getMinEvictableIdleTimeMillis());
        this.setNumTestsPerEvictionRun(bds.getNumTestsPerEvictionRun());
        this.setTestOnBorrow(bds.getTestOnBorrow());
        this.setTestOnReturn(bds.getTestOnReturn());
        this.setTestWhileIdle(bds.getTestWhileIdle());
        this.setRemoveAbandoned(bds.getRemoveAbandoned());
        this.setRemoveAbandonedTimeout(bds.getRemoveAbandonedTimeout());
        this.setPoolPreparedStatements(bds.isPoolPreparedStatements());
        this.setMaxOpenPreparedStatements(bds.getMaxOpenPreparedStatements());
        this.setLogAbandoned(bds.getLogAbandoned());
    }
}