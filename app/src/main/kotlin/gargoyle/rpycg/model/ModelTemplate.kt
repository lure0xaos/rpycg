package gargoyle.rpycg.model

import gargoyle.rpycg.ui.model.FULLNESS

object ModelTemplate {

    fun getTemplateTree(): ModelItem =
        with(ModelItem.createMenu("", "")) {
            addChild(ModelItem.createVariable(VarType.STR, "custom name", "variable_name1", ""))
            addChild(ModelItem.createVariable(VarType.STR, "variable_name2", "variable_name2", ""))
            with(ModelItem.createMenu("menu_title", "menu_title")) {
                addChild(ModelItem.createVariable(VarType.STR, "variable_name3", "variable_name3", ""))
                addChild(ModelItem.createVariable(VarType.STR, "variable_name4", "variable_name4", ""))
                this
            }.also { addChild(it) }
            addChild(ModelItem.createVariable(VarType.STR, "variable_name5", "variable_name5", ""))
            addChild(ModelItem.createVariable(VarType.INT, "fixed_variable6", "fixed_variable6", "100"))
            addChild(ModelItem.createVariable(VarType.INT, "custom name", "fixed_variable7", "100"))
            this
        }

    fun getTestTemplateTree(): ModelItem {
        var v = 1
        return ModelItem.createMenu("", "")
            .also {
                (0..2).forEach { _ ->
                    it.addChild(ModelItem.createVariable(VarType.STR, "custom name${v}", "variable_name${v}", ""))
                        .also { v++ }
                }
            }.also { root ->
                var m = 1
                root.addChild(ModelItem.createMenu("menu_title${m}", "menu_title${m}").also { m++ }.also {
                    (0..FULLNESS.ALMOST.size).forEach { _ ->
                        it.addChild(ModelItem.createVariable(VarType.STR, "custom name${v}", "variable_name${v}", ""))
                            .also { v++ }
                    }
                })
                root.addChild(ModelItem.createMenu("menu_title${m}", "menu_title${m}").also {
                    (0..FULLNESS.FULL.size).forEach { _ ->
                        it.addChild(ModelItem.createVariable(VarType.STR, "custom name${v}", "variable_name${v}", ""))
                            .also { v++ }
                    }
                }).also { m++ }
                (0..3).forEach { _ ->
                    root.addChild(ModelItem.createVariable(VarType.STR, "custom name${v}", "variable_name${v}", ""))
                        .also { v++ }
                }
            }
    }

}
