<html>
<head>
   <title>Upload success</title>
</head>
<body>
<#if (args.successCallback?exists)>
   <script type="text/javascript">
      ${args.successCallback}.call(${args.successScope},
      {
         nodeRef: "${document.nodeRef}",
         fileName: "${document.name}"
      });
   </script>
</#if>
</body>
</html>