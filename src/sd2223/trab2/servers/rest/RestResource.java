package sd2223.trab2.servers.rest;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import sd2223.trab2.api.java.Result;
import sd2223.trab2.api.java.Result.ErrorCode;
import sd2223.trab2.api.rest.FeedsServiceRep;
import sd2223.trab2.servers.kafka.sync.SyncPoint;


public class RestResource {

	/**
	 * Given a Result<T>, either returns the value, or throws the JAX-WS Exception
	 * matching the error code...
	 */
	protected <T> T fromJavaResult(Result<T> result) {
		if (result.isOK())
			return result.value();
		if( result.error() == ErrorCode.REDIRECTED && result.errorValue() != null )
			return result.errorValue();

		throw new WebApplicationException(statusCodeFrom(result));
	}
	@SuppressWarnings("unchecked")
	protected <T> T fromJavaResult(Result<T> result, Long version) {
		if (result.isOK()) {
			var sync = SyncPoint.getInstance();
			sync.setOffset(0);
			Result<T> res = (Result<T>)sync.waitForResult(version+=1L);
			var value = res.value();
			var status = value == null ? 204 : 200;
			throw new WebApplicationException(Response.status(status).
					header(FeedsServiceRep.HEADER_VERSION, version).
					encoding(MediaType.APPLICATION_JSON).entity(value).build());
		}
		if( result.error() == ErrorCode.REDIRECTED && result.errorValue() != null ) {
			return result.errorValue();
		}
		throw new WebApplicationException(statusCodeFrom(result));
	}

	/**
	 * Translates a Result<T> to an HTTP Status code
	 */
	private static Status statusCodeFrom(Result<?> result) {
		return switch (result.error()) {
			case CONFLICT -> Status.CONFLICT;
			case NOT_FOUND -> Status.NOT_FOUND;
			case FORBIDDEN -> Status.FORBIDDEN;
			case TIMEOUT, BAD_REQUEST -> Status.BAD_REQUEST;
			case NOT_IMPLEMENTED -> Status.NOT_IMPLEMENTED;
			case REDIRECTED -> result.errorValue() == null ? Status.NO_CONTENT : Status.OK;
			case OK -> result.value() == null ? Status.NO_CONTENT : Status.OK;
			default -> Status.INTERNAL_SERVER_ERROR;
		};
	}

}