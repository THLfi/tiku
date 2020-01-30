[#ftl]
<div>
    <div class="btn-group dropdown" role="group" aria-label="...">	  
      <button type="button" class="btn btn-default dropdown-toggle small-button" data-toggle="dropdown" aria-expanded="false">	            
	      <span>${message("cube.options")}</span>         
	      <span class="caret"></span>
      </button>	  
      <ul class="dropdown-menu" role="menu">
      <li class="reset-action" role="presentation"><a role="menuitem"><span class="glyphicon glyphicon-refresh"></span> ${message("cube.reset")}</a></li>
      <li class="transpose-action" role="presentation"><a role="menuitem"><span class="glyphicon glyphicon-resize-full"></span> ${message("cube.transpose")}</a></li>
      <li class="hide-zero-action" role="presentation"><a role="menuitem"><span class="glyphicon"></span>  [#if RequestParameters.fz??]${message("cube.filter-zero.off")}[#else]${message("cube.filter-zero.on")}[/#if]</a></li>
      <li class="hide-empty-action" [#if RequestParameters.fz??]disabled[/#if] role="presentation"><a role="menuitem"><span class="glyphicon"></span> [#if RequestParameters.fo??]${message("cube.filter-empty.off")}[#else]${message("cube.filter-empty.on")}[/#if]</a></li>
      <li class="show-codes-action" role="presentation"><a role="menuitem"><span class="glyphicon"></span> [#if RequestParameters.sc??]${message("cube.codes.off")}[#else]${message("cube.codes.on")}[/#if]</a></li>

      [#if views?size > 0]
         <li role="separator" class="divider"></li>
         [#list views as view]
         <li role="presentation"><a role="menuitem" href="?${view.url}">${view.label.getValue(lang)!"n/a"}</a></li>
         [/#list]
      [/#if]

      </ul>

    </div>
    
    <div class="btn-group dropdown" role="group" aria-label="...">

      <button type="button" class="btn btn-default dropdown-toggle small-button" data-toggle="dropdown" aria-expanded="false">	    
	      
	      <i class="fa fa-download"></i>  	
	     <span class="hide-xs">
		     ${message("cube.export")}
	     </span>   
	     <span class="hide-xs caret"></span> 
      </button>

      <ul class="dropdown-menu" role="menu">
      [#assign reqParameters][#compress]
        [#list rowParams as r]row=${r}&[/#list]
        [#list colParams as r]column=${r}&[/#list]
        [#list filters as r]filter=${r.dimension.id}-${r.surrogateId}&[/#list]
        [#if RequestParameters.fo??]&fo=1[/#if]
        [#if RequestParameters.fz??]&fz=1[/#if]
        [#if RequestParameters.sc??]&sc=1[/#if]
        [#if RequestParameters.sort??]&sort=${RequestParameters.sort}[/#if]
        [#if RequestParameters.mode??]&mode=${RequestParameters.mode}[/#if]

        [/#compress][/#assign]
        [#assign reqParameters=reqParameters?replace("\\n|\\r", "", "rm") /]

        <li class="csv-action" role="presentation">
          <a role="menuitem" href="${target}.csv?${reqParameters}">
            <span class="glyphicon glyphicon-file"></span>
            ${message("cube.export.csv")}
          </a>
        </li>
        <li class="xlsx-action" role="presentation">
          <a role="menuitem" href="${target}.xlsx?${reqParameters}">
            <span class="glyphicon glyphicon-file"></span>
            ${message("cube.export.xlsx")}
          </a>
        </li>
        <li class="pdf-action" role="presentation">
          <a role="menuitem" href="${target}.pdf?${reqParameters}">
          <span class="glyphicon glyphicon-file"></span>
          ${message("cube.export.pdf")}
          </a>
        </li>

      </ul>

    </div>
    [#if backUrl??]
     <div class="btn-group dropdown" role="group" aria-label="...">
       <div class="btn btn-default small-button" >
          <a class="btn btn-default small-button" href="${rc.contextPath}/${backUrl}">
         	<i class=" fa fa-th"></i> 
			<span class="hide-xs">${message("site.changeMaterial")}</span>		
           </a>
       </div></div>
      [/#if]
     <div class="btn-group pull-right  vlcube"></div>
    [#if metaLink??]
    <div class="btn-group pull-right" role="group" aria-label="...">
      <a class="btn btn-default" target="_blank" href="${metaLink.getValue(lang)}">
        <span class="glyphicon glyphicon-info-sign"></span>
        <span class="hide-xs">${message("summary.more")}</span>
      </a>
    </div>
    [/#if]

    <div class="btn-group pull-right" role="group" aria-label="...">
        <a href="${message("site.help.url")}" target="_blank" type="submit" class="btn btn-default">
          <span class="glyphicon glyphicon-question-sign"></span>
          <span class="hide-xs">${message("site.help")}</span>
        </a>
    </div>
    

    [#if requireLogin]
     <div class="btn-group dropdown pull-right" role="group" aria-label="...">
        <form class="form" method="POST" action="${target}/logout">
            <button type="submit" class="btn btn-default">
              <span class="glyphicon glyphicon-log-out"></span>
              <span class="hide-xs">${message("site.logout")}</span>
            </button>
        </form>
    </div>
    [/#if]
</div>
</div>
