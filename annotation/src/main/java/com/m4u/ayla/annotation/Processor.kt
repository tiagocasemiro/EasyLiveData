package com.m4u.ayla.annotation

import com.google.auto.service.AutoService
import javax.annotation.processing.*
import javax.annotation.processing.Processor
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions("kapt.kotlin.generated")
class Processor: AbstractProcessor() {
    override fun process(set: MutableSet<out TypeElement>?, roundEnvironment: RoundEnvironment?): Boolean {
        roundEnvironment?.let {
            val clazzs = ExtractMetadata(processingEnv).processing(it)
            clazzs.forEach { clazz ->
                val fileSpec = KotlinPoetClassBuilder(clazz).builder()
                val file = FileGenerate(processingEnv).create(clazz)
                fileSpec.writeTo(file)
            }
        }

        return true
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(Delegate::class.java.name)
    }
}
