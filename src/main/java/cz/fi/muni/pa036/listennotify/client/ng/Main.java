package cz.fi.muni.pa036.listennotify.client.ng;

/**
 * Runnable class
 *
 * @author Erik Horváth
 */
public class Main {
    public static void main(String[] args) {
        ListenNotifyClientNg client = new ListenNotifyClientNg();
        client.setCrudClient(new CrudClientJdbcNg());
        client.start();
    }
}
