package cc.woverflow.hysentials.handlers.htsl;

import cc.polyfrost.oneconfig.libs.universal.UChat;
import cc.woverflow.hysentials.util.*;
import cc.woverflow.hysentials.event.events.GuiMouseClickEvent;
import cc.woverflow.hysentials.guis.club.ClubDashboard;
import cc.woverflow.hysentials.guis.container.GuiItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cc.woverflow.hysentials.command.RemoveGlowCommand.enchantmentTagListToHashMap;
import static cc.woverflow.hysentials.command.RemoveGlowCommand.removeEnchant;
import static cc.woverflow.hysentials.guis.sbBoxes.SBBoxesEditor.drawRect;

public class FunctionsGUIHandler {
    Field guiTopField;
    Field guiLeftField;
    Field xSizeField;
    HashMap<Integer, Slot> selectedSlots = new HashMap<>();
    boolean showChoose = false;
    Input.Button clipboard;
    Input.Button file;
    Input.Button club;
    Input.Button library;
    boolean isSelecting = false;

    public FunctionsGUIHandler() {
        try {
            clipboard = new Input.Button(0, 0, 0, 20, "Clipboard");
            file = new Input.Button(0, 0, 0, 20, "File");
            club = new Input.Button(0, 0, 0, 20, "Club");
            library = new Input.Button(0, 0, 0, 20, "Action Library");

            guiTopField = GuiContainer.class.getDeclaredField("field_147009_r");
            guiTopField.setAccessible(true);
            guiLeftField = GuiContainer.class.getDeclaredField("field_147003_i");
            guiLeftField.setAccessible(true);
            xSizeField = GuiContainer.class.getDeclaredField("field_146999_f");
            xSizeField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onGuiRender(GuiScreenEvent.BackgroundDrawnEvent event) {
        if (Minecraft.getMinecraft().thePlayer == null || Minecraft.getMinecraft().thePlayer.openContainer == null)
            return;
        if (Navigator.getContainerName() == null || !Navigator.getContainerName().equals("Functions")) return;
        GlStateManager.pushMatrix();
        Slot slot = Minecraft.getMinecraft().thePlayer.openContainer.getSlot(48);

        if (!slot.getHasStack()) {
            ItemStack item = GuiItem.makeColorfulItem(Material.STORAGE_MINECART, "&aUpload to Action Library", 1, 0, "&7Uploads a selection of", "&7functions to your desired", "&7destination.", "", "&7Once you are done", "&7choosing your functions,", "&7click this again to", "&7start the export", "", "&eClick to toggle selecting!");
            GuiItem.hideFlag(item, 1);
            slot.putStack(item);
        }

        int chestGuiTop;
        int chestWidth;
        int chestGuiLeft;
        try {
            chestGuiTop = (int) guiTopField.get(Minecraft.getMinecraft().currentScreen);
            chestGuiLeft = (int) guiLeftField.get(Minecraft.getMinecraft().currentScreen);
            chestWidth = (int) xSizeField.get(Minecraft.getMinecraft().currentScreen);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        int rightMost = chestGuiLeft + chestWidth;


        if (isSelecting) {
            for (Map.Entry<Integer, Slot> entry : selectedSlots.entrySet()) {
                int slotIndex = entry.getKey();
                Slot s = entry.getValue();
                if (!s.getHasStack() || !s.getStack().hasDisplayName()) continue;
                String name = C.removeColor(s.getStack().getDisplayName());
                int x = rightMost + 10;
                int y = chestGuiTop + 1 + 25 * slotIndex - 5;
                drawRect(x, y, 50, 20, (int) Renderer.color(0, 0, 0, 150));


            }
        }

        if (showChoose) {

            clipboard.setWidth(chestWidth / 2 - 10);
            clipboard.xPosition = rightMost + 10;
            clipboard.yPosition = chestGuiTop + 1 + 25 - 5;

            file.setWidth(chestWidth / 2 - 10);
            file.xPosition = rightMost + 10;
            file.yPosition = chestGuiTop + 1 + 50 - 5;

            club.setWidth(chestWidth / 2 - 10);
            club.xPosition = rightMost + 10;
            club.yPosition = chestGuiTop + 1 + 75 - 5;

            library.setWidth(chestWidth / 2 - 10);
            library.xPosition = rightMost + 10;
            library.yPosition = chestGuiTop + 1 + 100 - 5;

            clipboard.drawButton(Minecraft.getMinecraft(), event.getMouseX(), event.getMouseY());
            file.drawButton(Minecraft.getMinecraft(), event.getMouseX(), event.getMouseY());
            club.drawButton(Minecraft.getMinecraft(), event.getMouseX(), event.getMouseY());
            library.drawButton(Minecraft.getMinecraft(), event.getMouseX(), event.getMouseY());
        }
        GlStateManager.popMatrix();
    }

    @SubscribeEvent
    public void mouseClick(GuiMouseClickEvent event) {
        if (Minecraft.getMinecraft().thePlayer == null || Minecraft.getMinecraft().thePlayer.openContainer == null)
            return;
        if (Navigator.getContainerName() == null || !Navigator.getContainerName().equals("Functions")) return;
        if (!showChoose) return;
        if (event.getX() > clipboard.xPosition && event.getX() < clipboard.xPosition + clipboard.width && event.getY() > clipboard.yPosition && event.getY() < clipboard.yPosition + clipboard.height) {
            EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
            Minecraft.getMinecraft().theWorld.playSound(player.posX, player.posY, player.posZ, "random.click", 1, 1, false);
            Exporter.export = "clipboard";
            showChoose = false;
        }
        if (event.getX() > file.xPosition && event.getX() < file.xPosition + file.width && event.getY() > file.yPosition && event.getY() < file.yPosition + file.height) {
            EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
            Minecraft.getMinecraft().theWorld.playSound(player.posX, player.posY, player.posZ, "random.click", 1, 1, false);
            Exporter.export = "file";
            showChoose = false;
        }
        if (event.getX() > club.xPosition && event.getX() < club.xPosition + club.width && event.getY() > club.yPosition && event.getY() < club.yPosition + club.height) {
            EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
            Minecraft.getMinecraft().theWorld.playSound(player.posX, player.posY, player.posZ, "random.click", 1, 1, false);
            if (ClubDashboard.getClub() == null) {
                UChat.chat("&cYou are not in a club!");
                return;
            }
            Exporter.export = "club";
            showChoose = false;
        }
        if (event.getX() > library.xPosition && event.getX() < library.xPosition + library.width && event.getY() > library.yPosition && event.getY() < library.yPosition + library.height) {
            EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
            Minecraft.getMinecraft().theWorld.playSound(player.posX, player.posY, player.posZ, "random.click", 1, 1, false);
            Exporter.export = "library";
            showChoose = false;
        }
    }

    @SubscribeEvent
    public void onGuiSlotClick(GuiMouseClickEvent event) {
        if (Minecraft.getMinecraft().thePlayer == null || Minecraft.getMinecraft().thePlayer.openContainer == null)
            return;
        if (Navigator.getContainerName() == null || !Navigator.getContainerName().equals("Functions")) return;
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (screen == null) return;
        if (screen instanceof GuiChest) {
            Slot slot = ((GuiChest) screen).getSlotUnderMouse();
            if (slot == null) return;
            if (slot.getStack() == null || !slot.getStack().hasDisplayName()) return;
            if (slot.getStack().getDisplayName().equals("§aUpload to Action Library")) {
                event.getCi().cancel();
                isSelecting = !isSelecting;
                selectedSlots.clear();
                return;
            }

            if (isSelecting) {
                event.getCi().cancel();
                if (slot.getHasStack() && slot.getStack().hasDisplayName()) {
                    if (selectedSlots.containsKey(slot.getSlotIndex())) {
                        selectedSlots.remove(slot.getSlotIndex());
                    } else {
                        selectedSlots.put(slot.getSlotIndex(), slot);
                    }
                }
            }
        }
    }
}
