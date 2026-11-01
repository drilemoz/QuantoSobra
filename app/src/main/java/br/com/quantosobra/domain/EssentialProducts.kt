package br.com.quantosobra.domain

import java.util.Locale

/** Pacotes mensais de referência, carregados apenas com consentimento explícito.
 * Valores INDICATIVOS de catálogo online do Atacadão (01/11/2026), não cotação ao vivo
 * nem preço garantido em determinada loja. O usuário pode alterar qualquer campo.
 */
data class BasicProduct(val name: String, val unit: String, val quantity: Int, val priceCents: Long)

object EssentialProducts {
    const val referenceDate = "01/11/2026"
    const val referenceUrl = "https://www.atacadao.com.br/catalogo"
    val basics = listOf(
        BasicProduct("Arroz", "pacote 5 kg", 3, 2226),
        BasicProduct("Óleo", "garrafa 900 ml", 4, 719), // 4 x 900 ml = 3,6 L
        BasicProduct("Açúcar", "pacote 1 kg", 1, 312),
        BasicProduct("Feijão", "pacote 1 kg", 5, 774),
        BasicProduct("Farinha", "pacote 1 kg", 2, 402), // farinha de trigo
        BasicProduct("Macarrão", "pacote 500 g", 5, 289),
        BasicProduct("Ovos", "cartela 30 un", 1, 2390),
        BasicProduct("Leite", "caixa 1 L", 5, 544)
    )

    /** Preserva quaisquer itens existentes, seus IDs e personalizações. Não cria duplicatas. */
    fun merge(current: List<FoodProduct>, incoming: List<FoodProduct>): List<FoodProduct> {
        val existing = current.map { it.name.trim().lowercase(Locale.ROOT) }.toSet()
        var id = current.maxOfOrNull { it.id } ?: 0L
        return current + incoming.asSequence()
            .filter { it.name.trim().lowercase(Locale.ROOT) !in existing }
            .distinctBy { it.name.trim().lowercase(Locale.ROOT) }
            .take((500 - current.size).coerceAtLeast(0))
            .map { it.copy(id = ++id) }.toList()
    }

    /** Compatibilidade de chamadas antigas, sem preço presumido. */
    fun add(current: List<FoodProduct>, names: Set<String>): List<FoodProduct> = merge(current,
        basics.filter { it.name in names }.map { FoodProduct(0L, it.name, 0L, 100, it.unit) })
}
