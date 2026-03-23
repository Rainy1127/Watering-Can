package com.example.simplest_watering_can.item;

import com.example.simplest_watering_can.WateringCanMod;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(WateringCanMod.MODID);

    public static final DeferredItem<WateringCanItem> WATERING_CAN =
            ITEMS.registerItem("watering_can",
                    props -> new WateringCanItem(props.stacksTo(1)));
}
