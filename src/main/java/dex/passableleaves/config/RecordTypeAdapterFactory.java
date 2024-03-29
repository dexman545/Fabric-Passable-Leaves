package dex.passableleaves.config;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class RecordTypeAdapterFactory implements TypeAdapterFactory {
    private static final Map<Class<?>, Object> PRIMITIVE_DEFAULTS = new HashMap<>();
    static {
        PRIMITIVE_DEFAULTS.put(byte.class, (byte)0);
        PRIMITIVE_DEFAULTS.put(int.class, 0);
        PRIMITIVE_DEFAULTS.put(long.class, 0L);
        PRIMITIVE_DEFAULTS.put(short.class, (short)0);
        PRIMITIVE_DEFAULTS.put(double.class, 0D);
        PRIMITIVE_DEFAULTS.put(float.class, 0F);
        PRIMITIVE_DEFAULTS.put(char.class, '\0');
        PRIMITIVE_DEFAULTS.put(boolean.class, false);
    }

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        @SuppressWarnings("unchecked")
        Class<T> clazz = (Class<T>) type.getRawType();
        if (!clazz.isRecord()) {
            return null;
        }
        TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);

        return new TypeAdapter<T>() {
            @Override
            public void write(JsonWriter out, T value) throws IOException {
                delegate.write(out, value);
            }

            @Override
            public T read(JsonReader reader) throws IOException {
                if (reader.peek() == JsonToken.NULL) {
                    reader.nextNull();
                    return null;
                } else {
                    var recordComponents = clazz.getRecordComponents();
                    var typeMap = new HashMap<String,TypeToken<?>>();
                    for (int i = 0; i < recordComponents.length; i++) {
                        typeMap.put(recordComponents[i].getName(), TypeToken.get(recordComponents[i].getGenericType()));
                    }
                    var argsMap = new HashMap<String,Object>();
                    reader.beginObject();
                    while (reader.hasNext()) {
                        String name = reader.nextName();
                        argsMap.put(name, gson.getAdapter(typeMap.get(name)).read(reader));
                    }
                    reader.endObject();

                    var argTypes = new Class<?>[recordComponents.length];
                    var args = new Object[recordComponents.length];
                    for (int i = 0; i < recordComponents.length; i++) {
                        argTypes[i] = recordComponents[i].getType();
                        String name = recordComponents[i].getName();
                        Object value = argsMap.get(name);
                        TypeToken<?> type = typeMap.get(name);
                        if (value == null && (type != null && type.getRawType().isPrimitive())) {
                            value = PRIMITIVE_DEFAULTS.get(type.getRawType());
                        }
                        args[i] = value;
                    }
                    Constructor<T> constructor;
                    try {
                        constructor = clazz.getDeclaredConstructor(argTypes);
                        constructor.setAccessible(true);
                        return constructor.newInstance(args);
                    } catch (NoSuchMethodException | InstantiationException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        };
    }
}
