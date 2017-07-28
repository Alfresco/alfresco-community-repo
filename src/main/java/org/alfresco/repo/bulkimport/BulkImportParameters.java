/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.repo.bulkimport;

import org.alfresco.service.cmr.repository.NodeRef;

public class BulkImportParameters
{
	// MNT-17703: Provide configurable behaviour for when the target file already exists in the repository.
	public enum ExistingFileMode
	{
		// If the file already exists...
		SKIP,          // skip the import from the source.
		REPLACE,       // replace the file, loosing any previous version history.
		ADD_VERSION    // create a new version of the file during import, preserving previous history.
	};

	private ExistingFileMode existingFileMode = ExistingFileMode.SKIP;
	private NodeRef target;
	private Integer batchSize;
	private Integer numThreads;
	private Integer loggingInterval;
	private boolean disableRulesService = false;
	
	public boolean isDisableRulesService()
	{
		return disableRulesService;
	}
	public void setDisableRulesService(boolean disableRulesService)
	{
		this.disableRulesService = disableRulesService;
	}
	public Integer getLoggingInterval()
	{
		return loggingInterval;
	}
	public void setLoggingInterval(Integer loggingInterval)
	{
		this.loggingInterval = loggingInterval;
	}
	public NodeRef getTarget()
	{
		return target;
	}
	public void setTarget(NodeRef target)
	{
		this.target = target;
	}
	public Integer getBatchSize()
	{
		return batchSize;
	}
	public void setBatchSize(Integer batchSize)
	{
		this.batchSize = batchSize;
	}
	public Integer getNumThreads()
	{
		return numThreads;
	}
	public void setNumThreads(Integer numThreads)
	{
		this.numThreads = numThreads;
	}

	/**
	 * @deprecated Use {@link #getExistingFileMode} (MNT-17703)
	 * @return
	 */
	public boolean isReplaceExisting()
	{
		return existingFileMode == ExistingFileMode.REPLACE;
	}

	/**
	 * @deprecated Use {@link #setExistingFileMode} (MNT-17703)
	 * @param replaceExisting
	 */
	@Deprecated()
	public void setReplaceExisting(boolean replaceExisting)
	{
		if (replaceExisting)
		{
			setExistingFileMode(ExistingFileMode.REPLACE);
		}
		else
		{
			setExistingFileMode(ExistingFileMode.SKIP);
		}
	}

	public ExistingFileMode getExistingFileMode()
	{
		return existingFileMode;
	}

	public void setExistingFileMode(ExistingFileMode existingFileMode)
	{
		this.existingFileMode = existingFileMode;
	}
}
