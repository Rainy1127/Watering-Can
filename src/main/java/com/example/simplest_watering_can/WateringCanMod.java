package com.example.simplest_watering_can;

import com.example.simplest_watering_can.item.ModCreativeTabs;
import com.example.simplest_watering_can.item.ModItems;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(WateringCanMod.MODID)
public class WateringCanMod {
    public static final String MODID = "simplest_watering_can";
    public static final Logger LOGGER = LogUtils.getLogger();

    public WateringCanMod(IEventBus modEventBus, ModContainer container) {
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        LOGGER.info("Watering Can mod loaded!");
    }
}