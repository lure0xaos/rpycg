init 999 python:
    #Enable console
    config.console = True
    # Define function to open the menu
    def EnableCheatMenu():
        renpy.call_in_new_context('ShowCheatMenu')
    config.keymap['CheatMenuBind'] = ['k']
    #Enable fast console
    config.keymap['console'].append('l')
    config.underlay.append(renpy.Keymap(CheatMenuBind=EnableCheatMenu))
    # Define function to write variables to file
    def WriteVariables():
        renpy.call_in_new_context('WriteVariablesToFile')
    #Enable write variables to file
    config.keymap['WriteVariableBind'] = ['m']
    config.underlay.append(renpy.Keymap(WriteVariableBind=WriteVariables))
#Find unique game variables
label WriteVariablesToFile:
    $ f = open("Game Variables.txt","w+")
    define inGameDefaults = set(",".join(globals()).split(","))
    $ inGameDiff = "\n".join(sorted(set(set(",".join(globals()).split(","))).difference(inGameDefaults))).split("\n")
    define newGameDefaults = []
    python:
        for item in inGameDiff:
            if not str(item) in ["EnableCheatMenu","WriteVariables","newGameDefaults","inGameDefaults","_history_list"]:
                newGameDefaults.append(str(item) + " = " + str(repr(globals().get(item))) + "\n")
    $ f.write('\n'.join([unicode(i) for i in newGameDefaults]))
    $ f.close()
    "Game variables written to file."
    return
label ShowCheatMenu:
    jump CheatMenu
label CheatMenu:
menu:
    "custom name \[[variable_name1]\]" :
        $variable_name1 = str(renpy.input("Change custom name from [variable_name1] to?").strip() or variable_name1)
        jump CheatMenu
    "variable_name2 \[[variable_name2]\]" :
        $variable_name2 = str(renpy.input("Change variable_name2 from [variable_name2] to?").strip() or variable_name2)
        jump CheatMenu
    "~menu_title~":
        menu:
            "variable_name3 \[[variable_name3]\]" :
                $variable_name3 = str(renpy.input("Change variable_name3 from [variable_name3] to?").strip() or variable_name3)
                jump CheatMenu
            "variable_name4 \[[variable_name4]\]" :
                $variable_name4 = str(renpy.input("Change variable_name4 from [variable_name4] to?").strip() or variable_name4)
                jump CheatMenu
            "~Back~":
                jump CheatMenu
    "variable_name5 \[[variable_name5]\]" :
        $variable_name5 = str(renpy.input("Change variable_name5 from [variable_name5] to?").strip() or variable_name5)
        jump CheatMenu
    "fixed_variable6=100 \[[fixed_variable6]\]" :
        $fixed_variable6 = 100
        jump CheatMenu
    "custom name=100 \[[fixed_variable7]\]" :
        $fixed_variable7 = 100
        jump CheatMenu
    "~Nevermind~":
        return
