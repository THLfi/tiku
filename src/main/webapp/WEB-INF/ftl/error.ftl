[#ftl]<!DOCTYPE html>

[#if lang??]
[#else]
	[#assign lang="fi" /]
[/#if]
[#setting locale=lang]

[#function message code]
    [#if rc.getMessage(code)??]
        [#return rc.getMessage(code) /]
    [/#if]
    [#return "- ${code} - " /]
[/#function]


<html>
    <head>
        <title>${message("site.error")} - ${message("site.title")}</title>
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

    <header class="summary-header container">
        <h1>${message("site.error")}</h1>
        <div class="logo">
            <img src="${rc.contextPath}/resources/img/thl_${uiLanguage!"fi"}.jpg">
        </div>
    </header>

    <div class="stripe">
    </div>
    <div class="summary-body container">
   	 	<div class="summary-content">
			[#if "${status!}" == "404" || "${status!}" == "403" || "${status!}" == "-1000"]
				<h2>${message("site.error.title.${status}")}</h2>
				<p>${message("site.error.message.${status}")}</p>
			[#else]
	      <h2>${message("site.error.title")}</h2>
	     	<p>${message("site.error.message")}</p>
			[/#if]
      </div>
    </div>

    <footer>

     <div class="mega">
            <div class="container">
            <div class="row">


                <div class="col-sm-3">
                    <h2>${message("site.thl.services")}</h2>

                    <ul>
                    	<li><a href="http://www.sotkanet.fi">${message("site.sotkanet")}</a></li>
                        <li><a href="http://www.hyvinvointikompassi.fi">${message("site.hyvinvointikompassi")}</a></li>
                        <li><a href="http://www.thl.fi/tietokantaraportit">${message("site.tietokantaraportit")}</a></li>
                        <li><a href="http://www.terveytemme.fi">${message("site.terveytemme")}</a></li>
                    </ul>
                </div>

                <div class="col-sm-3">
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
                    href="http://www.thl.fi/[#if lang! != "fi"]${lang}/web/thlfi-${lang}[/#if]">
                         <img
                            src="http://www.thl.fi/thl-liferay-theme/images/thl_common/thl-logo-${uiLanguage!"fi"}.png"
                            title="${message("site.company")}"
                            alt="${message("site.company")}"
                            height="42" />
                </a>
                </div>
                <div class="col-sm-8">
                &copy; ${message("site.title")} ${.now?string("yyyy")}
                </div>
            </div>
            </div>
        </div>
    </footer>
    <script src="${rc.contextPath}/resources/js/jquery.js"></script>
    <script src="${rc.contextPath}/resources/js/bootstrap.js"></script>

    </body>
</html>
