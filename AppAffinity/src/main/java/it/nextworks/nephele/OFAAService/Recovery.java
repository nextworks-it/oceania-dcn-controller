package it.nextworks.nephele.OFAAService;

public enum Recovery {
	
	UNPROTECTED(0),
	PROTECTED(1),
	FASTRECOVERY(2);

	public final int value;

	Recovery(int value) {
		this.value = value;
	}

	public static Recovery getFromValue(int value) {
		for (Recovery recovery : Recovery.values()) {
			if (recovery.value == value) {
				return recovery;
			}
		}
		throw new IllegalArgumentException(String.format("Recovery index %s not valid.", value));
	}

}
