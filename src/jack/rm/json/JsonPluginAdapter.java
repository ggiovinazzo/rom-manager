package jack.rm.json;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;


public class JsonPluginAdapter<T extends JsonnableContext> implements JsonSerializer<T>, JsonDeserializer<T>
{

  @Override
  public JsonElement serialize(T src, Type type, JsonSerializationContext context)
  {
    try
    {
      return src.serialize(context);
    }
    catch (IllegalAccessException e)
    {
      e.printStackTrace();
    }
    
    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public T deserialize(JsonElement json, Type type, JsonDeserializationContext context)
  {
    String name = json.getAsJsonObject().get("class").getAsString();
    
    try
    {
      Class<?> clazz = Class.forName(name);
      T instance = (T)clazz.newInstance();
      instance.unserialize(json, context);
      return instance;
    }
    catch (ClassNotFoundException e)
    {
      e.printStackTrace();
    }
    catch (IllegalAccessException|InstantiationException e)
    {
      e.printStackTrace();
    }
    
    return null;
  }
}
