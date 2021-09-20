package gargoyle.rpycg.util;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeView;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class TreeItemWalker<T> {
    private final Deque<TreeItemTuple<T>> stack = new ArrayDeque<>(2);

    private TreeItemWalker(final TreeTableView<T> tree) {
        if (null != tree.getRoot()) {
            stack.push(new TreeItemTuple<>(tree.getRoot(), -1));
        }
    }

    private TreeItemWalker(final TreeView<T> tree) {
        if (null != tree.getRoot()) {
            stack.push(new TreeItemTuple<>(tree.getRoot(), -1));
        }
    }

    @SuppressWarnings("MethodCallInLoopCondition")
    public static <T> void visit(final TreeTableView<T> tree, final Consumer<? super TreeItem<T>> visitor) {
        final TreeItemWalker<T> walker = new TreeItemWalker<>(tree);
        while (walker.hasNext()) {
            visitor.accept(walker.next());
        }
    }

    @SuppressWarnings("MethodCallInLoopCondition")
    public static <T> void visit(final TreeView<T> tree, final Consumer<? super TreeItem<T>> visitor) {
        final TreeItemWalker<T> walker = new TreeItemWalker<>(tree);
        while (walker.hasNext()) {
            visitor.accept(walker.next());
        }
    }

    @SuppressWarnings("MethodCallInLoopCondition")
    public static <T> void visitItems(final TreeTableView<T> tree, final Consumer<? super T> visitor) {
        final TreeItemWalker<T> walker = new TreeItemWalker<>(tree);
        while (walker.hasNext()) {
            visitor.accept(walker.next().getValue());
        }
    }

    @SuppressWarnings("MethodCallInLoopCondition")
    public static <T> void visitItems(final TreeView<T> tree, final Consumer<? super T> visitor) {
        final TreeItemWalker<T> walker = new TreeItemWalker<>(tree);
        while (walker.hasNext()) {
            visitor.accept(walker.next().getValue());
        }
    }

    public Stream<TreeItem<T>> stream() {
        return StreamSupport.stream(new TreeItemSpliterator(), false);
    }

    private boolean hasNext() {
        return !stack.isEmpty();
    }

    private void move() {
        final TreeItemTuple<T> tuple = stack.pop();
        final ObservableList<TreeItem<T>> children = tuple.getItem().getChildren();
        final int index = tuple.getIndex() + 1;
        if (children.size() <= index) {
            if (!stack.isEmpty()) {
                move();
            }
        } else {
            stack.push(tuple.setIndex(index));
            stack.push(new TreeItemTuple<>(children.get(index), -1));
        }
    }

    private TreeItem<T> next() {
        if (!hasNext()) {
            throw new IllegalStateException("");
        }
        if (null == stack.peek()) throw new AssertionError();
        final TreeItem<T> next = stack.peek().getItem();
        move();
        return next;
    }

    private static final class TreeItemTuple<T> {
        private final int index;
        private final TreeItem<T> item;

        private TreeItemTuple(final TreeItem<T> item, final int index) {
            this.item = item;
            this.index = index;
        }

        @Override
        public String toString() {
            return "TreeItemTuple [item=" + item + ", second=" + index + "]";
        }

        private int getIndex() {
            return index;
        }

        private TreeItemTuple<T> setIndex(final int index) {
            return new TreeItemTuple<>(item, index);
        }

        private TreeItem<T> getItem() {
            return item;
        }

        public TreeItemTuple<T> setItem(final TreeItem<T> item) {
            return new TreeItemTuple<>(item, index);
        }
    }

    @SuppressWarnings("unused")
    private final class TreeItemSpliterator implements Spliterator<TreeItem<T>> {
        @Override
        public boolean tryAdvance(final Consumer<? super TreeItem<T>> action) {
            if (hasNext()) {
                action.accept(next());
                return true;
            } else {
                return false;
            }
        }

        @Override
        public Spliterator<TreeItem<T>> trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            return Long.MAX_VALUE;
        }

        @Override
        public int characteristics() {
            return 0;
        }
    }
}
