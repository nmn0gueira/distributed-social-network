package sd2223.trab2.servers.mastodon;

import static sd2223.trab2.api.java.Result.error;
import static sd2223.trab2.api.java.Result.ok;
import static sd2223.trab2.api.java.Result.ErrorCode.*;

import java.util.List;

import com.google.gson.reflect.TypeToken;

import sd2223.trab2.api.Message;
import sd2223.trab2.api.PushMessage;
import sd2223.trab2.api.java.FeedsPull;
import sd2223.trab2.api.java.FeedsPush;
import sd2223.trab2.api.java.Result;
import sd2223.trab2.servers.mastodon.msgs.PostStatusArgs;
import sd2223.trab2.servers.mastodon.msgs.PostStatusResult;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;

import utils.JSON;

public class MastodonFeeds implements FeedsPush, FeedsPull {
	
	static String MASTODON_NOVA_SERVER_URI = "http://10.170.138.52:3000";
	static String MASTODON_SOCIAL_SERVER_URI = "https://mastodon.social";
	
	static String MASTODON_SERVER_URI = MASTODON_NOVA_SERVER_URI;
	
	private static final String clientKey = "F-xU2CFsGlZaBQoVjM8NRfL3Xj0ku812I1NoBFN_y4E";
	private static final String clientSecret = "cMpykugPT9ntPO3f9myAe0NiAwqPTjViSMnU7zqBuqs";
	private static final String accessTokenStr = "G50hfo4Awfi3KqvFhIFc0IaKkTMJzxUp1iyP5G3l3us";

	static final String STATUSES_PATH= "/api/v1/statuses";
	static final String TIMELINES_PATH = "/api/v1/timelines/home";
	static final String ACCOUNT_FOLLOWING_PATH = "/api/v1/accounts/%s/following";
	static final String VERIFY_CREDENTIALS_PATH = "/api/v1/accounts/verify_credentials";
	static final String SEARCH_ACCOUNTS_PATH = "/api/v1/accounts/search";
	static final String ACCOUNT_FOLLOW_PATH = "/api/v1/accounts/%s/follow";
	static final String ACCOUNT_UNFOLLOW_PATH = "/api/v1/accounts/%s/unfollow";
	
	private static final int HTTP_OK = 200;

	private static final int HTTP_BAD_REQUEST = 400;

	private static final int HTTP_FORBIDDEN = 403;
	private static final int HTTP_NOT_FOUND = 404;

	protected OAuth20Service service;
	protected OAuth2AccessToken accessToken;

	private static MastodonFeeds impl;
	
	protected MastodonFeeds() {
		try {
			service = new ServiceBuilder(clientKey).apiSecret(clientSecret).build(MastodonApi.instance());
			accessToken = new OAuth2AccessToken(accessTokenStr);
		} catch (Exception x) {
			x.printStackTrace();
			System.exit(0);
		}
	}

	synchronized public static MastodonFeeds getInstance() {
		if (impl == null)
			impl = new MastodonFeeds();
		return impl;
	}

	private String getEndpoint(String path, Object ... args ) {
		var fmt = MASTODON_SERVER_URI + path;
		return String.format(fmt, args);
	}
	
	@Override
	public Result<Long> postMessage(String user, String pwd, Message msg) {
		try {
			final OAuthRequest request = new OAuthRequest(Verb.POST, getEndpoint(STATUSES_PATH));

			JSON.toMap( new PostStatusArgs(msg.getText())).forEach( (k, v) -> {
				request.addBodyParameter(k, v.toString());
			});
			
			service.signRequest(accessToken, request);

			Response response = service.execute(request);
			if (response.getCode() == HTTP_OK) {
				var res = JSON.decode(response.getBody(), PostStatusResult.class);
				return ok(res.getId());
			}

			return error(getErrorCode(response.getCode()));

		} catch (Exception x) {
			x.printStackTrace();
		}
		return error(INTERNAL_ERROR);
	}


	@Override
	public Result<List<Message>> getMessages(String user, long time) {
		try {
			final OAuthRequest request = new OAuthRequest(Verb.GET, getEndpoint(TIMELINES_PATH));

			service.signRequest(accessToken, request);

			Response response = service.execute(request);

			if (response.getCode() == HTTP_OK) {
				List<PostStatusResult> res = JSON.decode(response.getBody(), new TypeToken<List<PostStatusResult>>() {
				});

				return ok(res.stream()
						.filter(result -> result.getCreationTime() > time)
						.map(PostStatusResult::toMessage)
						.toList());
			}

			return error(getErrorCode(response.getCode()));

		} catch (Exception x) {
			x.printStackTrace();
		}
		return error(Result.ErrorCode.INTERNAL_ERROR);
	}

	
	@Override
	public Result<Void> removeFromPersonalFeed(String user, long mid, String pwd) {
		try {
			var endpoint_url = getEndpoint(STATUSES_PATH) + "/" + mid;
			final OAuthRequest request = new OAuthRequest(Verb.DELETE, endpoint_url);

			service.signRequest(accessToken, request);

			Response response = service.execute(request);

			if (response.getCode() == HTTP_OK) {
				return ok();
			}

			return error(getErrorCode(response.getCode()));

		} catch (Exception x) {
			x.printStackTrace();
		}
		return error(INTERNAL_ERROR);

	}

	@Override
	public Result<Message> getMessage(String user, long mid) {
		try {
			var endpoint_url = getEndpoint(STATUSES_PATH) + "/" + mid;
			final OAuthRequest request = new OAuthRequest(Verb.GET, endpoint_url);

			service.signRequest(accessToken, request);

			Response response = service.execute(request);

			if (response.getCode() == HTTP_OK) {
				var res = JSON.decode(response.getBody(), PostStatusResult.class);
				return ok(res.toMessage());
			}

			return error(getErrorCode(response.getCode()));

		} catch (Exception x) {
			x.printStackTrace();
		}
		return error(INTERNAL_ERROR);
	}

	@Override
	public Result<Void> subUser(String user, String userSub, String pwd) {
		try {
			var res = getUserId(userSub);

			if (!res.isOK()) {
				return error(res.error());
			}

			var endpoint_url = getEndpoint(ACCOUNT_FOLLOW_PATH, res.value());
			final OAuthRequest request = new OAuthRequest(Verb.POST, endpoint_url);

			service.signRequest(accessToken, request);

			Response response = service.execute(request);

			if (response.getCode() == HTTP_OK) {
				return ok();
			}

			return error(getErrorCode(response.getCode()));

		} catch (Exception x) {
			x.printStackTrace();
		}
		return error(INTERNAL_ERROR);
	}

	@Override
	public Result<Void> unsubscribeUser(String user, String userSub, String pwd) {
		try {
			var res = getUserId(userSub);

			if (!res.isOK()) {
				return error(res.error());
			}

			System.out.println("ID USER: " +res);

			var endpoint_url = getEndpoint(ACCOUNT_UNFOLLOW_PATH, res.value());
			final OAuthRequest request = new OAuthRequest(Verb.POST, endpoint_url);

			service.signRequest(accessToken, request);

			Response response = service.execute(request);

			if (response.getCode() == HTTP_OK) {
				return ok();
			}

			return error(getErrorCode(response.getCode()));

		} catch (Exception x) {
			x.printStackTrace();
		}
		return error(INTERNAL_ERROR);
	}

	@Override
	public Result<List<String>> listSubs(String user) {
		try {
			var res1 = getUserId(user);
			if (!res1.isOK()) {
				return error(res1.error());
			}

			var endpoint_url = getEndpoint(ACCOUNT_FOLLOWING_PATH, res1.value());
			final OAuthRequest request = new OAuthRequest(Verb.GET, endpoint_url);

			service.signRequest(accessToken, request);

			Response response = service.execute(request);

			if (response.getCode() == HTTP_OK) {
				List<PostStatusResult> res2 = JSON.decode(response.getBody(), new TypeToken<List<PostStatusResult>>() {
				});

				return ok(res2.stream().map(PostStatusResult::getText).toList());
			}

			return error(getErrorCode(response.getCode()));

		} catch (Exception x) {
			x.printStackTrace();
		}
		return error(INTERNAL_ERROR);
	}

	private Result<Long> getUserId(String userName) {
		try {
			var endpoint_url = getEndpoint(SEARCH_ACCOUNTS_PATH);
			final OAuthRequest request = new OAuthRequest(Verb.GET, endpoint_url);

			request.addQuerystringParameter("q", userName);

			service.signRequest(accessToken, request);

			Response response = service.execute(request);

			if (response.getCode() == HTTP_OK) {
				List<PostStatusResult> res = JSON.decode(response.getBody(), new TypeToken<List<PostStatusResult>>() {
				});

				return ok(res.get(0).getId());

			}
		} catch (Exception x) {
			x.printStackTrace();
		}
		return error(INTERNAL_ERROR);
	}

	@Override
	public Result<Void> deleteUserFeed(String user) {
		return error(NOT_IMPLEMENTED);
	}

	@Override
	public Result<List<Message>> pull_getTimeFilteredPersonalFeed(String user, long time) {
		return error(NOT_IMPLEMENTED);
	}

	@Override
	public Result<Void> push_updateFollowers(String user, String follower, boolean following) {
		return error(NOT_IMPLEMENTED);
	}

	@Override
	public Result<Void> push_PushMessage(PushMessage msg) {
		return error(NOT_IMPLEMENTED);
	}

	private Result.ErrorCode getErrorCode(int code) {
		return switch (code) {
			case HTTP_BAD_REQUEST -> BAD_REQUEST;
			case HTTP_FORBIDDEN -> FORBIDDEN;
			case HTTP_NOT_FOUND -> NOT_FOUND;
			default -> Result.ErrorCode.INTERNAL_ERROR;
		};
	}
}
