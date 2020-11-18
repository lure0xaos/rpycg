# @formatter:off
<#macro menu root parentLabel><#lt><#rt>
<#list root.children as item><#lt><#rt>
<#if item.modelType.name()=="VARIABLE"><#lt><#rt>
    # variable ${item.name}=${item.type}(${item.value}) ${item.label}
<#if item.value?has_content><#lt><#rt>
    "${item.label}=${item.value} \[[${item.name}]\]" :
<#if item.type.name()=="STR"><#lt><#rt>
        $${item.name} = "${item.type.keyword}("${item.value}")"
<#else><#lt><#rt>
        $${item.name} = ${item.value}
</#if><#lt><#rt>
<#else><#lt><#rt>
    "${item.label} \[[${item.name}]\]" :
        $${item.name} = ${item.type.keyword}(renpy.input("${msg("message-prompt", item.label, "["+item.name+"]")}").strip() or ${item.name})
</#if><#lt><#rt>
        jump ${parentLabel}
</#if><#lt><#rt>
<#if item.modelType.name()=="MENU"><#lt><#rt>
    # menu ${item.label}
    "~${item.label}~":
        label ${item.name}:
            menu:
<@indent count=3><#lt><#rt>
<@menu item item.name/><#lt><#rt>
</@indent><#lt><#rt>
                # back
                "~${msg("back")}~":
                    jump ${parentLabel}
</#if><#lt><#rt>
</#list><#lt><#rt>
</#macro><#lt><#rt>
<#if settings.enableCheat><#lt><#rt>
label show_cheat_menu:
    jump CheatMenu
label CheatMenu:
    menu:
<@indent count=1><#lt><#rt>
<@menu model 'CheatMenu'/><#lt><#rt>
</@indent><#lt><#rt>
        # nevermind
        "~${msg("nevermind")}~":
            return
</#if><#lt><#rt>
