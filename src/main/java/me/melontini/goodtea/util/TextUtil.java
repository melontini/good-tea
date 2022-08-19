package me.melontini.goodtea.util;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.minecraft.MinecraftVersion;
import net.minecraft.util.Formatting;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressWarnings("unchecked")
public class TextUtil {//At the end of the day, just changing 2 lines of Text would've been easier...
    private static final String CURRENT_VERSION = MinecraftVersion.CURRENT.getName();
    public static <T> T createTranslatable(String namespace, Object... args) {
        switch (CURRENT_VERSION) {
            case "1.19", "1.19.1", "1.19.2" -> {
                try {
                    return (T) findMethod("class_2561", "method_43469", "(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/class_5250;", String.class, Object[].class).invoke(null, namespace, args);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
            default -> {
                try {
                    return (T) findClass("class_2588").getConstructor(String.class, Object[].class).newInstance(namespace, args);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    public static <T> T createTranslatable(String namespace) {
        switch (CURRENT_VERSION) {
            case "1.19", "1.19.1", "1.19.2" -> {
                try {
                    return (T) findMethod("class_2561", "method_43471", "(Ljava/lang/String;)Lnet/minecraft/class_5250;", String.class).invoke(null, namespace);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
            default -> {
                try {
                    return (T) findClass("class_2588").getConstructor(String.class).newInstance(namespace);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static  <T> T applyFormatting(T text, Formatting... formattings) {
        var method = findMethod("class_5250", "method_27695", "([Lnet/minecraft/class_124;)Lnet/minecraft/class_5250;", Formatting.class);
        for (Formatting formatting1 : formattings) {
            try {
                text = (T) method.invoke(text, formatting1);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        return text;
    }

    private static Class<?> findClass(String className) {
        MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();
        try {
            return Class.forName(resolver.mapClassName("intermediary", "net.minecraft." + className));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }



    private static Method findMethod(String className, String methodName, String descriptors, Class<?>... classes) {
        MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();
        try {
            return findClass(className).getMethod(resolver.mapMethodName("intermediary", "net.minecraft." + className, methodName, descriptors), classes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}