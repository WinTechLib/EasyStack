package dev.easyfamily.EasyStack.components;

import dev.easyfamily.EasyStack.Commands.EasyCommandScheduler;

public class EasyCommandComponent implements EasyComponent {

    private final EasyCommandScheduler scheduler;

    public EasyCommandComponent() {
        this.scheduler = EasyCommandScheduler.getInstance();
    }

    public EasyCommandComponent(EasyCommandScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void loop() {
        scheduler.run();
    }

    @Override
    public void stop() {
        scheduler.cancelAll();
    }
}