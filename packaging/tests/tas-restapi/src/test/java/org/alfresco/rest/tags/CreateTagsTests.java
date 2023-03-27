/*
 * #%L
 * Alfresco Remote API
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
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.tags;

import static org.alfresco.utility.data.RandomData.getRandomName;
import static org.alfresco.utility.report.log.Step.STEP;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.alfresco.rest.RestTest;
import org.alfresco.rest.model.RestTagModel;
import org.alfresco.rest.model.RestTagModelsCollection;
import org.alfresco.utility.model.TestGroup;
import org.alfresco.utility.model.UserModel;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CreateTagsTests extends RestTest
{
    private static final String FIELD_ID = "id";
    private static final String FIELD_TAG = "tag";
    private static final String FIELD_COUNT = "count";
    private static final String TAG_NAME_PREFIX = "tag-name";

    private UserModel admin;
    private UserModel user;

    @BeforeClass
    public void init()
    {
        admin = dataUser.getAdminUser();
        user = dataUser.createRandomTestUser();
    }

    /**
     * Verify if tag does not exist in the system, create one as admin and check if now it's there.
     */
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS })
    public void testCreateSingleTag()
    {
        STEP("Create single tag as admin");
        final RestTagModel tagModel = createTagModelWithName(getRandomName("99gat").toLowerCase());
        final RestTagModel createdTag = restClient.authenticateUser(admin).withCoreAPI().createSingleTag(tagModel);

        restClient.assertStatusCodeIs(CREATED);
        createdTag.assertThat().field(FIELD_TAG).is(tagModel.getTag())
            .assertThat().field(FIELD_ID).isNotEmpty();

        STEP("Verify that tag does exist in the system");
        RestTagModel tag = restClient.authenticateUser(admin).withCoreAPI().getTag(createdTag);
        restClient.assertStatusCodeIs(OK);
        tag.assertThat().isEqualTo(createdTag);
    }

    /**
     * Create multiple orphan tags.
     */
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS })
    public void testCreateMultipleTags()
    {
        STEP("Create several tags as admin");
        final List<RestTagModel> tagModels = IntStream.range(0, 3)
            .mapToObj(i -> createTagModelWithName(getRandomName(TAG_NAME_PREFIX + "-" + i).toLowerCase()))
            .collect(Collectors.toList());
        final RestTagModelsCollection createdTags = restClient.authenticateUser(admin).withCoreAPI().createTags(tagModels);

        restClient.assertStatusCodeIs(CREATED);
        IntStream.range(0, tagModels.size())
            .forEach(i -> createdTags.getEntries().get(i).onModel()
                .assertThat().field(FIELD_TAG).is(tagModels.get(i).getTag())
                .assertThat().field(FIELD_ID).isNotEmpty()
            );
    }

    /**
     * Verify that tag name's case will be lowered.
     */
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS })
    public void testCreateSingleTag_usingUppercaseName()
    {
        STEP("Create single tag as admin using uppercase name");
        final RestTagModel tagModel = createTagModelWithName(getRandomName(TAG_NAME_PREFIX).toUpperCase());
        final RestTagModel createdTag = restClient.authenticateUser(admin).withCoreAPI().createSingleTag(tagModel);

        restClient.assertStatusCodeIs(CREATED);
        createdTag.assertThat().field(FIELD_TAG).is(tagModel.getTag().toLowerCase())
            .assertThat().field(FIELD_ID).isNotEmpty();
    }

    /**
     * Try to create few tags including repeating ones. Repeated tags should be omitted.
     */
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS })
    public void testCreateMultipleTags_withRepeatedName()
    {
        STEP("Create models of tags");
        final String repeatedTagName = getRandomName(TAG_NAME_PREFIX).toLowerCase();
        final List<RestTagModel> tagModels = List.of(
            createTagModelWithName(repeatedTagName),
            createTagModelWithName(getRandomName(TAG_NAME_PREFIX).toLowerCase()),
            createTagModelWithName(repeatedTagName)
        );

        STEP("Create several tags skipping repeating names");
        final RestTagModelsCollection createdTags = restClient.authenticateUser(admin).withCoreAPI().createTags(tagModels);

        restClient.assertStatusCodeIs(CREATED);
        createdTags.assertThat().entriesListCountIs(2);
        createdTags.assertThat().entriesListContains(FIELD_TAG, tagModels.get(0).getTag())
            .and().entriesListContains(FIELD_TAG, tagModels.get(1).getTag());
    }

    /**
     * Try to create a tag as a common user and expect 403 (Forbidden)
     */
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS })
    public void testCreateTag_asUser()
    {
        STEP("Try to create single tag as a common user and expect 403");
        final RestTagModel tagModel = createTagModelWithRandomName();
        restClient.authenticateUser(user).withCoreAPI().createSingleTag(tagModel);

        restClient.assertStatusCodeIs(FORBIDDEN);
    }

    /**
     * Try to call create tag API passing empty list and expect 400 (Bad Request)
     */
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS })
    public void testCreateTags_passingEmptyList()
    {
        STEP("Pass empty list while creating tags and expect 400");
        restClient.authenticateUser(admin).withCoreAPI().createTags(Collections.emptyList());

        restClient.assertStatusCodeIs(BAD_REQUEST);
    }

    /**
     * Try to create a tag, which already exists in the system and expect 409 (Conflict)
     */
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS })
    public void testCreateTag_usingAlreadyExistingTagName()
    {
        STEP("Create some tag in the system");
        final RestTagModel tagToCreate = createTagModelWithRandomName();
        final RestTagModel alreadyExistingTag = prepareOrphanTag(tagToCreate);
        // set original name instead the case lowered one
        alreadyExistingTag.setTag(tagToCreate.getTag());

        STEP("Try to use already existing tag to create duplicate and expect 409");
        restClient.authenticateUser(admin).withCoreAPI().createSingleTag(alreadyExistingTag);

        restClient
            .assertStatusCodeIs(CONFLICT)
            .assertLastError().containsSummary("Duplicate child name not allowed: " + alreadyExistingTag.getTag().toLowerCase());
    }

    /**
     * Verify if count field is 0 for newly created tags.
     */
    @Test(groups = { TestGroup.REST_API, TestGroup.TAGS })
    public void testCreateTag_includingCount()
    {
        STEP("Create single tag as admin including count and verify if it is 0");
        final RestTagModel tagModel = createTagModelWithName(getRandomName(TAG_NAME_PREFIX).toLowerCase());
        final RestTagModel createdTag = restClient.authenticateUser(admin).withCoreAPI().include(FIELD_COUNT).createSingleTag(tagModel);

        restClient.assertStatusCodeIs(CREATED);
        createdTag.assertThat().field(FIELD_TAG).is(tagModel.getTag())
            .assertThat().field(FIELD_ID).isNotEmpty()
            .assertThat().field(FIELD_COUNT).is(0);
    }

    private RestTagModel prepareOrphanTagWithRandomName()
    {
        return prepareOrphanTag(createTagModelWithRandomName());
    }

    private RestTagModel prepareOrphanTag(final RestTagModel tagModel)
    {
        final RestTagModel tag = restClient.authenticateUser(admin).withCoreAPI().createSingleTag(tagModel);
        restClient.assertStatusCodeIs(CREATED);
        return tag;
    }

    private static RestTagModel createTagModelWithRandomName()
    {
        return createTagModelWithName(getRandomName(TAG_NAME_PREFIX));
    }

    private static RestTagModel createTagModelWithName(final String tagName)
    {
        return RestTagModel.builder().tag(tagName).create();
    }
}
