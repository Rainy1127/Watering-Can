package com.example.simplest_watering_can.client;

import com.example.simplest_watering_can.item.ModItems;
import com.example.simplest_watering_can.item.WateringCanItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;

public class WateringCanPropertyOverride {

    public static void register() {
        ItemProperties.register(
                // Указываем на наш предмет через ModItems
                ModItems.WATERING_CAN.get(),
                ResourceLocation.fromNamespaceAndPath("simplest_watering_can", "filled"),
                new ClampedItemPropertyFunction() {
                    @Override
                    public float unclampedCall(ItemStack stack, @Nullable ClientLevel level,
                                               @Nullable LivingEntity entity, int seed) {
                        // 0.0 = пустая, 1.0 = есть вода
                        return WateringCanItem.getWater(stack) > 0 ? 1.0f : 0.0f;
                    }
                }
        );
    }
}
