package cc.woverflow.hysentials.websocket;

import cc.polyfrost.oneconfig.libs.universal.UChat;
import cc.woverflow.hysentials.Hysentials;
import cc.woverflow.hysentials.handlers.chat.modules.bwranks.BWSReplace;
import cc.woverflow.hysentials.util.MUtils;
import cc.polyfrost.oneconfig.libs.universal.wrappers.message.UMessage;
import cc.polyfrost.oneconfig.libs.universal.wrappers.message.UTextComponent;
import cc.polyfrost.oneconfig.utils.Multithreading;
import cc.woverflow.hysentials.config.HysentialsConfig;
import cc.woverflow.hysentials.handlers.groupchats.GroupChat;
import cc.woverflow.hysentials.handlers.redworks.BwRanksUtils;
import cc.woverflow.hysentials.util.BlockWAPIUtils;
import cc.woverflow.hysentials.util.DuoVariable;
import cc.woverflow.hysentials.util.SSLStore;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.neovisionaries.ws.client.*;
import kotlin.random.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.IChatComponent;
import org.json.JSONObject;

import javax.net.ssl.SSLContext;
import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static cc.woverflow.hysentials.guis.actionLibrary.ActionViewer.toList;
import static cc.woverflow.hysentials.util.HypixelAPIUtils.getUsername;

public class Socket {
    public static WebSocket CLIENT;
    public static JSONObject cachedData = new JSONObject();
    public static List<JSONObject> cachedUsers = new ArrayList<>();
    public static JSONObject cachedServerData = new JSONObject();
    public static String serverId;
    public static boolean linking = false;
    public static boolean linked = false;
    public static JSONObject data = null;
    public static List<DuoVariable<String, Consumer<JSONObject>>> awaiting = new ArrayList<>();

    public static int relogAttempts = 0;

    public static void createSocket() {
        if (relogAttempts > 2) return;
        try {
            serverId = randomString(Random.Default.nextInt(3, 16));
            String hash = hash("Hysentials_" + serverId);

            Minecraft.getMinecraft().getSessionService().joinServer(
                Minecraft.getMinecraft().getSession().getProfile(),
                Minecraft.getMinecraft().getSession().getToken(),
                hash
            );

            WebSocketFactory factory = new WebSocketFactory();
            SSLStore store = new SSLStore();
            store.load("/ssl/socket.der");
            SSLContext context = store.finish();
            factory.setSSLContext(context);
            factory.setServerName("socket.redstone.llc");
            factory.getProxySettings().setSocketFactory(context.getSocketFactory());
            factory.getProxySettings().setServerName("socket.redstone.llc");
            factory.getProxySettings().setPort(443);
            factory.getProxySettings().setSSLContext(context);
            WebSocket socket = factory.createSocket("ws://socket.redstone.llc");

            socket.addListener(new WebSocketListener() {
                public void send(String message) {
                    socket.sendText(message);
                }

                @Override
                public void onStateChanged(WebSocket websocket, WebSocketState newState) throws Exception {

                }

                @Override
                public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
                    System.out.println("Connected to websocket server");
                    JSONObject obj = new JSONObject();
                    obj.put("method", "login");
                    obj.put("username", Minecraft.getMinecraft().getSession().getUsername());
                    obj.put("key", serverId);
                    send(obj.toString());
                }

                @Override
                public void onConnectError(WebSocket websocket, WebSocketException cause) throws Exception {

                }

                @Override
                public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
                    linking = false;
                    data = null;
                    relogAttempts++;
                    if (relogAttempts > 2) {
                        MUtils.chat(HysentialsConfig.chatPrefix + " §cFailed to connect to websocket server. This is probably because it is offline. Please try again later with `/hs reconnect`.");
                        return;
                    }
                    MUtils.chat(HysentialsConfig.chatPrefix + " §cDisconnected from websocket server. Attempting to reconnect in 5 seconds");
                    Multithreading.schedule(Socket::createSocket, 5, TimeUnit.SECONDS);
                }

                @Override
                public void onFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

                }

                @Override
                public void onContinuationFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

                }

                @Override
                public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

                }

                @Override
                public void onBinaryFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

                }

                @Override
                public void onCloseFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

                }

                @Override
                public void onPingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

                }

                @Override
                public void onPongFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

                }

                @Override
                public void onTextMessage(WebSocket websocket, String text) throws Exception {
                    JSONObject json = new JSONObject(text);
                    if (json.has("method")) {
                        switch (json.getString("method")) {
                            case "login": {
                                relogAttempts = 0;
                                if (json.has("success") && json.getBoolean("success")) {
                                    MUtils.chat(HysentialsConfig.chatPrefix + " §aLogged in successfully!");
                                    CLIENT = websocket;
                                    Multithreading.runAsync(BlockWAPIUtils::getOnline);
                                }
                                if (!json.getBoolean("linked")) {
                                    Socket.linked = false;
                                    MUtils.chat(HysentialsConfig.chatPrefix + " §cYou are not linked to a discord account! Some features will not work.");
                                } else {
                                    Socket.linked = true;
                                }
                                break;
                            }
                            case "data": {
                                cachedData = json.getJSONObject("data");
                                cachedServerData = json.getJSONObject("server");
                                cachedUsers = new ArrayList<>();
                                for (Object o : toList(json.getJSONArray("users"))) {
                                    cachedUsers.add((JSONObject) o);
                                }
                                break;
                            }
                            case "chat": {
                                if (HysentialsConfig.globalChatEnabled) {
                                    if (json.getString("username").equals("HYPIXELCONSOLE") && !json.has("uuid")) {
                                        MUtils.chat(HysentialsConfig.chatPrefix + " §c" + json.getString("message"));
                                        break;
                                    }
                                    if (HysentialsConfig.futuristicRanks) {
                                        BlockWAPIUtils.Rank rank = BlockWAPIUtils.getRank(json.getString("uuid"));
                                        IChatComponent comp = new UTextComponent("")
                                            .appendSibling(
                                                new UTextComponent(
                                                    ":globalchat: "
                                                )
                                            )
                                            .appendSibling(
                                                new UTextComponent(
                                                    "&6" + json.getString("username")
                                                )
                                                    .setHover(
                                                        HoverEvent.Action.SHOW_TEXT,
                                                        rank.getPlaceholder() + BlockWAPIUtils.getUsername(UUID.fromString(json.getString("uuid")))
                                                    )
                                            )
                                            .appendSibling(
                                                new UTextComponent(
                                                    "<#fff1d4>: "
                                                        + json.getString("message")
                                                )
                                            );
                                        Minecraft.getMinecraft().thePlayer.addChatMessage(comp);
                                    } else {
                                        BlockWAPIUtils.Rank rank = BlockWAPIUtils.getRank(json.getString("uuid"));
                                        IChatComponent comp = new UTextComponent("")
                                            .appendSibling(
                                                new UTextComponent(
                                                    ":globalchat: "
                                                )
                                            )
                                            .appendSibling(
                                                new UTextComponent(
                                                    "&6" + json.getString("username")
                                                )
                                                    .setHover(
                                                        HoverEvent.Action.SHOW_TEXT,
                                                        rank.getPrefix("") + BlockWAPIUtils.getUsername(UUID.fromString(json.getString("uuid")))
                                                    )
                                            )
                                            .appendSibling(
                                                new UTextComponent(
                                                    "&f: "
                                                        + json.getString("message")
                                                )
                                            );
                                        Minecraft.getMinecraft().thePlayer.addChatMessage(comp);
                                    }
                                }
                                break;
                            }

                            case "link": {
                                MUtils.chat(HysentialsConfig.chatPrefix + " §fA link request has been made, please type §6`/hysentials link` §fto link your account. §fThis will expire in 5 minutes. If this was not you, please ignore this!");
                                linking = true;
                                data = json;

                                Multithreading.schedule(() -> {
                                    linking = false;
                                    data = null;
                                }, 5, TimeUnit.MINUTES);
                            }

                            case "diagnose": {
                                JSONObject data = new JSONObject().put("ram", Runtime.getRuntime().maxMemory() / 1024 / 1024).put("cpu", Runtime.getRuntime().availableProcessors()).put("diagnoses", BWSReplace.diagnostics);
                                json.put("data", data);
                                send(json.toString());
                            }

                            case "groupChat": {
                                GroupChat.chat(json);
                                break;
                            }

                            case "groupInvite": {
                                GroupChat.invite(json);
                                break;
                            }

                            case "clubInvite": {
                                JSONObject club = json.getJSONObject("club");
                                MUtils.chat("&b-----------------------------------------------------");
                                new UTextComponent("§eYou have been invited to join the §6" + club.getString("name") + " §eclub. Type §6`/club join " + club.getString("name") + "` §eto join!")
                                    .setHover(HoverEvent.Action.SHOW_TEXT, "§eClick to join!")
                                    .setClick(ClickEvent.Action.RUN_COMMAND, "/club join " + club.getString("name"))
                                    .chat();
                                MUtils.chat(HysentialsConfig.chatPrefix + " §eThis invite will expire in 5 minutes.");
                                MUtils.chat("&b-----------------------------------------------------");
                                break;
                            }

                            case "clubAccept": {
                                if (json.getBoolean("success")) {
                                    MUtils.chat(HysentialsConfig.chatPrefix + " §aSuccessfully joined club!");
                                } else {
                                    MUtils.chat(HysentialsConfig.chatPrefix + " §cFailed to join club!");
                                }
                            }
                        }
                    }
                    for (int i = 0, awaitingSize = awaiting.size(); i < awaitingSize; i++) {
                        DuoVariable<String, Consumer<JSONObject>> value = awaiting.get(i);
                        if (json.has("method") && json.getString("method").equals(value.getFirst())) {
                            value.getSecond().accept(json);
                            awaiting.remove(i);
                        }
                    }
                }

                @Override
                public void onTextMessage(WebSocket websocket, byte[] data) throws Exception {

                }

                @Override
                public void onBinaryMessage(WebSocket websocket, byte[] binary) throws Exception {

                }

                @Override
                public void onSendingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

                }

                @Override
                public void onFrameSent(WebSocket websocket, WebSocketFrame frame) throws Exception {

                }

                @Override
                public void onFrameUnsent(WebSocket websocket, WebSocketFrame frame) throws Exception {

                }

                @Override
                public void onThreadCreated(WebSocket websocket, ThreadType threadType, Thread thread) throws Exception {

                }

                @Override
                public void onThreadStarted(WebSocket websocket, ThreadType threadType, Thread thread) throws Exception {

                }

                @Override
                public void onThreadStopping(WebSocket websocket, ThreadType threadType, Thread thread) throws Exception {

                }

                @Override
                public void onError(WebSocket websocket, WebSocketException cause) throws Exception {

                }

                @Override
                public void onFrameError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame) throws Exception {

                }

                @Override
                public void onMessageError(WebSocket websocket, WebSocketException cause, List<WebSocketFrame> frames) throws Exception {

                }

                @Override
                public void onMessageDecompressionError(WebSocket websocket, WebSocketException cause, byte[] compressed) throws Exception {

                }

                @Override
                public void onTextMessageError(WebSocket websocket, WebSocketException cause, byte[] data) throws Exception {

                }

                @Override
                public void onSendError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame) throws Exception {

                }

                @Override
                public void onUnexpectedError(WebSocket websocket, WebSocketException cause) throws Exception {

                }

                @Override
                public void handleCallbackError(WebSocket websocket, Throwable cause) throws Exception {

                }

                @Override
                public void onSendingHandshake(WebSocket websocket, String requestLine, List<String[]> headers) throws Exception {

                }
            });

            socket.connect();
        } catch (AuthenticationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            MUtils.chat("&cAn error occurred whilst connecting to the Hysentials websocket. Please contact @sinender on Discord if this issue persists.");
        }
    }

    private static String randomString(int size) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            int random = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(random));
        }
        return sb.toString();
    }

    public static String hash(String str) {
        try {
            byte[] digest = digest(str, "SHA-1");
            return new BigInteger(digest).toString(16);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] digest(String str, String algorithm) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);
        return md.digest(strBytes);
    }
}
