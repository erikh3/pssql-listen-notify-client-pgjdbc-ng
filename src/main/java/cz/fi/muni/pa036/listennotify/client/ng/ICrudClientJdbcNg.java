package cz.fi.muni.pa036.listennotify.client.ng;

import java.util.List;

/**
 * Additional public methods for CRUD client with pgjdbc-ng driver
 *
 * @author Erik Horváth
 */
public interface ICrudClientJdbcNg {
    /**
     * Gets notifications as list, notifications will be deleted afterwards
     *
     * @return list of notifications
     */
    List<PGNotificationWrapper> getNotifications();

    /**
     * Gets notifications as array, notifications will be deleted afterwards
     *
     * @return array of notifications
     */
    PGNotificationWrapper[] getNotificationsAsArray();
}
