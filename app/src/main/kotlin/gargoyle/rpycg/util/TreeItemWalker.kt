package gargoyle.rpycg.util

import javafx.collections.ObservableList
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeTableView
import javafx.scene.control.TreeView
import java.util.ArrayDeque
import java.util.Deque

@Suppress("unused")
class TreeItemWalker<T : Any> {
    private val stack: Deque<TreeItemTuple<T>> = ArrayDeque(2)

    private constructor(tree: TreeTableView<T>) {
        if (null != tree.root) stack.push(TreeItemTuple(tree.root, -1))
    }

    private constructor(tree: TreeView<T>) {
        if (null != tree.root) stack.push(TreeItemTuple(tree.root, -1))
    }

    private operator fun hasNext(): Boolean = !stack.isEmpty()

    private fun move() {
        val tuple = stack.pop()
        val children: ObservableList<TreeItem<T>> = tuple.getItem().children
        val index: Int = tuple.getIndex() + 1
        if (children.size <= index) {
            if (!stack.isEmpty()) {
                move()
            }
        } else {
            stack.push(tuple!!.setIndex(index))
            stack.push(TreeItemTuple(children[index], -1))
        }
    }

    private operator fun next(): TreeItem<T> {
        check(hasNext()) { "" }
        if (null == stack.peek()) throw AssertionError()
        val next: TreeItem<T> = stack.peek().getItem()
        move()
        return next
    }

    private class TreeItemTuple<T>(private val item: TreeItem<T>, private val index: Int) {

        override fun toString(): String = "TreeItemTuple [item=$item, second=$index]"

        fun setIndex(index: Int): TreeItemTuple<T> = TreeItemTuple(item, index)

        fun setItem(item: TreeItem<T>): TreeItemTuple<T> = TreeItemTuple(item, index)

        fun getItem(): TreeItem<T> = item

        fun getIndex(): Int = index
    }

//    private inner class TreeItemSpliterator : Spliterator<TreeItem<T>> {
//        override fun tryAdvance(action: Consumer<in TreeItem<T>>): Boolean {
//            return if (hasNext()) {
//                action.accept(next())
//                true
//            } else {
//                false
//            }
//        }
//
//        override fun trySplit(): Spliterator<TreeItem<T>> {
//        }
//
//        override fun estimateSize(): Long {
//            return Long.MAX_VALUE
//        }
//
//        override fun characteristics(): Int {
//            return 0
//        }
//    }

    companion object {
        fun <T : Any> visit(tree: TreeTableView<T>, visitor: (TreeItem<T>) -> Unit) {
            val walker = TreeItemWalker(tree)
            while (walker.hasNext()) visitor(walker.next())
        }

        fun <T : Any> visit(tree: TreeView<T>, visitor: (TreeItem<T>) -> Unit) {
            val walker = TreeItemWalker(tree)
            while (walker.hasNext()) visitor(walker.next())
        }

        fun <T : Any> visitItems(tree: TreeTableView<T>, visitor: (T) -> Unit) {
            val walker = TreeItemWalker(tree)
            while (walker.hasNext()) visitor(walker.next().value)
        }

        fun <T : Any> visitItems(tree: TreeView<T>, visitor: (T) -> Unit) {
            val walker = TreeItemWalker(tree)
            while (walker.hasNext()) visitor(walker.next().value)
        }
    }
}
