package com.example.transformers;

import net.minecraft.modding.api.Side;
import net.minecraft.src.GuiMainMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

@Side.Client
@Mixin(GuiMainMenu.class)
public class MixinGuiMainMenu {

    @Inject(method = "initGui", at = @At(value = "HEAD"))
    public void initGui() {
        System.out.println("This line is printed by an example mod mixin!");
    }

}
