package com.m4u.ayla.annotation

import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.*

class KotlinPoetClassBuilder(private val metaClass: MetaClass) {
    private val observerObject =  MemberName("androidx.lifecycle", "Observer")
    private val observerObjectEvent =  MemberName("com.m4u.ayla.recharge.util", "observeEvent")
    private val objectEvent =  MemberName("com.m4u.ayla.recharge.util", "Event")

    fun builder(): FileSpec {
        val typeSpec = TypeSpec.classBuilder(metaClass.name)
        val fileSpec = FileSpec.builder(metaClass.pack, metaClass.name)
        typeSpec.primaryConstructor(generateConstructor())
        metaClass.methods?.forEach { method ->
            if(method.isEvent) {
                typeSpec.addProperty(generateAttributesEvent(method))
                typeSpec.addFunction(generateMethodEvent(method))
            } else {
                typeSpec.addProperty(generateAttributes(method))
                typeSpec.addFunction(generateMethod(method))
            }
            method.parameters.forEach {
                if(it.type.isPrimitive.not()) {
                    fileSpec.addImport(it.type.packageName, it.type.name)
                }
                it.genericType?.let {  metaType ->
                    if(metaType.isPrimitive.not()) {
                        fileSpec.addImport(metaType.packageName, metaType.name)
                    }
                }
            }
        }

        return  fileSpec.addType(typeSpec.build()).build()
    }

    private fun generateConstructor(): FunSpec {
        val greeterClass = ClassName(metaClass.pack, metaClass.target.name)
        val funSpec = FunSpec.constructorBuilder().addParameter("target", greeterClass)
        metaClass.methods?.forEach { method ->
            var liveDataName = method.name
            method.parameters.forEach { parameter ->
                liveDataName = liveDataName + "_" + parameter.name + "_" + extractSimpleName(parameter.type.name)
            }
            val liveDataParameters = generateLiveDataConsumerParameter(method)
            if(method.isEvent) {
                funSpec
                    .addStatement("$liveDataName.%M(target, {", observerObjectEvent)
                    .addStatement("    target.${method.name}($liveDataParameters)")
                    .addStatement("})")
            } else {
                funSpec
                    .addStatement("$liveDataName.observe(target, %M {", observerObject)
                    .addStatement("    target.${method.name}($liveDataParameters)")
                    .addStatement("})")
            }
        }

        return funSpec.build()
    }

    private fun extractSimpleName(name: String): String {
        return name.split("<").first().split(".").last()
    }

    private fun generateLiveDataConsumerParameter(method: MetaMethod): String {
        val content = StringBuilder()
        method.parameters.forEachIndexed { index, parameter ->
            val type = parameter.type.name.split(".").last()
            if(content.toString().isEmpty()) {
                content.append("it[$index] as $type")
            } else {
                content.append(", it[$index] as $type")
            }
        }

        return content.toString()
    }

    private fun generateAttributes(method: MetaMethod): PropertySpec {
        var name = method.name
        method.parameters.forEach { parameter ->
            name = name + "_" + parameter.name + "_" + extractSimpleName(parameter.type.name)
        }
        val parameterizedTypeName = ClassName("androidx.lifecycle", "MutableLiveData").
            parameterizedBy(TypeVariableName("Array<*>"))

        return PropertySpec.builder(name, parameterizedTypeName)
            .addModifiers(KModifier.PRIVATE)
            .addModifiers(KModifier.FINAL)
            .initializer("MutableLiveData<Array<*>>()")
            .build()
    }

    private fun generateAttributesEvent(method: MetaMethod): PropertySpec {
        var name = method.name
        method.parameters.forEach { parameter ->
            name = name + "_" + parameter.name + "_" + extractSimpleName(parameter.type.name)
        }
        val parameterizedTypeName = ClassName("androidx.lifecycle", "MutableLiveData").
        parameterizedBy(TypeVariableName("Event<Array<*>>"))

        return PropertySpec.builder(name, parameterizedTypeName)
            .addModifiers(KModifier.PRIVATE)
            .addModifiers(KModifier.FINAL)
            .initializer("MutableLiveData<%M<Array<*>>>()", objectEvent)
            .build()
    }

    private fun generateMethod(method: MetaMethod): FunSpec {
        val funSpec = FunSpec.builder(method.name)
        var liveDataName = method.name
        method.parameters.forEach { parameter ->
            liveDataName = liveDataName + "_" + parameter.name + "_" + extractSimpleName(parameter.type.name)
            funSpec.addParameter(generateParameter(parameter))
        }
        funSpec.addStatement("$liveDataName.postValue(${generateLiveDataSendParameter(method)})")

        return funSpec.build()
    }

    private fun generateMethodEvent(method: MetaMethod): FunSpec {
        val funSpec = FunSpec.builder(method.name)
        var liveDataName = method.name
        method.parameters.forEach { parameter ->
            liveDataName = liveDataName + "_" + parameter.name + "_" + extractSimpleName(parameter.type.name)
            funSpec.addParameter(generateParameter(parameter))
        }
        funSpec.addStatement("$liveDataName.postValue(%M(${generateLiveDataSendParameter(method)}))", objectEvent)

        return funSpec.build()
    }

    private fun generateParameter(parameter: MetaParameter): ParameterSpec {
        val className =  ClassName(parameter.type.packageName, parameter.type.name)

        if(parameter.haveGenericType()) {
            className.parameterizedBy(ClassName(parameter.genericType!!.packageName, parameter.genericType.name))
        }

        return ParameterSpec.builder(parameter.name, className).build()
    }

    private fun generateLiveDataSendParameter(method: MetaMethod): String {
        val content = StringBuilder()
        if(method.parameters.isEmpty()) {
            return "emptyArray<Any>()"
        }
        content.append("arrayOf(")
        var isFirst = true
        method.parameters.forEach { parameter ->
            if(isFirst) {
                isFirst = false
                content.append(parameter.name)
            } else {
                content.append(", ${parameter.name}")
            }
        }
        content.append(")")

        return content.toString()
    }
}
