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
<#include 'write_variables_to_file.ftl'><#lt><#rt>
</#if><#lt><#rt>
<#include 'cheat_menu.ftl'><#lt><#rt>
