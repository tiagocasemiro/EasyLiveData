package com.m4u.ayla.annotation

import com.squareup.kotlinpoet.asClassName
import java.math.BigDecimal
import java.math.BigInteger
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment

class ExtractMetadata(private val processingEnv: ProcessingEnvironment) {

    fun processing(roundEnvironment: RoundEnvironment): List<MetaClass> {
        val clazzs = mutableListOf<MetaClass>()
        roundEnvironment.getElementsAnnotatedWith(Delegate::class.java)?.forEach { clazz->
            val targetName = clazz.simpleName.toString()
            val delegateName = clazz.simpleName.toString()
                .replace("Fragment", "Delegate")
                .replace("Activity", "Delegate")

            val pack = processingEnv.elementUtils.getPackageOf(clazz).toString()
            val methods = mutableListOf<MetaMethod>()
            clazz.enclosedElements.filter {
                it.getAnnotationsByType(Callback::class.java).isNotEmpty()
            }.forEach { method ->
                val isEvent = method.getAnnotation(Callback::class.java).event
                val name = method.simpleName.toString()
                val parameters = mutableListOf<MetaParameter>()
                method.asType().toString().removePrefix("(").split(")")[0].split(",")
                    .filter {
                        it.isNotEmpty()
                    }.forEachIndexed { index, type ->
                        if(isPrimitive(type)) {
                            parameters.add(MetaParameter("arg$index", primitiveToMetaType(type)))
                        } else {
                            val objects = type.removeSuffix(">").split("<")
                            val objectMetaType = objectToMetaType(objects.first())
                            if(objects.size > 1) {
                                val genericMetaType = objectToMetaType(objects.last())
                                parameters.add(MetaParameter("arg$index", objectMetaType, genericMetaType))
                            } else {
                                parameters.add(MetaParameter("arg$index", objectMetaType))
                            }
                        }
                    }
                methods.add(MetaMethod(name = name, parameters = parameters, isEvent = isEvent))
            }
            clazzs.add(MetaClass(pack = pack, name = delegateName, methods = methods, target = MetaTarget(targetName)))
        }

        return clazzs
    }

    private fun isPrimitive(type: String): Boolean {
        if(type.contains("<") || type.contains(">")) {
            return false
        }
        val simpleType = type.split(".").last()

        return when {
            simpleType.contains("STRING", true)-> true
            simpleType.contains("BOOLEAN", true)-> true
            simpleType.contains("BYTE", true)-> true
            simpleType.contains("SHORT", true)-> true
            simpleType.contains("INT", true)-> true
            simpleType.contains("LONG", true)-> true
            simpleType.contains("CHAR", true)-> true
            simpleType.contains("FLOAT", true)-> true
            simpleType.contains("DOUBLE", true) -> true
            simpleType.contains("BIGDECIMAL", true) -> true
            simpleType.contains("BIGINTEGER", true) -> true
            else -> false
        }
    }

    private fun primitiveToMetaType(primitive: String): MetaType {
        val fullName = when {
            primitive.contains("STRING", true)-> String::class.asClassName().canonicalName
            primitive.contains("BOOLEAN", true)-> Boolean::class.asClassName().canonicalName
            primitive.contains("BYTE", true)-> Byte::class.asClassName().canonicalName
            primitive.contains("SHORT", true)-> Short::class.asClassName().canonicalName
            primitive.contains("INT", true)-> Int::class.asClassName().canonicalName
            primitive.contains("LONG", true)-> Long::class.asClassName().canonicalName
            primitive.contains("CHAR", true)-> Char::class.asClassName().canonicalName
            primitive.contains("FLOAT", true)-> Float::class.asClassName().canonicalName
            primitive.contains("DOUBLE", true) -> Double::class.asClassName().canonicalName
            primitive.contains("BIGDECIMAL", true) -> BigDecimal::class.asClassName().canonicalName
            primitive.contains("BIGINTEGER", true) -> BigInteger::class.asClassName().canonicalName
            else -> primitive
        }

        return objectToMetaType(fullName, true)
    }

    private fun objectToMetaType(objectName: String, isPrimitive: Boolean = false): MetaType {
        val list = objectName.split(".").toMutableList()
        val name = list.last()
        list.removeAt(list.lastIndex)
        val packageName = StringBuilder()
        list.forEach {
            if(packageName.toString().isEmpty()) {
                packageName.append(it)
            } else {
                packageName.append(".$it")
            }
        }

        return MetaType(name, packageName.toString(), isPrimitive)

    }
}
