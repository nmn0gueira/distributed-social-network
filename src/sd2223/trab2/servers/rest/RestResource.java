package sd2223.trab2.servers.rest;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import sd2223.trab2.api.java.Result;
import sd2223.trab2.api.java.Result.ErrorCode;
import sd2223.trab2.api.rest.FeedsServiceRep;
import sd2223.trab2.servers.kafka.sync.SyncPoint;

import java.util.logging.Logger;

public class RestResource {

	private static final Logger Log = Logger.getLogger(RestResource.class.getName());

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
		System.out.println("fromJavaResult: " + version);
		if (result.isOK()) {
			Result<T> res = (Result<T>)SyncPoint.getInstance().waitForResult(version+=1L);
			throw new WebApplicationException(Response.status(200).
					header(FeedsServiceRep.HEADER_VERSION, version).
					encoding(MediaType.APPLICATION_JSON).entity(res.value()).build());
		}
		if( result.error() == ErrorCode.REDIRECTED && result.errorValue() != null )
			return result.errorValue();

		throw new WebApplicationException(statusCodeFrom(result));
	}

	/**
	 * Translates a Result<T> to a HTTP Status code
	 */
	private static Status statusCodeFrom(Result<?> result) {
		switch (result.error()) {
		case CONFLICT:
			return Status.CONFLICT;
		case NOT_FOUND:
			return Status.NOT_FOUND;
		case FORBIDDEN:
			return Status.FORBIDDEN;
		case TIMEOUT:
		case BAD_REQUEST:
			return Status.BAD_REQUEST;
		case NOT_IMPLEMENTED:
			return Status.NOT_IMPLEMENTED;
		case INTERNAL_ERROR:
			return Status.INTERNAL_SERVER_ERROR;
		case REDIRECTED:
			return result.errorValue() == null ? Status.NO_CONTENT : Status.OK;
		case OK:
			return result.value() == null ? Status.NO_CONTENT : Status.OK;
			
		default:
			return Status.INTERNAL_SERVER_ERROR;
		}
	}

}