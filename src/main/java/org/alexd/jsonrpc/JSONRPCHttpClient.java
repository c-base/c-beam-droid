package org.alexd.jsonrpc;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * Implementation of JSON-RPC over HTTP/POST
 */
public class JSONRPCHttpClient extends JSONRPCClient
{

	/*
	 * HttpClient to issue the HTTP/POST request
	 */
	private HttpClient httpClient;
	/*
	 * Service URI
	 */
	private String serviceUri;

	// HTTP 1.0
	private static final ProtocolVersion PROTOCOL_VERSION = new ProtocolVersion("HTTP", 1, 0);

	/**
	 * Construct a JsonRPCClient with the given httpClient and service uri
	 *
	 * @param client
	 *            httpClient to use
	 * @param uri
	 *            uri of the service
	 */
	public JSONRPCHttpClient(HttpClient cleint, String uri){
		httpClient = cleint;
		serviceUri = uri;


		/* SSLContext.setDefault() is not available on Android 2.2 - but we currently don't support SSL anyway
		SSLContext ctx;
		try {
			ctx = SSLContext.getInstance("TLS");
			ctx.init(new KeyManager[0], new TrustManager[] {new DefaultTrustManager()}, new SecureRandom());
			SSLContext.setDefault(ctx);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/


		httpClient.getParams();
		System.out.println("");
	}

	/**
	 * Construct a JsonRPCClient with the given service uri
	 *
	 * @param uri
	 *            uri of the service
	 */
	public JSONRPCHttpClient(String uri)
	{
		this(new DefaultHttpClient(), uri);
	}

	protected JSONObject doJSONRequest(JSONObject jsonRequest) throws JSONRPCException
	{
		// Create HTTP/POST request with a JSON entity containing the request
		HttpPost request = new HttpPost(serviceUri);
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, getConnectionTimeout());
		HttpConnectionParams.setSoTimeout(params, getSoTimeout());
		HttpProtocolParams.setVersion(params, PROTOCOL_VERSION);
		request.setParams(params);

		if(_debug){
			Log.i(JSONRPCHttpClient.class.toString(), "Request: " + jsonRequest.toString());
		}

		HttpEntity entity;

		try
		{
			if(encoding.length() > 0){
				entity = new JSONEntity(jsonRequest, encoding);
			}
			else{
				entity = new JSONEntity(jsonRequest);
			}
		}
		catch (UnsupportedEncodingException e1)
		{
			throw new JSONRPCException("Unsupported encoding", e1);
		}
		request.setEntity(entity);

		try
		{
			// Execute the request and try to decode the JSON Response
			long t = System.currentTimeMillis();
			HttpResponse response = httpClient.execute(request);


			t = System.currentTimeMillis() - t;
			String responseString = EntityUtils.toString(response.getEntity());

			responseString = responseString.trim();

			if(_debug){
				Log.i(JSONRPCHttpClient.class.toString(), "Response: " + responseString);
			}

			JSONObject jsonResponse = new JSONObject(responseString);
			// Check for remote errors
			if (jsonResponse.has("error"))
			{
				Object jsonError = jsonResponse.get("error");
				if (!jsonError.equals(null))
					throw new JSONRPCException(jsonResponse.get("error"));
				return jsonResponse; // JSON-RPC 1.0
			}
			else
			{
				return jsonResponse; // JSON-RPC 2.0
			}
		}
		// Underlying errors are wrapped into a JSONRPCException instance
		catch (ClientProtocolException e)
		{
			throw new JSONRPCException("HTTP error", e);
		}
		catch (IOException e)
		{
			throw new JSONRPCException("IO error", e);
		}
		catch (JSONException e)
		{
			throw new JSONRPCException("Invalid JSON response", e);
		}
	}
}
