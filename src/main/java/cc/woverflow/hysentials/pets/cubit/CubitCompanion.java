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

package cc.woverflow.hysentials.pets.cubit;

import cc.woverflow.hysentials.Hysentials;
import cc.woverflow.hysentials.pets.AbstractCosmetic;
import cc.woverflow.hysentials.user.Player;
import cc.woverflow.hysentials.util.UUIDUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CubitCompanion extends AbstractCosmetic {
    private Map<UUID, EntityCubit> hamsters = new HashMap<>();

    public CubitCompanion() {
        super(false);
    }

    private int ticks = 0;

    @SubscribeEvent
    public void onTick(TickEvent e) {
        WorldClient theWorld = Minecraft.getMinecraft().theWorld;
        if (theWorld == null) return;
        if (ticks++ % 20 != 0) return;
        for (EntityPlayer player : theWorld.playerEntities) {
            if (player == null) continue;
            if (player.getUniqueID().equals(UUIDUtil.getClientUUID())) continue;
            if (player.isDead) continue;
            if (!hamsters.containsKey(player.getUniqueID())) {
                if (!worldHasEntityWithUUID(theWorld, player.getUniqueID())) {
                    spawnPet(player);
                }
            } else {
                boolean isPlus = Player.getPlayer(player).isPlus();
                if (!isPlus) {
                    hamsters.get(player.getUniqueID()).setDead();
                    hamsters.remove(player.getUniqueID());
                }
            }
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load e) {
        hamsters.clear();
        UUID id = UUIDUtil.getClientUUID();
        if (id == null) {
            return;
        }
        new Thread(() -> {
                spawnPet(Minecraft.getMinecraft().thePlayer);
        }).start();
    }

    public boolean worldHasEntityWithUUID(World world, UUID id) {
        return world.loadedEntityList.stream().anyMatch(entity -> entity.getUniqueID().equals(id));
    }

    @Override
    public void spawnPet(EntityPlayer player) {
        if (!Hysentials.INSTANCE.getOnlineCache().plusPlayers.contains(player.getUniqueID())) return;
        WorldClient theWorld = Minecraft.getMinecraft().theWorld;
        EntityCubit hamster = new EntityCubit(theWorld);
        hamster.setPosition(player.posX, player.posY, player.posZ);
        hamster.setOwnerId(player.getUniqueID().toString());
        EntityArmorStand armorStand = new EntityArmorStand(theWorld);
        armorStand.setPosition(player.posX, player.posY, player.posZ);
        armorStand.setInvisible(true);
        armorStand.setCustomNameTag("Cubit Companion");
        armorStand.setAlwaysRenderNameTag(true);
        armorStand.noClip = true;
        theWorld.spawnEntityInWorld(hamster);
        hamsters.put(player.getUniqueID(), hamster);
    }
}
