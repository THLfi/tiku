[#ftl]<!DOCTYPE html>
[#setting locale="fi"]
[#assign dimensionSeparator = '-' /]
[#assign nodeSeparator = '.' /]
[#function value val]
    [#return val /]
[/#function]

[#function message code]
    [#if rc.getMessage(code)??]
        [#return rc.getMessage(code, "[${code}]") /]
    [/#if]
    [#return "- ${code} - " /]
[/#function]

[#macro label e][#if e?? && e.label??]${e.label.getValue(lang)}[#else]???[/#if][/#macro]

[#include "cube/cube-dimension-dropdown.ftl" /]

<html>
    <head>
        <title>[#if cubeLabel??]${cubeLabel.getValue(lang)}[#else]n/a[/#if] - ${message("site.title")}</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">

        <link rel="stylesheet" href="${rc.contextPath}/resources/css/bootstrap.min.css" />
        <link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Source+Sans+Pro:400,400italic,600,600italic,700,700italic" />
        <link rel="stylesheet" href="${rc.contextPath}/resources/css/style.css?v=${buildTimestamp}" />

        <!--[if lt IE 9]>
          <script src="${rc.contextPath}/resources/js/html5shiv.js"></script>
          <script src="${rc.contextPath}/resources/js/respond.min.js"></script>
        <![endif]-->

    </head>
    <body>

    <div class="pivot-header">
        <h1>
          [#if cubeLabel??]${cubeLabel.getValue(lang)}[#else]n/a[/#if]
          [#if env != "prod"]
          <span class="environment">${env}</span>
          [/#if]
        </h1>
        <div class="logo">
            <img src="${rc.contextPath}/resources/img/thl_${uiLanguage!"fi"}.jpg">
        </div>
        <div id="languages">
          [#if supportedLanguages?? && supportedLanguages?size > 1]
            <ul>
            [#list supportedLanguages as x]
              [#if x != lang]
                <li><a href="${rc.contextPath}/${cubeRequest.getCubeUrl(x)}">${x}</a></li>
              [#else]
                <li class="active"><a href="${rc.contextPath}/${cubeRequest.cubeUrl}">${x}</a></li>
              [/#if]
            [/#list]
          [/#if]
          </ul>
        </div>
        [#include "cube/cube-form.ftl" /]
        <div class="toolbar-control">
            [#include "cube/cube-toolbar.ftl" /]
        </div>
    </div>

    <div class="pivot-body">
    <div class="pivot-sidebar">

         <button class="browser-toggle btn btn-default">
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="sr-only">${message("cube.options")}</span>
        </button>
         <ul class="tree-browser">

         </ul>

    </div>

    <div class="pivot-content">

        [#include "cube/cube-filter.ftl" /]

        [#if pivot.columns?size == 0 || pivot.rows?size == 0]
            [#include "cube/cube-manual.ftl" /]
            [#include "cube/cube-placeholder.ftl" /]
        [#else]
            [#if pivot.columnCount == 0 && pivot.rowCount == 0]
                <p class="alert alert-warning">
                    ${message("cube.error.no-data")}
                </p>
            [#else]
            	[#include "cube/cube-table.ftl" /]
            [/#if]
        [/#if]

        [#include "cube/cube-modal.ftl" /]
    </div>

    </div>

    <div class="pivot-footer">
        &copy; ${message("site.company")} ${.now?string("yyyy")}[#if isOpenData], ${message("site.license.cc")}[/#if]. ${message("cube.updated")} ${runDate?string("dd.MM.yyyy")}
    </div>

    <script src="${rc.contextPath}/resources/js/jquery.js"></script>
    <script src="${rc.contextPath}/resources/js/jquery-ui.js"></script>
    <script src="${rc.contextPath}/resources/js/jquery.ui.touch-punch.min.js"></script>
    <script src="${rc.contextPath}/resources/js/bootstrap.js"></script>
    <script>
        [#include "cube/cube-script.ftl" /]
    </script>
    <script src="${rc.contextPath}/resources/js/cube.js?v=${buildTimestamp}"></script>
    <script src="${rc.contextPath}/${dimensionsUrl}"></script>
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
