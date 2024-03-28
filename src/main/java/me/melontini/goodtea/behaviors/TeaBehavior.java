package me.melontini.goodtea.behaviors;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import me.melontini.dark_matter.api.base.util.MakeSure;
import me.melontini.dark_matter.api.base.util.MathUtil;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import me.melontini.goodtea.GoodTea;
import me.melontini.goodtea.ducks.ChorusAccess;
import me.melontini.goodtea.ducks.CraftingScreenAllowanceAccess;
import me.melontini.goodtea.ducks.DivineAccess;
import me.melontini.goodtea.ducks.HoglinRepellentAccess;
import net.minecraft.block.*;
import net.minecraft.entity.Bucketable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;
import org.apache.commons.compress.utils.Lists;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static me.melontini.goodtea.util.GoodTeaStuff.OBSIDIAN_TOUGHNESS;
import static me.melontini.goodtea.util.GoodTeaStuff.RABBITS_LUCK;

@SuppressWarnings("unused")
public class TeaBehavior {
    public static TeaBehavior INSTANCE = new TeaBehavior();
    private final Map<Item, Behavior> behaviors = new Object2ObjectLinkedOpenHashMap<>();

    private TeaBehavior() {
    }

    public Map<Item, Behavior> getBehaviors() {
        return Collections.unmodifiableMap(behaviors);
    }

    public void init() {
        addBehavior((entity, stack) -> entity.world.createExplosion(null, entity.getX(), entity.getY(), entity.getZ(), 4.0F, World.ExplosionSourceType.MOB), Items.TNT, Items.TNT_MINECART);
        addBehavior(Items.GUNPOWDER, (entity, stack) -> entity.world.createExplosion(null, entity.getX(), entity.getY(), entity.getZ(), 1.0F, World.ExplosionSourceType.MOB));

        addBehavior(Items.END_ROD, (entity, stack) -> {
            ((ServerWorld) entity.world).spawnParticles(ParticleTypes.END_ROD, entity.getX(), entity.getY() + 1.6, entity.getZ(), 35, MathUtil.nextDouble(-0.2, 0.2), MathUtil.nextDouble(-0.2, 0.2), MathUtil.nextDouble(-0.2, 0.2), 0.3);
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 600, 0));
        });

        addBehavior(Items.SPORE_BLOSSOM, (entity, stack) -> {
            BlockPos.Mutable mutable = new BlockPos.Mutable();

            int i = entity.getBlockPos().getX();
            int j = entity.getBlockPos().getY();
            int k = entity.getBlockPos().getZ();
            for (int l = 0; l < 3; ++l) {
                mutable.set(i + MathUtil.nextInt(-4, 4), j + 4, k + MathUtil.nextInt(-4, 4));
                BlockState blockState = entity.world.getBlockState(mutable);
                if (!blockState.isFullCube(entity.world, mutable)) {
                    ((ServerWorld) entity.world).spawnParticles(ParticleTypes.SPORE_BLOSSOM_AIR, mutable.getX() + MathUtil.threadRandom().nextDouble(), mutable.getY() + MathUtil.threadRandom().nextDouble(), mutable.getZ() + MathUtil.threadRandom().nextDouble(), 7, 0.0, 0.0, 0.0, 0.0);
                }
            }

            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 300, 0));
        });

        addBehavior(Items.AXOLOTL_BUCKET, (entity, stack) -> {
                    entity.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 100, 0));
                    var bucketEntity = ((EntityBucketItem) Items.AXOLOTL_BUCKET).entityType.spawnFromItemStack((ServerWorld) entity.world, stack, null, new BlockPos((int) entity.getX(), (int) entity.getEyePos().y, (int) entity.getZ()), SpawnReason.BUCKET, true, false);
                    if (bucketEntity instanceof Bucketable bucketable) {
                        bucketable.copyDataFromNbt(stack.getOrCreateNbt());
                        bucketable.setFromBucket(true);
                    }
                    if (bucketEntity == null) return;
                    bucketEntity.setVelocity(entity.getRotationVector());
                    entity.world.playSound(null, entity.getBlockPos(), SoundEvents.ITEM_BUCKET_EMPTY_AXOLOTL, SoundCategory.AMBIENT, 1.0f, 1.0f);
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
            if (!instance.hasModifier(OBSIDIAN_TOUGHNESS)) {
                instance.addPersistentModifier(OBSIDIAN_TOUGHNESS);
                if (entity instanceof PlayerEntity player)
                    player.sendMessage(TextUtil.translatable("text.good-tea.obsidian_toughness"), true);
            }
        });

        addBehavior(Items.RABBIT_FOOT, (entity, stack) -> {
            var instance = Objects.requireNonNull(entity.getAttributeInstance(EntityAttributes.GENERIC_LUCK));
            if (!instance.hasModifier(RABBITS_LUCK)) {
                instance.addPersistentModifier(RABBITS_LUCK);
                if (entity instanceof PlayerEntity player)
                    player.sendMessage(TextUtil.translatable("text.good-tea.rabbits_luck"), true);
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
            else if (!list1.isEmpty()) potionEntity.applySplashPotion(list1, entity);

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
            else if (!list1.isEmpty()) potionEntity.applyLingeringPotion(stack, potion);

            int i = potion.hasInstantEffect() ? WorldEvents.INSTANT_SPLASH_POTION_SPLASHED : WorldEvents.SPLASH_POTION_SPLASHED;
            entity.world.syncWorldEvent(i, entity.getBlockPos(), PotionUtil.getColor(stack));

            potionEntity.discard();
        });

        addBehavior(Items.SPONGE, (entity, stack) -> {
            if (((SpongeBlock) Blocks.SPONGE).absorbWater(entity.world, entity.getBlockPos())) {
                ItemScatterer.spawn(entity.world, entity.getX(), entity.getY(), entity.getZ(), new ItemStack(Items.WET_SPONGE));
            } else {
                ItemScatterer.spawn(entity.world, entity.getX(), entity.getY(), entity.getZ(), new ItemStack(Items.SPONGE));
            }
        });

        addBehavior(Items.WARPED_FUNGUS, (entity, stack) -> {
            ((HoglinRepellentAccess) entity).good_tea$makeHoglinRepellent(2400);
            if (entity instanceof PlayerEntity player)
                player.sendMessage(TextUtil.translatable("text.good-tea.hoglin_repellent"), true);
        });

        addBehavior(Items.CRAFTING_TABLE, (entity, stack) -> {
            if (entity instanceof PlayerEntity player) {
                ((CraftingScreenAllowanceAccess) entity).good_tea$setAllowed(true);
                player.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, inv, entity1) -> new CraftingScreenHandler(syncId, inv, ScreenHandlerContext.create(entity.world, entity.getBlockPos())), TextUtil.translatable("container.crafting")));
            }
        });

        addBehavior(Items.LAVA_BUCKET, (entity, stack) -> entity.setOnFireFor(1200));

        addBehavior(Items.TOTEM_OF_UNDYING, (entity, stack) -> {
            ((DivineAccess) entity).good_tea$setDivine(true);
            if (entity instanceof PlayerEntity player)
                player.sendMessage(TextUtil.translatable("text.good-tea.divine"), true);
            entity.world.playSound(null, entity.getBlockPos(), SoundEvents.ITEM_TOTEM_USE, SoundCategory.AMBIENT, 1.0f, 1.0f);
        });

        addBehavior((entity, stack) -> entity.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 1200, 0)), Items.OCHRE_FROGLIGHT, Items.PEARLESCENT_FROGLIGHT, Items.VERDANT_FROGLIGHT);

        addBehavior(Items.GOAT_HORN, (entity, stack) -> ((GoatHornItem) Items.GOAT_HORN).getInstrument(stack).ifPresent(registryEntry -> {
            Instrument instrument = (Instrument) ((RegistryEntry<?>) registryEntry).value();
            SoundEvent soundEvent = instrument.soundEvent().value();
            float f = instrument.range() / 16.0F;
            entity.world.playSoundFromEntity(null, entity, soundEvent, SoundCategory.RECORDS, f, 1.0F);
            entity.world.emitGameEvent(GameEvent.INSTRUMENT_PLAY, entity.getPos(), GameEvent.Emitter.of(entity));
        }));

        addBehavior(Items.FIREWORK_ROCKET, (entity, stack) -> {
            FireworkRocketEntity fireworkRocketEntity = new FireworkRocketEntity(entity.world, stack, entity, entity.getX(), entity.getEyeY() - 0.15F, entity.getZ(), true);

            Vec3d vec3d = entity.getOppositeRotationVector(1.0F);
            Quaternionf quaternion = new Quaternionf().setAngleAxis(0, vec3d.x, vec3d.y, vec3d.z);
            Vec3d vec3d2 = entity.getRotationVec(1.0F);
            Vector3f vec3f = vec3d2.toVector3f().rotate(quaternion);
            fireworkRocketEntity.setVelocity(vec3f.x(), vec3f.y(), vec3f.z(), 1.6f, 1);

            entity.world.spawnEntity(fireworkRocketEntity);
            if (entity instanceof PlayerEntity player) {
                player.incrementStat(Stats.USED.getOrCreateStat(Items.FIREWORK_ROCKET));
            }
        });

        addBehavior(Items.CHORUS_FRUIT, (entity, stack) -> {
            if (entity instanceof PlayerEntity player) {
                if (((ChorusAccess) entity).good_tea$isTeleporting()) {
                    player.sendMessage(TextUtil.translatable("text.good-tea.chorus-tea-renew"), true);
                } else player.sendMessage(TextUtil.translatable("text.good-tea.chorus-tea"), true);
            }
            ((ChorusAccess) entity).good_tea$addTeleportingTime(3600);
        });
    }

    public void initAuto(Item item) {
        if (item instanceof SpawnEggItem spawnEggItem) {
            addBehavior(item, (entity, stack) -> {
                Entity spawnEggEntity = spawnEggItem.getEntityType(new NbtCompound()).create(entity.world);
                if (spawnEggEntity != null) {
                    spawnEggEntity.setPos(entity.getX(), entity.getY(), entity.getZ());
                    spawnEggEntity.setVelocity(entity.getRotationVector());
                    entity.world.spawnEntity(spawnEggEntity);
                }
            });
        }
        if (item instanceof EntityBucketItem entityBucketItem) {
            if (item != Items.AXOLOTL_BUCKET) addBehavior(item, (entity, stack) -> {
                var bucketEntity = entityBucketItem.entityType.spawnFromItemStack((ServerWorld) entity.world, stack, null, new BlockPos((int) entity.getX(), (int) entity.getEyePos().y, (int) entity.getZ()), SpawnReason.BUCKET, true, false);
                if (bucketEntity instanceof Bucketable bucketable) {
                    bucketable.copyDataFromNbt(stack.getOrCreateNbt());
                    bucketable.setFromBucket(true);
                }
                if (bucketEntity == null) return;
                bucketEntity.setVelocity(entity.getRotationVector());
                entity.world.playSound(null, entity.getBlockPos(), SoundEvents.ITEM_BUCKET_EMPTY_FISH, SoundCategory.AMBIENT, 1.0f, 1.0f);
            });
        }
        if (item instanceof SwordItem swordItem) {
            addBehavior(item, (entity, stack) -> {
                entity.damage(entity.getWorld().getDamageSources().generic(), swordItem.getAttackDamage() * 2);
                entity.world.playSound(null, entity.getBlockPos(), SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.AMBIENT, 1.0f, 1.0f);
                stack.damage((int) Math.ceil(swordItem.getAttackDamage() * 3.0F), entity.world.random, entity instanceof ServerPlayerEntity player ? player : null);
                ItemScatterer.spawn(entity.world, entity.getX(), entity.getY(), entity.getZ(), stack);
            });
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
                        entity.world.createExplosion(null, entity.world.getDamageSources().badRespawnPoint(entity.getPos()), null, entity.getX() + 0.5, entity.getY() + 0.5, entity.getZ() + 0.5, 5.0F, true, World.ExplosionSourceType.MOB);
                });
            }
        }
    }

    public void addBehavior(Item item, Behavior behavior) {
        MakeSure.notNulls(item, behavior);
        if (!behaviors.containsKey(item)) behaviors.putIfAbsent(item, behavior);
        else GoodTea.LOGGER.error("Tried to add behaviour for the same item twice! {}", item);
    }

    public void addBehavior(Behavior behavior, Item... items) {
        for (Item item : items) addBehavior(item, behavior);
    }

    public void damageEntitiesHurtByWater(PotionEntity entity) {
        Box box = entity.getBoundingBox().expand(4.0, 2.0, 4.0);
        List<LivingEntity> list = entity.world.getEntitiesByClass(LivingEntity.class, box, PotionEntity.AFFECTED_BY_WATER);
        if (!list.isEmpty()) list.stream()
                .filter(livingEntity -> entity.squaredDistanceTo(livingEntity) < 16.0 && livingEntity.hurtByWater())
                .forEach(livingEntity -> livingEntity.damage(entity.world.getDamageSources().indirectMagic(entity, entity.getOwner()), 1.0F));

        entity.world.getNonSpectatingEntities(AxolotlEntity.class, box)
                .forEach(AxolotlEntity::hydrateFromPotion);
    }


    @FunctionalInterface
    public interface Behavior {
        void run(LivingEntity entity, ItemStack stack);
    }
}