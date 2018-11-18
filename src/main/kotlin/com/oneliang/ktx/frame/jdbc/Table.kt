package com.oneliang.ktx.frame.jdbc

import com.oneliang.ktx.Constants

@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@Retention(AnnotationRetention.RUNTIME)
annotation class Table(val table: String, val dropIfExist: Boolean = false, val condition: String = Constants.String.BLANK, val columns: Array<Column> = []) {

    @MustBeDocumented
    @Target(AnnotationTarget.FIELD, AnnotationTarget.ANNOTATION_CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Column(val field: String = Constants.String.BLANK, val column: String, val isId: Boolean = false, val condition: String = Constants.String.BLANK)
}
