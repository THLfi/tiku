[#ftl]

[#if lang??]
[#else]
  [#assign lang = "fi" /]
[/#if]
[#function message code]
    [#if rc.getMessage(code)??]
        [#return rc.getMessage(code, "[${code}]") /]
    [/#if]
    [#return "- ${code} - " /]
[/#function]

[#macro footer]
  <footer class="default">
    <div class="container-fluid">
      <div class="row">
        <div class="col-xs-12">
          <a title="${message("site.company")}" href="http://thl.fi/[#if lang!="fi"]${lang}/web/thlfi-${lang}[/#if]">
   	        <img class="footer-logo" src="${rc.contextPath}/resources/img/THL.svg"/>
   	      </a>
        </div>
        [#-- This is useful when we have destination for the links
        <div class="col-xs-3 col-xs-offset-6">
          <a href="">${message("site.dataPrivacyDocument")}</a>
          <a href="">${message("site.accessibilityDocument")}</a>
        </div>
         --]
      </div>
    </div>
  </footer>
[/#macro]

[#macro amor_page title="Ympäristössä olevat aineistot"]
<!DOCTYPE html>
<html>
    <head>
        <title>THL TIKU: AMOR</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">

        <link rel="stylesheet" href="${rc.contextPath}/resources/css/bootstrap.min.css" />
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css" />
        <link rel="stylesheet" href="${rc.contextPath}/resources/css/style.css" />
        <link rel="stylesheet" href="${rc.contextPath}/resources/css/summary.css" />
        <style>
          .table td, table.th {text-align: left;}
          .summary-body {padding-top: 20px;}
        </style>
        <!--[if lt IE 9]>
          <script src="${rc.contextPath}/resources/js/html5shiv.js"></script>
          <script src="${rc.contextPath}/resources/js/respond.min.js"></script>
        <![endif]-->

        <script src="${rc.contextPath}/resources/js/jquery.js"></script>
        <script src="${rc.contextPath}/resources/js/jquery-ui.js"></script>
        <script src="${rc.contextPath}/resources/js/jquery.ui.touch-punch.min.js"></script>
        <script src="${rc.contextPath}/resources/js/bootstrap.js"></script>
        <script>
            $(document).ready(function() {
                ${javascript!}
            });
        </script>
    </head>
    <body class="common">

      <header class="summary-header container-fluid">
          <div class="logo">
              <img src="${rc.contextPath}/resources/img/THL_tunnus_pitka_${lang}_RGB.svg">
          </div>
          <div class="col-xs-10 col-md-9">
              <h1>
                ${title}
                [#if env?? && env != "prod"]
                <span class="environment">${env}</span>
                [/#if]
              </h1>

          </div>
          <div class="clearfix"></div>
      </header>

      <div class="stripe"></div>

      <div class="summary-body container">

        [#if showRestrictedView??]
        [#else]
        <ol class="breadcrumb">
            ${breadcrumbs!}
        </ol>
        [/#if]
        [#nested /]
      </div>

	  [@footer/]

      <script>
      (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
        (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
      m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
      })(window,document,'script','https://www.google-analytics.com/analytics.js','ga');

        ga('create', 'UA-20028405-1', 'auto');
        ga('send', 'pageview');

      </script>
    </body>
</html>
[/#macro]
