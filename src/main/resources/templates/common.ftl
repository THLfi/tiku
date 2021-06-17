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
   	        <img class="footer-logo" src="${resourceUrl}/images/THL.svg"/>
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

        <link rel="stylesheet" href="${resourceUrl}/css/bootstrap.min.css" />
        <link rel="stylesheet" href="${resourceUrl}/webjars/font-awesome/4.7.0/css/font-awesome.min.css" />
        <link rel="stylesheet" href="${rc.contextPath}/css/style.css" />
        <link rel="stylesheet" href="${rc.contextPath}/css/summary.css" />
        <link href="${resourceUrl}/images/favicon.ico" rel="shortcut icon" type="image/x-icon" />
        <link rel="stylesheet" href="${rc.contextPath}/css/common.css" />
        <!--[if lt IE 9]>
          <script src="${resourceUrl}/js/html5shiv.js"></script>
          <script src="${resourceUrl}/js/respond.min.js"></script>
        <![endif]-->

        <script src="${resourceUrl}/js/jquery.js"></script>
        <script src="${resourceUrl}/js/jquery-ui.js"></script>
        <script src="${resourceUrl}/js/jquery.ui.touch-punch.min.js"></script>
        <script src="${resourceUrl}/js/bootstrap.js"></script>

    </head>
    <body class="common">

      <header class="summary-header container-fluid">
          <div class="logo">
              <img src="${resourceUrl}/images/THL_tunnus_pitka_${lang}_RGB.svg">
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

      <script src="${rc.contextPath}/js/google-analytics.js"></script>
    </body>
</html>
[/#macro]
