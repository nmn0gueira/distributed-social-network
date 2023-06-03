package sd2223.trab2.servers;

public class Domain {
	static String domain;
	static long uuid;

	static long offset = 0;
	
	public static void set( String _domain, long _uuid) {
		domain = _domain;
		uuid = _uuid;
	}

	public static void setOffset( long _offset ) {
		offset = _offset;
	}
	
	public static String get() {
		return domain;
	}

	public static long uuid() {
		return uuid;
	}

	public static long offset() {
		return offset;
	}

	public static void incOffset() {
		offset++;
	}
	
	public static boolean isRemoteUser(String user) {
		var parts = user.split("@");
		return parts.length > 1 && ! parts[1].equals( domain );
	}
}
