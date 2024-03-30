package me.melontini.goodtea.behaviors;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.melontini.dark_matter.api.data.codecs.ExtraCodecs;
import me.melontini.dark_matter.api.data.codecs.JsonCodecDataLoader;
import me.melontini.dark_matter.api.data.loading.ReloaderType;
import me.melontini.goodtea.util.GoodTeaStuff;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.block.Block;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

import java.util.*;

public class KettleBlockStates extends JsonCodecDataLoader<KettleBlockStates.Data> implements IdentifiableResourceReloadListener {

    public static final ReloaderType<KettleBlockStates> RELOADER_TYPE = ReloaderType.create(GoodTeaStuff.id("kettle_block_states"));

    public record Data(Block block, StatePredicate predicate) {
        public static final Codec<Data> CODEC = RecordCodecBuilder.create(data -> data.group(
                Registries.BLOCK.getCodec().fieldOf("block").forGetter(Data::block),
                ExtraCodecs.optional("predicate", Codecs.JSON_ELEMENT.xmap(StatePredicate::fromJson, StatePredicate::toJson), StatePredicate.ANY).forGetter(Data::predicate)
        ).apply(data, Data::new));
    }

    public final Map<Block, List<StatePredicate>> hotBlocksWithState = new HashMap<>();

    public KettleBlockStates() {
        super(RELOADER_TYPE.identifier(), Data.CODEC);
    }

    public Optional<List<StatePredicate>> getProperties(Block block) {
        return Optional.ofNullable(hotBlocksWithState.get(block));
    }

    @Override
    protected void apply(Map<Identifier, Data> parsed, ResourceManager manager) {
        parsed.values().forEach(data -> this.hotBlocksWithState.computeIfAbsent(data.block(), block -> new ArrayList<>()).add(data.predicate()));
    }
}
