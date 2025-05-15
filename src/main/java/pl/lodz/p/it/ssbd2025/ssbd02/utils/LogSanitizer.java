package pl.lodz.p.it.ssbd2025.ssbd02.utils;

import java.lang.reflect.*;
import java.util.*;
import java.util.regex.Pattern;

public class LogSanitizer {

    // add field names to protect them, they must be matching 1:1
    private static final Set<String> SENSITIVE_FIELD_NAMES = Set.of("password", "token", "secret", "accessToken", "refreshToken", "passwordPlaintext", "passwordHash", "oldPassword", "newPassword"); //eventually place this into secrets.properties

    // add field names to exclude them (skip) from causing database access errors
    private static final Set<String> EXCLUDED_FIELD_NAMES = Set.of("periodicSurveys", "bloodTestReports", "foodPyramidIds", "feedbacks");

    // add types of fields you want to force-tostring on, like Date instead of Date=[...] -> Date=Mon May 12 17:40:41 CEST 2025
    private static final Set<Class<?>> FORCE_TO_STRING_TYPES = Set.of(String.class, Number.class, Boolean.class, Date.class, Enum.class, UUID.class, Pattern.class, PropertyResourceBundle.class, Long.class);

    public static String sanitize(Object obj, String paramName) {
        return sanitize(obj, new IdentityHashMap<>(), paramName);
    }

    private static String sanitize(Object obj, Map<Object, Boolean> visited, String paramName) {
        if (obj == null) return "null";



        if(obj instanceof String str && SENSITIVE_FIELD_NAMES.contains(paramName)) {
            return "[PROTECTED]";
        }

        if (obj.getClass().isPrimitive() || FORCE_TO_STRING_TYPES.contains(obj.getClass())) {
            return obj.toString();
        }


        if (visited.containsKey(obj)) {
            return "[CIRC.REF./VISITED]";
        }

        visited.put(obj, true);

        Class<?> clazz = obj.getClass();

        if (clazz.isArray()) {
            Object[] array = (Object[]) obj;
            return Arrays.stream(array)
                    .map(element -> sanitize(element, visited, paramName))
                    .toList().toString();
        }

        if (obj instanceof Collection<?> collection) {
            List<String> elements = new ArrayList<>();
            for (Object item : collection) {
                elements.add(sanitize(item, visited, paramName));
            }
            return elements.toString();
        }

        if (obj instanceof Map<?, ?> map) {
            Map<String, String> sanitizedMap = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                sanitizedMap.put(String.valueOf(entry.getKey()), sanitize(entry.getValue(), visited, paramName));
            }
            return sanitizedMap.toString();
        }

        if (isFrameworkClass(obj.getClass())) {
            return ""; //TODO IMPORTANT <- this skips completely classes that are considered 'framework' like anything java. that is NOT included in force-tostring. Edit as you wish.
        }

        StringBuilder builder = new StringBuilder();
        builder.append(clazz.getSimpleName()).append("[");

        try {
            Field[] fields = getAllFields(clazz).toArray(new Field[0]);
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                field.setAccessible(true);

                builder.append(field.getName()).append("=");

                try {
                    Object value = field.get(obj);
                    if(EXCLUDED_FIELD_NAMES.contains(field.getName())) {
                        builder.append(field.getType().getSimpleName()).append("[SKIPPED]");
                    } else {
                        String pom = field.getName().toLowerCase();
                        if (SENSITIVE_FIELD_NAMES.contains(field.getName())) {
                            builder.append("[PROTECTED]");
                        } else {
                            builder.append(sanitize(value, visited, paramName));
                        }
                    }
                } catch (Throwable fieldEx) {
                    builder.append("[ERROR]");
                }

                if (i < fields.length - 1) builder.append(", ");
            }
        } catch (Throwable ex) {
            return obj.toString();
        }

        builder.append("]");
        return builder.toString();
    }

    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    private static boolean isFrameworkClass(Class<?> clazz) {
        String name = clazz.getName();
        return name.startsWith("jakarta.servlet.")
                || name.startsWith("org.apache.catalina.")
                || name.startsWith("org.apache.tomcat.")
                || name.startsWith("org.springframework.")
                || name.startsWith("java.")
                || name.startsWith("sun.")
                || name.startsWith("jdk.");
    }

}
