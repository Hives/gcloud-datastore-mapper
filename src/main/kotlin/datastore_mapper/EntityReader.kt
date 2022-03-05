package datastore_mapper

import com.google.cloud.datastore.Entity
import java.math.BigDecimal
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.withNullability
import kotlin.reflect.typeOf

class EntityReader<T : Any>(
    targetClass: KClass<T>,
    private val keyProperty: KProperty1<T, String>,
) {
    private val constructor = targetClass.primaryConstructor!!

    fun readEntity(entity: Entity): T {
        val constructorArguments = constructor.parameters
            .associateWith { parameter ->
                if (parameter.name == keyProperty.name) getKeyValue(entity)
                else getValue(parameter, entity)
            }

        return constructor.callBy(constructorArguments)
    }

    private fun getKeyValue(entity: Entity): String =
        entity.key.name

    private fun getValue(parameter: KParameter, entity: Entity): Any? {
        val name = parameter.name

        return if (!entity.contains(name)) null
        else {
            when (parameter.type.withNullability(false)) {
                typeOf<String>() -> entity.getString(name)
                typeOf<Int>() -> entity.getLong(name).toInt()
                typeOf<Long>() -> entity.getLong(name)
                typeOf<BigDecimal>() -> BigDecimal(entity.getString(name))
                typeOf<Boolean>() -> entity.getBoolean(name)
                else -> throw RuntimeException("Unknown parameter type: ${parameter.type}")
            }
        }
    }
}