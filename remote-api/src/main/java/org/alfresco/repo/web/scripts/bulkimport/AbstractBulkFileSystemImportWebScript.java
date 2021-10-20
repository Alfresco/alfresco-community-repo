/*
 * #%L
 * Alfresco Remote API
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

package org.alfresco.repo.web.scripts.bulkimport;

import com.google.common.primitives.Ints;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.Supplier;
import org.alfresco.repo.bulkimport.BulkFilesystemImporter;
import org.alfresco.repo.bulkimport.BulkImportParameters;
import org.alfresco.repo.bulkimport.NodeImporter;
import org.alfresco.repo.bulkimport.impl.MultiThreadedBulkFilesystemImporter;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * contains common fields and methods for the import web scripts.
 */
public class AbstractBulkFileSystemImportWebScript
  extends DeclarativeWebScript {

  protected static final Log logger = LogFactory.getLog(
    BulkFilesystemImporter.class
  );

  protected static final String WEB_SCRIPT_URI_BULK_FILESYSTEM_IMPORT_STATUS =
    "/bulkfsimport/status";

  protected static final String PARAMETER_TARGET_NODEREF = "targetNodeRef";
  protected static final String PARAMETER_TARGET_PATH = "targetPath";

  protected static final String COMPANY_HOME_NAME = "Company Home";
  protected static final String COMPANY_HOME_PATH = "/" + COMPANY_HOME_NAME;

  // Web scripts parameters (common)
  protected static final String PARAMETER_REPLACE_EXISTING = "replaceExisting";
  protected static final String PARAMETER_EXISTING_FILE_MODE =
    "existingFileMode";
  protected static final String PARAMETER_VALUE_REPLACE_EXISTING = "true";
  protected static final String PARAMETER_SOURCE_DIRECTORY = "sourceDirectory";
  protected static final String PARAMETER_DISABLE_RULES = "disableRules";
  protected static final String PARAMETER_VALUE_DISABLE_RULES = "true";

  protected static final String IMPORT_ALREADY_IN_PROGRESS_MODEL_KEY =
    "importInProgress";
  protected static final String IMPORT_ALREADY_IN_PROGRESS_ERROR_KEY =
    "bfsit.error.importAlreadyInProgress";

  protected static final String PARAMETER_BATCH_SIZE = "batchSize";
  protected static final String PARAMETER_NUM_THREADS = "numThreads";

  protected FileFolderService fileFolderService;
  protected Repository repository;

  protected volatile boolean importInProgress;

  protected NodeRef getTargetNodeRef(
    String targetNodeRefStr,
    String targetPath
  ) throws FileNotFoundException {
    NodeRef targetNodeRef;

    if (targetNodeRefStr == null || targetNodeRefStr.trim().length() == 0) {
      if (targetPath == null || targetPath.trim().length() == 0) {
        throw new WebScriptException(
          "Error: neither parameter '" +
          PARAMETER_TARGET_NODEREF +
          "' nor parameter '" +
          PARAMETER_TARGET_PATH +
          "' was provided, but at least one is required !"
        );
      }
      targetNodeRef = convertPathToNodeRef(targetPath.trim());
    } else {
      targetNodeRef = new NodeRef(targetNodeRefStr.trim());
    }

    return targetNodeRef;
  }

  protected NodeRef convertPathToNodeRef(String targetPath)
    throws FileNotFoundException {
    NodeRef result = null;
    NodeRef companyHome = repository.getCompanyHome();
    String cleanTargetPath = targetPath.replaceAll("/+", "/");

    if (cleanTargetPath.startsWith(COMPANY_HOME_PATH)) cleanTargetPath =
      cleanTargetPath.substring(COMPANY_HOME_PATH.length());

    if (cleanTargetPath.startsWith("/")) cleanTargetPath =
      cleanTargetPath.substring(1);

    if (cleanTargetPath.endsWith("/")) cleanTargetPath =
      cleanTargetPath.substring(0, cleanTargetPath.length() - 1);

    if (cleanTargetPath.length() == 0) result = companyHome; else {
      FileInfo info = fileFolderService.resolveNamePath(
        companyHome,
        Arrays.asList(cleanTargetPath.split("/"))
      );
      if (info == null) throw new WebScriptException(
        "could not determine NodeRef for path :'" + cleanTargetPath + "'"
      );

      result = info.getNodeRef();
    }

    return (result);
  }

  protected String buildTextMessage(Throwable t) {
    StringBuffer result = new StringBuffer();
    String timeOfFailure = (new Date()).toString();
    String hostName = null;
    String ipAddress = null;

    try {
      hostName = InetAddress.getLocalHost().getHostName();
      ipAddress = InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException uhe) {
      hostName = "unknown";
      ipAddress = "unknown";
    }

    result.append("\nTime of failure:             " + timeOfFailure);
    result.append(
      "\nHost where failure occurred: " + hostName + " (" + ipAddress + ")"
    );

    if (t != null) {
      result.append("\nRoot exception:");
      result.append(renderExceptionStackAsText(t));
    } else {
      result.append("\nNo exception was provided.");
    }

    return (result.toString());
  }

  private String renderExceptionStackAsText(Throwable t) {
    StringBuffer result = new StringBuffer();

    if (t != null) {
      String message = t.getMessage();
      Throwable cause = t.getCause();

      if (cause != null) {
        result.append(renderExceptionStackAsText(cause));
        result.append("\nWrapped by:");
      }

      if (message == null) {
        message = "";
      }

      result.append("\n");
      result.append(t.getClass().getName());
      result.append(": ");
      result.append(message);
      result.append("\n");
      result.append(renderStackTraceElements(t.getStackTrace()));
    }

    return (result.toString());
  }

  private String renderStackTraceElements(StackTraceElement[] elements) {
    StringBuffer result = new StringBuffer();

    if (elements != null) {
      for (int i = 0; i < elements.length; i++) {
        result.append("\tat " + elements[i].toString() + "\n");
      }
    }

    return (result.toString());
  }

  // boilerplate setters

  public void setFileFolderService(FileFolderService fileFolderService) {
    this.fileFolderService = fileFolderService;
  }

  public void setRepository(Repository repository) {
    this.repository = repository;
  }

  protected class MultithreadedImportWebScriptLogic {

    private final MultiThreadedBulkFilesystemImporter bulkImporter;
    private final Supplier<NodeImporter> nodeImporterFactory;
    private final WebScriptRequest request;
    private final Status status;
    private final Cache cache;

    public MultithreadedImportWebScriptLogic(
      MultiThreadedBulkFilesystemImporter bulkImporter,
      Supplier<NodeImporter> nodeImporterFactory,
      WebScriptRequest request,
      Status status,
      Cache cache
    ) {
      this.bulkImporter = Objects.requireNonNull(bulkImporter);
      this.nodeImporterFactory = Objects.requireNonNull(nodeImporterFactory);
      this.request = Objects.requireNonNull(request);
      this.status = Objects.requireNonNull(status);
      this.cache = Objects.requireNonNull(cache);
    }

    public Map<String, Object> executeImport() {
      Map<String, Object> model = new HashMap<>();
      cache.setNeverCache(true);
      String targetPath = null;

      try {
        targetPath = request.getParameter(PARAMETER_TARGET_PATH);
        if (isRunning()) {
          model.put(
            IMPORT_ALREADY_IN_PROGRESS_MODEL_KEY,
            I18NUtil.getMessage(IMPORT_ALREADY_IN_PROGRESS_ERROR_KEY)
          );
          return model;
        }

        final BulkImportParameters bulkImportParameters = getBulkImportParameters();
        final NodeImporter nodeImporter = nodeImporterFactory.get();

        bulkImporter.asyncBulkImport(bulkImportParameters, nodeImporter);

        waitForImportToBegin();

        // redirect to the status Web Script
        status.setCode(Status.STATUS_MOVED_TEMPORARILY);
        status.setRedirect(true);
        status.setLocation(
          request.getServiceContextPath() +
          WEB_SCRIPT_URI_BULK_FILESYSTEM_IMPORT_STATUS
        );
      } catch (WebScriptException | IllegalArgumentException e) {
        status.setCode(Status.STATUS_BAD_REQUEST, e.getMessage());
        status.setRedirect(true);
      } catch (FileNotFoundException fnfe) {
        status.setCode(
          Status.STATUS_BAD_REQUEST,
          "The repository path '" + targetPath + "' does not exist !"
        );
        status.setRedirect(true);
      } catch (Throwable t) {
        throw new WebScriptException(
          Status.STATUS_INTERNAL_SERVER_ERROR,
          buildTextMessage(t),
          t
        );
      }

      return model;
    }

    private void waitForImportToBegin() throws InterruptedException {
      // ACE-3047 fix, since bulk import is started asynchronously there is a chance that client
      // will get into the status page before import is actually started.
      // In this case wrong information (for previous import) will be displayed.
      // So lets ensure that import started before redirecting client to status page.
      int i = 0;
      while (!bulkImporter.getStatus().inProgress() && i < 10) {
        Thread.sleep(100);
        i++;
      }
    }

    private BulkImportParameters getBulkImportParameters()
      throws FileNotFoundException {
      final BulkImportParametersExtractor extractor = new BulkImportParametersExtractor(
        request::getParameter,
        AbstractBulkFileSystemImportWebScript.this::getTargetNodeRef,
        bulkImporter.getDefaultBatchSize(),
        bulkImporter.getDefaultNumThreads()
      );
      return extractor.extract();
    }

    private boolean isRunning() {
      return bulkImporter.getStatus().inProgress();
    }
  }

  protected static class BulkImportParametersExtractor {

    private final Function<String, String> paramsProvider;
    private final NodeRefCreator nodeRefCreator;
    private final int defaultBatchSize;
    private final int defaultNumThreads;

    public BulkImportParametersExtractor(
      final Function<String, String> paramsProvider,
      final NodeRefCreator nodeRefCreator,
      final int defaultBatchSize,
      final int defaultNumThreads
    ) {
      this.paramsProvider = Objects.requireNonNull(paramsProvider);
      this.nodeRefCreator = Objects.requireNonNull(nodeRefCreator);
      this.defaultBatchSize = defaultBatchSize;
      this.defaultNumThreads = defaultNumThreads;
    }

    public BulkImportParameters extract() throws FileNotFoundException {
      BulkImportParameters result = new BulkImportParameters();

      result.setTarget(getTargetNodeRef());
      setExistingFileMode(result);
      result.setNumThreads(
        getOptionalPositiveInteger(PARAMETER_NUM_THREADS)
          .orElse(defaultNumThreads)
      );
      result.setBatchSize(
        getOptionalPositiveInteger(PARAMETER_BATCH_SIZE)
          .orElse(defaultBatchSize)
      );
      setDisableRules(result);

      return result;
    }

    private void setExistingFileMode(BulkImportParameters params) {
      String replaceExistingStr = getParamStringValue(
        PARAMETER_REPLACE_EXISTING
      );
      String existingFileModeStr = getParamStringValue(
        PARAMETER_EXISTING_FILE_MODE
      );

      if (
        !isNullOrEmpty(replaceExistingStr) &&
        !isNullOrEmpty(existingFileModeStr)
      ) {
        // Check that we haven't had both the deprecated and new (existingFileMode)
        // parameters supplied.
        throw new IllegalStateException(
          String.format(
            "Only one of these parameters may be used, not both: %s, %s",
            PARAMETER_REPLACE_EXISTING,
            PARAMETER_EXISTING_FILE_MODE
          )
        );
      }

      if (!isNullOrEmpty(existingFileModeStr)) {
        params.setExistingFileMode(
          BulkImportParameters.ExistingFileMode.valueOf(existingFileModeStr)
        );
      } else {
        params.setReplaceExisting(
          PARAMETER_VALUE_REPLACE_EXISTING.equals(replaceExistingStr)
        );
      }
    }

    private void setDisableRules(final BulkImportParameters params) {
      final String disableRulesStr = getParamStringValue(
        PARAMETER_DISABLE_RULES
      );
      params.setDisableRulesService(
        !isNullOrEmpty(disableRulesStr) &&
        PARAMETER_VALUE_DISABLE_RULES.equals(disableRulesStr)
      );
    }

    private NodeRef getTargetNodeRef() throws FileNotFoundException {
      String targetNodeRefStr = getParamStringValue(PARAMETER_TARGET_NODEREF);
      String targetPath = getParamStringValue(PARAMETER_TARGET_PATH);
      return nodeRefCreator.fromNodeRefAndPath(targetNodeRefStr, targetPath);
    }

    private OptionalInt getOptionalPositiveInteger(final String paramName) {
      final String strValue = getParamStringValue(paramName);
      if (isNullOrEmpty(strValue)) {
        return OptionalInt.empty();
      }

      final Integer asInt = Ints.tryParse(strValue);
      if (asInt == null || asInt < 1) {
        throw new WebScriptException(
          "Error: parameter '" + paramName + "' must be an integer > 0."
        );
      }

      return OptionalInt.of(asInt);
    }

    private String getParamStringValue(String paramName) {
      Objects.requireNonNull(paramName);

      return paramsProvider.apply(paramName);
    }

    private boolean isNullOrEmpty(String str) {
      return str == null || str.trim().length() == 0;
    }

    @FunctionalInterface
    protected interface NodeRefCreator {
      NodeRef fromNodeRefAndPath(String nodeRef, String path)
        throws FileNotFoundException;
    }
  }
}
