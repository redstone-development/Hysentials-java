package llc.redstone.hysentials.event;

/**
 * Core event class
 */
public class Event {
    public void post() {
        EventBus.INSTANCE.post(this);
    }
}
