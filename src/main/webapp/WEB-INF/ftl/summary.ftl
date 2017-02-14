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

[#macro show_filter_values presentation]

[/#macro]
[#macro summary_hierarchical_option option stages]
  [#if stages?seq_contains(option.level.id)]
    <option value="${option.surrogateId}" ${selected}>[@label option /]</option>
  [/#if]
  [#list option.children as child]
    [@summary_hierarchical_option child stages /]
  [/#list]
[/#macro]

[#macro summary_filter_form]

<form id="summary-form" class="form form-horizontal" method="GET" action="#">
[#list summary.selections as filter]
    [#assign dim = filter.dimensionEntity /]

    [#if !filter.visible]
      <input type="hidden" name="${filter.id}_0" value="${filter.selected[0].id}" />
    [#else]
      [#list filter.filterStages as filterStage]
        <div class="form-group">
          <label for="${filter.id}-${filterStage_index}">${filterStage.label.getValue(lang)}</label>
          [#if filter.searchable]
            <div class="dropdown">
              <div class="search-group">
                <input class="form-control search-control" />
                <span class="glyphicon glyphicon-search"></span>
              </div>
              <ul class="dropdown-menu">
              </ul>
            </div>
          [/#if]
          <select class="form-control" name="${filter.id}_${filterStage_index}" id="s-${filter.id}-${filterStage_index}" [#if filter.multiple]multiple[/#if]>
            [#if filterStage_index > 0]
              <option value=""></option>
            [/#if]
            [#list filterStage.options as option]
                [#assign selected]
                    [#if filterStage.selected??]
                      [#list filterStage.selected as s]
                        [#if s.id == option.id]
                          selected
                          [#break /]
                        [/#if]
                      [/#list]
                    [/#if]
                [/#assign]
                <option value="${option.surrogateId}" ${selected}>
                  [#if filter.isCompleteDimension][#list (0..option.level.index) as i]&nbsp;&nbsp;[/#list][/#if][@label option /]
                </option>
            [/#list]
          </select>
        </div>
      [/#list]
      <hr />
    [/#if]

[/#list]
[#list summary.drilledDimensions as dim]
    [#list summary.getDrilledNodes(dim) as dn]
        <input name="drill-${dim}" value="${dn.id}" type="hidden" />
    [/#list]
[/#list]
</form>
[/#macro]

[#macro export presentation type]
<div class="export btn-group dropdown" role="group" aria-label="...">
  <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-expanded="false">${message("cube.export")} <span class="caret"></span></button>
  <ul class="dropdown-menu" role="menu">
    <li class="csv-action" role="presentation">
      <a role="menuitem" href="${factTable}.csv?${presentation.dataUrl}">
        <span class="glyphicon glyphicon-file"></span>
        ${message("cube.export.csv")}
      </a>
    </li>
    <li class="xlsx-action" role="presentation">
      <a role="menuitem" href="${factTable}.xlsx?${presentation.dataUrl}">
        <span class="glyphicon glyphicon-file"></span>
        ${message("cube.export.xlsx")}
      </a>
    </li>
    [#if type == "image"]
    <li class="img-action" role="presentation">
      <a role="menuitem" href="#">
        <span class="glyphicon glyphicon-file"></span>
        ${message("cube.export.image")}
      </a>
    </li>
    [/#if]
  </ul>
</div>
[/#macro]

[#macro summary_presentations]
[#list summary.presentations as presentation]

    [#if presentation.groupSize > 0]
        [#if presentation.isFirst()]
        [#-- put a group into a single row --]
        <div class="row">
        [/#if]
        [#-- each presentation into their respective column --]
        <div class="col-sm-${12/presentation.groupSize}">
    [/#if]

    [#if "subtitle" = presentation.type]
        <h3>
            ${presentation.getContent(lang)}
        </h3>
    [#elseif "info" = presentation.type]
        <p>
            ${presentation.getContent(lang)}
        </p>
    [#elseif "bar" = presentation.type  || "barstacked" = presentation.type || "barstacked100" = presentation.type]
        [@show_filter_values presentation /]
        <div
            class="presentation bar"
            [#if presentation.min??]data-min="${presentation.min}"[/#if]
            [#if presentation.max??]data-max="${presentation.max}"[/#if]
            [#if "barstacked" = presentation.type]data-stacked="true" data-stacked="true"[/#if]
            [#if "barstacked100" = presentation.type]data-stacked="true" data-percent="true"[/#if]
            [#if presentation.showConfidenceInterval]data-ci="true"[/#if]
            [#if presentation.emphasizedNode??]data-em="[#list presentation.emphasizedNode as n][#if n_index>0],[/#if]${n.surrogateId}[/#list]"[/#if]
            data-ref="${factTable}.json?${presentation.dataUrl}"
            data-sort="${presentation.sortMode}">
            [@export presentation "image" /]
            <img src="${rc.contextPath}/resources/img/loading.gif" alt="loading"/>
            </div>
        <hr />
    [#elseif "column" = presentation.type || "columnstacked" = presentation.type || "columnstacked100" = presentation.type]
        [@show_filter_values presentation /]
        <div
            class="presentation column"
            [#if "columnstacked" = presentation.type]data-stacked="true"[/#if]
            [#if "columnstacked100" = presentation.type]data-stacked="true" data-percent="true"[/#if]
            [#if presentation.min??]data-min="${presentation.min}"[/#if]
            [#if presentation.max??]data-max="${presentation.max}"[/#if]
            [#if presentation.showConfidenceInterval]data-ci="true"[/#if]
            [#if presentation.emphasizedNode??]data-em="[#list presentation.emphasizedNode as n][#if n_index>0],[/#if]${n.surrogateId}[/#list]"[/#if]
            data-ref="${factTable}.json?${presentation.dataUrl}"
            data-sort="${presentation.sortMode}">
            [@export presentation "image" /]
            <img src="${rc.contextPath}/resources/img/loading.gif" alt="loading"/>
        </div>
        <hr />
    [#elseif "line" = presentation.type]
        [@show_filter_values presentation /]
        <div
            id = "${presentation.id}"
            class="presentation line"
            [#if presentation.min??]data-min="${presentation.min}"[/#if]
            [#if presentation.max??]data-max="${presentation.max}"[/#if]
            data-ref="${factTable}.json?${presentation.dataUrl}"
            data-sort="${presentation.sortMode}"
            [#if presentation.showConfidenceInterval]data-ci="true"[/#if]>
            [@export presentation "image" /]
            <img src="${rc.contextPath}/resources/img/loading.gif" alt="loading"/>
            </div>
        <hr />
    [#elseif "pie" = presentation.type]
        [@show_filter_values presentation /]
        <div
            class="presentation pie"
            data-ref="${factTable}.json?${presentation.dataUrl}">
            [@export presentation "image" /]
            <img src="${rc.contextPath}/resources/img/loading.gif" alt="loading"/>
        </div>
        <hr />
    [#elseif "gauge" = presentation.type]
        [@show_filter_values presentation /]
        <div
            class="presentation gauge"
            [#if presentation.min??]data-min="${presentation.min}"[/#if]
            [#if presentation.max??]data-max="${presentation.max}"[/#if]
            [#if presentation.palette??]data-palette="${presentation.palette}"[/#if]
            data-ref="${factTable}.json?${presentation.dataUrl}">
            [@export presentation "image" /]
            <img src="${rc.contextPath}/resources/img/loading.gif" alt="loading"/>
        </div>
        <hr />
    [#elseif "table" = presentation.type]
        [@show_filter_values presentation /]
        <div
            class="presentation table"
            data-column-count="${presentation.columns?size}"
            data-columns="[#list presentation.columns as column]${column.id!}[/#list]"
            data-row-count="${presentation.rows?size}"
            data-rows="[#list presentation.rows as row]${row.id!}[/#list]"
            data-ref="${factTable}.json?${presentation.dataUrl}">
            [@export presentation "table" /]
            <img src="${rc.contextPath}/resources/img/loading.gif" alt="loading"/>
        </div>
        <hr />
    [#else]

    <h3>${presentation.type}</h3>
    <p>${presentation.content}</p>
    [/#if]


    [#if presentation.groupSize > 0]
        [#-- close presentation groups --]
        </div>
        [#if presentation.isLast()]
        [#-- close row --]
        </div>
        [/#if]
    [/#if]

[/#list]
[/#macro]
[#macro breadcrumbs drillNode isLeaf = true]
    [#if drillNode.parent??]
        [#if drillNode.level.id != summary.getMaximumLevelId(drillNode.dimension.id)]
            [@breadcrumbs drillNode.parent false /]
        [/#if]
    [/#if]
    <li>
        [#if isLeaf]
            [@label drillNode /]
        [#else]
            <a href="#" data-node="${drillNode.surrogateId}" data-dim="${drillNode.dimension.id}">
                [@label drillNode /]
            </a>
        [/#if]
    </li>
[/#macro]
[#macro label e][#if e?? && e.label??]${e.label.getValue(lang)} [#else]???[/#if][/#macro]

<html>
    <head>
        <title>${summary.title.getValue(lang)} - ${factName.getValue(lang)} -  ${message("site.title")}</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">


        <link rel="stylesheet" href="${rc.contextPath}/resources/css/bootstrap.min.css" />
        <link rel="stylesheet"
    href="https://fonts.googleapis.com/css?family=Source+Sans+Pro:400,400italic,600,600italic,700,700italic" />
        <link rel="stylesheet" href="${rc.contextPath}/resources/css/style.css" />
        <link rel="stylesheet" href="${rc.contextPath}/resources/css/summary.css" />

        <!--[if lt IE 9]>
          <script src="${rc.contextPath}/resources/js/html5shiv.js"></script>
          <script src="${rc.contextPath}/resources/js/respond.min.js"></script>
        <![endif]-->

    </head>
    <body>

    <header class="summary-header container-fluid">
        <div class="logo">
            <img src="${rc.contextPath}/resources/img/thl_${uiLanguage!"fi"}.jpg">
        </div>
        <div class="col-xs-10 col-md-9">
            <h1>
              ${summary.subject.getValue(lang)}
              [#if env != "prod"]
              <span class="environment">${env}</span>
              [/#if]
            </h1>
        </div>

        <div class="clearfix"></div>
    </header>
    <div id="languages">
      [#if supportedLanguages?? && supportedLanguages?size > 1]
        <ul>
        [#list supportedLanguages as x]
          [#if x != lang]
            <li><a href="${rc.contextPath}/${cubeRequest.getSummaryUrl(x)}">${x}</a></li>
          [#else]
            <li class="active"><a href="${rc.contextPath}/${cubeRequest.summaryUrl}">${x}</a></li>
          [/#if]
        [/#list]
      [/#if]
      </ul>
    </div>

    <div class="stripe">
      <div>

      [#if requireLogin]
      <div class="btn-group" role="group" aria-label="${message("site.logout")}">
          <form class="form" method="POST" action="${rc.contextPath}/${summaryRequest.summaryUrl}/logout">
              <button type="submit" class="btn btn-default">${message("site.logout")}</button>
          </form>
      </div>
      [/#if]
      <div class="btn-group pull-right" role="group" aria-label="...">
          ${message("site.help")}
      </div>
      [#--<a href="#">pdf</a>--]
    </div>
    </div>

    <div class="summary-body container-fluid">
        <div class="summary-content">

            <div class="col-sm-3">
                [@summary_filter_form /]
            </div>

            <div class="col-sm-9">

                [#list drillNodes as drillNode]
                <ul class="breadcrumb">
                    [@breadcrumbs drillNode /]
                </ul>
                [/#list]

                <div class="content">
                  <h2>${summary.title.getValue(lang)}</h2>
                  [#if summary.note.getValue(lang)! == "n/a"]
                  [#else]
                  <p>${summary.note.getValue(lang)!}</p>
                  [/#if]
                  <p><a href="${summary.link.getValue(lang)!}">${message("summary.more")}</a></p>

                  [@summary_presentations /]
                </div>
            </div>

            <div class="clearfix"></div>
        </div>
        <div class="clearfix"></div>
    </div>



    <footer>
        <div class="mega">
            <div class="container">
            <div class="row">
                <nav class="col-sm-3">
                    <h2>${message("site.content")}</h2>
                    <ul>
                        [#list reports as report]
                        <li><a href="${rc.contextPath}/${summaryRequest.env}/${lang}/${report.subject}/${report.hydra}/summary_${report.fact}">${report.title.getValue(lang)}</a></li>
                        [/#list]
                    </ul>
                </nav>

                <div class="col-sm-3">
                    <h2>${message("site.thl.services")}</h2>

                    <ul>
                        <li><a href="https://www.sotkanet.fi">${message("site.sotkanet")}</a></li>
                        <li><a href="http://www.hyvinvointikompassi.fi">${message("site.hyvinvointikompassi")}</a></li>
                        <li><a href="http://www.terveytemme.fi">${message("site.terveytemme")}</a></li>
                        <li>${message("site.tietokantaraportit")}</li>
                    </ul>
                </div>

                <div class="col-sm-offset-3 col-sm-3">
                    <h2>${message("site.contact")}</h2>
                    <p>
                        ${message("site.contact.information")}
                    </p>
                </div>
            </div>
            </div>
        </div>

        <div class="license">
            <div class="container">
            <div class="row">
                <div class="col-sm-4">
                <a title="${message("site.company")}"
                    href="http://www.thl.fi/[#if lang!="fi"]${lang}/web/thlfi-${lang}[/#if]">
                         <img
                            src="${rc.contextPath}/resources/img/thl_${uiLanguage!"fi"}.jpg"
                            title="${message("site.company")}"
                            alt="${message("site.company")}"
                            height="42" />
                </a>
                </div>
                <div class="col-sm-8">
                &copy; ${message("site.company")} ${.now?string("yyyy")}[#if isOpenData], ${message("site.license.cc")}[/#if]. ${message("cube.updated")} ${runDate?string("dd.MM.yyyy")}
                </div>
            </div>
            </div>
        </div>
    </footer>

    <script>
        [#include "summary-script.ftl" /]
    </script>

    <script src="${rc.contextPath}/resources/js/jquery.js"></script>
    <script src="${rc.contextPath}/resources/js/jquery-ui.js"></script>
    <script src="${rc.contextPath}/resources/js/jquery.ui.touch-punch.min.js"></script>
    <script src="${rc.contextPath}/resources/js/bootstrap.js"></script>
    <script src="${rc.contextPath}/resources/js/d3.min.js"></script>
    <script src="${rc.contextPath}/resources/js/json-stat.js"></script>
    [#--<script src="${rc.contextPath}/resources/js/jspdf.min.js"></script>--]
    <script src="${rc.contextPath}/resources/js/summary.js"></script>

    <script>
    var labels = {},
        dimensionData = {},
        isDrillEnabled = [#if summary.isDrillEnabled()]true[#else]false[/#if];
    [#list summary.nodes as n]
    [#if n??]
    labels[${n.surrogateId!"-1"}] = '[#if n?? && n.label??]${n.label.getValue(lang)?js_string} [#else]???[/#if]';
    dimensionData[${n.surrogateId}] = {
        hasChild: [#if n.children?size>0]true[#else]false[/#if],
        dim: '${n.dimension.id}'
    };
    [/#if]
    [/#list]
    </script>

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
