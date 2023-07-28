package com.example;

import net.lenni0451.classtransform.TransformerManager;
import net.minecraft.modding.api.Mod;
import net.minecraft.modding.api.ModInfo;

public class ExampleMod implements Mod {

    @Override
    public void init(ModInfo modInfo, TransformerManager transformerManager) throws Exception {
        System.out.println("Hello from prcraft!");
    }

}
