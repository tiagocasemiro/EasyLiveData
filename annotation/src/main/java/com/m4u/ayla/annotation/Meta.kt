package com.m4u.ayla.annotation

data class MetaClass(val pack: String, val name: String, val attributes: List<MetaParameter>? = null, val methods: List<MetaMethod>? = null, val target: MetaTarget)
data class MetaMethod(val name: String, val parameters: List<MetaParameter>, val isEvent:Boolean)
data class MetaParameter(val name: String, val type: MetaType, val genericType: MetaType? = null) {
    fun haveGenericType(): Boolean {
        return genericType != null
    }
}
data class MetaTarget(val name: String)
data class MetaType(val name: String, val packageName: String, val isPrimitive: Boolean = false)
