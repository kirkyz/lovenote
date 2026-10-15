package com.lovenote.module.impl;

import com.lovenote.module.Category;
import com.lovenote.module.Module;
import com.lovenote.setting.BoolSetting;
import com.lovenote.setting.ModeSetting;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;

/** Appends a little tag to everything you say. */
public class ChatSuffix extends Module {
	private final ModeSetting tag = add(new ModeSetting("Tag", "lovenote", "lovenote", "dg", "\u2665", "none"));
	private final BoolSetting commands = add(new BoolSetting("Skip commands", true));

	public ChatSuffix() {
		super("ChatSuffix", Category.MISC);
		describe("Adds a tag to the end of your chat messages.");
		ClientSendMessageEvents.MODIFY_CHAT.register(this::modify);
	}

	private String modify(String message) {
		if (!enabled || tag.is("none")) return message;
		if (commands.get() && message.startsWith("/")) return message;
		return message + " " + tag.get();
	}
}