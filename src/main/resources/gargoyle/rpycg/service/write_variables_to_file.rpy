    # Define function to write variables to file
    def write_variables():
        renpy.call_in_new_context("write_variables_to_file")
    # Enable write variables to file
    config.keymap["write_variables_bind"] = ["${keyWrite}"]
    config.underlay.append(renpy.Keymap(write_variables_bind=write_variables))
# Find unique game variables
label write_variables_to_file:
    $ f = open("${fileVariables}", "w+")
    define in_game_defaults = set(",".join(globals()).split(","))
    $ in_game_diff = "\n".join(sorted(set(set(",".join(globals()).split(","))).difference(in_game_defaults))).split("\n")
    define new_game_defaults = []
    python:
        for item in in_game_diff:
            if not str(item) in ["f", "enable_cheat_menu", "write_variables", "new_game_defaults", "in_game_defaults", "in_game_diff", "_history_list"]:
                new_game_defaults.append(str(item) + " = " + str(repr(globals().get(item))) + "\n")
    $ f.write("\n".join([unicode(i) for i in new_game_defaults]))
    $ f.close()
    "${messageWritten}"
    return
