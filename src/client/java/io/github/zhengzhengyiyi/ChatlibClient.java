package io.github.zhengzhengyiyi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.zhengzhengyiyi.api.AIClient;
import net.fabricmc.api.ClientModInitializer;

public class ChatlibClient implements ClientModInitializer {
	public static Logger LOGGER = LoggerFactory.getLogger("ai");
	
	@Override
	public void onInitializeClient() {
		AIClient client = new AIClient();

		client.checkServerStatus().thenAccept(available -> {
		    if (available) {
		        client.sendChatRequest("tinyllama:latest", "hello, test").thenAccept(response -> {
		        	LOGGER.info(response);
		        	LOGGER.info("---------------------------------");
		        });
		    } else {
		        LOGGER.warn("did not open ollama server");
		    }
		});
	}
}
