/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.rm.community.utils;

import static java.nio.charset.Charset.forName;

import static com.google.common.io.Resources.getResource;

import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.CONTENT_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.NON_ELECTRONIC_RECORD_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.RECORD_CATEGORY_TYPE;
import static org.alfresco.rest.rm.community.model.fileplancomponents.FilePlanComponentType.RECORD_FOLDER_TYPE;
import static org.alfresco.utility.data.RandomData.getRandomAlphanumeric;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;

import org.alfresco.rest.rm.community.model.record.Record;
import org.alfresco.rest.rm.community.model.record.RecordProperties;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategory;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChild;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryChildProperties;
import org.alfresco.rest.rm.community.model.recordcategory.RecordCategoryProperties;
import org.alfresco.rest.rm.community.model.recordfolder.RecordFolder;
import org.alfresco.rest.rm.community.model.recordfolder.RecordFolderProperties;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledContainerChild;
import org.alfresco.rest.rm.community.model.unfiledcontainer.UnfiledContainerChildProperties;

/**
 * Utility class for file plan component models
 *
 * @author Tuna Aksoy
 * @since 2.6
 */
public class FilePlanComponentsUtil
{
    private FilePlanComponentsUtil()
    {
        // Intentionally blank
    }

    /**
     * Name of the image resource file to be used for records body
     */
    public static final String IMAGE_FILE = "money.JPG";

    /**
     * Title prefix for record category children
     */
    public static final String TITLE_PREFIX = "Title for ";

    /**
     * Description prefix for record category children
     */
    public static final String DESCRIPTION_PREFIX = "This is the description for";


    /**
     * Helper method to get a file by its name
     *
     * @return The file
     */
    public static File getFile(String fileName)
    {
        return new File(getResource(fileName).getFile());
    }

    /**
     * Creates a record model with the given type and a random name (with "Record " prefix)
     *
     * @param nodeType The node type
     * @return The {@link Record} with for the given node type
     */
    private static Record createRecordModel(String nodeType)
    {
        return Record.builder()
                     .name("Record " + getRandomAlphanumeric())
                     .nodeType(nodeType)
                     .build();
    }

    /**
     * Creates an electronic record model with a random name (with "Record " prefix)
     *
     * @return The electronic record as {@link Record}
     */
    public static Record createElectronicRecordModel()
    {
        return createRecordModel(CONTENT_TYPE);
    }

    /**
     * Creates a non-electronic unfiled container child model with a random name (with "Record " prefix)
     *
     * @return The electronic record as {@link UnfiledContainerChild}
     */
    public static UnfiledContainerChild createElectronicUnfiledContainerChildModel()
    {
        return createUnfiledContainerChildRecordModel("Record " + getRandomAlphanumeric(), CONTENT_TYPE);
    }

    /**
     * Creates an electronic unfiled container child model with a random name (with "Record " prefix)
     *
     * @return The electronic record as {@link UnfiledContainerChild}
     */
    public static UnfiledContainerChild createNonElectronicUnfiledContainerChildModel()
    {
        return createUnfiledContainerChildRecordModel("Record " + getRandomAlphanumeric(), NON_ELECTRONIC_RECORD_TYPE);
    }

    /**
     * Creates an unfiled records container child record model with the given name and type
     *
     * @param name     The name of the unfiled records container child
     * @param nodeType The type of the record category child
     * @return The {@link UnfiledContainerChild} with the given details
     */
    public static UnfiledContainerChild createUnfiledContainerChildRecordModel(String name, String nodeType)
    {
        return UnfiledContainerChild.builder()
                                    .name(name)
                                    .nodeType(nodeType)
                                    .build();
    }

    /**
     * Creates a nonElectronic container child record model with all available properties for the non electronic records
     *
     * @param name     The name of the unfiled records container child
     * @param nodeType The type of the record category child
     * @return The {@link UnfiledContainerChild} with the given details
     */
    public static UnfiledContainerChild createFullNonElectronicUnfiledContainerChildRecordModel(String name, String title, String description, String box, String file,
                                                                                                String shelf, String storageLocation, Integer numberOfCopies, Integer physicalSize)
    {
        return UnfiledContainerChild.builder()
                                    .name(name)
                                    .nodeType(NON_ELECTRONIC_RECORD_TYPE)
                                    .properties(UnfiledContainerChildProperties.builder()
                                                                               .title(title)
                                                                               .description(description)
                                                                               .box(box)
                                                                               .file(file)
                                                                               .shelf(shelf)
                                                                               .storageLocation(storageLocation)
                                                                               .numberOfCopies(numberOfCopies)
                                                                               .physicalSize(physicalSize)
                                                                               .build())
                                    .build();
    }

    /**
     * Creates a non-electronic record model with a random name (with "Record " prefix)
     *
     * @return The non-electronic record as {@link Record}
     */
    public static Record createNonElectronicRecordModel()
    {
        return createRecordModel(NON_ELECTRONIC_RECORD_TYPE);
    }

    /**
     * Creates a non-electronic record model with with all available properties for the non electronic records
     *
     * @return The non-electronic record as {@link Record}
     */
    public static Record createFullNonElectronicRecordModel(String name, String title, String description, String box, String file,
                                                            String shelf, String storageLocation, Integer numberOfCopies, Integer physicalSize)
    {
        return Record.builder()
                     .name(name)
                     .nodeType(NON_ELECTRONIC_RECORD_TYPE)
                     .properties(RecordProperties.builder()
                                                 .title(title)
                                                 .description(description)
                                                 .box(box)
                                                 .file(file)
                                                 .shelf(shelf)
                                                 .storageLocation(storageLocation)
                                                 .numberOfCopies(numberOfCopies)
                                                 .physicalSize(physicalSize)
                                                 .build())
                     .build();
    }

    /**
     * Creates a record model with the given name, description and title
     *
     * @param name        The name of the record
     * @param description The description of the record
     * @param title       The title of the record
     * @return The {@link Record} with the given details
     */
    public static Record createRecordModel(String name, String description, String title)
    {
        return Record.builder()
                     .name(name)
                     .properties(RecordProperties.builder()
                                                 .description(description)
                                                 .title(title)
                                                 .build())
                     .build();
    }

    /**
     * Creates a record category child model with the given name and type
     *
     * @param name     The name of the record category child
     * @param nodeType The type of the record category child
     * @return The {@link RecordCategoryChild} with the given details
     */
    public static RecordCategoryChild createRecordCategoryChildModel(String name, String nodeType)
    {
        return RecordCategoryChild.builder()
                                  .name(name)
                                  .nodeType(nodeType)
                                  .properties(RecordCategoryChildProperties.builder()
                                                                           .title(TITLE_PREFIX + name)
                                                                           .build())
                                  .build();
    }

    /**
     * Creates a record category model with the given name and title
     *
     * @param name  The name of the record category
     * @param title The title of the record category
     * @return The {@link RecordCategory} with the given details
     */
    public static RecordCategory createRecordCategoryModel(String name, String title)
    {
        return RecordCategory.builder()
                             .name(name)
                             .nodeType(RECORD_CATEGORY_TYPE)
                             .properties(RecordCategoryProperties.builder()
                                                                 .title(title)
                                                                 .build())
                             .build();
    }

    /**
     * Creates a record folder model with the given name and title
     *
     * @param name  The name of the record folder
     * @param title The title of the record folder
     * @return The {@link RecordFolder} with the given details
     */
    public static RecordFolder createRecordFolderModel(String name, String title)
    {
        return RecordFolder.builder()
                           .name(name)
                           .nodeType(RECORD_FOLDER_TYPE)
                           .properties(RecordFolderProperties.builder()
                                                             .title(title)
                                                             .build())
                           .build();
    }

    /**
     * Creates an unfiled records container child model with the given name and type
     *
     * @param name     The name of the unfiled records container child
     * @param nodeType The type of the record category child
     * @return The {@link UnfiledContainerChild} with the given details
     */
    public static UnfiledContainerChild createUnfiledContainerChildModel(String name, String nodeType)
    {
        return UnfiledContainerChild.builder()
                                    .name(name)
                                    .nodeType(nodeType)
                                    .properties(UnfiledContainerChildProperties.builder()
                                                                               .title(TITLE_PREFIX + name)
                                                                               .build())
                                    .build();
    }

    /**
     * Create temp file with content
     *
     * @param name The file name
     * @return {@link File} The created file
     */
    public static File createTempFile(final String name, String content)
    {
        try
        {
            // Create file
            final File file = File.createTempFile(name, ".txt");

            // Create writer
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), forName("UTF-8").newEncoder()))
            {
                // place content in file
                writer.write(content);
            }

            return file;
        }
        catch (Exception exception)
        {
            throw new RuntimeException("Unable to create test file.", exception);
        }
    }

    /**
     * Method to create a temporary file with specific size
     *
     * @param name            file name
     * @param sizeInMegaBytes size
     * @return temporary file
     */
    public static File createTempFile(final String name, long sizeInMegaBytes)
    {
        try
        {
            // Create file
            final File file = File.createTempFile(name, ".txt");

            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            raf.setLength(sizeInMegaBytes * 1024 * 1024);
            raf.close();

            return file;
        }
        catch (Exception exception)
        {
            throw new RuntimeException("Unable to create test file.", exception);
        }
    }

    /**
     * Helper method to verify all properties of a nonElectronic record
     *
     * @param nonElectronicRecord
     * @param name
     * @param title
     * @param description
     * @param box
     * @param file
     * @param shelf
     * @param storageLocation
     * @param numberOfCopies
     * @param physicalSize
     */
    public static void verifyFullNonElectronicRecord(Record nonElectronicRecord, String name, String title, String description, String box, String file,
                                                     String shelf, String storageLocation, Integer numberOfCopies, Integer physicalSize)
    {
        RecordProperties properties = nonElectronicRecord.getProperties();
        assertEquals(title, properties.getTitle());
        assertEquals(description, properties.getDescription());
        assertEquals(box, properties.getBox());
        assertEquals(file, properties.getFile());
        assertEquals(shelf, properties.getShelf());
        assertEquals(storageLocation, properties.getStorageLocation());
        assertEquals(numberOfCopies, properties.getNumberOfCopies());
        assertEquals(physicalSize, properties.getPhysicalSize());
        assertTrue(nonElectronicRecord.getName().contains(properties.getIdentifier()));
        assertTrue(nonElectronicRecord.getName().contains(name));
    }
}
