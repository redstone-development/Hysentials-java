package llc.redstone.hysentials.cosmetic

import cc.polyfrost.oneconfig.utils.Multithreading
import com.google.gson.JsonElement
import llc.redstone.hysentials.HYSENTIALS_API
import llc.redstone.hysentials.Hysentials
import llc.redstone.hysentials.cosmetic.CosmeticGui.Companion.paginationList
import llc.redstone.hysentials.schema.HysentialsSchema
import llc.redstone.hysentials.schema.HysentialsSchema.Cosmetic.Companion.deserialize
import llc.redstone.hysentials.util.BlockWAPIUtils
import llc.redstone.hysentials.util.NetworkUtils
import llc.redstone.hysentials.util.Renderer
import llc.redstone.hysentials.websocket.Socket
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.resources.model.IBakedModel
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import org.json.JSONObject
import java.util.*

object CosmeticManager {
    var actions = mapOf(
        "equip" to mutableListOf<String>(),
        "unequip" to mutableListOf<String>(),
        "purchase" to mutableListOf<String>()
    )

    fun unEquipCosmetic(name: String) {
        if (name == "kzero bundle") return kzero(false)
        val cosmetics = BlockWAPIUtils.getCosmetics()
        val cosmetic = cosmetics.find { it.name == name }
        cosmetic?.let {
            if (it.equipped.contains(Minecraft.getMinecraft().thePlayer.uniqueID.toString())) {
                it.equipped.remove(Minecraft.getMinecraft().thePlayer.uniqueID.toString())
            }
            actions["unequip"]?.add(name)
        }
    }

    fun equipCosmetic(name: String) {
        if (name == "kzero bundle") return kzero(true)
        val cosmetics = BlockWAPIUtils.getCosmetics()
        val cosmetic = cosmetics.find { it.name == name }
        cosmetic?.let {
            if (BlockWAPIUtils.getCosmetic(it.type).isNotEmpty()) {
                BlockWAPIUtils.getCosmetic(it.type).forEach { cosmetic ->
                    unEquipCosmetic(cosmetic.name)
                }
            }
            if (!it.equipped.contains(Minecraft.getMinecraft().thePlayer.uniqueID.toString())) {
                it.equipped.add(Minecraft.getMinecraft().thePlayer.uniqueID.toString())
            }
            actions["equip"]?.add(name)
        }
    }

    fun kzero(equip: Boolean) {
        var list = listOf(
            "kzero hair",
            "kzero robe",
            "kzero slipper"
        )
        for (cosmetic in list) {
            val name = cosmetic.replace(" ", "%20")
            if (equip) {
                equipCosmetic(name)
            } else {
                unEquipCosmetic(name)
            }
        }
    }

    fun purchaseCosmetic(cosmeticName: String) {
        val cosmetics = BlockWAPIUtils.getCosmetics()
        val cosmetic = cosmetics.find { it.name == cosmeticName }
        cosmetic?.let {
            if (!it.users.contains(Minecraft.getMinecraft().thePlayer.uniqueID.toString())) {
                it.users.add(Minecraft.getMinecraft().thePlayer.uniqueID.toString())
                Socket.cachedUser.amountSpent = Socket.cachedUser.amountSpent?.plus(it.cost)
                Socket.cachedUser.emeralds = Socket.cachedUser.emeralds.minus(it.cost)
                actions["purchase"]?.add(cosmeticName)
            }
        }
    }

    fun updateCosmetics() {
        Multithreading.runAsync {
            for (action in actions) {
                val list = action.value
                val func = action.key

                for (name in list) {
                    val response =
                        NetworkUtils.postString(HYSENTIALS_API + "/cosmetic?name=$name&function=${func}&uuid=${Minecraft.getMinecraft().thePlayer.uniqueID}&key=${Socket.serverId}")
                    val json = JSONObject(response)
                    if (json.get("success") == false) {
                        Hysentials.INSTANCE.sendMessage(
                            "&cFailed to $func $name: ${json.get("message")}",
                        )
                    }
                }
            }
            actions = mapOf(
                "equip" to mutableListOf<String>(),
                "unequip" to mutableListOf<String>(),
                "purchase" to mutableListOf<String>()
            )
            var cosmetics: JsonElement? =
                NetworkUtils.getJsonElement("$HYSENTIALS_API/cosmetic", true) ?: return@runAsync
            val `object` = cosmetics!!.asJsonObject
            val array = `object`.getAsJsonArray("cosmetics")
            BlockWAPIUtils.cosmetics = ArrayList()
            for (cosmeticObj in array) {
                val cosmetic = deserialize(cosmeticObj.asJsonObject)
                BlockWAPIUtils.cosmetics.add(cosmetic)
            }
        }
    }

    val slotResource = ResourceLocation("hysentials:gui/wardrobe/selected_slot.png")

    fun drawSlot(slotIn: Slot, cosmetic: HysentialsSchema.Cosmetic) {
        val i: Int = slotIn.xDisplayPosition
        val j: Int = slotIn.yDisplayPosition
        GlStateManager.disableLighting()
        GlStateManager.disableDepth()
        GlStateManager.colorMask(true, true, true, false)
        val name = cosmetic.name
        val uuid = Minecraft.getMinecraft().thePlayer.uniqueID
        if (equippedCosmetic(uuid, name)) {
            Renderer.drawImage(slotResource, i.toDouble(), j.toDouble(), 25.0, 26.0)
        }

        val itemstack: ItemStack = slotIn.stack ?: return

        val ibakedmodel: IBakedModel = Minecraft.getMinecraft().renderItem.itemModelMesher.getItemModel(itemstack)
        val width = ibakedmodel.particleTexture.iconWidth
        val height = ibakedmodel.particleTexture.iconHeight
        Minecraft.getMinecraft().renderItem.renderItemAndEffectIntoGUI(
            itemstack,
            i + (25 - width) / 2,
            j + (26 - height) / 2
        )
        Minecraft.getMinecraft().renderItem.renderItemOverlayIntoGUI(
            Minecraft.getMinecraft().fontRendererObj,
            itemstack,
            i + (25 - width) / 2,
            j + (26 - height) / 2,
            ""
        )

        Minecraft.getMinecraft().renderItem.zLevel = 0.0f
        GlStateManager.enableLighting()
        GlStateManager.enableDepth()
        GlStateManager.colorMask(true, true, true, true)


    }


    @JvmStatic
    fun equippedCosmetic(uuid: UUID, name: String): Boolean {
        try {
            val cosmetics = BlockWAPIUtils.getCosmetics()
            cosmetics.find { it.name == name }?.let {
                if (it.equipped.contains(uuid.toString())) {
                    return true
                }
            }
        } catch (_: Exception) {
        }
        return false
    }

    @JvmStatic
    fun hasCosmetic(uuid: UUID, name: String): Boolean {
        try {
            val cosmetics = BlockWAPIUtils.getCosmetics()
            cosmetics.find { it.name == name }?.let {
                if (it.users.contains(uuid.toString())) {
                    return true
                }
            }
        } catch (_: Exception) {
        }
        return false
    }

    @JvmStatic
    fun getOwnedCosmetics(uuid: UUID): ArrayList<HysentialsSchema.Cosmetic> {
        val cosmetics = BlockWAPIUtils.getCosmetics()
        val ownedCosmetics = ArrayList<HysentialsSchema.Cosmetic>()
        for (cosmetic in cosmetics) {
            if (cosmetic.users.contains(uuid.toString())) {
                ownedCosmetics.add(cosmetic)
            }
        }
        return ownedCosmetics
    }

    @JvmStatic
    fun getEquippedCosmetics(uuid: UUID): ArrayList<HysentialsSchema.Cosmetic> {
        val cosmetics = BlockWAPIUtils.getCosmetics()
        val equippedCosmetics = ArrayList<HysentialsSchema.Cosmetic>()
        for (cosmetic in cosmetics) {
            if (cosmetic.equipped.contains(uuid.toString())) {
                equippedCosmetics.add(cosmetic)
            }
        }
        return equippedCosmetics
    }

    fun colorFromRarity(rarity: String): String {
        return when (rarity) {
            "COMMON" -> "#828282"
            "RARE" -> "#0099DB"
            "EPIC" -> "#8B5CF6"
            "LEGENDARY" -> "#F49E0B"
            "EXCLUSIVE" -> "#EA323C"
            else -> "#FFFFFF"
        }
    }

    fun indexFromRarity(rarity: String): Int {
        return when (rarity) {
            "COMMON" -> 0
            "RARE" -> 1
            "EPIC" -> 2
            "LEGENDARY" -> 3
            "EXCLUSIVE" -> 4
            else -> 0
        }
    }

    fun tabFromType(type: String): CosmeticTab? {
        return CosmeticTab.tabs.filter { it.name == type }.firstOrNull()
    }
}