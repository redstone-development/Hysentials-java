package llc.redstone.hysentials.mixin;

import cc.polyfrost.oneconfig.libs.universal.UGraphics;
import cc.polyfrost.oneconfig.libs.universal.UMinecraft;
import llc.redstone.hysentials.config.HysentialsConfig;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(value = GuiIngame.class, priority = 9001)
public class GuiIngameMixin {
    @Inject(method = "renderBossHealth", at = @At("HEAD"), cancellable = true)
    private void cancelBossBar(CallbackInfo ci) {
        if (HysentialsConfig.bossBarHUD.isEnabled()) {
            ci.cancel();
            UGraphics.color4f(1.0f, 1.0f, 1.0f, 1.0f);
            UMinecraft.getMinecraft().getTextureManager().bindTexture(Gui.icons);
        }
    }

    @Redirect(method = "renderScoreboard", at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/Scoreboard;getSortedScores(Lnet/minecraft/scoreboard/ScoreObjective;)Ljava/util/Collection;"))
    private Collection<Score> injectScore(Scoreboard scoreboard, ScoreObjective scoreObjective) {
        return scoreboard.getSortedScores(scoreObjective);
    }

    @Inject(method = "renderSelectedItem", at = @At("HEAD"), cancellable = true)
    private void cancelSelectedItem(CallbackInfo ci) {
        if (HysentialsConfig.heldItemTooltipHUD.isEnabled()) {
            ci.cancel();
        }
    }
}
