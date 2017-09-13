package it.nextworks.nephele.OFAAService;

public enum ServiceStatus {
	
	SCHEDULED(0),
	ESTABLISHING(1),
	ACTIVE(2),
	TERMINATING(3),
	DELETED(4);

	public final int value;

	ServiceStatus(int value) {
		this.value = value;
	}

	public static ServiceStatus getFromValue(int value) {
		for (ServiceStatus serviceStatus : ServiceStatus.values()) {
			if (serviceStatus.value == value) {
				return serviceStatus;
			}
		}
		throw new IllegalArgumentException(String.format("ServiceStatus index %s not valid.", value));
	}

}
