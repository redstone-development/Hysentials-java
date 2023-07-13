package cc.woverflow.hysentials.handlers.chat.modules.misc;

import cc.polyfrost.oneconfig.events.event.ChatSendEvent;
import cc.woverflow.hysentials.command.HypixelChatCommand;
import cc.woverflow.hysentials.handlers.chat.ChatReceiveModule;
import cc.woverflow.hysentials.handlers.chat.ChatSendModule;
import cc.woverflow.hysentials.handlers.chat.modules.bwranks.BWSReplace;
import cc.woverflow.hysentials.util.MUtils;
import cc.woverflow.hysentials.websocket.Request;
import cc.woverflow.hysentials.websocket.Socket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class GlobalChatStuff {
    public static class GlobalInChannel implements ChatReceiveModule {
        @Override
        public void onMessageReceived(@NotNull ClientChatReceivedEvent event) {
            if (event.type != 0 && event.type != 1) return;
            if (HypixelChatCommand.isInGlobalChat) {
                if (getStrippedMessage(event.message).equals("You're already in this channel!")) {
                    BWSReplace.diagnostics.add("Setting isInGlobalChat to false (1)");
                    event.setCanceled(true);
                    MUtils.chat("&aYou are now in the &6" + HypixelChatCommand.gotoChannel.toUpperCase() + " &achannel!");
                    HypixelChatCommand.isInGlobalChat = false;
                }else if (getStrippedMessage(event.message).startsWith("You are now in the ") && !getStrippedMessage(event.message).startsWith("You are now in the GLOBAL")) {
                    BWSReplace.diagnostics.add("Setting isInGlobalChat to false (2)");
                    HypixelChatCommand.isInGlobalChat = false;
                }
            }
        }
    }

    public static class GlobalSendMessage {
        public static @Nullable String onMessageSend(String message) {
            if (message.startsWith("/")) return message;
            if (HypixelChatCommand.isInGlobalChat) {
                BWSReplace.diagnostics.add("Heard a message in global chat, sending to server");
                JSONObject json = new JSONObject();
                json.put("method", "chat");
                json.put("message", message);
                json.put("username", Minecraft.getMinecraft().thePlayer.getName());
                json.put("uuid", Minecraft.getMinecraft().thePlayer.getCommandSenderEntity().getUniqueID().toString());
                json.put("server", false);
                json.put("displayName", Minecraft.getMinecraft().thePlayer.getDisplayName().getFormattedText()); //This gets overwritten by the server lol!
                json.put("key", Socket.serverId);

                Socket.CLIENT.sendText(json.toString());
                return null;
            }
            return message;
        }

    }
}
