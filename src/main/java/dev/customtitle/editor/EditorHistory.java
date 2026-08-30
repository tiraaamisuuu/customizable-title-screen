package dev.customtitle.editor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.UnaryOperator;

public final class EditorHistory<T> {
    private final int limit;
    private final UnaryOperator<T> copier;
    private final Deque<T> undo = new ArrayDeque<>();
    private final Deque<T> redo = new ArrayDeque<>();

    public EditorHistory(int limit, UnaryOperator<T> copier) {
        this.limit = Math.max(1, limit);
        this.copier = copier;
    }

    public void checkpoint(T current) {
        undo.push(copier.apply(current));
        while (undo.size() > limit) undo.removeLast();
        redo.clear();
    }

    public T undo(T current) {
        if (undo.isEmpty()) return current;
        redo.push(copier.apply(current));
        return undo.pop();
    }

    public T redo(T current) {
        if (redo.isEmpty()) return current;
        undo.push(copier.apply(current));
        return redo.pop();
    }

    public boolean canUndo() { return !undo.isEmpty(); }
    public boolean canRedo() { return !redo.isEmpty(); }
}
