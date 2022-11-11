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
  <footer class="default" role="contentinfo">
    <div class="container-fluid">
        <div class="col-xs-12">
          <a title="${message("site.company")}" href="http://thl.fi/[#if lang!="fi"]${lang}/web/thlfi-${lang}[/#if]">
   	        <img class="footer-logo" src="${rc.contextPath}/images/THL.svg"/>
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

[#macro amor_page title=message("site.common.title") pagename=message("site.common.pagename")]
<!DOCTYPE html>
<html>
    <head>
        <title>${pagename}</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">

        <link rel="stylesheet" href="${rc.contextPath}/css/bootstrap.min.css" />
        <link rel="stylesheet" href="${rc.contextPath}/css/fontawesome.min.css" />
        <link rel="stylesheet" href="${rc.contextPath}/css/solid.min.css" />
        <link rel="stylesheet" href="${rc.contextPath}/css/style.css" />
        <link rel="stylesheet" href="${rc.contextPath}/css/summary.css" />
        <link href="${rc.contextPath}/images/favicon.ico" rel="shortcut icon" type="image/x-icon" />
        <link rel="stylesheet" href="${rc.contextPath}/css/common.css" />
        <!--[if lt IE 9]>
          <script src="${rc.contextPath}/js/html5shiv.js"></script>
          <script src="${rc.contextPath}/js/respond.min.js"></script>
        <![endif]-->

    </head>
    <body class="common">

      <header role="banner" class="summary-header container-fluid">
          <div class="logo">
              <img src="${rc.contextPath}/images/THL_tunnus_pitka_${lang}_RGB.svg">
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

      <div class="stripe">
          <div id="languages" role="navigation" aria-labelledby="languages">
              [#if languages?? && languages?size > 1]
                  <ul>
                      <li><div class="hide-xl btn-group vl"></div></li>
                      [#list languages as x]
                          [#if x != lang]
                              <li><a href="${rc.contextPath}/${env}/${x}/${subject}/">${x}</a></li>
                          [#else]
                              <li class="active"><a href="${rc.contextPath}/${env}/${x}/${subject}/">${x}</a></li>
                          [/#if]
                      [/#list]
                  </ul>
              [/#if]
          </div>
      </div>

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

    </body>
</html>
[/#macro]
