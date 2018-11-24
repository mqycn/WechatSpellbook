package com.gh0u1l5.wechatmagician.spellbook.util

import java.util.concurrent.Executors
import kotlin.concurrent.thread

/**
 * 封装了一批用于并行计算的函数
 */
object ParallelUtil {
    /**
     * 当前设备可用 CPU 数量
     */
    val processors: Int = Runtime.getRuntime().availableProcessors()

    /**
     * 创建一个线程池, 其线程总数固定不变
     *
     * @param nThread 该线程池的线程总数, 默认值为当前设备的 CPU 总数
     */
    fun createThreadPool(nThread: Int = processors) = Executors.newFixedThreadPool(processors)

    /**
     * 进行一次并行计算的 Map 操作
     */
    inline fun <T, R> List<T>.parallelMap(crossinline transform: (T) -> R): List<R> {
        val sectionSize = size / processors

        val main = List(processors) { mutableListOf<R>() }
        (0 until processors).map { section ->
            thread(start = true) {
                for (offset in 0 until sectionSize) {
                    val idx = section * sectionSize + offset
                    main[section].add(transform(this[idx]))
                }
            }
        }.forEach { it.join() }

        val rest = (0 until size % processors).map { offset ->
            val idx = processors * sectionSize + offset
            transform(this[idx])
        }

        return main.flatten() + rest
    }

    /**
     * 进行一次并行计算的 ForEach 操作
     */
    inline fun <T> Iterable<T>.parallelForEach(crossinline action: (T) -> Unit) {
        val pool = createThreadPool()
        val iterator = iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            pool.execute { action(item) }
        }
    }
}