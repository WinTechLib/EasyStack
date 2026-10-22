package dev.easyfamily.wintechlib.Commands;

public class NoOpCommand extends EasyCommandBase {
    @Override
    public boolean isFinished() {
        return true;
    }
}
