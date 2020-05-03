package cz.fi.muni.pa036.listennotify.client.ng;

import com.impossibl.postgres.api.jdbc.PGConnection;
import com.impossibl.postgres.api.jdbc.PGNotificationListener;
import com.impossibl.postgres.jdbc.PGDataSource;
import cz.fi.muni.pa036.listennotify.api.CrudClient;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Implementation of the CrudClient using standard low-level JDBC API.
 *
 * @author Erik Horváth
 */
@Log
public class CrudClientJdbcNg extends CrudClient implements ICrudClientJdbcNg {
    private static final int QUEUE_CAPACITY = 1024;

    /**
     * Provides access to extended features and functionality
     */
    @Getter
    private PGConnection pgConnection;

    @Getter
    private Connection connection;

    private PGNotificationListener customListener;

    // Create the queue for notifications
    @Getter
    private BlockingQueue<PGNotificationWrapper> queue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);

    public CrudClientJdbcNg() {
        PGDataSource ds = new PGDataSource();
        ds.setHost(ConfigProperties.get("db.host"));
        ds.setPort(Integer.parseInt(ConfigProperties.get("db.port")));
        ds.setDatabaseName(ConfigProperties.get("db.name"));
        ds.setUser(ConfigProperties.get("db.user"));
        ds.setPassword(ConfigProperties.get("db.password"));

        try {
            connection = ds.getConnection();
            pgConnection = connection.unwrap(PGConnection.class);
            pgConnection.addNotificationListener(defaultNotificationListener());
        } catch (SQLException e) {
            log.severe("SQL exception while creating connection to database " + e.getMessage());
        }
    }

    @SneakyThrows
    @Override
    protected Statement createStatement() {
        return pgConnection.createStatement();
    }

    @SneakyThrows
    @Override
    protected PreparedStatement createPreparedStatement(String s) {
        return pgConnection.prepareStatement(s);
    }

    public List<PGNotificationWrapper> getNotifications() {
        List<PGNotificationWrapper> list = new ArrayList<>();
        queue.drainTo(list);
        return list;
    }

    @Override
    public PGNotificationWrapper[] getNotificationsAsArray() {
        return getNotifications().toArray(new PGNotificationWrapper[]{});
    }

    /**
     * Defines additional behaviour on notification received event
     *
     * @param listener the listener to execute
     */
    public void setNotificationListener(PGNotificationListener listener) {
        if (this.customListener != null) {
            pgConnection.removeNotificationListener(this.customListener);
        }

        this.customListener = listener;
        pgConnection.addNotificationListener(listener);
    }

    /**
     * Defines default behaviour on notification received event
     *
     * @return the listener to execute
     */
    private PGNotificationListener defaultNotificationListener() {
        return new PGNotificationListener() {
            @Override
            public void notification(int processId, String channelName, String payload) {
                PGNotificationWrapper notification = new PGNotificationWrapper(channelName, processId,payload);

                try {
                    queue.add(notification);
                    log.info("Notification received, on a channel: " + channelName + ", with payload: " + payload);
                } catch (IllegalStateException e) {
                    log.warning("Notification queue full, new notification cannot be added");
                }
            }
        };
    }
}
