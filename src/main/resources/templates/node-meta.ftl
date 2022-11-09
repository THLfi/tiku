[#ftl][#setting locale="fi"]
<!DOCTYPE html>
<html lang="${lang}">
[#function message code]
    [#if rc.getMessage(code)??]
        [#return rc.getMessage(code, "[${code}]") /]
    [/#if]
    [#return "- ${code} - " /]
[/#function]
[#macro label e][#if e?? && e.label??]${(e.label.getValue(lang)!"n/a")?json_string}[#else]???[/#if][/#macro]
[#macro breadcrumb e leaf = false]
  [#if e.parent??]
    [@breadcrumb e.parent /]
  [/#if]
  [#if leaf]
    <li class="breadcrumb-item active">[@label e /]</li>
  [#else]
    <li class="breadcrumb-item"><a href="${rc.contextPath}/${env}/${lang}/${subject}/${hydra}/${cube}/${e.surrogateId}">[@label e /]</a></li>
  [/#if]
[/#macro]
[#if nodes??]
[#list nodes as node]
<head>

  <title>[@label node /] - [#if cubeLabel??]${cubeLabel.getValue(lang)}[#else]n/a[/#if] - ${message("site.title")}</title>
  <link rel="stylesheet" href="${rc.contextPath}/css/bootstrap.min.css" />
  <link rel="stylesheet" href="${rc.contextPath}/fonts/source-sans-pro/2.0.10/source-sans-pro.css" />
  <link rel="stylesheet" href="${rc.contextPath}/css/style.css" />
  <link href="${rc.contextPath}/images/favicon.ico" rel="shortcut icon" type="image/x-icon" />

  <style>
    h1 { margin-left: 0px; border-bottom: 1px solid #ccc; }
    h2 { font-size: 24px; }
    dd { margin-left: 15px; margin-bottom: 15px;}
  </style>
  <!--[if lt IE 9]>
    <script src="${rc.contextPath}/js/html5shiv.js"></script>
    <script src="${rc.contextPath}/js/respond.min.js"></script>
    <![endif]-->
</head>
<body>
  <div class="container">

    <h1>[@label node /]</h1>
    <ul class="breadcrumb">
      [@breadcrumb node true /]
    </ul>

    [#if node.getProperty("meta:description")?? || node.getProperty("meta:comment")??]
      <h2>${message("meta.description")}</h2>
      [#if node.getProperty("meta:description")??]
        <p>
          ${node.getProperty("meta:description").getValue(lang)!}
        </p>
      [/#if]
      [#if node.getProperty("meta:comment")??]
        <p>
          ${node.getProperty("meta:comment").getValue(lang)!}
        </p>
      [/#if]
    [/#if]

    [#if node.children?size > 0]
    <h2>${message("meta.children")}</h2>
      <ul>
        [#list node.children as child]
        <li><a href="${rc.contextPath}/${env}/${lang}/${subject}/${hydra}/${cube}/${child.surrogateId}">[@label child /]</a></li>
        [/#list]
      </ul>
    [/#if]

    <h2>${message("meta.technicalmetadata")}</h2>
    <dl>
      [#--
        <dt>${message("meta.dimension")}</dt>
        <dd>[@label node.dimension /] / [@label node.level /]</dd>
        <dt>${message("meta.id")}<dt>
        <dd>${node.id}</dd>
      --]
      [#if node.code??]
      <dt>${message("meta.code")}</dt>
      <dd>${node.code}</dd>
      [/#if]
      <dt>${message("meta.uri")}</dt>
      <dd>${node.reference}</dd>
    </dl>

  </div>

</body>
[/#list]
[/#if]
</html>
