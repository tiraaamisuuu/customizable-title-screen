package dev.customtitle.editor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EditorHistoryTest {
    @Test
    void undoRedoUsesDefensiveCopies() {
        EditorHistory<Box> history = new EditorHistory<>(10, Box::copy);
        Box current = new Box(1);
        history.checkpoint(current);
        current.value = 2;
        current = history.undo(current);
        assertEquals(1, current.value);
        current = history.redo(current);
        assertEquals(2, current.value);
    }

    @Test
    void newCheckpointClearsRedo() {
        EditorHistory<Box> history = new EditorHistory<>(10, Box::copy);
        Box current = new Box(1);
        history.checkpoint(current);
        current = history.undo(new Box(2));
        assertTrue(history.canRedo());
        history.checkpoint(current);
        assertFalse(history.canRedo());
    }

    private static final class Box {
        int value;
        Box(int value) { this.value = value; }
        Box copy() { return new Box(value); }
    }
}
