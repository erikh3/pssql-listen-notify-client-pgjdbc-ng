package cz.fi.muni.pa036.listennotify.client.ng;

import lombok.Getter;
import org.postgresql.core.Notification;

/**
 * Wrapper for notification from database
 *
 * @author Erik Horváth
 */
public class PGNotificationWrapper extends Notification {
    @Getter
    private final long creationNanoTime;

    public PGNotificationWrapper(String name, int pid, String parameter) {
        super(name, pid, parameter);
        creationNanoTime = System.nanoTime();
    }
}
