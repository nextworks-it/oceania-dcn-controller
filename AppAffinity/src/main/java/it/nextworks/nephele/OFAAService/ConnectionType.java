package it.nextworks.nephele.OFAAService;

public enum ConnectionType {

    POINT_TO_POINT(0),
    POINT_TO_MULTIPOINT(1);

    public final int value;

    ConnectionType(int value) {
        this.value = value;
    }

    public static ConnectionType getFromValue(int value) {
        for (ConnectionType connectionType : ConnectionType.values()) {
            if (connectionType.value == value) {
                return connectionType;
            }
        }
        throw new IllegalArgumentException(String.format("ConnectionType index %s not valid.", value));
    }

}
