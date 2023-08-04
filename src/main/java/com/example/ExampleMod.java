package com.example;

import net.lenni0451.classtransform.TransformerManager;
import net.minecraft.modding.api.Mod;
import net.minecraft.modding.api.ModInfo;
import org.jetbrains.annotations.NotNull;

public class ExampleMod implements Mod {

    @Override
    public void init(@NotNull ModInfo modInfo, @NotNull TransformerManager transformerManager) throws Exception {
        System.out.println("Hello from prcraft!");
    }

}
