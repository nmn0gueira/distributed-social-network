package sd2223.trab2.api.soap.pull;

import java.util.List;

import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import sd2223.trab2.api.Message;
import sd2223.trab2.api.soap.FeedsException;

@WebService(serviceName=FeedsService.NAME, targetNamespace=FeedsService.NAMESPACE, endpointInterface=FeedsService.INTERFACE)
public interface FeedsService extends sd2223.trab2.api.soap.FeedsService {
	String INTERFACE = "sd2223.trab2.api.soap.pull.FeedsService";

	@WebMethod
	List<Message> pull_getTimeFilteredPersonalFeed(String user, long time) throws FeedsException;		
}
