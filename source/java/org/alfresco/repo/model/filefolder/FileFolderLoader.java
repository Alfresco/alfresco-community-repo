/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
package org.alfresco.repo.model.filefolder;

import org.alfresco.repo.admin.RepositoryState;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.transaction.TransactionService;

/**
 * Class to aid in the generation of file-folder data structures for load test purposes.
 * <p/>
 * All paths referenced are in relation to the standard Alfresco "Company Home" folder,
 * which acts as the root for accessing documents and folders via many APIs.
 * <p/>
 * <strong>WARNING:  This class may be used but will probably NOT be considered part of the public API i.e.
 * will probably change in line with Alfresco's internal requirements; nevertheless, backward
 * compatibility will be maintained where practical.</strong>
 * 
 * @author Derek Hulley
 * @since 5.1
 */
public class FileFolderLoader
{
    private final RepositoryState repoState;
    private final TransactionService transactionService;
    private final Repository repositoryHelper;
    
    /**
     * @param repoState             keep track of repository readiness
     * @param transactionService    ensure proper rollback, where required
     * @param repositoryHelper      access standard repository paths
     */
    public FileFolderLoader(RepositoryState repoState, TransactionService transactionService, Repository repositoryHelper)
    {
        this.repoState = repoState;
        this.transactionService = transactionService;
        this.repositoryHelper = repositoryHelper;
    }
    
    /**
     * 
     * @param folderPath                        the full path to the folder
     * @param fileCount                         the number of files to create
     * @param minFileSize                       the smallest file size (all sizes within 1 standard deviation of the mean)
     * @param maxFileSize                       the largest file size (all sizes within 1 standard deviation of the mean)
     * @param uniqueContentCount                the total number of unique files that can be generated i.e. each file will be
     *                                          one of a total number of unique files.
     * @param forceBinaryStorage                <tt>true</tt> to actually write the spoofed text data to the binary store
     *                                          i.e. the physical underlying storage will have a real file
     * @return                                  the number of files successfully created
     * @throws FileNotFoundException            if the folder path does not exist
     * @throws IllegalStateException            if the repository is not ready
     */
    public int createFiles(
            String folderPath,
            int fileCount,
            long minFileSize, long maxFileSize,
            boolean forceBinaryStorage,
            long uniqueContentCount) throws FileNotFoundException
    {
        if (repoState.isBootstrapping())
        {
            throw new IllegalStateException("Repository is still bootstrapping.");
        }
        return 0;
    }
}
