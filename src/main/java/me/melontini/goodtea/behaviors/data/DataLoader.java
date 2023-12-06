package me.melontini.goodtea.behaviors.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.melontini.dark_matter.api.base.util.classes.Tuple;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import me.melontini.goodtea.behaviors.TeaBehavior;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.item.Item;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;

import java.util.*;

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
                .map(object -> {
                    DataPackBehaviors.Data data = new DataPackBehaviors.Data();

                    Set<Item> items = new HashSet<>();

                    if (!object.has("item_id")) throw new InvalidIdentifierException("(Good Tea) missing item_id!");

                    JsonElement element = object.get("item_id");
                    if (element.isJsonArray()) {
                        element.getAsJsonArray().forEach(e -> items.add(parseFromId(e.getAsString(), Registry.ITEM)));
                    } else {
                        items.add(parseFromId(element.getAsString(), Registry.ITEM));
                    }

                    data.disabled = JsonHelper.getBoolean(object, "disabled", false);
                    data.complement = JsonHelper.getBoolean(object, "complement", true);

                    data.commands = new DataPackBehaviors.Data.CommandHolder(
                            readCommands(object, "user_commands"),
                            readCommands(object, "server_commands"));

                    return Tuple.of(items, data);
                }).forEach(tuple -> {
                    for (Item item : tuple.left()) {
                        DataPackBehaviors.Data data = tuple.right();
                        if (data.disabled) {
                            DataPackBehaviors.INSTANCE.disable(item);
                            return;
                        }

                        DataPackBehaviors.INSTANCE.addBehavior(item, (entity, stack) -> {
                            ServerWorld serverWorld = (ServerWorld) entity.world;

                            if (data.commands.user_commands() != null) {
                                ServerCommandSource source = new ServerCommandSource(
                                        serverWorld.getServer(), entity.getPos(), new Vec2f(entity.getPitch(), entity.getYaw()), serverWorld, 4, entity.getEntityName(), TextUtil.literal(entity.getEntityName()), serverWorld.getServer(), entity).withSilent();
                                for (String command : data.commands.user_commands()) {
                                    serverWorld.getServer().getCommandManager().executeWithPrefix(source, command);
                                }
                            }

                            if (data.commands.server_commands() != null) {
                                for (String command : data.commands.server_commands()) {
                                    serverWorld.getServer().getCommandManager().executeWithPrefix(serverWorld.getServer().getCommandSource().withSilent(), command);
                                }
                            }
                        }, tuple.right().complement);
                    }
                });
    }

    public static <T> T parseFromId(String id, Registry<T> registry) {
        Identifier identifier = Identifier.tryParse(id);
        if (!registry.containsId(identifier))
            throw new InvalidIdentifierException(String.format("(Good Tea) invalid identifier provided! id: %s, registry: %s", identifier, registry));
        return registry.get(identifier);
    }

    private static List<String> readCommands(JsonObject json, String source) {
        var item_arr = JsonHelper.getArray(json, source, null);
        if (item_arr != null) {
            List<String> commands = new ArrayList<>();
            for (JsonElement element : item_arr) {
                commands.add(element.getAsString());
            }
            return commands;
        }
        return null;
    }
}
