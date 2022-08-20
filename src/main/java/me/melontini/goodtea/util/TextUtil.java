package me.melontini.goodtea.util;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.minecraft.util.Formatting;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressWarnings("unchecked")
public class TextUtil {//At the end of the day, just changing 2 lines of Text would've been easier...
    private static final boolean DEV_ENV = FabricLoader.getInstance().isDevelopmentEnvironment();
    public static <T> T createTranslatable(String namespace, Object... args) {
        try {
            return (T) findMethod("class_2561", "method_43469", "(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/class_5250;", String.class, Object[].class).invoke(null, namespace, args);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            try {
                return (T) findClass("class_2588").getConstructor(String.class, Object[].class).newInstance(namespace, args);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public static <T> T createTranslatable(String namespace) {
        try {
            return (T) findMethod("class_2561", "method_43471", "(Ljava/lang/String;)Lnet/minecraft/class_5250;", String.class).invoke(null, namespace);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            try {
                return (T) findClass("class_2588").getConstructor(String.class).newInstance(namespace);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public static  <T> T applyFormatting(T text, Formatting... formattings) {
        Method method;
        try {
            method = findMethod("class_5250", DEV_ENV ? "method_27695" : "method_27692", "([Lnet/minecraft/class_124;)Lnet/minecraft/class_5250;", findClass("class_124"));
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        for (Formatting formatting1 : formattings) {
            try {
                text = (T) method.invoke(text, formatting1);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        return text;
    }

    private static Class<?> findClass(String className) throws ClassNotFoundException {
        MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();
        return Class.forName(resolver.mapClassName("intermediary", "net.minecraft." + className));
    }

    private static Class<?> findArrayClass(String className) throws ClassNotFoundException {
        MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();
        return Class.forName(resolver.mapClassName("intermediary", "[Lnet.minecraft." + className + ";"));
    }

    private static Method findMethod(String className, String methodName, String descriptors, Class<?>... classes) throws NoSuchMethodException, ClassNotFoundException {
        MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();
        return findClass(className).getMethod(resolver.mapMethodName("intermediary", "net.minecraft." + className, methodName, descriptors), classes);
    }
}