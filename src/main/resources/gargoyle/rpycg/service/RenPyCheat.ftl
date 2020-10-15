# @formatter:off
init 999 python:
<#if settings.enableConsole><#lt><#rt>
    # Enable console
    config.console = True
    persistent._console_short = False
</#if><#lt><#rt>
<#if settings.enableDeveloper><#lt><#rt>
    # Enable developer mode
    config.developer = True
</#if><#lt><#rt>
<#if settings.enableCheat><#lt><#rt>
    # Define function to open the menu
    def enable_cheat_menu():
        renpy.call_in_new_context("show_cheat_menu")
    config.keymap["cheat_menu_bind"] = ["${settings.keyCheat}"]
</#if><#lt><#rt>
<#if settings.enableConsole><#lt><#rt>
    # Enable fast console
    config.keymap["console"] = ["${settings.keyConsole}"]
</#if><#lt><#rt>
<#if settings.enableCheat><#lt><#rt>
    # Enable developer mode
    config.keymap["developer"] = ["${settings.keyDeveloper}"]
    config.underlay.append(renpy.Keymap(cheat_menu_bind=enable_cheat_menu))
</#if><#lt><#rt>
<#if settings.enableRollback><#lt><#rt>
    # Enable rollback
    config.rollback_enabled = True
</#if><#lt><#rt>
<#if settings.enableWrite><#lt><#rt>
    # Define function to write variables to file
    def write_variables():
        renpy.call_in_new_context("write_variables_to_file")
    # Enable write variables to file
    config.keymap["write_variables_bind"] = ["${settings.keyWrite}"]
    config.underlay.append(renpy.Keymap(write_variables_bind=write_variables))
# Find unique game variables
label write_variables_to_file:
    $ f = open("${fileVariables}", "w+")
    define inGameDefaults = set(",".join(globals()).split(","))
    $ inGameDiff = "\n".join(sorted(set(set(",".join(globals()).split(","))).difference(inGameDefaults))).split("\n")
    define newGameDefaults = []
    python:
        for item in inGameDiff:
            if not str(item) in ["enable_cheat_menu", "write_variables", "newGameDefaults", "inGameDefaults", "inGameDiff", "_history_list"]:
                newGameDefaults.append(str(item) + " = " + str(repr(globals().get(item))) + "\n")
    $ f.write("\n".join([unicode(i) for i in newGameDefaults]))
    $ f.close()
    "${msg("message-written")}"
    return
</#if><#lt><#rt>
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
