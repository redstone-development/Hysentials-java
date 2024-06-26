package llc.redstone.hysentials.handlers.sbb;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import llc.redstone.hysentials.guis.utils.SBBoxes;
import cc.polyfrost.oneconfig.utils.Multithreading;
import llc.redstone.hysentials.Hysentials;
import llc.redstone.hysentials.guis.sbBoxes.SBBoxesEditor;
import llc.redstone.hysentials.handlers.redworks.HousingScoreboard;
import llc.redstone.hysentials.util.Renderer;
import llc.redstone.hysentials.util.ScoreboardWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.json.JSONArray;
import org.json.JSONObject;
import org.lwjgl.input.Mouse;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static net.minecraft.client.Minecraft.getMinecraft;

public class SbbRenderer {
    public static HousingScoreboard housingScoreboard;

    public SbbRenderer() {
        housingScoreboard = new HousingScoreboard();
    }

    int tick = 0;

    @SubscribeEvent
    public void onTick(TickEvent event) {
        if (event.phase == TickEvent.Phase.END) return;
        if (event.type != TickEvent.Type.CLIENT) return;
        ScoreboardWrapper.resetCache();

        if (++tick % (60 * 20 * 5) == 0) {
            JSONArray array = new JSONArray();
            for (SBBoxes box : SBBoxes.boxes) {
                array.put(box.save());
            }
            JSONObject object = new JSONObject();
            object.put("lines", array);
            Hysentials.INSTANCE.sbBoxes.jsonObject = object;
            Hysentials.INSTANCE.sbBoxes.save();
        }

        if (SBBoxesEditor.configGui != null && SBBoxesEditor.configGui.isClosed && SBBoxesEditor.isConfigOpen) {
            SBBoxesEditor.configGui = null;
            SBBoxesEditor.isConfigOpen = false;
            getMinecraft().thePlayer.closeScreen();
            new SBBoxesEditor().show();
        }
    }

    @SubscribeEvent
    public void onMouseClick(MouseEvent event) {
        // add new dragged
        if (Mouse.isButtonDown(event.button)) {
            draggedState.put(event.button, new State(getMouseX(), getMouseY()));
        } else draggedState.remove(event.button);
    }

    @SubscribeEvent
    public void onGuiMouseInput(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (getMinecraft().theWorld == null) {
            draggedState.clear();
            return;
        }
        // add new dragged
        if (Mouse.isButtonDown(Mouse.getEventButton())) {
            draggedState.put(Mouse.getEventButton(), new State(getMouseX(), getMouseY()));
        } else draggedState.remove(Mouse.getEventButton());
    }

    public static void drawBox(float x, float y, float width, float height, OneColor color, boolean boxShadows, int radius) {
        int shadowColor = (int) Renderer.color(color.getRed(), color.getGreen(), color.getBlue(), (long) (color.getAlpha() * 0.42F));
        drawBox(x, y, width, height, color, boxShadows ? new OneColor(shadowColor) : null, radius);
    }

    public static void drawBox(float x, float y, float width, float height, OneColor color, OneColor boxShadows, int radius) {
        x = (float) Math.round(x);
        y = (float) Math.round(y);
        width = (float) Math.round(width);
        height = (float) Math.round(height);
        long boxColor = Renderer.color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        Renderer.drawRect(boxColor, x + radius, y, width - (radius * 2), height);
        Renderer.drawRect(boxColor, x, y + radius, radius, height - (2 * radius));
        Renderer.drawRect(boxColor, x + width - radius, y + radius, radius, height - (2 * radius));

        if (boxShadows != null) {
            long shadowColor = Renderer.color(boxShadows.getRed(), boxShadows.getGreen(), boxShadows.getBlue(), boxShadows.getAlpha());
            Renderer.drawRect(shadowColor, x + width, y + (2 * radius), radius, height - (2 * radius));
            if (radius != 0) {
                Renderer.drawRect(shadowColor, x + width - radius, y + height - radius, radius, radius);
            }
            Renderer.drawRect(shadowColor, x + (2 * radius), y + height, width - (2 * radius), radius);
        }
    }

    static NanoVGHelper nvg = NanoVGHelper.INSTANCE;
    public static void drawBox(long vg, float x, float y, float width, float height, OneColor oneColor, boolean boxShadows, int radius) {
        int color = Renderer.color(oneColor.getRed(), oneColor.getGreen(), oneColor.getBlue(), oneColor.getAlpha());
        nvg.drawRect(vg, x + radius, y, width - (radius * 2), height, color);
        nvg.drawRect(vg, x, y + radius, radius, height - (2 * radius), color);
        nvg.drawRect(vg, x + width - radius, y + radius, radius, height - (2 * radius), color);

        if (boxShadows) {
            int shadowColor = Renderer.color(oneColor.getRed(), oneColor.getGreen(), oneColor.getBlue(), (int) (oneColor.getAlpha() * 0.42F));
            nvg.drawRect(vg, x + width, y + (2 * radius), radius, height - (2 * radius), shadowColor);
            if (radius != 0) {
                nvg.drawRect(vg, x + width - radius, y + height - radius, radius, radius, shadowColor);
            }
            nvg.drawRect(vg, x + (2 * radius), y + height, width - (2 * radius), radius, shadowColor);
        }
    }


    private static Map<Integer, State> draggedState = new HashMap<>();

    public static class State {
        private final double x;
        private final double y;

        public State(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }
    }

    public static float getMouseX() {
        float mx = Mouse.getX();
        float rw = new ScaledResolution(getMinecraft()).getScaledWidth();
        float dw = getMinecraft().displayWidth;
        return mx * rw / dw;
    }

    public static float getMouseY() {
        float my = Mouse.getY();
        float rh = new ScaledResolution(getMinecraft()).getScaledHeight();
        float dh = getMinecraft().displayHeight;
        return rh - my * rh / dh - 1f;
    }
}
