package dev.easyfamily.easystack.util;
public class EdgeDetector {
    private boolean last;
    private boolean current;
    private boolean initialized = false;

    public EdgeDetector() {}

    public boolean update(boolean value) {
        last = initialized ? current : value;
        current = value;
        initialized = true;
        return value;
    }

    public boolean rising() { return current && !last; }

    public boolean falling() { return !current && last; }

    public boolean changed() { return current != last; }

    public boolean get() { return current; }
}
