/*
 * Hytils Reborn - Hypixel focused Quality of Life mod.
 * Copyright (C) 2022  W-OVERFLOW
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package cc.woverflow.hysentials.handlers.chat;

import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.NotNull;

/**
 * This interface is used to handle many different {@link ClientChatReceivedEvent}-consuming methods
 * without having to add them all to the {@link MinecraftForge#EVENT_BUS}.
 * <p>
 * ChatModules essentially behave like small listener classes, except instead of going directly to Forge,
 * the {@link cc.woverflow.hysentials.handlers.chat.ChatHandler} handles them and passes them to Forge, taking account of things like priority and cancelled events.
 * <p>
 * Must be registered in {@link cc.woverflow.hysentials.handlers.chat.ChatHandler#ChatHandler()} to be executed.
 *
 * @see cc.woverflow.hysentials.handlers.chat.ChatModule
 * @see ChatHandler
 */
public interface ChatReceiveModule extends cc.woverflow.hysentials.handlers.chat.ChatModule {

    /**
     * Place your code here. Called when a Forge {@link ClientChatReceivedEvent} is received.
     * <p>
     * If the event is cancelled, {@link cc.woverflow.hysentials.handlers.chat.ChatModule}s after that event will not execute. Therefore,
     * {@link ClientChatReceivedEvent#isCanceled()}} checks are unnecessary.
     *
     * @param event a {@link ClientChatReceivedEvent}
     */
    default void onMessageReceived(@NotNull ClientChatReceivedEvent event) {

    }

    default IChatComponent onMessageReceivedS(@NotNull ClientChatReceivedEvent event) {
        return null;
    }

}
