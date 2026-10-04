package dev.easyfamily.easystack.Commands;

public class NoOpCommand extends EasyCommandBase {
    @Override
    public boolean isFinished() {
        return true;
    }
}
