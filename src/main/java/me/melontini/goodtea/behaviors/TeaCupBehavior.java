package me.melontini.goodtea.behaviors;

import me.melontini.goodtea.GoodTea;
import me.melontini.goodtea.ducks.CraftingScreenAllowanceAccess;
import me.melontini.goodtea.ducks.DivineAccess;
import me.melontini.goodtea.ducks.HoglinRepellentAccess;
import me.melontini.goodtea.mixin.BucketItemAccessor;
import me.melontini.goodtea.mixin.PotionEntityAccessor;
import me.melontini.goodtea.mixin.SpongeBlockAccessor;
import me.melontini.goodtea.util.JavaRandomUtil;
import me.melontini.goodtea.util.LogUtil;
import me.melontini.goodtea.util.TextUtil;
import net.minecraft.block.*;
import net.minecraft.entity.Bucketable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.explosion.Explosion;
import org.apache.commons.compress.utils.Lists;

import java.util.*;

public class TeaCupBehavior {
    public static TeaCupBehavior INSTANCE = new TeaCupBehavior();
    Map<Item, Behavior> TEA_CUP_BEHAVIOR = new HashMap<>();
    Map<Item, MutableText> TEA_CUP_TOOLTIP = new HashMap<>();

    private TeaCupBehavior() {
    }

    public void addDefaultBehaviours() {
        for (Item item : Registry.ITEM) {
            if (item instanceof EntityBucketItem entityBucketItem) {
                if (item != Items.AXOLOTL_BUCKET) addBehavior(item, (entity, stack) -> {
                    var bucketEntity = ((BucketItemAccessor)entityBucketItem).getEntityType().spawnFromItemStack((ServerWorld) entity.world, stack, null, new BlockPos(entity.getX(), entity.getEyePos().y, entity.getZ()), SpawnReason.BUCKET, false, false);
                    if (bucketEntity instanceof Bucketable bucketable) {
                        bucketable.copyDataFromNbt(stack.getOrCreateNbt());
                        bucketEntity.setVelocity(entity.getRotationVector());
                        entity.world.spawnEntity(bucketEntity);
                    }
                });
            }
            if (item instanceof SwordItem swordItem) {
                addBehavior(item, (entity, stack) -> entity.damage(DamageSource.GENERIC, swordItem.getAttackDamage() * 3.0F));
            }
            if (item instanceof MusicDiscItem musicDiscItem) {
                addBehavior(item, (entity, stack) -> entity.world.playSoundFromEntity(null, entity, musicDiscItem.getSound(), SoundCategory.RECORDS, 1F, 1F));
            }
            if (item instanceof BlockItem blockItem) {
                if (blockItem.getBlock() instanceof FlowerBlock flowerBlock) {
                    addBehavior(item, (entity, stack) -> {
                        StatusEffectInstance effectInstance = new StatusEffectInstance(flowerBlock.getEffectInStew(), flowerBlock.getEffectInStew().isInstant() ? flowerBlock.getEffectInStewDuration() : (flowerBlock.getEffectInStewDuration() * 2));
                        entity.addStatusEffect(effectInstance);
                    });
                }
                if (blockItem.getBlock() instanceof BedBlock) {
                    addBehavior(item, (entity, stack) -> {
                        if (!BedBlock.isBedWorking(entity.world))
                            entity.world.createExplosion(null, DamageSource.badRespawnPoint(), null, entity.getX() + 0.5, entity.getY() + 0.5, entity.getZ() + 0.5, 5.0F, true, Explosion.DestructionType.DESTROY);
                    });
                }
            }
        }
        addBehavior(Items.ENCHANTED_GOLDEN_APPLE, (entity, stack) -> {
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 600, 0));
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 8000, 0));
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 8000, 0));
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 3200, 2));
        });

        addBehavior(Items.TNT, (entity, stack) -> entity.world.createExplosion(null, entity.getX(), entity.getY(), entity.getZ(), 4.0F, Explosion.DestructionType.DESTROY));
        addBehavior(Items.GUNPOWDER, (entity, stack) -> entity.world.createExplosion(null, entity.getX(), entity.getY(), entity.getZ(), 1.0F, Explosion.DestructionType.DESTROY));
        addBehavior(Items.TNT_MINECART, (entity, stack) -> entity.world.createExplosion(null, entity.getX(), entity.getY(), entity.getZ(), 4.0F, Explosion.DestructionType.DESTROY));

        addBehavior(Items.END_ROD, (entity, stack) -> {
            Random random = new Random();
            ((ServerWorld) entity.world).spawnParticles(ParticleTypes.END_ROD, entity.getX(), entity.getY() + 1.6, entity.getZ(), 35, random.nextDouble(0.4) - 0.2, random.nextDouble(0.4) - 0.2, random.nextDouble(0.4) - 0.2, 0.3);
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 600, 0));
        });

        addBehavior(Items.SPORE_BLOSSOM, (entity, stack) -> {
            Random random = new Random();
            BlockPos.Mutable mutable = new BlockPos.Mutable();

            int i = entity.getBlockPos().getX();
            int j = entity.getBlockPos().getY();
            int k = entity.getBlockPos().getZ();
            for (int l = 0; l < 3; ++l) {
                mutable.set(i + JavaRandomUtil.nextInt(random, -4, 4), j + 4, k + JavaRandomUtil.nextInt(random, -4, 4));
                BlockState blockState = entity.world.getBlockState(mutable);
                if (!blockState.isFullCube(entity.world, mutable)) {
                    ((ServerWorld) entity.world).spawnParticles(ParticleTypes.SPORE_BLOSSOM_AIR, mutable.getX() + random.nextDouble(), mutable.getY() + random.nextDouble(), mutable.getZ() + random.nextDouble(), 7, 0.0, 0.0, 0.0, 0.0);
                }
            }

            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 300, 0));
        });

        addBehavior(Items.AXOLOTL_BUCKET, (entity, stack) -> {
                    entity.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 100, 0));
                    var bucketEntity = ((BucketItemAccessor) Items.AXOLOTL_BUCKET).getEntityType().spawnFromItemStack((ServerWorld) entity.world, stack, null, new BlockPos(entity.getX(), entity.getEyePos().y, entity.getZ()), SpawnReason.BUCKET, true, false);
                    if (bucketEntity instanceof Bucketable bucketable) {
                        bucketable.copyDataFromNbt(stack.getOrCreateNbt());
                        bucketEntity.setVelocity(entity.getRotationVector());
                        bucketEntity.world.spawnEntity(entity);
                    }
                }
        );
        addBehavior(Items.RED_MUSHROOM, (entity, stack) -> entity.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 100, 0)));
        addBehavior(Items.RED_MUSHROOM_BLOCK, (entity, stack) -> entity.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 250, 1)));
        addBehavior(Items.WHEAT, (entity, stack) -> {
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 600));
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 1000));
        });
        addBehavior(Items.HAY_BLOCK, (entity, stack) -> {
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 600 * 9));
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 1000 * 9));
        });

        addBehavior(Items.OBSIDIAN, (entity, stack) -> {
            var instance = Objects.requireNonNull(entity.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS));
            if (!instance.hasModifier(GoodTea.OBSIDIAN_TOUGHNESS)) {
                instance.addPersistentModifier(GoodTea.OBSIDIAN_TOUGHNESS);
                if (entity instanceof PlayerEntity player) player.sendMessage(TextUtil.createTranslatable("text.good-tea.obsidian_toughness"), true);
            }
        });

        addBehavior(Items.RABBIT_FOOT, (entity, stack) -> {
            var instance = Objects.requireNonNull(entity.getAttributeInstance(EntityAttributes.GENERIC_LUCK));
            if (!instance.hasModifier(GoodTea.RABBITS_LUCK)) {
                instance.addPersistentModifier(GoodTea.RABBITS_LUCK);
                if (entity instanceof PlayerEntity player) player.sendMessage(TextUtil.createTranslatable("text.good-tea.rabbits_luck"), true);
            }
        });

        addBehavior(Items.POTION, (entity, stack) -> {
            for (StatusEffectInstance sei : PotionUtil.getPotionEffects(stack)) {
                if (sei.getEffectType().isInstant()) {
                    sei.getEffectType().applyInstantEffect(entity, entity, entity, sei.getAmplifier(), 1.0);
                } else {
                    var effect = new StatusEffectInstance(sei.getEffectType(), (int) (sei.getDuration() * 1.2), sei.getAmplifier(), sei.isAmbient(), sei.shouldShowParticles(), sei.shouldShowIcon());
                    entity.addStatusEffect(new StatusEffectInstance(effect));
                }
            }
        });

        addBehavior(Items.SPLASH_POTION, (entity, stack) -> {
            PotionEntity potionEntity = new PotionEntity(entity.world, entity.getX(), entity.getY(), entity.getZ());
            potionEntity.setItem(stack);

            Potion potion = PotionUtil.getPotion(stack);
            List<StatusEffectInstance> list = PotionUtil.getPotionEffects(stack);
            List<StatusEffectInstance> list1 = Lists.newArrayList();
            for (StatusEffectInstance sei : list) {
                var effect = new StatusEffectInstance(sei.getEffectType(), (int) (sei.getDuration() * 1.2), sei.getAmplifier(), sei.isAmbient(), sei.shouldShowParticles(), sei.shouldShowIcon());
                list1.add(effect);
            }
            boolean bl = potion == Potions.WATER && list1.isEmpty();

            if (bl) this.damageEntitiesHurtByWater(potionEntity);
            else if (!list1.isEmpty()) ((PotionEntityAccessor)potionEntity).applySplashPotion(list1, entity);

            int i = potion.hasInstantEffect() ? WorldEvents.INSTANT_SPLASH_POTION_SPLASHED : WorldEvents.SPLASH_POTION_SPLASHED;
            entity.world.syncWorldEvent(i, entity.getBlockPos(), PotionUtil.getColor(stack));

            potionEntity.discard();
        });

        addBehavior(Items.LINGERING_POTION, (entity, stack) -> {
            PotionEntity potionEntity = new PotionEntity(entity.world, entity.getX(), entity.getY(), entity.getZ());
            potionEntity.setItem(stack);

            Potion potion = PotionUtil.getPotion(stack);
            List<StatusEffectInstance> list = PotionUtil.getPotionEffects(stack);
            List<StatusEffectInstance> list1 = Lists.newArrayList();
            for (StatusEffectInstance sei : list) {
                var effect = new StatusEffectInstance(sei.getEffectType(), (int) (sei.getDuration() * 1.2), sei.getAmplifier(), sei.isAmbient(), sei.shouldShowParticles(), sei.shouldShowIcon());
                list1.add(effect);
            }

            boolean bl = potion == Potions.WATER && list1.isEmpty();

            if (bl) this.damageEntitiesHurtByWater(potionEntity);
            else if (!list1.isEmpty()) ((PotionEntityAccessor)potionEntity).applyLingeringPotion(stack, potion);

            int i = potion.hasInstantEffect() ? WorldEvents.INSTANT_SPLASH_POTION_SPLASHED : WorldEvents.SPLASH_POTION_SPLASHED;
            entity.world.syncWorldEvent(i, entity.getBlockPos(), PotionUtil.getColor(stack));

            potionEntity.discard();
        });

        addBehavior(Items.SPONGE, (entity, stack) -> {
            ((SpongeBlockAccessor) Blocks.SPONGE).absorbWater(entity.world, entity.getBlockPos());
            ItemScatterer.spawn(entity.world, entity.getX(), entity.getY(), entity.getZ(), new ItemStack(Items.WET_SPONGE));
        });

        addBehavior(Items.WARPED_FUNGUS, (entity, stack) -> {
            ((HoglinRepellentAccess) entity).good_tea$makeHoglinRepellent(2400);
            if (entity instanceof PlayerEntity player) player.sendMessage(TextUtil.createTranslatable("text.good-tea.hoglin_repellent"), true);
        });

        addBehavior(Items.CRAFTING_TABLE, (entity, stack) -> {
            if (entity instanceof PlayerEntity player) {
                ((CraftingScreenAllowanceAccess) entity).good_tea$setAllowed(true);
                player.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, inv, entity1) -> new CraftingScreenHandler(syncId, inv, ScreenHandlerContext.create(entity.world, entity.getBlockPos())), TextUtil.createTranslatable("container.crafting")));
            }
        });

        addBehavior(Items.LAVA_BUCKET, (entity, stack) -> entity.setOnFireFor(1200));

        addBehavior(Items.TOTEM_OF_UNDYING, (entity, stack) -> {
            ((DivineAccess)entity).good_tea$setDivine(true);
            if (entity instanceof PlayerEntity player) player.sendMessage(TextUtil.createTranslatable("text.good-tea.divine"), true);
        });
    }

    public Behavior getBehavior(ItemStack stack) {
        return getBehavior(stack.getItem());
    }

    public Behavior getBehavior(Item item) {
        return TEA_CUP_BEHAVIOR.getOrDefault(item, (entity, stack) -> {
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
            if (item instanceof MusicDiscItem discItem) {
                addTooltip(item, TextUtil.applyFormatting((discItem.getDescription()), Formatting.GRAY));
            }
            if (item instanceof BlockItem blockItem) {
                if (blockItem.getBlock() instanceof BedBlock) {
                    addTooltip(item, TextUtil.applyFormatting(TextUtil.createTranslatable("tea-tooltip.good-tea.bed-tea"), Formatting.GRAY, Formatting.ITALIC));
                }
            }
        }
        addTooltip(GoodTea.TEA_CUP, TextUtil.applyFormatting(TextUtil.createTranslatable("tea-tooltip.good-tea.tea-cup-tea"), Formatting.GRAY, Formatting.ITALIC));
        addTooltip(GoodTea.KETTLE_BLOCK_ITEM, TextUtil.applyFormatting(TextUtil.createTranslatable("tea-tooltip.good-tea.tea-cup-tea"), Formatting.GRAY, Formatting.ITALIC));
        addTooltip(Items.AXOLOTL_BUCKET, TextUtil.applyFormatting(TextUtil.createTranslatable("tea-tooltip.good-tea.axolotl_tea"), Formatting.GRAY, Formatting.ITALIC));
        addTooltip(Items.WHEAT, TextUtil.applyFormatting(TextUtil.createTranslatable("tea-tooltip.good-tea.wheat_tea"), Formatting.GRAY, Formatting.ITALIC));
        addTooltip(Items.HAY_BLOCK, TextUtil.applyFormatting(TextUtil.createTranslatable("tea-tooltip.good-tea.wheat_tea"), Formatting.GRAY, Formatting.ITALIC));
    }

    public void damageEntitiesHurtByWater(PotionEntity entity) {
        Box box = entity.getBoundingBox().expand(4.0, 2.0, 4.0);
        List<LivingEntity> list = entity.world.getEntitiesByClass(LivingEntity.class, box, PotionEntity.WATER_HURTS);
        if (!list.isEmpty()) {
            for (LivingEntity livingEntity : list) {
                double d = entity.squaredDistanceTo(livingEntity);
                if (d < 16.0 && livingEntity.hurtByWater()) {
                    livingEntity.damage(DamageSource.magic(entity, entity.getOwner()), 1.0F);
                }
            }
        }

        for (AxolotlEntity axolotlEntity : entity.world.getNonSpectatingEntities(AxolotlEntity.class, box)) {
            axolotlEntity.hydrateFromPotion();
        }

    }

    @FunctionalInterface
    public interface Behavior {
        void run(LivingEntity entity, ItemStack stack);
    }
}