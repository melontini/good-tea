package me.melontini.goodtea.behaviors;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.melontini.dark_matter.api.data.codecs.ExtraCodecs;
import me.melontini.dark_matter.api.data.loading.ReloaderType;
import me.melontini.goodtea.util.GoodTeaStuff;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.block.Block;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.registry.Registries;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.profiler.Profiler;

import java.util.*;

public class KettleBlockStates extends JsonDataLoader implements IdentifiableResourceReloadListener {

    public static final ReloaderType<KettleBlockStates> RELOADER_TYPE = ReloaderType.create(GoodTeaStuff.id("kettle_block_states"));

    record Data(Block block, StatePredicate predicate) {
        public static final Codec<Data> CODEC = RecordCodecBuilder.create(data -> data.group(
                Registries.BLOCK.getCodec().fieldOf("block").forGetter(Data::block),
                ExtraCodecs.optional("predicate", Codecs.JSON_ELEMENT.xmap(StatePredicate::fromJson, StatePredicate::toJson), StatePredicate.ANY).forGetter(Data::predicate)
        ).apply(data, Data::new));
    }

    public final Map<Block, List<StatePredicate>> hotBlocksWithState = new HashMap<>();

    public KettleBlockStates() {
        super(new Gson(), RELOADER_TYPE.identifier().toString().replace(":", "/"));
    }

    public Optional<List<StatePredicate>> getProperties(Block block) {
        return Optional.ofNullable(hotBlocksWithState.getOrDefault(block, null));
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        Maps.transformValues(prepared, input -> Data.CODEC.parse(JsonOps.INSTANCE, input).getOrThrow(false, string -> {
            throw new JsonParseException(string);
        })).values().forEach(data -> this.hotBlocksWithState.computeIfAbsent(data.block(), block -> new ArrayList<>()).add(data.predicate()));
    }

    @Override
    public Identifier getFabricId() {
        return RELOADER_TYPE.identifier();
    }
}
