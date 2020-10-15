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

public final class TreeTableViewWalker<T> {

    @NotNull
    private final Deque<Tuple<TreeItem<T>, Integer>> stack = new ArrayDeque<>(2);

    private TreeTableViewWalker(@NotNull TreeTableView<T> tree) {
        if (tree.getRoot() != null) {
            stack.push(new Tuple<>(tree.getRoot(), -1));
        }
    }

    private TreeTableViewWalker(@NotNull TreeView<T> tree) {
        if (tree.getRoot() != null) {
            stack.push(new Tuple<>(tree.getRoot(), -1));
        }
    }

    @SuppressWarnings("MethodCallInLoopCondition")
    public static <T> void visit(@NotNull TreeTableView<T> tree, @NotNull Consumer<TreeItem<T>> visitor) {
        TreeTableViewWalker<T> walker = new TreeTableViewWalker<>(tree);
        while (walker.hasNext()) {
            visitor.accept(walker.next());
        }
    }

    @SuppressWarnings("MethodCallInLoopCondition")
    public static <T> void visit(@NotNull TreeView<T> tree, @NotNull Consumer<TreeItem<T>> visitor) {
        TreeTableViewWalker<T> walker = new TreeTableViewWalker<>(tree);
        while (walker.hasNext()) {
            visitor.accept(walker.next());
        }
    }

    @SuppressWarnings("MethodCallInLoopCondition")
    public static <T> void visitItems(@NotNull TreeTableView<T> tree, @NotNull Consumer<T> visitor) {
        TreeTableViewWalker<T> walker = new TreeTableViewWalker<>(tree);
        while (walker.hasNext()) {
            visitor.accept(walker.next().getValue());
        }
    }

    @SuppressWarnings("MethodCallInLoopCondition")
    public static <T> void visitItems(@NotNull TreeView<T> tree, @NotNull Consumer<T> visitor) {
        TreeTableViewWalker<T> walker = new TreeTableViewWalker<>(tree);
        while (walker.hasNext()) {
            visitor.accept(walker.next().getValue());
        }
    }

    private boolean hasNext() {
        return !stack.isEmpty();
    }

    private void move() {
        Tuple<TreeItem<T>, Integer> tuple = stack.pop();
        ObservableList<TreeItem<T>> children = tuple.getFirst().getChildren();
        int idx = tuple.getSecond() + 1;
        if (children.size() <= idx) {
            if (!stack.isEmpty()) {
                move();
            }
        } else {
            stack.push(tuple.setSecond(idx));
            stack.push(new Tuple<>(children.get(idx), -1));
        }
    }

    private TreeItem<T> next() {
        if (!hasNext()) {
            throw new IllegalStateException("");
        }
        if (stack.peek() == null) throw new AssertionError();
        TreeItem<T> nxt = stack.peek().getFirst();
        move();
        return nxt;
    }

    @NotNull
    public Stream<TreeItem<T>> stream() {
        return StreamSupport.stream(new TreeItemSpliterator(), false);
    }

    private static final class Tuple<E, F> {
        @NotNull
        private final E first;
        @NotNull
        private final F second;

        private Tuple(@NotNull E first, @NotNull F second) {
            this.first = first;
            this.second = second;
        }

        @NotNull
        private E getFirst() {
            return first;
        }

        public Tuple<E, F> setFirst(@NotNull E first) {
            return new Tuple<>(first, second);
        }

        @NotNull
        private F getSecond() {
            return second;
        }

        private Tuple<E, F> setSecond(@NotNull F second) {
            return new Tuple<>(first, second);
        }

        @Override
        public String toString() {
            return "Tuple [first=" + first + ", second=" + second + "]";
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
