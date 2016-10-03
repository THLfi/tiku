[#ftl][#setting locale="fi"]
[#macro label e][#if e?? && e.label??]${(e.label.getValue(lang)!"n/a")?json_string}[#else]???[/#if][/#macro]
[#macro traverseTree nodes]
    [
    [#list nodes as node]
        [#if node_index > 0],[/#if]
        {
            "id": "${node.id?json_string}",
            "sid": [#if node.surrogateId??]${node.surrogateId?json_string}[#else]-1[/#if],
            "label": "[@label node /]"
            [#if node.level??],"stage": "${node.level.id?json_string}"[/#if]
            [#if node.code??],"code":"${node.code?json_string}"[/#if]
            [#if node.properties!?size > 0]
            ,"properties": {
                [#list node.properties! as p]
                [#if p_index > 0],[/#if]"${p.key?json_string}": "${p.value?json_string}"
                [/#list]
            }[/#if]
            ,"children": [#if node.children??][@traverseTree node.children /][#else][][/#if]
        }
    [/#list]
    ]
[/#macro]
thl.pivot.loadDimensions([
 [#list dimensions as dim]
 [#if dim_index > 0],[/#if]
 {
    "id": "${dim.id}",
    "label": "[@label dim /]",
    "children": [@traverseTree dim.rootLevel.nodes /]
 }
 [/#list]
]);
