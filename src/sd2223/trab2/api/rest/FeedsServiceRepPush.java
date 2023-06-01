package sd2223.trab2.api.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import sd2223.trab2.api.PushMessage;

public interface FeedsServiceRepPush extends FeedsServiceRep {
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	void push_PushMessage(@HeaderParam(HEADER_VERSION) Long version, PushMessage msg);

	@PUT
	@Path("/followers/{" + USERSUB + "}/{" + USER + "}")
	@Consumes(MediaType.APPLICATION_JSON)
	void push_updateFollowers(@HeaderParam(HEADER_VERSION) Long version, @PathParam(USERSUB) String user, @PathParam(USER) String follower, boolean following);
}
