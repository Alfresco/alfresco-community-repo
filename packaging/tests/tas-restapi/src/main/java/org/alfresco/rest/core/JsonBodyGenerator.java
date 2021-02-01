package org.alfresco.rest.core;

import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.alfresco.dataprep.CMISUtil.Priority;
import org.alfresco.rest.model.RestProcessVariableModel;
import org.alfresco.rest.model.RestVariableModel;
import org.alfresco.utility.Utility;
import org.alfresco.utility.constants.UserRole;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.RepoTestModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.UserModel;

/**
 * Json builder for small post calls
 */
public class JsonBodyGenerator
{
    private static JsonBuilderFactory jsonBuilder;

    /**
     * @return {@link JsonObjectBuilder}
     */
    public static JsonObjectBuilder defineJSON()
    {
        return jsonBuilder().createObjectBuilder();
    }
    
    /**
     * @return {@link JsonArrayBuilder}
     */
    public static JsonArrayBuilder defineJSONArray()
    {
        return jsonBuilder().createArrayBuilder();
    }
    /**
     * {
     *    "tag":"test-tag-1"
     * }
     * 
     * @param key
     * @param value
     * @return
     */
    public static String keyValueJson(String key, String value)
    {
        return defineJSON().add(key, value).build().toString();
    }

    /**
     * {
     *  "target": {
     *          "site": {
     *                  "guid": "abcde-01234"
     *                  }
     *          }
     * }
     * 
     * @param siteModel
     * @return
     */
    public static String targetSiteWithGuid(SiteModel siteModel)
    {
        JsonObject value = defineJSON()
                .add("target", defineJSON()
                        .add("site", defineJSON()
                                .add("guid", siteModel.getGuid()))).build();

        return value.toString();
    }

    /**
     * {
     * "target": {
     *          "file": {
     *                  "guid": "abcde-01234"
     *                  }
     *          }
     * }
     * 
     * @param siteModel
     * @return
     */
    public static String targetFileWithGuid(FileModel fileModel)
    {
        JsonObject value = defineJSON()
                .add("target", defineJSON()
                        .add("file", defineJSON()
                                .add("guid", fileModel.getNodeRef().replace(";1.0", "")))).build();
        return value.toString();
    }

    /**
     * {
     * "target": {
     *                  "folder": {
     *                          "guid": "abcde-01234"
     *                          }
     *          }
     * }
     * 
     * @param siteModel
     * @return
     */
    public static String targetFolderWithGuid(FolderModel folderModel)
    {
        JsonObject value = defineJSON()
                .add("target", defineJSON()
                        .add("folder", defineJSON()
                                .add("guid", folderModel.getNodeRef()))).build();
        return value.toString();
    }

    /**
     * @return the initialized JSON builder
     */
    private static JsonBuilderFactory jsonBuilder()
    {
        if (jsonBuilder == null)
            return Json.createBuilderFactory(null);
        else
        {
            return jsonBuilder;
        }
    }

    public static String likeRating(boolean likeOrNot)
    {
        JsonObject value = defineJSON()
                .add("id", "likes")
                .add("myRating", likeOrNot).build();
        return value.toString();
    }
    
    public static String fiveStarRating(int stars)
    {
        JsonObject value = defineJSON()
                .add("id", "fiveStar")
                .add("myRating", stars).build();
        return value.toString();
    }
    
    
    public static String siteMember(UserModel userModel)
    {
        Utility.checkObjectIsInitialized(userModel.getUserRole(), "userModel.getUserRole()");
        JsonObject value = defineJSON()
                .add("role", userModel.getUserRole().name())
                .add("id", userModel.getUsername()).build();
        return value.toString();
    }
    
    public static String siteGroup(String authorityId, UserRole role)
    {
        Utility.checkObjectIsInitialized(authorityId, "authorityId");
        JsonObject value = defineJSON()
                .add("role", role.name())
                .add("id", authorityId).build();
        return value.toString();
    }

    public static String siteMemberhipRequest(String message, SiteModel siteModel, String title)
    {
        JsonObject value = defineJSON()
                .add("message", message)
                .add("id", siteModel.getId())
                .add("title", title).build();
        return value.toString();
    }
    /**
     * Method to create a Json object for SiteBody with site title, description, visibility
     * @param siteModel
     * @return String
     */
    public static String updateSiteRequest(SiteModel siteModel)
    {
        JsonObject value = defineJSON()
                .add("title", siteModel.getTitle())
                .add("description", siteModel.getDescription())
                .add("visibility", siteModel.getVisibility().toString()).build();
        return value.toString();
    }

    public static String process(String processDefinitionKey, UserModel assignee, boolean sendEmailNotifications, Priority priority)
    {
        JsonObject value = defineJSON()
                .add("processDefinitionKey", processDefinitionKey)
                .add("variables", jsonBuilder().createObjectBuilder()
                        .add("bpm_assignee", assignee.getUsername())
                        .add("bpm_sendEMailNotifications", sendEmailNotifications)
                        .add("bpm_workflowPriority", priority.getLevel())).build();
        return value.toString();
    }
    
    public static String processVariable(RestProcessVariableModel variableModel)
    {
        JsonObject value = defineJSON()
                .add("name", variableModel.getName())
                .add("value", variableModel.getValue())
                .add("type", variableModel.getType()).build();
        return value.toString();
    }
    
    public static String taskVariable(RestVariableModel taskVariableModel)
    {
        JsonObject value = defineJSON()
                .add("scope", taskVariableModel.getScope())
                .add("name", taskVariableModel.getName())
                .add("type", taskVariableModel.getType())
                .add("value", taskVariableModel.getValue().toString()).build();
        return value.toString();
    }

    /**
     * {
     *    "actionDefinitionId": "copy",
     *    "targetId": "4c4b3c43-f18b-43ff-af84-751f16f1ddfd",
     *    "params": {
             "destination-folder": "34219f79-66fa-4ebf-b371-118598af898c"
     *      }
     * }
     * 
     * @param actionDefinitionId
     * @param targetNode
     * @param params
     * @return
     */
    public static String executeActionPostBody(String actionDefinitionId, RepoTestModel targetNode, Map<String, String> params)
    {
        JsonObjectBuilder objectBuilder = jsonBuilder().createObjectBuilder();
        for(Map.Entry<String, String> param : params.entrySet())
        {
            objectBuilder.add(param.getKey(), param.getValue());
        }
        JsonObject value = defineJSON()
                .add("actionDefinitionId", actionDefinitionId)
                .add("targetId", targetNode.getNodeRefWithoutVersion())
                .add("params", objectBuilder).build();
        return value.toString();
    }
    
    /**
     * {
     * "actionDefinitionId": "check-out",
     * "targetId": "4c4b3c43-f18b-43ff-af84-751f16f1ddfd",
     * }
     *
     * @param actionDefinitionId
     * @param targetNode
     * @return
     */
    public static String executeActionPostBody(String actionDefinitionId, RepoTestModel targetNode)
    {
        JsonObject value = defineJSON()
                .add("actionDefinitionId", actionDefinitionId)
                .add("targetId", targetNode.getNodeRefWithoutVersion())
                .build();
        return value.toString();
    }

    /**
     * {
     *    "key1":"key1",
     *    "key2":"key2",
     *    "key3":"key3"
     * }
     * 
     * @param key
     * @param value
     * @return
     */
    public static String keyValueJson(HashMap<String, String> mapJson)
    {
        JsonObjectBuilder builder= defineJSON();

        for (Map.Entry<String, String> entry : mapJson.entrySet())
        {
            builder.add(entry.getKey().toString(), entry.getValue().toString());
        }
        return builder.build().toString();
    }
}
