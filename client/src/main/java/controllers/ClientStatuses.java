package controllers;

/**
 * Client statuses
 */
public enum ClientStatuses {
    OFFLINE, ONLINE, CONNECTING;

    private static final ClientStatuses[] LIST = ClientStatuses.values();

    public static ClientStatuses getClientStatus(int i) {
        return LIST[i];
    }

    public static int length() {
        return LIST.length;
    }
}
