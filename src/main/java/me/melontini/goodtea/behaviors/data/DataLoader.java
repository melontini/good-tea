package me.melontini.goodtea.behaviors.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import me.melontini.goodtea.behaviors.TeaBehavior;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.item.Item;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.profiler.Profiler;

import java.util.Map;

import static me.melontini.goodtea.GoodTea.MODID;

public class DataLoader extends JsonDataLoader implements IdentifiableResourceReloadListener {

    public DataLoader() {
        super(new Gson(), "good_tea/behaviors");
    }

    @Override
    public Identifier getFabricId() {
        return new Identifier(MODID, "behaviors");
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        DataPackBehaviors.INSTANCE.clear();

        TeaBehavior.INSTANCE.getBehaviors().forEach((key, value) -> DataPackBehaviors.INSTANCE.addBehavior(key, value, true));

        prepared.values().stream().filter(JsonElement::isJsonObject).map(JsonElement::getAsJsonObject)
                .map(object -> DataPackBehaviors.Data.CODEC.parse(JsonOps.INSTANCE, object).getOrThrow(false, string -> {
                    throw new JsonParseException(string);
                })).forEach(data -> {
                    for (Item item : data.items()) {
                        if (data.disabled()) {
                            DataPackBehaviors.INSTANCE.disable(item);
                            continue;
                        }

                        DataPackBehaviors.INSTANCE.addBehavior(item, (entity, stack) -> {
                            ServerWorld serverWorld = (ServerWorld) entity.getWorld();

                            if (!data.user_commands().isEmpty()) {
                                ServerCommandSource source = new ServerCommandSource(
                                        serverWorld.getServer(), entity.getPos(), new Vec2f(entity.getPitch(), entity.getYaw()), serverWorld, 4, entity.getEntityName(), TextUtil.literal(entity.getEntityName()), serverWorld.getServer(), entity).withSilent();
                                for (String command : data.user_commands()) {
                                    serverWorld.getServer().getCommandManager().executeWithPrefix(source, command);
                                }
                            }

                            if (!data.server_commands().isEmpty()) {
                                for (String command : data.server_commands()) {
                                    serverWorld.getServer().getCommandManager().executeWithPrefix(serverWorld.getServer().getCommandSource().withSilent(), command);
                                }
                            }
                        }, data.complement());
                    }
                });
    }
}
