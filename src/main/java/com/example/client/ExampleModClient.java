package com.example.client;

import net.minecraft.modding.api.ClassEventHandler;
import net.minecraft.modding.api.ModInfo;
import net.minecraft.modding.api.Side;
import net.minecraft.modding.api.event.client.ClientInitializingEvent;

@Side.Client
public class ExampleModClient implements ClassEventHandler<ClientInitializingEvent> {
    @Override
    public void handle(ModInfo modInfo, ClientInitializingEvent clientInitializingEvent) {
        System.out.println("Hello from prcraft client!");
    }
}
