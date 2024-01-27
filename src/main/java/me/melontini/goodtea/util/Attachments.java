package me.melontini.goodtea.util;

import com.mojang.serialization.Codec;
import me.melontini.goodtea.GoodTea;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.util.Identifier;

@SuppressWarnings("UnstableApiUsage")
public class Attachments {

    public static final AttachmentType<Boolean> IS_DIVINE = AttachmentRegistry.createPersistent(new Identifier(GoodTea.MODID, "is_divine"), Codec.BOOL);

    public static final AttachmentType<Integer> HOGLIN_REPELLENT = AttachmentRegistry.createPersistent(new Identifier(GoodTea.MODID, "hoglin_repellent"), Codec.INT);

    public static final AttachmentType<Boolean> CAN_USE_CRAFTING_TABLE = AttachmentRegistry.createDefaulted(new Identifier(GoodTea.MODID, "can_use_crafting_table"), () -> false);

    public static final AttachmentType<Integer> CHORUS_TELEPORT_TIME = AttachmentRegistry.createPersistent(new Identifier(GoodTea.MODID, "chorus_time"), Codec.INT);
    public static final AttachmentType<Integer> CHORUS_LAST_TELEPORT_TIME = AttachmentRegistry.createPersistent(new Identifier(GoodTea.MODID, "chorus_last_time"), Codec.INT);
}
