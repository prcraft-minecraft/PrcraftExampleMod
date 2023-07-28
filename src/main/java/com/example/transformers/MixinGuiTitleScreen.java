package com.example.transformers;

import net.lenni0451.classtransform.annotations.injection.CInject;
import net.minecraft.src.GuiMainMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(GuiMainMenu.class)
public class TransformMainMenu {

    @Inject(method = "initGui", at = @At(value = "HEAD"))
    public void initGui() {
        System.out.println("This line is printed by an example mod mixin!");
    }

}
