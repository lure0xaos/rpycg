package gargoyle.rpycg.model

import gargoyle.rpycg.ui.model.FULLNESS

object ModelTemplate {

    fun getTemplateTree(): ModelItem {
        val rootMenu = ModelItem.createMenu("", "")
        rootMenu.addChild(ModelItem.createVariable(VarType.STR, "custom name", "variable_name1", ""))
        rootMenu.addChild(ModelItem.createVariable(VarType.STR, "variable_name2", "variable_name2", ""))
        val menu = ModelItem.createMenu("menu_title", "menu_title")
        menu.addChild(ModelItem.createVariable(VarType.STR, "variable_name3", "variable_name3", ""))
        menu.addChild(ModelItem.createVariable(VarType.STR, "variable_name4", "variable_name4", ""))
        rootMenu.addChild(menu)
        rootMenu.addChild(ModelItem.createVariable(VarType.STR, "variable_name5", "variable_name5", ""))
        rootMenu.addChild(ModelItem.createVariable(VarType.INT, "fixed_variable6", "fixed_variable6", "100"))
        rootMenu.addChild(ModelItem.createVariable(VarType.INT, "custom name", "fixed_variable7", "100"))
        return rootMenu
    }

    fun getTestTemplateTree(): ModelItem {
        val rootMenu = ModelItem.createMenu("", "")
        var v = 1
        run {
            var i = 0
            while (2 > i) {
                rootMenu.addChild(createVariable(v))
                v++
                i++
            }
        }
        var m = 1
        val menuAlmost = createMenu(m)
        m++
        val almostSize = FULLNESS.ALMOST.size
        for (i in 0..almostSize) {
            menuAlmost.addChild(createVariable(v))
            v++
        }
        rootMenu.addChild(menuAlmost)
        val menuFull = createMenu(m)
        val fullSize = FULLNESS.FULL.size
        for (i in 0..fullSize) {
            menuFull.addChild(createVariable(v))
            v++
        }
        rootMenu.addChild(menuFull)
        var i = 0
        while (3 > i) {
            rootMenu.addChild(createVariable(v))
            v++
            i++
        }
        return rootMenu
    }

    private fun createMenu(m: Int): ModelItem =
        ModelItem.createMenu("menu_title${m}", "menu_title${m}")

    private fun createVariable(v: Int): ModelItem =
        ModelItem.createVariable(VarType.STR, "custom name${v}", "variable_name${v}", "")

}
