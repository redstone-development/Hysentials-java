/*
 *       Copyright (C) 2018-present Hyperium <https://hyperium.cc/>
 *
 *       This program is free software: you can redistribute it and/or modify
 *       it under the terms of the GNU Lesser General Public License as published
 *       by the Free Software Foundation, either version 3 of the License, or
 *       (at your option) any later version.
 *
 *       This program is distributed in the hope that it will be useful,
 *       but WITHOUT ANY WARRANTY; without even the implied warranty of
 *       MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *       GNU Lesser General Public License for more details.
 *
 *       You should have received a copy of the GNU Lesser General Public License
 *       along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package cc.woverflow.hysentials.pets;

import cc.woverflow.hysentials.user.Player;
import cc.woverflow.hysentials.util.UUIDUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Thread.sleep;

/**
 * Created by mitchellkatz on 3/17/18. Designed for production use on Sk1er.club
 */
public class AbstractCosmetic {

    private final boolean selfOnly;
    private final Map<UUID, Boolean> purchasedBy = new ConcurrentHashMap<>();
    private boolean selfUnlocked;

    public AbstractCosmetic(boolean selfOnly) {
        this.selfOnly = selfOnly;
        try {
            selfUnlocked = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void worldSwitch(WorldEvent.Load changeEvent) {
        UUID id = UUIDUtil.getClientUUID();
        if (id == null) {
            return;
        }
        new Thread(() -> {
            boolean playerExists = false;
            while (!playerExists) {
                if (Player.CLIENT == null) {
                    continue;
                }
                if (Player.CLIENT.getClientPlayer() == null) {
                    continue;
                }
                spawnPet(Minecraft.getMinecraft().thePlayer);
                playerExists = true;
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    public void spawnPet(EntityPlayer player) {

    };

}
