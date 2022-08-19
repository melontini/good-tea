package me.melontini.goodtea.behaviors;

import me.melontini.goodtea.GoodTea;
import me.melontini.goodtea.ducks.HoglinRepellentAccess;
import me.melontini.goodtea.util.LogUtil;
import net.minecraft.block.*;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.explosion.Explosion;

import java.util.*;

public class TeaCupBehavior {
    public static TeaCupBehavior INSTANCE = new TeaCupBehavior();
    Map<Item, Behavior> TEA_CUP_BEHAVIOR = new HashMap<>();
    Map<Item, MutableText> TEA_CUP_TOOLTIP = new HashMap<>();

    private TeaCupBehavior() {
    }

    public void addDefaultBehaviours() {
        for (Item item : Registry.ITEM) {
            if (item instanceof SwordItem swordItem) {
                if (item != Items.NETHERITE_SWORD) addBehavior(item, (player, stack) -> player.damage(DamageSource.GENERIC, swordItem.getAttackDamage() * 1.4F));
                else addBehavior(item, (player, stack) -> player.damage(DamageSource.GENERIC, Float.MAX_VALUE));
            }
            if (item instanceof MusicDiscItem musicDiscItem) {
                addBehavior(item, (player, stack) -> player.world.playSoundFromEntity(null, player, musicDiscItem.getSound(), SoundCategory.RECORDS, 1F, 1F));
            }
            if (item instanceof BlockItem blockItem) {
                if (blockItem.getBlock() instanceof FlowerBlock flowerBlock) {
                    addBehavior(item, (player, stack) -> {
                        StatusEffectInstance effectInstance = new StatusEffectInstance(flowerBlock.getEffectInStew(), flowerBlock.getEffectInStew().isInstant() ? flowerBlock.getEffectInStewDuration() : (flowerBlock.getEffectInStewDuration() * 2));
                        player.addStatusEffect(effectInstance);
                    });
                }
                if (blockItem.getBlock() instanceof BedBlock) {
                    addBehavior(item, (player, stack) -> {
                        if (!BedBlock.isBedWorking(player.world))
                            player.world.createExplosion(null, DamageSource.badRespawnPoint(), null, player.getX() + 0.5, player.getY() + 0.5, player.getZ() + 0.5, 5.0F, true, Explosion.DestructionType.DESTROY);
                    });
                }
            }
        }
        addBehavior(Items.ENCHANTED_GOLDEN_APPLE, (player, stack) -> {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 600, 0));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 8000, 0));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 8000, 0));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 3200, 2));
        });

        addBehavior(Items.TNT, (player, stack) -> player.world.createExplosion(null, player.getX(), player.getY(), player.getZ(), 4.0F, Explosion.DestructionType.DESTROY));
        addBehavior(Items.GUNPOWDER, (player, stack) -> player.world.createExplosion(null, player.getX(), player.getY(), player.getZ(), 1.0F, Explosion.DestructionType.DESTROY));
        addBehavior(Items.TNT_MINECART, (player, stack) -> player.world.createExplosion(null, player.getX(), player.getY(), player.getZ(), 4.0F, Explosion.DestructionType.DESTROY));

        addBehavior(Items.END_ROD, (player, stack) -> {
            Random random = new Random();
            ((ServerWorld) player.world).spawnParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1, player.getZ(), 35, random.nextDouble(0.4) - 0.2, random.nextDouble(0.4) - 0.2, random.nextDouble(0.4) - 0.2, 0.3);
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 600, 0));
        });

        addBehavior(Items.SPORE_BLOSSOM, (player, stack) -> {
            Random random = new Random();
            BlockPos.Mutable mutable = new BlockPos.Mutable();

            int i = player.getBlockPos().getX();
            int j = player.getBlockPos().getY();
            int k = player.getBlockPos().getZ();
            for (int l = 0; l < 3; ++l) {
                mutable.set(i + MathHelper.nextInt(random, -4, 4), j + 4, k + MathHelper.nextInt(random, -4, 4));
                BlockState blockState = player.world.getBlockState(mutable);
                if (!blockState.isFullCube(player.world, mutable)) {
                    ((ServerWorld) player.world).spawnParticles(ParticleTypes.SPORE_BLOSSOM_AIR, mutable.getX() + random.nextDouble(), mutable.getY() + random.nextDouble(), mutable.getZ() + random.nextDouble(), 7, 0.0, 0.0, 0.0, 0.0);
                }
            }

            player.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 300, 0));
        });

        addBehavior(Items.AXOLOTL_BUCKET, (player, stack) -> player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 100, 0)));
        addBehavior(Items.RED_MUSHROOM, (player, stack) -> player.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 100, 0)));
        addBehavior(Items.RED_MUSHROOM_BLOCK, (player, stack) -> player.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 250, 1)));
        addBehavior(Items.WHEAT, (player, stack) -> {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 600));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 1000));
        });
        addBehavior(Items.HAY_BLOCK, (player, stack) -> {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 600 * 9));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 1000 * 9));
        });

        addBehavior(Items.OBSIDIAN, (player, stack) -> {
            var instance = Objects.requireNonNull(player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS));
            if (!instance.hasModifier(GoodTea.OBSIDIAN_TOUGHNESS)) {
                instance.addPersistentModifier(GoodTea.OBSIDIAN_TOUGHNESS);
                player.sendMessage(new TranslatableText("text.good-tea.obsidian_toughness"), true);
            }
        });

        addBehavior(Items.RABBIT_FOOT, (player, stack) -> {
            var instance = Objects.requireNonNull(player.getAttributeInstance(EntityAttributes.GENERIC_LUCK));
            if (!instance.hasModifier(GoodTea.RABBITS_LUCK)) {
                instance.addPersistentModifier(GoodTea.RABBITS_LUCK);
                player.sendMessage(new TranslatableText("text.good-tea.rabbits_luck"), true);
            }
        });

        addBehavior(Items.POTION, (player, stack) -> {
            for (StatusEffectInstance sei : PotionUtil.getPotionEffects(stack)) {
                if (sei.getEffectType().isInstant()) {
                    sei.getEffectType().applyInstantEffect(player, player, player, sei.getAmplifier(), 1.0);
                } else {
                    var effect = new StatusEffectInstance(sei.getEffectType(), (int) (sei.getDuration() * 1.6), sei.getAmplifier(), sei.isAmbient(), sei.shouldShowParticles(), sei.shouldShowIcon());
                    player.addStatusEffect(new StatusEffectInstance(effect));
                }
            }
        });

        addBehavior(Items.SPLASH_POTION, (player, stack) -> {
            PotionEntity entity = new PotionEntity(player.world, player.getX(), player.getY(), player.getZ());
            entity.setItem(stack);

            Potion potion = PotionUtil.getPotion(stack);
            List<StatusEffectInstance> list = PotionUtil.getPotionEffects(stack);
            boolean bl = potion == Potions.WATER && list.isEmpty();

            if (bl) entity.damageEntitiesHurtByWater();
            else if (!list.isEmpty()) entity.applySplashPotion(list, player);

            int i = potion.hasInstantEffect() ? WorldEvents.INSTANT_SPLASH_POTION_SPLASHED : WorldEvents.SPLASH_POTION_SPLASHED;
            player.world.syncWorldEvent(i, player.getBlockPos(), PotionUtil.getColor(stack));

            entity.discard();
        });

        addBehavior(Items.LINGERING_POTION, (player, stack) -> {
            PotionEntity entity = new PotionEntity(player.world, player.getX(), player.getY(), player.getZ());
            entity.setItem(stack);

            Potion potion = PotionUtil.getPotion(stack);
            List<StatusEffectInstance> list = PotionUtil.getPotionEffects(stack);
            boolean bl = potion == Potions.WATER && list.isEmpty();

            if (bl) entity.damageEntitiesHurtByWater();
            else if (!list.isEmpty()) entity.applyLingeringPotion(stack, potion);

            entity.discard();
        });

        addBehavior(Items.SPONGE, (player, stack) -> {
            ((SpongeBlock)Blocks.SPONGE).absorbWater(player.world, player.getBlockPos());
            player.getInventory().insertStack(new ItemStack(Items.WET_SPONGE));
        });

        addBehavior(Items.WARPED_FUNGUS, (player, stack) -> {
            ((HoglinRepellentAccess) player).makeHoglinRepellent(2400);
            player.sendMessage(new TranslatableText("text.good-tea.hoglin_repellent"), true);
        });
    }

    public Behavior getBehavior(ItemStack stack) {
        return getBehavior(stack.getItem());
    }

    public Behavior getBehavior(Item item) {
        return TEA_CUP_BEHAVIOR.getOrDefault(item, (player, stack) -> {
        });
    }

    public void removeBehavior(ItemStack stack) {
        removeBehavior(stack.getItem());
    }

    public void removeBehavior(Item item) {
        TEA_CUP_BEHAVIOR.remove(item);
    }

    public boolean hasBehavior(ItemStack stack) {
        return TEA_CUP_BEHAVIOR.containsKey(stack.getItem());
    }

    public boolean hasBehavior(Item item) {
        return TEA_CUP_BEHAVIOR.containsKey(item);
    }

    public void addBehavior(Item item, Behavior behavior) {
        if (!TEA_CUP_BEHAVIOR.containsKey(item)) {
            TEA_CUP_BEHAVIOR.putIfAbsent(item, behavior);
        } else {
            LogUtil.error("Tried to add behaviour for the same item twice! {}", item);
        }
    }


    public MutableText getTooltip(ItemStack stack) {
        return getTooltip(stack.getItem());
    }

    public MutableText getTooltip(Item item) {
        return TEA_CUP_TOOLTIP.get(item);
    }

    public void removeTooltip(ItemStack stack) {
        removeTooltip(stack.getItem());
    }

    public void removeTooltip(Item item) {
        TEA_CUP_TOOLTIP.remove(item);
    }

    public boolean hasTooltip(ItemStack stack) {
        return TEA_CUP_TOOLTIP.containsKey(stack.getItem());
    }

    public boolean hasTooltip(Item item) {
        return TEA_CUP_TOOLTIP.containsKey(item);
    }

    public void addTooltip(Item item, MutableText text) {
        if (!TEA_CUP_TOOLTIP.containsKey(item)) {
            TEA_CUP_TOOLTIP.putIfAbsent(item, text);
        } else {
            LogUtil.error("Tried to add a tooltip for the same item twice! {}", item);
        }
    }

    public void addDefaultTooltips() {
        for (Item item : Registry.ITEM) {
            if (item instanceof MusicDiscItem) {
                addTooltip(item, new TranslatableText("tea-tooltip.good-tea.music-disc-tea").formatted(Formatting.GRAY, Formatting.ITALIC));
            }
            if (item instanceof BlockItem blockItem) {
                if (blockItem.getBlock() instanceof BedBlock) {
                    addTooltip(item, new TranslatableText("tea-tooltip.good-tea.bed-tea").formatted(Formatting.GRAY, Formatting.ITALIC));
                }
            }
        }
        addTooltip(GoodTea.TEA_CUP, new TranslatableText("tea-tooltip.good-tea.tea-cup-tea").formatted(Formatting.GRAY, Formatting.ITALIC));
        addTooltip(GoodTea.KETTLE_BLOCK_ITEM, new TranslatableText("tea-tooltip.good-tea.tea-cup-tea").formatted(Formatting.GRAY, Formatting.ITALIC));
        addTooltip(Items.AXOLOTL_BUCKET, new TranslatableText("tea-tooltip.good-tea.axolotl_tea").formatted(Formatting.GRAY, Formatting.ITALIC));
        addTooltip(Items.WHEAT, new TranslatableText("tea-tooltip.good-tea.wheat_tea").formatted(Formatting.GRAY, Formatting.ITALIC));
        addTooltip(Items.HAY_BLOCK, new TranslatableText("tea-tooltip.good-tea.wheat_tea").formatted(Formatting.GRAY, Formatting.ITALIC));
    }

    @FunctionalInterface
    public interface Behavior {
        void run(PlayerEntity player, ItemStack stack);
    }
}