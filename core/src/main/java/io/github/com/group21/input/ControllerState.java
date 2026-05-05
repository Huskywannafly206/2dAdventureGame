package io.github.com.group21.input;

public interface ControllerState {
    void keyDown(Command command);

    default void keyUp(Command command) {
    }
}
