[#ftl]
[#macro formatDuration durationInNs]
  [@compress single_line=true]
    [#assign days         = (durationInNs / (1000 * 1000 * 1000 * 60 * 60 * 24))?floor]
    [#assign hours        = (durationInNs / (1000 * 1000 * 1000 * 60 * 60))?floor % 24]
    [#assign minutes      = (durationInNs / (1000 * 1000 * 1000 * 60))?floor % 60]
    [#assign seconds      = (durationInNs / (1000 * 1000 * 1000))?floor % 60]
    [#assign milliseconds = (durationInNs / (1000 * 1000)) % 1000]
    [#assign microseconds = (durationInNs / (1000)) % 1000]
    ${days}d ${hours}h ${minutes}m ${seconds}s ${milliseconds}.${microseconds}ms
  [/@compress]
[/#macro]
[#macro formatBytes bytes]
  [@compress single_line=true]
    [#if bytes   > 1000000000]${(bytes / 1000000000)?string("#,##0.00")}GB
    [#elseif bytes > 1000000]${(bytes / 1000000)?string("#,##0.00")}MB
    [#elseif bytes > 1000]${(bytes / 1000)?string("#,##0.00")}kB
    [#else]${bytes?string("#,##0")}B
    [/#if]
  [/@compress]
[/#macro]
[#assign refreshIntervalInSeconds = 5]
<!DOCTYPE HTML>
<html>
<head>
  <title>Bulk Filesystem Import Status</title>
  <link rel="stylesheet" href="${url.context}/css/main.css" type="text/css"/>
[#if importStatus.inProgress()]
  <meta http-equiv="refresh" content="${refreshIntervalInSeconds}" />
[/#if]
</head>
<body>
  <table>
    <tr>
      <td><img src="${url.context}/images/logo/AlfrescoLogo32.png" alt="Alfresco" /></td>
      <td><nobr>Bulk Filesystem Import Tool Status</nobr></td>
    </tr>
    <tr><td><td>Alfresco ${server.edition} v${server.version}
  </table>
  <blockquote>
    <p>
    <table border="1" cellspacing="0" cellpadding="1" width="80%">
      <tr>
        <td colspan="2"><strong>General Statistics</strong></td>
      </tr>
      <tr>
        <td width="25%">Current status:</td>
        <td width="75%">
[#if importStatus.inProgress()]
          <span style="color:red">In progress</span>
[#else]
          <span style="color:green">Idle</span>
[/#if]
        </td>
      </tr>
      <tr>
        <td>Successful:</td>
[#if importStatus.inProgress() || !importStatus.endDate??]
        <td>n/a</td>
[#elseif importStatus.lastExceptionAsString??]
        <td style="color:red">No</td>
[#else]
        <td style="color:green">Yes</td>
[/#if]
      </tr>
      <tr>
        <td>Batch Size:</td>
        <td>${importStatus.batchSize}</td>      
      </tr>
      <tr>
        <td>Number of threads:</td>
        <td>${importStatus.numThreads}</td>      
      </tr>
      <tr>
        <td>Source Directory:</td>
        <td>
[#if importStatus.sourceDirectory??]
          ${importStatus.sourceDirectory}
[#else]
          n/a
[/#if]
        </td>
      </tr>
      <tr>
        <td>Target Space:</td>
        <td>
[#if importStatus.targetSpace??]
          ${importStatus.targetSpace}
[#else]
          n/a
[/#if]
        </td>
      </tr>
      <tr>
        <td>Start Date:</td>
        <td>
[#if importStatus.startDate??]
          ${importStatus.startDate?datetime?string("yyyy-MM-dd hh:mm:ss.SSSaa")}
[#else]
          n/a
[/#if]
        </td>
      </tr>
      <tr>
        <td>End Date:</td>
        <td>
[#if importStatus.endDate??]
          ${importStatus.endDate?datetime?string("yyyy-MM-dd hh:mm:ss.SSSaa")}</td>
[#else]
          n/a
[/#if]
        </td>
      </tr>
      <tr>
        <td>
[#if importStatus.inProgress()]
          Elapsed Time:
[#else]
          Duration:
[/#if]
        </td>
        <td>
[#if importStatus.durationInNs??]
          [@formatDuration durationInNs = importStatus.durationInNs/]
[#else]
          n/a
[/#if]
        </td>
      </tr>
      <tr>
        <td>Number of Completed Batches:</td>
        <td>${importStatus.numberOfBatchesCompleted}</td>
      </tr>
      <tr>
        <td colspan="2"><strong>Source (read) Statistics</strong></td>
      </tr>
<!--
      <tr>
        <td>Last folder or file processed:</td>
        <td>${importStatus.currentFileBeingProcessed!"n/a"}</td>
      </tr>
-->
      <tr>
        <td>Scanned:</td>
        <td>
          <table border="1" cellspacing="0" cellpadding="1">
            <tr>
              <td>Folders</td>
              <td>Files</td>
              <td>Unreadable</td>
            </tr>
            <tr>
              <td>${importStatus.numberOfFoldersScanned}</td>
              <td>${importStatus.numberOfFilesScanned}</td>
              <td>${importStatus.numberOfUnreadableEntries}</td>
            </tr>
          </table>
        </td>
      </tr>
      <tr>
        <td>Read:</td>
        <td>
          <table border="1" cellspacing="0" cellpadding="1">
            <tr>
              <td>Content</td>
              <td>Metadata</td>
              <td>Content Versions</td>
              <td>Metadata Versions</td>
            </tr>
            <tr>
              <td>${importStatus.numberOfContentFilesRead} ([@formatBytes importStatus.numberOfContentBytesRead/])</td>
              <td>${importStatus.numberOfMetadataFilesRead} ([@formatBytes importStatus.numberOfMetadataBytesRead/])</td>
              <td>${importStatus.numberOfContentVersionFilesRead} ([@formatBytes importStatus.numberOfContentVersionBytesRead/])</td>
              <td>${importStatus.numberOfMetadataVersionFilesRead} ([@formatBytes importStatus.numberOfMetadataVersionBytesRead/])</td>
            </tr>
            </tr>
          </table>
        </td>
      </tr>
      <tr>
        <td>Throughput:</td>
        <td>
          [#if importStatus.durationInNs?? && importStatus.durationInNs > 0]
          [#if importStatus.entriesScannedPerSecond??]
          ${importStatus.entriesScannedPerSecond} entries scanned / sec<br/>
          [/#if]
          [#if importStatus.filesReadPerSecond??]
          ${importStatus.filesReadPerSecond} files read / sec<br/>
          [/#if]
          [#if importStatus.bytesReadPerSecond??]
          [@formatBytes importStatus.bytesReadPerSecond /] / sec
          [/#if]
          [#else]
          n/a
          [/#if]
        </td>
      </tr>
      <tr>
        <td colspan="2"><strong>Target (write) Statistics</strong></td>
      </tr>
      <tr>
        <td>Space Nodes:</td>
        <td>
          <table border="1" cellspacing="0" cellpadding="1">
            <tr>
              <td># Created</td>
              <td># Replaced</td>
              <td># Skipped</td>
              <td># Properties</td>
            </tr>
            <tr>
              <td>${importStatus.numberOfSpaceNodesCreated}</td>
              <td>${importStatus.numberOfSpaceNodesReplaced}</td>
              <td>${importStatus.numberOfSpaceNodesSkipped}</td>
              <td>${importStatus.numberOfSpacePropertiesWritten}</td>
            </tr>
          </table>
        </td>
      </tr>
      <tr>
        <td>Content Nodes:</td>
        <td>
          <table border="1" cellspacing="0" cellpadding="1">
            <tr>
              <td># Created</td>
              <td># Replaced</td>
              <td># Skipped</td>
              <td>Data Written</td>
              <td># Properties</td>
            </tr>
            <tr>
              <td>${importStatus.numberOfContentNodesCreated}</td>
              <td>${importStatus.numberOfContentNodesReplaced}</td>
              <td>${importStatus.numberOfContentNodesSkipped}</td>
              <td>[@formatBytes importStatus.numberOfContentBytesWritten/]</td>
              <td>${importStatus.numberOfContentPropertiesWritten}</td>
            </tr>
          </table>
        </td>
      </tr>
      <tr>
        <td>Content Versions:</td>
        <td>
          <table border="1" cellspacing="0" cellpadding="1">
            <tr>
              <td># Created</td>
              <td>Data Written</td>
              <td># Properties</td>
            </tr>
            </tr>
              <td>${importStatus.numberOfContentVersionsCreated}</td>
              <td>[@formatBytes importStatus.numberOfContentVersionBytesWritten/]</td>
              <td>${importStatus.numberOfContentVersionPropertiesWritten}</td>
            </tr>
          </table>
        </td>
      <tr>
      <tr>
        <td>Throughput (write):</td>
        <td>
          [#if importStatus.durationInNs?? && importStatus.durationInNs > 0]
          [#if importStatus.nodesCreatedPerSecond??]
          ${importStatus.nodesCreatedPerSecond?string("#0")} nodes / sec<br/>
          [/#if]
          [#if importStatus.bytesWrittenPerSecond??]
          [@formatBytes importStatus.bytesWrittenPerSecond /] / sec
          [/#if]
          [#else]
          n/a
          [/#if]
        </td>
      </tr>
[#if importStatus.lastExceptionAsString??]
      <tr>
        <td colspan="2"><strong>Error Information From Last Run</strong></td>
      </tr>
      <tr>
        <td>File that failed:</td>
        <td>${importStatus.currentFileBeingProcessed!"n/a"}</td>
      </tr>
      <tr>
        <td>Exception:</td>
        <td><pre>${importStatus.lastExceptionAsString}</pre></td>
      </tr>
[/#if]
    </table>
    </p>
    <p>
[#if importStatus.inProgress()]
    This page will automatically refresh in <span id="countdownTimer">${refreshIntervalInSeconds}</span> seconds.
[#else]
    <a href="${url.serviceContext}/bulkfsimport">Initiate another import</a><br><br>
    [#if isEnterprise]
        <a href="${url.serviceContext}/bulkfsimport/inplace">Initiate another in-place import</a>
    [/#if]
[/#if]
    </p>
  </blockquote>
  <script type="text/javascript">
    var seconds = ${refreshIntervalInSeconds} + 1;

    function display()
    {
      seconds -= 1;

      var countdownTimerElement = document.getElementById("countdownTimer");
      if(countdownTimerElement)
      {
         countdownTimerElement.textContent = seconds;
      }
      setTimeout("display()", 1000);
    }

    display()
  </script>
</body>
</html>
