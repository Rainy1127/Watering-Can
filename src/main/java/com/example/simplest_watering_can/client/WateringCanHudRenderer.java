//package com.example.wateringcan.client;
//
//import com.example.wateringcan.item.ModItems;
//import com.example.wateringcan.item.WateringCanItem;
//import com.mojang.blaze3d.systems.RenderSystem;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.GuiGraphics;
//import net.minecraft.network.chat.Component;
//import net.minecraft.world.item.ItemStack;
//import net.neoforged.neoforge.client.event.RenderGuiEvent;
//import net.neoforged.bus.api.SubscribeEvent;
//
//public class WateringCanHudRenderer {
//
//    private static final int BAR_WIDTH  = 100; // ширина бара в пикселях
//    private static final int BAR_HEIGHT = 8;   // высота бара
//
//    @SubscribeEvent
//    public void onRenderGui(RenderGuiEvent.Post event) {
//        Minecraft mc = Minecraft.getInstance();
//        if (mc.player == null || mc.level == null) return;
//
//        ItemStack mainHand = mc.player.getMainHandItem();
//        ItemStack offHand  = mc.player.getOffhandItem();
//
//        ItemStack can = ItemStack.EMPTY;
//        if (mainHand.getItem() instanceof WateringCanItem) can = mainHand;
//        else if (offHand.getItem() instanceof WateringCanItem) can = offHand;
//
//        if (can.isEmpty()) return;
//
//        int water    = WateringCanItem.getWater(can);
//        int maxWater = WateringCanItem.MAX_WATER;
//
//        GuiGraphics gui    = event.getGuiGraphics();
//        int screenW        = mc.getWindow().getGuiScaledWidth();
//        int screenH        = mc.getWindow().getGuiScaledHeight();
//
//        int x = (screenW - BAR_WIDTH) / 2;
//        int y = screenH - 55;
//
//        gui.fill(x - 1, y - 1, x + BAR_WIDTH + 1, y + BAR_HEIGHT + 1, 0xAA000000);
//
//        int fillWidth = (int) (BAR_WIDTH * ((float) water / maxWater));
//        int color;
//        if (water > maxWater * 0.5f)       color = 0xFF2288FF; // синий — норм
//        else if (water > maxWater * 0.2f)  color = 0xFF44AAFF; // голубой — маловато
//        else                                color = 0xFFFF4444; // красный — почти пусто
//
//        if (fillWidth > 0) {
//            gui.fill(x, y, x + fillWidth, y + BAR_HEIGHT, color);
//        }
//
//        String text = "💧 " + water + " / " + maxWater;
//        int textColor = water <= 5 ? 0xFF4444 : 0xFFFFFF;
//        int textX = (screenW - mc.font.width(text)) / 2;
//        gui.drawString(mc.font, text, textX, y - 11, textColor, true);
//    }
//}
