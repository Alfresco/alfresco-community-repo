/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
package org.alfresco.heartbeat;

import com.sun.management.OperatingSystemMXBean;
import com.sun.management.UnixOperatingSystemMXBean;
import org.alfresco.heartbeat.datasender.HBData;
import org.alfresco.heartbeat.jobs.HeartBeatJobScheduler;
import org.alfresco.repo.descriptor.DescriptorDAO;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.util.*;

/**
 * A collector of data related the <code>Runtime</code> data of the system. Every Java application has a single instance of class
 * <code>Runtime</code> that allows the application to interface with the environment in which the application is running.
 * <ul>
 *  <li>Collector ID: <b>acs.repository.usage.system</b></li>
 *  <li>Data:
 *      <ul>
 *          <li><b>mem: Object which contains memory information:</b>
 *              <ul>
 *                  <li><b>free:</b> Long - The amount of free memory in the Java Virtual Machine. {@link Runtime#freeMemory()}</li>
 *                  <li><b>max:</b> Long -T he maximum amount of memory that the Java virtual machine will
 * attempt to use. {@link Runtime#maxMemory()}</li>
 *                  <li><b>total:</b> Long - The total amount of memory in the Java virtual machine. {@link Runtime#totalMemory()}</li>
 *              </ul>
 *          </li>
 *          <li><b>openFileDescriptorCount:</b> Long - The number of open file descriptors. {@link UnixOperatingSystemMXBean#getOpenFileDescriptorCount()}</li>
 *          <li><b>cpu: Object which contains processor information:</b>
 *              <ul>
 *                  <li>percentageProcessLoad: Integer - The "recent cpu usage" for the JVM process (as a percentage). {@link OperatingSystemMXBean#getProcessCpuLoad()}</li>
 *                  <li>percentageSystemLoad: Integer - The "recent cpu usage" for the whole system (as a percentage). {@link OperatingSystemMXBean#getSystemCpuLoad()}</li>
 *                  <li>systemLoadAverage: Double - The system load average as returned by {@link OperatingSystemMXBean#getSystemLoadAverage()}</li>
 *                  <li>availableProcessors: Integer - The number of available processors. {@link Runtime#availableProcessors()}</li>
 *              </ul>
 *          </li>
 *          <li><b>db: Object which contains database usage information:</b>
 *              <ul>
 *                  <li>idleConnections: Integer - The number of idle connections. {@link BasicDataSource#getNumIdle()}</li>
 *                  <li>activeConnections: Integer - The number of active connections. {@link BasicDataSource#getNumActive()}</li>
 *              </ul>
 *          </li>
 *      </ul>
 *  </li>
 * </ul>
 *
 * @author eknizat
 */
public class SystemUsageDataCollector extends HBBaseDataCollector implements InitializingBean
{
    /** The logger. */
    private static final Log logger = LogFactory.getLog(SystemUsageDataCollector.class);

    /** DAO for current repository descriptor. */
    private DescriptorDAO currentRepoDescriptorDAO;
    private DataSource dataSource;

    public SystemUsageDataCollector(String collectorId, String collectorVersion, String cronExpression,
                                    HeartBeatJobScheduler hbJobScheduler)
    {
        super(collectorId, collectorVersion, cronExpression, hbJobScheduler);
    }

    public void setCurrentRepoDescriptorDAO(DescriptorDAO currentRepoDescriptorDAO)
    {
        this.currentRepoDescriptorDAO = currentRepoDescriptorDAO;
    }

    public void setDataSource( DataSource dataSource )
    {
        this.dataSource = dataSource;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "currentRepoDescriptorDAO", currentRepoDescriptorDAO);
        PropertyCheck.mandatory(this, "dataSource", dataSource);
    }

    @Override
    public List<HBData> collectData()
    {
        logger.debug("Preparing repository usage (system) data...");

        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> systemUsageValues = new HashMap<>();

        // operating system MBean info
        Map<String, Object> cpu = new HashMap<>();
        OperatingSystemMXBean osMBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        if (osMBean != null)
        {
            if (osMBean instanceof UnixOperatingSystemMXBean)
            {
                long openFileDescriptorCount = ((UnixOperatingSystemMXBean) osMBean).getOpenFileDescriptorCount();
                systemUsageValues.put("openFileDescriptorCount", new Long(openFileDescriptorCount));
            }

            // processor info
            double processCpuLoad = osMBean.getProcessCpuLoad() * 100;
            double systemCpuLoad  = osMBean.getSystemCpuLoad()  * 100;
            int intProcessCpuLoad = (int) Math.round(processCpuLoad);
            int intSystemCpuLoad  = (int) Math.round(systemCpuLoad);

            cpu.put("percentageProcessLoad", new Integer(intProcessCpuLoad) );
            cpu.put("percentageSystemLoad", new Integer(intSystemCpuLoad));
            cpu.put("systemLoadAverage", new Double(osMBean.getSystemLoadAverage()));
        }
        cpu.put("availableProcessors", new Integer( runtime.availableProcessors()));
        systemUsageValues.put("cpu", cpu);

        // database connections info
        if (dataSource instanceof BasicDataSource)
        {
            Map<String, Object> db = new HashMap<>();
            int idleConnections = ((BasicDataSource) dataSource).getNumIdle();
            int activeConnections = ((BasicDataSource) dataSource).getNumActive();
            db.put("idleConnections", new Integer(idleConnections));
            db.put("activeConnections", new Integer(activeConnections));
            systemUsageValues.put("db", db);
        }

        // memory info
        Map<String, Object> mem = new HashMap<>();
        mem.put("free", runtime.freeMemory());
        mem.put("max", runtime.maxMemory());
        mem.put("total", runtime.totalMemory());
        systemUsageValues.put( "mem", mem);

        HBData systemUsageData = new HBData(
                this.currentRepoDescriptorDAO.getDescriptor().getId(),
                this.getCollectorId(),
                this.getCollectorVersion(),
                new Date(),
                systemUsageValues);

        return Arrays.asList(systemUsageData);
    }
}
