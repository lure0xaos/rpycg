package gargoyle.rpycg.util;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class TreeItemWalker<T> {

    @NotNull
    private final Deque<TreeItemTuple<T>> stack = new ArrayDeque<>(2);

    private TreeItemWalker(@NotNull TreeTableView<T> tree) {
        if (tree.getRoot() != null) {
            stack.push(new TreeItemTuple<>(tree.getRoot(), -1));
        }
    }

    private TreeItemWalker(@NotNull TreeView<T> tree) {
        if (tree.getRoot() != null) {
            stack.push(new TreeItemTuple<>(tree.getRoot(), -1));
        }
    }

    @SuppressWarnings("MethodCallInLoopCondition")
    public static <T> void visit(@NotNull TreeTableView<T> tree, @NotNull Consumer<TreeItem<T>> visitor) {
        TreeItemWalker<T> walker = new TreeItemWalker<>(tree);
        while (walker.hasNext()) {
            visitor.accept(walker.next());
        }
    }

    @SuppressWarnings("MethodCallInLoopCondition")
    public static <T> void visit(@NotNull TreeView<T> tree, @NotNull Consumer<TreeItem<T>> visitor) {
        TreeItemWalker<T> walker = new TreeItemWalker<>(tree);
        while (walker.hasNext()) {
            visitor.accept(walker.next());
        }
    }

    @SuppressWarnings("MethodCallInLoopCondition")
    public static <T> void visitItems(@NotNull TreeTableView<T> tree, @NotNull Consumer<T> visitor) {
        TreeItemWalker<T> walker = new TreeItemWalker<>(tree);
        while (walker.hasNext()) {
            visitor.accept(walker.next().getValue());
        }
    }

    @SuppressWarnings("MethodCallInLoopCondition")
    public static <T> void visitItems(@NotNull TreeView<T> tree, @NotNull Consumer<T> visitor) {
        TreeItemWalker<T> walker = new TreeItemWalker<>(tree);
        while (walker.hasNext()) {
            visitor.accept(walker.next().getValue());
        }
    }

    private boolean hasNext() {
        return !stack.isEmpty();
    }

    private void move() {
        TreeItemTuple<T> tuple = stack.pop();
        ObservableList<TreeItem<T>> children = tuple.getItem().getChildren();
        int index = tuple.getIndex() + 1;
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
        if (stack.peek() == null) throw new AssertionError();
        TreeItem<T> next = stack.peek().getItem();
        move();
        return next;
    }

    @NotNull
    public Stream<TreeItem<T>> stream() {
        return StreamSupport.stream(new TreeItemSpliterator(), false);
    }

    private static final class TreeItemTuple<T> {
        @NotNull
        private final TreeItem<T> item;
        private final int index;

        private TreeItemTuple(@NotNull TreeItem<T> item, int index) {
            this.item = item;
            this.index = index;
        }

        @NotNull
        private TreeItem<T> getItem() {
            return item;
        }

        public TreeItemTuple<T> setItem(@NotNull TreeItem<T> item) {
            return new TreeItemTuple<>(item, index);
        }

        private int getIndex() {
            return index;
        }

        private TreeItemTuple<T> setIndex(int index) {
            return new TreeItemTuple<>(item, index);
        }

        @Override
        public String toString() {
            return "TreeItemTuple [item=" + item + ", second=" + index + "]";
        }
    }

    @SuppressWarnings("unused")
    private final class TreeItemSpliterator implements Spliterator<TreeItem<T>> {

        @Override
        public boolean tryAdvance(@NotNull Consumer<? super TreeItem<T>> action) {
            if (hasNext()) {
                action.accept(next());
                return true;
            } else {
                return false;
            }
        }

        @Override
        @Nullable
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
