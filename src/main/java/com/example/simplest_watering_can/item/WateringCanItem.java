package com.example.simplest_watering_can.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;


import javax.swing.text.html.parser.Entity;
import java.util.List;

public class WateringCanItem extends Item {

    // ── Константы ──────────────────────────────────────────────────────────
    public static final int MAX_WATER       = 16;  // макс. воды
    public static final int MAX_BONEMEAL    = 16;  // макс. костной муки
    private static final int COOLDOWN_TICKS = 20;  // кулдаун между кликами
    private static final float GROW_CHANCE  = 0.5f; // шанс роста без муки
    private static final int RADIUS         = 1;   // 3x3

    private static final String NBT_WATER    = "WaterLevel";
    private static final String NBT_BONEMEAL = "BonemealLevel";
    private static final String NBT_COOLDOWN = "WaterCooldown";

    public WateringCanItem(Properties properties) {
        super(properties);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  ВСПОМОГАТЕЛЬНЫЙ МЕТОД — читает весь тег не затирая другие поля
    // ══════════════════════════════════════════════════════════════════════

    private static CompoundTag getFullTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null ? data.copyTag() : new CompoundTag();
    }

    private static void saveTag(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    // ══════════════════════════════════════════════════════════════════════
    //  ВОДА
    // ══════════════════════════════════════════════════════════════════════

    public static int getWater(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null ? data.copyTag().getInt(NBT_WATER) : 0;
    }

    public static void setWater(ItemStack stack, int amount) {
        CompoundTag tag = getFullTag(stack);
        tag.putInt(NBT_WATER, Math.max(0, Math.min(MAX_WATER, amount)));
        saveTag(stack, tag);
    }

    public static float getWaterFraction(ItemStack stack) {
        return (float) getWater(stack) / MAX_WATER;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  КОСТНАЯ МУКА
    // ══════════════════════════════════════════════════════════════════════

    public static int getBonemeal(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null ? data.copyTag().getInt(NBT_BONEMEAL) : 0;
    }

    public static void setBonemeal(ItemStack stack, int amount) {
        CompoundTag tag = getFullTag(stack);
        tag.putInt(NBT_BONEMEAL, Math.max(0, Math.min(MAX_BONEMEAL, amount)));
        saveTag(stack, tag);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  КУЛДАУН
    // ══════════════════════════════════════════════════════════════════════

    private static long getLastUsed(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null ? data.copyTag().getLong(NBT_COOLDOWN) : 0L;
    }

    private static void setLastUsed(ItemStack stack, long gameTick) {
        CompoundTag tag = getFullTag(stack);
        tag.putLong(NBT_COOLDOWN, gameTick);
        saveTag(stack, tag);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  ПКМ — заправка водой / мукой / полив
    // ══════════════════════════════════════════════════════════════════════

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        HitResult hit = player.pick(5.0, 0.0f, false);

        if (!(hit instanceof BlockHitResult blockHit)) return InteractionResultHolder.fail(stack);

        BlockPos targetPos = blockHit.getBlockPos();

        // ── Заправка водой: ПКМ по источнику воды ────────────────────────
        FluidState fluid = level.getFluidState(targetPos);
        if (fluid.is(FluidTags.WATER) && fluid.isSource()) {
            if (getWater(stack) < MAX_WATER) {
                setWater(stack, MAX_WATER);
                level.playSound(player, player.blockPosition(),
                        SoundEvents.BUCKET_FILL, SoundSource.PLAYERS, 1.0f, 1.0f);
                if (!level.isClientSide()) {
                    player.displayClientMessage(
                            Component.translatable("item.simplest_watering_can.watering_can.filled")
                                    .withStyle(ChatFormatting.AQUA), true);
                }
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
            }
            return InteractionResultHolder.fail(stack);
        }

        // ── Нет воды ──────────────────────────────────────────────────────
        if (getWater(stack) <= 0) {
            if (!level.isClientSide()) {
                player.displayClientMessage(
                        Component.translatable("item.simplest_watering_can.watering_can.tooltip.empty")
                                .withStyle(ChatFormatting.RED), true);
            }
            return InteractionResultHolder.fail(stack);
        }

        // ── Кулдаун ───────────────────────────────────────────────────────
        if (!level.isClientSide()) {
            if (level.getGameTime() - getLastUsed(stack) < COOLDOWN_TICKS) {
                return InteractionResultHolder.fail(stack);
            }
        }

        // -- Увлажнение пашни: ПКМ по farmland ----------------------
        BlockState targetState = level.getBlockState(targetPos);
        if (targetState.getBlock() instanceof FarmBlock) {
            if (!level.isClientSide()) {
                int moisturized = 0;
                for (int dx = -RADIUS; dx <= RADIUS; dx++) {
                    for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                        BlockPos pos = targetPos.offset(dx, 0, dz);
                        BlockState state = level.getBlockState(pos);
                        if (state.getBlock() instanceof FarmBlock) {
                            level.setBlock(pos,
                                    state.setValue(BlockStateProperties.MOISTURE, 7), 3);
                            moisturized++;
                            if (level instanceof ServerLevel sl) {
                                sl.sendParticles(ParticleTypes.FALLING_WATER,
                                        pos.getX() + 0.5, pos.getY() + 1.05, pos.getZ() + 0.5,
                                        4, 0.3, 0.05, 0.3, 0.02);
                            }
                        }
                    }
                }
                if (moisturized == 0) return InteractionResultHolder.fail(stack);
                setWater(stack, getWater(stack) - 1);
                setLastUsed(stack, level.getGameTime());
                level.playSound(null, targetPos, SoundEvents.WATER_AMBIENT,
                        SoundSource.BLOCKS, 0.6f, 1.2f);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        // -- Тушение огня и костра: ПКМ по fire/campfire ------------
        if (targetState.getBlock() instanceof BaseFireBlock
                || targetState.is(Blocks.CAMPFIRE)
                || targetState.is(Blocks.SOUL_CAMPFIRE)) {
            if (!level.isClientSide()) {
                if (targetState.is(Blocks.CAMPFIRE) || targetState.is(Blocks.SOUL_CAMPFIRE)) {
                        level.setBlock(targetPos,
                                targetState.setValue(BlockStateProperties.LIT, false), 3);
                } else {
                    level.removeBlock(targetPos, false);
                    level.getEntitiesOfClass(net.minecraft.world.entity.Entity.class, new AABB(targetPos).inflate(1))
                            .forEach(e -> e.clearFire());
                }
                setWater(stack, getWater(stack) - 1);
                setLastUsed(stack, level.getGameTime());
                level.playSound(null, targetPos, SoundEvents.FIRE_EXTINGUISH,
                        SoundSource.BLOCKS, 1.0f, 1.0f);
                if (level instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.LARGE_SMOKE,
                            targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5,
                            6, 0.2, 0.2, 0.2, 0.02);
                }
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        // ── Ищем все растения в 3×3 ───────────────────────────────────────
        if (!level.isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) level;
            int bonemealInCan = getBonemeal(stack);
            boolean hasBonemeal = bonemealInCan > 0;
            boolean hasAnyCrop  = false;
            boolean anyGrew     = false;

            for (int dx = -RADIUS; dx <= RADIUS; dx++) {
                for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                    BlockPos pos = targetPos.offset(dx, 0, dz);
                    BlockState state = serverLevel.getBlockState(pos);

                    if (!(state.getBlock() instanceof BonemealableBlock bonemealable)) continue;
                    if (!bonemealable.isValidBonemealTarget(serverLevel, pos, state)) continue;

                    hasAnyCrop = true;

                    // Без муки — вообще не растёт
                    if (!hasBonemeal) continue;

                    // С мукой — 50/50
                    if (serverLevel.random.nextFloat() < GROW_CHANCE) {
                        bonemealable.performBonemeal(serverLevel, serverLevel.random, pos, state);
                        anyGrew = true;
                        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                                pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5,
                                4, 0.3, 0.1, 0.3, 0.04);
                    } else {
                        serverLevel.sendParticles(ParticleTypes.FALLING_WATER,
                                pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5,
                                6, 0.3, 0.1, 0.3, 0.04);
                    }
                }
            }

            if (!hasAnyCrop) return InteractionResultHolder.fail(stack);

// Вода тратится всегда если есть грядка
            setWater(stack, getWater(stack) - 1);
// Мука тратится только если хоть что-то выросло
            if (hasBonemeal && anyGrew) setBonemeal(stack, bonemealInCan - 1);
            setLastUsed(stack, level.getGameTime());

            // Ничего не нашли — не тратим ресурсы
            if (!hasAnyCrop) return InteractionResultHolder.fail(stack);

            // -1 вода за клик
            setWater(stack, getWater(stack) - 1);
            // -1 мука за клик если была
            if (hasBonemeal) setBonemeal(stack, bonemealInCan - 1);
            setLastUsed(stack, level.getGameTime());

            level.playSound(null, targetPos, SoundEvents.WATER_AMBIENT,
                    SoundSource.BLOCKS, 0.8f, 1.1f);

        } else {
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Shift+ПКМ по лейке в инвентаре — загрузка костной муки
    //  Вызывается через событие ContainerScreenEvent или overrideStackedOnOther
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Вызывается когда игрок кликает стопкой предметов ПО лейке в инвентаре.
     * Если это костная мука — загружаем в лейку.
     */
    @Override
    public boolean overrideOtherStackedOnMe(ItemStack can, ItemStack incoming,
                                            net.minecraft.world.inventory.Slot slot,
                                            net.minecraft.world.inventory.ClickAction action,
                                            Player player,
                                            net.minecraft.world.entity.SlotAccess access) {
        // Только ПКМ клик (action == ClickAction.SECONDARY)
        if (action != net.minecraft.world.inventory.ClickAction.SECONDARY) return false;
        // Только костная мука
        if (!incoming.is(Items.BONE_MEAL)) return false;

        int current = getBonemeal(can);
        if (current >= MAX_BONEMEAL) return false;

        // Сколько можем принять
        int space    = MAX_BONEMEAL - current;
        int toAdd    = Math.min(space, incoming.getCount());

        setBonemeal(can, current + toAdd);
        incoming.shrink(toAdd);

        // Звук
        player.playSound(SoundEvents.BONE_MEAL_USE, 1.0f, 1.0f);
        return true;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  ПРОГРЕСС-БАР ПОД ИКОНКОЙ (вода — синий)
    // ══════════════════════════════════════════════════════════════════════

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0f * getWaterFraction(stack));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return getWater(stack) <= 0 ? 0xFF4444 : 0x2255FF;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  TOOLTIP
    // ══════════════════════════════════════════════════════════════════════

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltipComponents, TooltipFlag flag) {
        int water    = getWater(stack);
        int bonemeal = getBonemeal(stack);

        tooltipComponents.add(
                Component.translatable("item.simplest_watering_can.watering_can.tooltip.water",
                                water, MAX_WATER)
                        .withStyle(water > 0 ? ChatFormatting.AQUA : ChatFormatting.DARK_GRAY)
        );

        tooltipComponents.add(
                Component.translatable("item.simplest_watering_can.watering_can.tooltip.bonemeal",
                                bonemeal, MAX_BONEMEAL)
                        .withStyle(bonemeal > 0 ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY)
        );
    }
}