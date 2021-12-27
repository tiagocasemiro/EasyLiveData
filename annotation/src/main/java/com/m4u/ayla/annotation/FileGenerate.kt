package com.m4u.ayla.annotation

import java.io.File
import javax.annotation.processing.ProcessingEnvironment

class FileGenerate(private var processingEnv: ProcessingEnvironment) {

    fun create(metaClazz: MetaClass): File {
        val name = metaClazz.name
        val directory = processingEnv.options["kapt.kotlin.generated"]

        return File(directory, "$name.kt")
    }
}
