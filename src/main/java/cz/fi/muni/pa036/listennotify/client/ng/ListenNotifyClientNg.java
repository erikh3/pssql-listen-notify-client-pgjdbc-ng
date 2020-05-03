package cz.fi.muni.pa036.listennotify.client.ng;

import com.impossibl.postgres.api.jdbc.PGNotificationListener;
import cz.fi.muni.pa036.listennotify.api.AbstractListenNotifyClient;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.postgresql.core.Notification;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

/**
 * Client implementation for pgjdbc-ng driver
 *
 * @author Erik Horváth
 */
@Log
public class ListenNotifyClientNg extends AbstractListenNotifyClient {
    private final BlockingQueue<PGNotificationWrapper> textQueue = new ArrayBlockingQueue<>(1024*1024);
    private final BlockingQueue<PGNotificationWrapper> binaryQueue = new ArrayBlockingQueue<>(1024*1024);

    private Map<ChannelName, BlockingQueue<PGNotificationWrapper>> queues = new EnumMap<>(ChannelName.class);

    @Override
    public void run() {
        log.info("Started " + this.getClass().getSimpleName());

        queues.put(ChannelName.Q_EVENT, textQueue);
        queues.put(ChannelName.Q_EVENT_BIN, binaryQueue);

        Objects.requireNonNull(crudClient, "CRUD client must be initialized first.");

        // Create the notification listener
        PGNotificationListener listener = new PGNotificationListener() {
            @Override
            public void notification(int processId, String channelName, String payload) {
                Queue<PGNotificationWrapper> queue = queues.get(ChannelName.valueOf(channelName.toUpperCase()));

                if (queue != null) {
                    queue.add(new PGNotificationWrapper(channelName, processId, payload));
                }
            }
        };

        CrudClientJdbcNg crudClientJdbc = (CrudClientJdbcNg) this.crudClient;
        crudClientJdbc.setNotificationListener(listener);

        try {
            // keep thread running until interrupted
            while (!Thread.currentThread().isInterrupted()) {
                // do anything else (notifications are asynchronous)
                Thread.sleep(1000);
            }

        } catch (InterruptedException e) {
            log.info(this.getClass().getSimpleName() + " thread interrupted");
            Thread.currentThread().interrupt();
        } finally {
            textQueue.clear();
            binaryQueue.clear();
        }
    }

    private BlockingQueue<PGNotificationWrapper> getQueue(ChannelName channelName) {
        if (!queues.containsKey(channelName)) {
            throw new UnsupportedOperationException("Channel " + channelName + " not supported.");
        }
        return queues.get(channelName);
    }

    @SneakyThrows
    @Override
    protected String nextRawJson(ChannelName channelName) {
        return getQueue(channelName).take().getParameter();
    }

    @Override
    protected List<String> nextRawJson(ChannelName channelName, int noElements) {
        BlockingQueue<PGNotificationWrapper> queue = getQueue(channelName);
        if (queue.size() < noElements) {
            throw new IllegalArgumentException(String.format("Cannot drain event queue by the number of %d "
                    + "since it only contains %d elements.", noElements, queue.size()));
        }

        List<PGNotificationWrapper> result = new ArrayList<>(noElements);
        queue.drainTo(result, noElements);
        return result.stream().map(Notification::getParameter).collect(Collectors.toList());
    }
}
