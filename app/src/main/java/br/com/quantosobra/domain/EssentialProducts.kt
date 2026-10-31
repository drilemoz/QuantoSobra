package br.com.quantosobra.domain

import java.util.Locale

/** Produtos comuns, sem preços presumidos; escolha explícita do usuário. */
object EssentialProducts {
    val items = linkedMapOf("Arroz" to "kg", "Feijão" to "kg", "Óleo" to "garrafa",
        "Sal" to "pacote", "Ovos" to "dúzia", "Macarrão" to "pacote",
        "Leite" to "L", "Café" to "pacote", "Açúcar" to "kg", "Farinha" to "kg")
    fun add(current: List<FoodProduct>, names: Set<String>): List<FoodProduct> {
        val existing = current.map { it.name.trim().lowercase(Locale.ROOT) }.toSet()
        var id = current.maxOfOrNull { it.id } ?: 0L
        return current + items.filter { (name, _) -> name in names && name.lowercase(Locale.ROOT) !in existing }
            .entries.take((500 - current.size).coerceAtLeast(0)).map { (name, unit) ->
                FoodProduct(++id, name, 0L, 100, unit)
            }
    }
}

