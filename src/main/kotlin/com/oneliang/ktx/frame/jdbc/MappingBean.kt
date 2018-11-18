package com.oneliang.ktx.frame.jdbc

import com.oneliang.ktx.Constants

open class MappingBean {
    companion object {
        const val TAG_BEAN = "bean"
    }

    var table: String = Constants.String.BLANK
    var type: String = Constants.String.BLANK
    val mappingColumnBeanList = mutableListOf<MappingColumnBean>()

    /**
     * get column
     * @param field
     * @return column
     */
    fun getColumn(field: String): String {
        var column: String? = null
        for (mappingColumnBean in mappingColumnBeanList) {
            val columnField = mappingColumnBean.field
            if (columnField.isNotBlank() && columnField == field) {
                column = mappingColumnBean.column
                break
            }
        }
        return column ?: Constants.String.BLANK
    }

    /**
     * get field
     * @param column
     * @return field
     */
    fun getField(column: String): String {
        var field: String? = null
        for (mappingColumnBean in mappingColumnBeanList) {
            val fieldColumn = mappingColumnBean.column
            if (fieldColumn.isNotBlank() && fieldColumn == column) {
                field = mappingColumnBean.field
                break
            }
        }
        return field ?: Constants.String.BLANK
    }

    /**
     * judge the field is id or not
     * @param field
     * @return is id
     */
    fun isId(field: String): Boolean {
        var isId = false
        for (mappingColumnBean in mappingColumnBeanList) {
            val columnField = mappingColumnBean.field
            if (columnField.isNotBlank() && columnField == field) {
                isId = mappingColumnBean.isId
                break
            }
        }
        return isId
    }

    /**
     * @param mappingColumnBean
     * @return boolean
     */
    fun addMappingColumnBean(mappingColumnBean: MappingColumnBean): Boolean {
        return this.mappingColumnBeanList.add(mappingColumnBean)
    }
}
