package me.a0xcaff.forte

/**
 * A cache which only holds the latest value.
 */
class SingleValueCache<K, V> {
    private var lastKey: K? = null
    private var value: V? = null

    fun get(key: K): V? {
        if (key != lastKey) {
            return null
        }

        return value!!
    }

    fun put(key: K, value: V) {
        this.lastKey = key
        this.value = value
    }

    fun computeIfAbsent(key: K, compute: () -> V): V {
        val gotten = get(key)
        if (gotten != null) {
            return gotten
        }

        val computed = compute()
        put(key, computed)

        return computed
    }
}
