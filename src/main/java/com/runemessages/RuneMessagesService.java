/*
 * Copyright (c) 2025, LordStrange
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.runemessages;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
@Singleton
public class RuneMessagesService
{
	private static final String API_URL = "https://runemessages-api-production.up.railway.app";
	private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

	private final OkHttpClient httpClient;
	private final Gson gson;

	@Getter
	@Setter
	private String apiKey;

	@Inject
	public RuneMessagesService(OkHttpClient httpClient, Gson gson)
	{
		this.httpClient = httpClient;
		this.gson = gson;
	}

	public CompletableFuture<String> register(String username)
	{
		CompletableFuture<String> future = new CompletableFuture<>();

		JsonObject body = new JsonObject();
		body.addProperty("username", username);

		Request request = new Request.Builder()
			.url(API_URL + "/register")
			.post(RequestBody.create(JSON, gson.toJson(body)))
			.build();

		httpClient.newCall(request).enqueue(new Callback()
		{
			@Override
			public void onFailure(Call call, IOException e)
			{
				log.warn("Failed to register: {}", e.getMessage());
				future.completeExceptionally(e);
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException
			{
				try
				{
					if (!response.isSuccessful())
					{
						future.completeExceptionally(new IOException("Registration failed: HTTP " + response.code()));
						return;
					}

					String responseBody = response.body().string();
					JsonObject json = gson.fromJson(responseBody, JsonObject.class);

					if (json.has("apiKey"))
					{
						String key = json.get("apiKey").getAsString();
						apiKey = key;
						future.complete(key);
					}
					else
					{
						future.completeExceptionally(new IOException("No API key in response"));
					}
				}
				finally
				{
					response.close();
				}
			}
		});

		return future;
	}

	public CompletableFuture<Boolean> verifyApiKey(String key)
	{
		CompletableFuture<Boolean> future = new CompletableFuture<>();

		Request request = new Request.Builder()
			.url(API_URL + "/verify")
			.header("X-API-Key", key)
			.get()
			.build();

		httpClient.newCall(request).enqueue(new Callback()
		{
			@Override
			public void onFailure(Call call, IOException e)
			{
				log.warn("Failed to verify API key: {}", e.getMessage());
				future.complete(false);
			}

			@Override
			public void onResponse(Call call, Response response)
			{
				response.close();
				future.complete(response.isSuccessful());
			}
		});

		return future;
	}

	public CompletableFuture<List<MessageData>> getMessagesForRegion(int worldId, int regionId)
	{
		CompletableFuture<List<MessageData>> future = new CompletableFuture<>();

		String url = API_URL + "/messages?worldId=" + worldId + "&regionId=" + regionId;

		Request request = new Request.Builder()
			.url(url)
			.get()
			.build();

		httpClient.newCall(request).enqueue(new Callback()
		{
			@Override
			public void onFailure(Call call, IOException e)
			{
				log.warn("Failed to fetch messages for region {}: {}", regionId, e.getMessage());
				future.completeExceptionally(e);
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException
			{
				try
				{
					if (!response.isSuccessful())
					{
						log.warn("Failed to fetch messages: HTTP {}", response.code());
						future.complete(new ArrayList<>());
						return;
					}

					String responseBody = response.body().string();
					List<MessageData> messages = parseMessageArray(responseBody);
					log.debug("Fetched {} messages for region {}", messages.size(), regionId);
					future.complete(messages);
				}
				finally
				{
					response.close();
				}
			}
		});

		return future;
	}

	public CompletableFuture<List<MessageData>> getAuthorMessagesInWorld(int worldId, String author)
	{
		CompletableFuture<List<MessageData>> future = new CompletableFuture<>();

		if (apiKey == null || apiKey.isEmpty())
		{
			future.complete(new ArrayList<>());
			return future;
		}

		String url = API_URL + "/messages/mine?worldId=" + worldId;

		Request request = new Request.Builder()
			.url(url)
			.header("X-API-Key", apiKey)
			.get()
			.build();

		httpClient.newCall(request).enqueue(new Callback()
		{
			@Override
			public void onFailure(Call call, IOException e)
			{
				log.warn("Failed to fetch author messages: {}", e.getMessage());
				future.complete(new ArrayList<>());
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException
			{
				try
				{
					if (!response.isSuccessful())
					{
						future.complete(new ArrayList<>());
						return;
					}

					String responseBody = response.body().string();
					List<MessageData> messages = parseMessageArray(responseBody);
					future.complete(messages);
				}
				finally
				{
					response.close();
				}
			}
		});

		return future;
	}

	public CompletableFuture<MessageData> saveMessage(WorldPoint location, String message, String author, int worldId, int modelId)
	{
		CompletableFuture<MessageData> future = new CompletableFuture<>();

		if (apiKey == null || apiKey.isEmpty())
		{
			future.completeExceptionally(new IOException("Not authenticated"));
			return future;
		}

		int regionId = location.getRegionID();

		JsonObject body = new JsonObject();
		body.addProperty("worldId", worldId);
		body.addProperty("regionId", regionId);
		body.addProperty("message", message);
		body.addProperty("x", location.getX());
		body.addProperty("y", location.getY());
		body.addProperty("plane", location.getPlane());
		body.addProperty("modelId", modelId);

		Request request = new Request.Builder()
			.url(API_URL + "/messages")
			.header("X-API-Key", apiKey)
			.post(RequestBody.create(JSON, gson.toJson(body)))
			.build();

		httpClient.newCall(request).enqueue(new Callback()
		{
			@Override
			public void onFailure(Call call, IOException e)
			{
				log.warn("Failed to save message: {}", e.getMessage());
				future.completeExceptionally(e);
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException
			{
				try
				{
					String responseBody = response.body().string();

					if (!response.isSuccessful())
					{
						try
						{
							JsonObject errorJson = gson.fromJson(responseBody, JsonObject.class);
							String errorMsg = errorJson.has("error") ? errorJson.get("error").getAsString() : "Unknown error";
							future.completeExceptionally(new IOException(errorMsg));
						}
						catch (Exception e)
						{
							future.completeExceptionally(new IOException("HTTP " + response.code()));
						}
						return;
					}

					MessageData data = gson.fromJson(responseBody, MessageData.class);
					log.debug("Saved message '{}' by {} at region {}", message, author, regionId);
					future.complete(data);
				}
				finally
				{
					response.close();
				}
			}
		});

		return future;
	}

	public CompletableFuture<Void> deleteMessage(int worldId, int regionId, String messageId)
	{
		CompletableFuture<Void> future = new CompletableFuture<>();

		if (apiKey == null || apiKey.isEmpty())
		{
			future.completeExceptionally(new IOException("Not authenticated"));
			return future;
		}

		String url = API_URL + "/messages/" + worldId + "/" + regionId + "/" + messageId;

		Request request = new Request.Builder()
			.url(url)
			.header("X-API-Key", apiKey)
			.delete()
			.build();

		httpClient.newCall(request).enqueue(new Callback()
		{
			@Override
			public void onFailure(Call call, IOException e)
			{
				log.warn("Failed to delete message: {}", e.getMessage());
				future.completeExceptionally(e);
			}

			@Override
			public void onResponse(Call call, Response response)
			{
				response.close();
				if (response.isSuccessful())
				{
					log.debug("Deleted message {}", messageId);
					future.complete(null);
				}
				else
				{
					future.completeExceptionally(new IOException("HTTP " + response.code()));
				}
			}
		});

		return future;
	}

	public CompletableFuture<Void> rateMessage(MessageData message, boolean thumbsUp)
	{
		CompletableFuture<Void> future = new CompletableFuture<>();

		if (apiKey == null || apiKey.isEmpty())
		{
			future.completeExceptionally(new IOException("Not authenticated"));
			return future;
		}

		String url = API_URL + "/messages/" + message.getWorldId() + "/" + message.getRegionId() + "/" + message.getId() + "/vote";

		JsonObject body = new JsonObject();
		body.addProperty("vote", thumbsUp ? "up" : "down");

		Request request = new Request.Builder()
			.url(url)
			.header("X-API-Key", apiKey)
			.post(RequestBody.create(JSON, gson.toJson(body)))
			.build();

		httpClient.newCall(request).enqueue(new Callback()
		{
			@Override
			public void onFailure(Call call, IOException e)
			{
				log.warn("Failed to vote on message: {}", e.getMessage());
				future.completeExceptionally(e);
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException
			{
				try
				{
					if (response.isSuccessful())
					{
						if (thumbsUp)
						{
							message.setThumbsUp(message.getThumbsUp() + 1);
						}
						else
						{
							message.setThumbsDown(message.getThumbsDown() + 1);
						}
						log.debug("Voted {} on message {}", thumbsUp ? "up" : "down", message.getId());
						future.complete(null);
					}
					else
					{
						String responseBody = response.body().string();
						try
						{
							JsonObject errorJson = gson.fromJson(responseBody, JsonObject.class);
							String errorMsg = errorJson.has("error") ? errorJson.get("error").getAsString() : "Vote failed";
							future.completeExceptionally(new IOException(errorMsg));
						}
						catch (Exception e)
						{
							future.completeExceptionally(new IOException("HTTP " + response.code()));
						}
					}
				}
				finally
				{
					response.close();
				}
			}
		});

		return future;
	}

	public CompletableFuture<Void> reportMessage(MessageData message, String reporterName)
	{
		CompletableFuture<Void> future = new CompletableFuture<>();

		if (apiKey == null || apiKey.isEmpty())
		{
			future.completeExceptionally(new IOException("Not authenticated"));
			return future;
		}

		String url = API_URL + "/messages/" + message.getWorldId() + "/" + message.getRegionId() + "/" + message.getId() + "/report";

		JsonObject body = new JsonObject();
		body.addProperty("reason", "Reported by " + reporterName);

		Request request = new Request.Builder()
			.url(url)
			.header("X-API-Key", apiKey)
			.post(RequestBody.create(JSON, gson.toJson(body)))
			.build();

		httpClient.newCall(request).enqueue(new Callback()
		{
			@Override
			public void onFailure(Call call, IOException e)
			{
				log.warn("Failed to report message: {}", e.getMessage());
				future.completeExceptionally(e);
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException
			{
				try
				{
					if (response.isSuccessful())
					{
						message.setReported(true);
						log.debug("Reported message {}", message.getId());
						future.complete(null);
					}
					else
					{
						String responseBody = response.body().string();
						try
						{
							JsonObject errorJson = gson.fromJson(responseBody, JsonObject.class);
							String errorMsg = errorJson.has("error") ? errorJson.get("error").getAsString() : "Report failed";
							future.completeExceptionally(new IOException(errorMsg));
						}
						catch (Exception e)
						{
							future.completeExceptionally(new IOException("HTTP " + response.code()));
						}
					}
				}
				finally
				{
					response.close();
				}
			}
		});

		return future;
	}

	private List<MessageData> parseMessageArray(String json)
	{
		List<MessageData> messages = new ArrayList<>();

		if (json == null || json.isEmpty() || json.equals("null"))
		{
			return messages;
		}

		try
		{
			JsonArray array = gson.fromJson(json, JsonArray.class);
			for (JsonElement element : array)
			{
				try
				{
					MessageData msg = gson.fromJson(element, MessageData.class);
					if (msg != null)
					{
						messages.add(msg);
					}
				}
				catch (Exception e)
				{
					log.warn("Failed to parse message: {}", e.getMessage());
				}
			}
		}
		catch (Exception e)
		{
			log.warn("Failed to parse message array: {}", e.getMessage());
		}

		return messages;
	}
}
