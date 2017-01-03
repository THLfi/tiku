[#ftl]

[#macro dimDropdown type node index maxIndex]
<div class="dropdown" data-level="${index}">
  <button class="btn btn-xs btn-default" id="dLabel" type="button" data-toggle="dropdown" aria-haspopup="true" role="button" aria-expanded="false">
    <span class="glyphicon glyphicon-cog"></span>
  </button>
  <ul class="dropdown-menu ${type}" role="menu" aria-labelledby="dLabel">
    <li>
        <a href="#" data-ref="${index}" class="remove">
            <span class="glyphicon glyphicon-remove-sign"></span> ${message("cube.dimension.remove")}
        </a>
    </li>
    <li>
        <a href="#" data-ref="${index}" class="select-subset">
            <span class="glyphicon glyphicon-edit"></span> ${message("cube.dimension.customize")}
        </a>
    </li>

    [#if node.parent?? && ((type == "row" && !query.isRowSubset(index)) || (type=="column" && !query.isColumnSubset(index))) ]
    <li>
        <a href="#" data-ref="${index}" value-ref="${node.dimension.id}-${node.parent.surrogateId}[#if (type=="row" && rowParams[index]?ends_with("L")) || (type=="column" && colParams[index]?ends_with("L")) ]L[/#if]" class="drill-up">
            <span class="glyphicon glyphicon-arrow-up"></span> ${message("cube.dimension.drill-up")}
        </a>
    </li>
    [/#if]

    [#if ((type == "row" && !query.isRowSubset(index)) || (type=="column" && !query.isColumnSubset(index)))]
      <li>
          <a href="#" data-index="${index}" data-ref="[#if node.children?size > 0]${node.children[0].surrogateId}[#else]${node.surrogateId}[/#if]" class="expand">
              <span class="glyphicon glyphicon-fullscreen"></span> ${message("cube.dimension.show-level")}
          </a>
      </li>

      [#if node.parent?? && node.level.childLevel?? && node.level.childLevel.nodes?size > 0]
      <li>
          <a href="#" data-ref="${node.level.childLevel.nodes[0].surrogateId}" class="add-level">
              <span class="glyphicon [#if type == "row"]glyphicon-arrow-right[#else]glyphicon-arrow-down[/#if]"></span> ${message("cube.dimension.expand")}
          </a>
      </li>
      [#elseif node.level.childLevel?? && node.level.childLevel.childLevel??  && node.level.childLevel.nodes?size > 0]
      <li>
          <a href="#" data-ref="${node.level.childLevel.childLevel.nodes[0].surrogateId}" class="add-level">
              <span class="glyphicon [#if type == "row"]glyphicon-arrow-right[#else]glyphicon-arrow-down[/#if]"></span> ${message("cube.dimension.expand")}
          </a>
      </li>
      [/#if]
    [/#if]
    [#if index > 0]
    <li>
        <a href="#" data-ref="${index}" class="move-up">
            <span class="glyphicon [#if type == "row"]glyphicon-arrow-left[#else]glyphicon-arrow-up[/#if]"></span> ${message("cube.dimension.move.up.${type}")}
        </a>
    </li>
    [/#if]
    [#if index + 1 < maxIndex]
    <li>
        <a href="#" data-ref="${index}" class="move-down">
            <span class="glyphicon [#if type == "row"]glyphicon-arrow-right[#else]glyphicon-arrow-down[/#if]"></span> ${message("cube.dimension.move.down.${type}")}
        </a>
    </li>
    [/#if]

  </ul>
</div>
[/#macro]
