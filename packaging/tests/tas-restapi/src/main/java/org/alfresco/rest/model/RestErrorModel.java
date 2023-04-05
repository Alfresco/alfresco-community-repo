/*-
 * #%L
 * alfresco-tas-restapi
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
package org.alfresco.rest.model;

import org.springframework.http.HttpStatus;
import org.testng.Assert;

public class RestErrorModel
{
    private String errorKey = "";
    private String logId = "";
    private int statusCode = 0;

    private String briefSummary = "";
    private String stackTrace = "";
    private String descriptionURL = "";
    public static String PERMISSION_WAS_DENIED = "Permission was denied";
    public static String ENTITY_NOT_FOUND = "%s was not found";
    public static String ENTITY_WAS_NOT_FOUND = "The entity with id: %s was not found";
    public static String RELATIONSHIP_NOT_FOUND = "The relationship resource was not found for the entity with id: %s and a relationship id of %s";
    public static String AUTHENTICATION_FAILED = "Authentication failed";
    public static String INVALID_ARGUMENT = "An invalid %s was received";
    public static String UNABLE_TO_LOCATE = "Unable to locate resource";
    public static String NULL_ARGUMENT = "Must provide a non-null %s";
    public static String CANNOT_COMMENT = "Cannot comment on this node";
    public static String NON_NULL_COMMENT = "Must provide a non-null comment";
    public static String INVALID_RATING = "Invalid ratingSchemeId %s";
    public static String NO_CONTENT = "Could not read content from HTTP request body: %s";
    public static String NULL_LIKE_RATING = "Rating should be non-null and a boolean for 'likes' rating scheme";
    public static String NULL_FIVESTAR_RATING = "Rating should be non-null and an integer for 'fiveStar' rating scheme";
    public static String CANNOT_RATE = "Cannot rate this node";
    public static String CANNOT_TAG = "Cannot tag this node";
    public static String NOT_SUFFICIENT_PERMISSIONS = "The current user does not have sufficient permissions to delete membership details of the site %s";
    public static String DELETE_EMPTY_ARGUMENT = "DELETE not supported for Actions";
    public static String PUT_EMPTY_ARGUMENT = "PUT is executed against the instance URL";
    public static String TASK_INVALID_STATE = "The task state property has an invalid value: %s";
    public static String INVALID_MAXITEMS = "Invalid paging parameter maxItems:%s";
    public static String INVALID_SKIPCOUNT = "Invalid paging parameter skipCount:%s";
    public static String INVALID_TAG = "Tag name must not contain %s char sequence";
    public static String BLANK_TAG = "New tag cannot be blank";
    public static String UNKNOWN_ROLE = "Unknown role '%s'";
    public static String ALREADY_Site_MEMBER = "%s is already a member of site %s";
    public static String ALREADY_INVITED = "%s is already invited to site %s";
    public static String NOT_A_MEMBER = "User is not a member of the site";
    public static String ONLY_POSITIVE_VALUES_MAXITEMS = "Only positive values supported for maxItems";
    public static String NEGATIVE_VALUES_SKIPCOUNT = "Negative values not supported for skipCount";
    public static String PROCESS_ENTITY_NOT_FOUND = "The entity with id: Item %s not found in the process package variable was not found";
    public static String MUST_PROVIDE_ROLE = "Must provide a role";
    public static String INVALID_ORDERBY = "OrderBy %s is not supported, supported items are [%s]";
    public static String ONLY_ONE_ORDERBY = "Only one orderBy parameter is supported";
    public static String INVALID_WHERE_QUERY = "An invalid WHERE query was received. %s";
    public static String PROCESS_RUNNING_IN_ANOTHER_TENANT = "Process is running in another tenant";
    public static String ILLEGAL_SCOPE = "Illegal value for variable scope: '%s'.";
    public static String UNSUPPORTED_TYPE = "Unsupported type of variable: '%s'.";
    public static String PROPERTY_DOES_NOT_EXIST = "The property selected for update does not exist for this resource: %s";
    public static String PROPERTY_IS_NOT_SUPPORTED_EQUALS = "The property '%s' with value '%s' isn't supported for EQUALS comparison";
    public static String INVALID_VALUE = "The %s property has an invalid value: %s";
    public static String REQUIRED_TO_ADD = "%s is required to add an attached item";
    public static String ACCESS_INFORMATION_NOT_ALLOWED = "user is not allowed to access information about process %s";
    public static String TASK_ALREADY_COMPLETED = "Task with id: %s cannot be updated, it's completed";
    public static String DELEGATED_TASK_CAN_NOT_BE_COMPLETED = "A delegated task cannot be completed, but should be resolved instead.";
    public static String UNEXPECTED_TENANT = "Unexpected tenant: %s (contains %s)";
    public static String DELETE_LAST_MANAGER = "Can't remove last manager of site %s";
    public static String RATING_OUT_OF_BOUNDS = "Rating is out of bounds.";
    public static String ACCEPT_ME= "Please accept me";
    public static String NO_WORKFLOW_DEFINITION_FOUND = "No workflow definition could be found with key '%s'.";
    public static String INVALID_USER_ID = "%s is not a valid person user id";
    public static String TASK_ALREADY_CLAIMED = "The task is already claimed by another user.";
    public static String INVALID_SELECT = "An invalid SELECT query was received";
    public static String DELEGATING_ASSIGNEE_PROVIDED = "When delegating a task, assignee should be selected and provided in the request.";
    public static String INVALID_NAMEPACE_PREFIX="Namespace prefix %s is not mapped to a namespace URI";
    public static String FOR_INPUT_STRING ="For input string: \"%s\"";
    public static String UNRECOGNIZED_FIELD ="Unrecognized field \"%s\"";
    
    public static String LOCAL_NAME_CONSISTANCE = "A QName must consist of a local name";
    public static String INVALID_PARAMETER_WHO = "Parameter who should be one of [me, others]";
    public static String NOT_FAVOURITE_SITE = "Site %s is not a favourite site";

    public static String RESTAPIEXPLORER = "https://api-explorer.alfresco.com";
    public static String STACKTRACE = "For security reasons the stack trace is no longer displayed, but the property is kept for previous versions";
    public static String RELATIONSHIP_NOT_FOUND_ERRORKEY = "framework.exception.RelationshipNotFound";
    public static String ENTITY_NOT_FOUND_ERRORKEY = "framework.exception.EntityNotFound";
    public static String PERMISSION_DENIED_ERRORKEY = "framework.exception.PermissionDenied";
    public static String API_DEFAULT_ERRORKEY = "framework.exception.ApiDefault";
    public static String INVALID_QUERY_ERRORKEY = "framework.exception.InvalidQuery";
    public static String INVALID_PROPERTY_ERRORKEY = "framework.exception.InvalidProperty";
    public static String NOT_FOUND_ERRORKEY = "framework.exception.NotFound";
    public static String VARIABLE_NAME_REQUIRED = "Variable name is required.";
    public static String MANDATORY_PARAM = "%s is a mandatory parameter";
    public static String MANDATORY_COLLECTION = "%s collection must contain at least one item";
    public static String INVALID_SELECT_ERRORKEY = "framework.exception.InvalidSelect";
    public static String LOCKED_NODE_OPERATION = "Cannot perform operation since the node (id:%s) is locked.";
    public static String LOCKED_NODE_SUMMARY = "The node (id: %s) could not be locked since it is already locked by another user.";

    public String getErrorKey()
    {
        return errorKey;
    }

    public void setErrorKey(String errorKey)
    {
        this.errorKey = errorKey;
    }

    public int getStatusCode()
    {
        return statusCode;
    }

    public void setStatusCode(int statusCode)
    {
        this.statusCode = statusCode;
    }

    public String getBriefSummary()
    {
        return briefSummary;
    }

    public void setBriefSummary(String briefSummary)
    {
        this.briefSummary = briefSummary;
    }

    public String getStackTrace()
    {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace)
    {
        this.stackTrace = stackTrace;
    }

    public String getDescriptionURL()
    {
        return descriptionURL;
    }

    public void setDescriptionURL(String descriptionURL)
    {
        this.descriptionURL = descriptionURL;
    }

    public String getLogId()
    {
        return logId;
    }

    public void setLogId(String logId)
    {
        this.logId = logId;
    }

    public RestErrorModel containsSummary(String summary)
    {
        if (!getBriefSummary().contains(summary))
            Assert.fail(String.format("Expected [%s] error to be found in actual briefSummary returned by server: %s", summary, getBriefSummary()));
        return this;
    }

    public RestErrorModel containsErrorKey(String expected)
    {
        if (!getErrorKey().contains(expected))
            Assert.fail(String.format("Expected [%s] error to be found in actual errorKey returned by server: %s", expected, getErrorKey()));
        return this;
    }

    public RestErrorModel statusCodeIs(HttpStatus statusCode)
    {
        if (getStatusCode() != statusCode.value())
            Assert.fail(String.format("Expected [%s] to be found. Actual statusCode returned by server is [%s]", statusCode, getStatusCode()));
        return this;
    }

    public RestErrorModel descriptionURLIs(String expected)
    {
        if (!getDescriptionURL().equals(expected))
            Assert.fail(String.format("Expected [%s] to be found. Actual descriptionURL returned by server is [%s]", expected, getDescriptionURL()));
        return this;
    }

    public RestErrorModel stackTraceIs(String expected)
    {
        if (!getStackTrace().equals(expected))
            Assert.fail(String.format("Expected [%s] to be found. Actual stackTrace returned by server is [%s]", expected, getStackTrace()));
        return this;
    }
}
