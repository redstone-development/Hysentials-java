package llc.redstone.hysentials.mixin;

import net.minecraft.client.gui.GuiSpectator;
import net.minecraft.client.gui.spectator.ISpectatorMenuObject;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GuiSpectator.class)
public interface GuiSpectatorAccessor {

    @Accessor
    SpectatorMenu getField_175271_i();

    @Invoker("func_175265_c")
    float alpha();

    @Invoker("func_175266_a")
    void drawItem(int i, int j, float f, float g, ISpectatorMenuObject iSpectatorMenuObject);
}
