package llc.redstone.hysentials.util;

import cc.polyfrost.oneconfig.libs.universal.UGraphics;
import cc.polyfrost.oneconfig.platform.Platform;
import cc.polyfrost.oneconfig.renderer.NanoVGHelper;
import cc.polyfrost.oneconfig.renderer.TextRenderer;
import llc.redstone.hysentials.guis.ResolutionUtil;
import llc.redstone.hysentials.guis.utils.Position;
import llc.redstone.hysentials.guis.ResolutionUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;

import static cc.polyfrost.oneconfig.renderer.TextRenderer.drawBorderedText;
import static java.lang.Math.*;

// From ChatTriggers
public class Renderer {
    public Renderer() {
    }

    private static Long colorized = null;
    private static Long drawMode = null;
    private static boolean retainTransforms = false;

    private static final Tessellator tessellator = Tessellator.getInstance();
    private static final WorldRenderer worldRenderer = tessellator.getWorldRenderer();

    public static void drawString(String text, float x, float y) {
        text = text.replace("&", "§");
        Minecraft.getMinecraft().fontRendererObj.drawString(text, x, y, (int) color(255, 255, 255, 255), false);
    }

    public static void drawString(FontRenderer renderer, String text, float x, float y) {
        text = text.replace("&", "§");
        renderer.drawString(text, x, y, (int) color(255, 255, 255, 255), false);
    }

    public static void drawString(String text, int x, int y) {
        text = text.replace("&", "§");
        Minecraft.getMinecraft().fontRendererObj.drawString(text, x, y, (int) color(255, 255, 255, 255), false);
    }

    public static void drawString(FontRenderer renderer, String text, float x, float y, boolean shadow) {
        text = text.replace("&", "§");
        renderer.drawString(text, x, y, (int) color(255, 255, 255, 255), shadow);
    }

    public static void drawString(String text, float x, int y, boolean shadow) {
        text = text.replace("&", "§");
        Minecraft.getMinecraft().fontRendererObj.drawString(text, x, y, (int) color(255, 255, 255, 255), shadow);
    }

    public static void drawScaledString(String text, float x, float y, TextRenderer.TextType type, float scale) {
        UGraphics.GL.pushMatrix();
        UGraphics.GL.scale(scale, scale, 1);
        switch (type) {
            case NONE:
                Platform.getGLPlatform().drawText(text, x * (1 / scale), y * (1 / scale), (int) color(255, 255, 255, 255), false);
                break;
            case SHADOW:
                Platform.getGLPlatform().drawText(text, x * (1 / scale), y * (1 / scale), (int) color(255, 255, 255, 255), true);
                break;
            case FULL:
                drawBorderedText(text, x * (1 / scale), y * (1 / scale), (int) color(255, 255, 255, 255), 100);
                break;
        }
        UGraphics.GL.popMatrix();
    }

    public static void drawString(String text, int x, int y, float size, boolean shadow) {
        GlStateManager.pushMatrix();
        GL11.glScalef(size, size, size);
        float mSize = (float) Math.pow(size, -1);
        drawString(text, Math.round(x / size), Math.round(y / size), shadow);
        GL11.glScalef(mSize, mSize, mSize);
        GlStateManager.popMatrix();
    }

    private static final float TWO_PI = 2.0f * (float) PI;

    public static void drawCircle(long color, float x, float y, float radius, int steps, int drawMode) {
        float theta = TWO_PI / steps;
        float cos = (float) cos(theta);
        float sin = (float) sin(theta);

        float xHolder;
        float circleX = 1f;
        float circleY = 0f;

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        doColor(color);

        worldRenderer.begin(drawMode, DefaultVertexFormats.POSITION);

        for (int i = 0; i <= steps; i++) {
            worldRenderer.pos(x, y, 0.0).endVertex();
            worldRenderer.pos((circleX * radius + x), (circleY * radius + y), 0.0).endVertex();
            xHolder = circleX;
            circleX = cos * circleX - sin * circleY;
            circleY = sin * xHolder + cos * circleY;
            worldRenderer.pos((circleX * radius + x), (circleY * radius + y), 0.0).endVertex();
        }

        tessellator.draw();

        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();

        finishDraw();
    }

    public static void drawRect(long color, float x, float y, float width, float height) {
        float[] pos = {x, y, x + width, y + height};
        if (pos[0] > pos[2])
            Collections.swap(Arrays.asList(pos), 0, 2);
        if (pos[1] > pos[3])
            Collections.swap(Arrays.asList(pos), 1, 3);

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        doColor(color);

        worldRenderer.begin(drawMode != null ? Math.toIntExact(drawMode) : 7, DefaultVertexFormats.POSITION);
        worldRenderer.pos((double) pos[0], (double) pos[3], 0.0).endVertex();
        worldRenderer.pos((double) pos[2], (double) pos[3], 0.0).endVertex();
        worldRenderer.pos((double) pos[2], (double) pos[1], 0.0).endVertex();
        worldRenderer.pos((double) pos[0], (double) pos[1], 0.0).endVertex();

        tessellator.draw();

        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();

        finishDraw();
    }

    public static void drawRect(float x, float y, float width, float height) {
        float[] pos = {x, y, x + width, y + height};
        if (pos[0] > pos[2])
            Collections.swap(Arrays.asList(pos), 0, 2);
        if (pos[1] > pos[3])
            Collections.swap(Arrays.asList(pos), 1, 3);

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        worldRenderer.begin(drawMode != null ? Math.toIntExact(drawMode) : 7, DefaultVertexFormats.POSITION);
        worldRenderer.pos((double) pos[0], (double) pos[3], 0.0).endVertex();
        worldRenderer.pos((double) pos[2], (double) pos[3], 0.0).endVertex();
        worldRenderer.pos((double) pos[2], (double) pos[1], 0.0).endVertex();
        worldRenderer.pos((double) pos[0], (double) pos[1], 0.0).endVertex();

        tessellator.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawGradientRect(int zLevel, int left, int top, int right, int bottom, int startColor, int endColor)
    {
        float startAlpha = (float)(startColor >> 24 & 255) / 255.0F;
        float startRed = (float)(startColor >> 16 & 255) / 255.0F;
        float startGreen = (float)(startColor >> 8 & 255) / 255.0F;
        float startBlue = (float)(startColor & 255) / 255.0F;
        float endAlpha = (float)(endColor >> 24 & 255) / 255.0F;
        float endRed = (float)(endColor >> 16 & 255) / 255.0F;
        float endGreen = (float)(endColor >> 8 & 255) / 255.0F;
        float endBlue = (float)(endColor & 255) / 255.0F;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(right, top, zLevel).color(startRed, startGreen, startBlue, startAlpha).endVertex();
        worldrenderer.pos(left, top, zLevel).color(startRed, startGreen, startBlue, startAlpha).endVertex();
        worldrenderer.pos(left, bottom, zLevel).color(endRed, endGreen, endBlue, endAlpha).endVertex();
        worldrenderer.pos(right, bottom, zLevel).color(endRed, endGreen, endBlue, endAlpha).endVertex();
        tessellator.draw();

        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void drawImage(ITextureObject image, double x, double y, double width, double height) {
        if (colorized == null)
            GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.enableBlend();
        GlStateManager.scale(1f, 1f, 50f);
        GlStateManager.bindTexture(image.getGlTextureId());
        GlStateManager.enableTexture2D();

        worldRenderer.begin(drawMode != null ? Math.toIntExact(drawMode) : 7, DefaultVertexFormats.POSITION_TEX);

        worldRenderer.pos(x, y + height, 0.0).tex(0.0, 1.0).endVertex();
        worldRenderer.pos(x + width, y + height, 0.0).tex(1.0, 1.0).endVertex();
        worldRenderer.pos(x + width, y, 0.0).tex(1.0, 0.0).endVertex();
        worldRenderer.pos(x, y, 0.0).tex(0.0, 0.0).endVertex();
        tessellator.draw();

        finishDraw();
    }

    public static void drawFrame(Frame frame, double x, double y) {
        drawImage(frame.texture, x, y, frame.width, frame.height);
    }

    public static void drawFrameCentered(Frame frame, double x, double y, double offsetX, double offsetY) {
        drawImage(frame.texture, offsetX + ((x - (double) frame.width) / 2), offsetY + ((y - (double) frame.height) / 2), frame.width, frame.height);
    }

    public static void drawFrameCentered(Frame frame, double x, double y, double maxWidth, double maxHeight, double offsetX, double offsetY) {
        if (frame == null) return;
        drawImage(frame.texture, offsetX + ((x - (double) Math.min(frame.width, maxWidth)) / 2), offsetY + ((y - (double) Math.min(frame.height, maxHeight)) / 2), Math.min(frame.width, maxWidth), Math.min(frame.height, maxHeight));
    }

    public static void drawFrameCentered(Frame frame, double x, double y) {
        drawImage(frame.texture, (x - (double) frame.width) / 2, (y - (double) frame.height) / 2, frame.width, frame.height);
    }

    public static void drawImage(ResourceLocation image, int x, int y, int width, int height) {
        drawImage(image, x, y, width, Double.valueOf(height));
    }

    public static void drawImage(ResourceLocation image, double x, double y, double width, double height) {
        if (colorized == null)
            GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.enableBlend();
        GlStateManager.scale(1f, 1f, 50f);
        Minecraft.getMinecraft().getTextureManager().bindTexture(image);
        GlStateManager.enableTexture2D();

        worldRenderer.begin(drawMode != null ? Math.toIntExact(drawMode) : 7, DefaultVertexFormats.POSITION_TEX);

        worldRenderer.pos(x, y + height, 0.0).tex(0.0, 1.0).endVertex();
        worldRenderer.pos(x + width, y + height, 0.0).tex(1.0, 1.0).endVertex();
        worldRenderer.pos(x + width, y, 0.0).tex(1.0, 0.0).endVertex();
        worldRenderer.pos(x, y, 0.0).tex(0.0, 0.0).endVertex();
        tessellator.draw();

        finishDraw();
    }

    public static Text drawText(String s, float x, float y) {
        Text text = new Text(s, x, y);
        text.draw();
        return text;
    }

    public static Text drawTextSplit(String s, float x, float y) {
        Text text = new Text(s, x, y);
        text.drawSplit();
        return text;
    }

    public static Text drawTextScaled(String s, float x, float y, float scale) {
        Text text = new Text(s, x, y);
        text.setScale(scale);
        text.draw();
        return text;
    }

    public static Text drawTextCentered(String s, float x, float y, float width, float height) {
        Text text = new Text(s, x, y, width, height);
        text.drawCenter();
        return text;
    }

    public static Text drawTextCenteredScaled(String s, float x, float y, float width, float height, float scale) {
        Text text = new Text(s, x, y, width, height);
        text.setScale(scale);
        text.drawCenter();
        return text;
    }

    private static void doColor(long longColor) {
        int color = (int) longColor;

        if (colorized == null) {
            float a = ((color >> 24) & 255) / 255.0f;
            float r = ((color >> 16) & 255) / 255.0f;
            float g = ((color >> 8) & 255) / 255.0f;
            float b = (color & 255) / 255.0f;
            GlStateManager.color(r, g, b, a);
        }
    }

    public static void finishDraw() {
        if (!retainTransforms) {
            colorized = null;
            drawMode = null;
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
        }
    }

    public static void translate(double x, double y, double z) {
        GlStateManager.translate(x, y, z);
        retainTransforms = true;
    }
    public static void untranslate(double x, double y, double z) {
        GlStateManager.translate(x, y, z);
        retainTransforms = false;
    }

    public static int shadow (long color1, long color2) {
        return (int) (color1 & 16579836 >> 2 | color2 & -16777216);
    }


    public static long color (String hex) {
        return Long.parseLong(hex, 16);
    }

    public static long color(long red, long green, long blue) {
        return color(red, green, blue, 255);
    }

    public static long color(long red, long green, long blue, long alpha) {
        return (clamp((int) alpha) * 0x1000000L
            + clamp((int) red) * 0x10000L
            + clamp((int) green) * 0x100L
            + clamp((int) blue));
    }

    public static int color(int red, int green, int blue, int alpha) {
        return (clamp(alpha) * 0x1000000
            + clamp(red) * 0x10000
            + clamp(green) * 0x100
            + clamp(blue));
    }

    static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    public static BufferedImage getImageFromUrl(String url) throws IOException {
        InputStream conn = NetworkUtils.setupConnection(url, "OneConfig/1.0.0", 5000, false);
        return ImageIO.read(conn);
    }

    private static void translate(int x, int y, float z) {
        GlStateManager.translate(x, y, z);
    }

    private static void scale(float scaleX, float scaleY) {
        GlStateManager.scale(scaleX, scaleY, 1.0f);
    }


    public static void drawLine(long color, float x1, float y1, float x2, float y2, float thickness, int drawMode) {
        double theta = -Math.atan2(y2 - y1, x2 - x1);
        double i = Math.sin(theta) * (thickness / 2);
        double j = Math.cos(theta) * (thickness / 2);

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        doColor(color);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();

        worldRenderer.begin(drawMode, DefaultVertexFormats.POSITION);

        worldRenderer.pos(x1 + i, y1 + j, 0.0).endVertex();
        worldRenderer.pos(x2 + i, y2 + j, 0.0).endVertex();
        worldRenderer.pos(x2 - i, y2 - j, 0.0).endVertex();
        worldRenderer.pos(x1 - i, y1 - j, 0.0).endVertex();

        tessellator.draw();

        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();

        finishDraw();
    }

    @NotNull
    public static int getStringWidth(@NotNull String str) {
        return Minecraft.getMinecraft().fontRendererObj.getStringWidth(str);
    }

    public static class IconButton {
        ITextureObject texture;
        Consumer<Integer> callback;
        int width, height;
        int x, y;

        public IconButton(String url, int width, int height, Consumer<Integer> callback) {
            try {
                this.texture = new DynamicTexture(getImageFromUrl(url));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            this.callback = callback;
            this.width = width;
            this.height = height;
        }

        public IconButton(ResourceLocation image, int width, int height, Consumer<Integer> callback) {
            Minecraft.getMinecraft().getTextureManager().loadTexture(image, new SimpleTexture(image));
            this.texture = Minecraft.getMinecraft().getTextureManager().getTexture(image);
            this.callback = callback;
            this.width = width;
            this.height = height;
        }

        public void draw(int x, int y) {
            drawImage(texture, x, y, width, height);
            this.x = x;
            this.y = y;
        }

        public void click(int mouseX, int mouseY) {
            if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height)
                callback.accept(0);
        }
    }

    public static class screen {
        public static int getWidth() {
            return ResolutionUtil.current().getScaledWidth();
        }

        public static int getScale() {
            return ResolutionUtil.current().getScaleFactor();
        }

        public static int getHeight() {
            return ResolutionUtil.current().getScaledHeight();
        }
    }

    public static class Text {
        float scale = 1f;
        public Position position;
        String text;

        public Text(String text, float x, float y) {
            this.text = text;
            this.position = new Position(x, y, getWidth(text), getHeight(text));
        }

        public Text(String text, float x, float y, float width, float height) {
            this.text = text;
            this.position = new Position(x, y, width, height);
        }

        public void draw() {
            TextRenderer.drawScaledString(text, position.getX(), position.getY(), 0xFFFFFF, TextRenderer.TextType.NONE, scale);
        }

        public void drawCenter() {
            TextRenderer.drawScaledString(text, position.getX() + ((position.getWidth() - getStringWidth(text)) / 2), position.getY() + ((position.getHeight() - getHeight(text)) / 2), 0xFFFFFF, TextRenderer.TextType.NONE, scale);
        }

        public void drawSplit() {
            String[] split = text.split("\n");
            for (int i = 0; i < split.length; i++) {
                String s = split[i];
                TextRenderer.drawScaledString(s, position.getX(), position.getY() + ((split.length - 1 - i) * 9f), 0xFFFFFF, TextRenderer.TextType.NONE, scale);
            }
        }

        public void setScale(float scale) {
            this.scale = scale;
        }

        public float getScale() {
            return scale;
        }

        public float getWidth(String s) {
            return getStringWidth(s);
        }

        public float getHeight(String s) {
            return 10 * scale;
        }

        public Position getPosition() {
            return position;
        }

        private float getStringWidth(String s) {
            return Minecraft.getMinecraft().fontRendererObj.getStringWidth(s) * scale;
        }
    }

    public static class Frame {
        int width, height;
        BufferedImage image;
        ITextureObject texture;
        public Frame(BufferedImage image) {
            this.image = image;
            this.width = image.getWidth();
            this.height = image.getHeight();
            this.texture = new DynamicTexture(image);
        }

        public Frame(ResourceLocation location) {
            Minecraft.getMinecraft().getTextureManager().loadTexture(location, new SimpleTexture(location));
            this.texture = Minecraft.getMinecraft().getTextureManager().getTexture(location);
            try {
                this.image = ImageIO.read(Minecraft.getMinecraft().getResourceManager().getResource(location).getInputStream());
                this.width = image.getWidth();
                this.height = image.getHeight();
            } catch (IOException e) {
            }
        }
    }
}
