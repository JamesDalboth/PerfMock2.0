package uk.jamesdal.perfmock.perf.events;

import uk.jamesdal.perfmock.perf.SimEvent;

public class ForkEvent extends SimEvent {
    private final long parent;

    public ForkEvent(double simTime, double realTime, long child) {
        super(simTime, realTime, child);
        this.parent = Thread.currentThread().getId();
    }

    @Override
    public EventTypes getType() {
        return EventTypes.FORK;
    }

    public long getParent() {
        return parent;
    }
}
