package com.example.simplest_watering_can.item;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

public class ModCreativeTabs {

    public static void register(IEventBus modEventBus) {
        // Подписываемся на событие заполнения вкладок
        modEventBus.addListener(ModCreativeTabs::addToExistingTab);
    }

    // Добавляем лейку в существующую вкладку Tools and Utilities
    private static void addToExistingTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.WATERING_CAN);
        }
    }
}