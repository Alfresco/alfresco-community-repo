package org.alfresco.repo.domain.ibatis;

import java.io.IOException;
import java.util.Properties;

import org.alfresco.ibatis.HierarchicalSqlMapClientFactoryBean;
import org.hibernate.cfg.Environment;
import org.springframework.core.io.Resource;

import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * Extension to the SQLMap factory to produce <tt>SqlMapClient</tt> instances that
 * cater for Alfresco extensions.
 * <p>
 * Currently, this is just a hack to find the Hibernate dialect and provide that as
 * a property to the factory code.  This will go away if we move over to iBatis; to be
 * replaced with something similar to the schema script loading that uses a hierarchy
 * of databases.
 * 
 * @author Derek Hulley
 * @since 3.1
 */
public class AlfrescoSqlMapClientFactoryBean extends HierarchicalSqlMapClientFactoryBean
{

    @Override
    protected SqlMapClient buildSqlMapClient(Resource configLocation, Properties properties) throws IOException
    {
        // Get the Hibernate dialect from the system properties
        String hibernateDialect = System.getProperty(Environment.DIALECT);
        if (hibernateDialect == null)
        {
            return super.buildSqlMapClient(configLocation, properties);
        }
        else
        {
            if (properties == null)
            {
                properties = new Properties();
            }
            properties.put("hibernate.dialect", hibernateDialect);
            return super.buildSqlMapClient(configLocation, properties);
        }
    }
    
}
