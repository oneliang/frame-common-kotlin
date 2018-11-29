package com.oneliang.ktx.frame.jdbc

import com.oneliang.ktx.exception.InitializeException
import com.oneliang.ktx.frame.context.AnnotationContextUtil

class AnnotationMappingContext : MappingContext() {

    /**
     * initialize
     */
    override fun initialize(parameters: String) {
        try {
            val classList = AnnotationContextUtil.parseAnnotationContextParameterAndSearchClass(parameters, classLoader, classesRealPath, jarClassLoader, projectRealPath, Table::class)
            for (clazz in classList) {
                val className = clazz.java.name
                val classSimpleName = clazz.java.simpleName
                val tableAnnotation = clazz.java.getAnnotation(Table::class.java)
                val annotationMappingBean = AnnotationMappingBean()
                annotationMappingBean.isDropIfExist = tableAnnotation.dropIfExist
                annotationMappingBean.table = tableAnnotation.table
                annotationMappingBean.type = className
                annotationMappingBean.condition = tableAnnotation.condition
                val columnAnnotations = tableAnnotation.columns
                for (columnAnnotation in columnAnnotations) {
                    val annotationMappingColumnBean = AnnotationMappingColumnBean()
                    annotationMappingColumnBean.field = columnAnnotation.field
                    annotationMappingColumnBean.column = columnAnnotation.column
                    annotationMappingColumnBean.isId = columnAnnotation.isId
                    annotationMappingColumnBean.condition = columnAnnotation.condition
                    annotationMappingBean.addMappingColumnBean(annotationMappingColumnBean)
                }
                val fields = clazz.java.declaredFields
                if (fields != null) {
                    for (field in fields) {
                        if (field.isAnnotationPresent(Table.Column::class.java)) {
                            val columnAnnotation = field.getAnnotation(Table.Column::class.java)
                            val annotationMappingColumnBean = AnnotationMappingColumnBean()
                            annotationMappingColumnBean.field = field.name
                            annotationMappingColumnBean.column = columnAnnotation.column
                            annotationMappingColumnBean.isId = columnAnnotation.isId
                            annotationMappingColumnBean.condition = columnAnnotation.condition
                            annotationMappingBean.addMappingColumnBean(annotationMappingColumnBean)
                        }
                    }
                }
                annotationMappingBean.createTableSqls = SqlUtil.createTableSqls(annotationMappingBean)
                MappingContext.classNameMappingBeanMap[className] = annotationMappingBean
                MappingContext.simpleNameMappingBeanMap[classSimpleName] = annotationMappingBean
            }
        } catch (e: Exception) {
            throw InitializeException(parameters, e)
        }
    }
}
