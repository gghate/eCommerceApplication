package utils;

import java.lang.reflect.Field;

public class TestingUtils {

    public static void setTarget(Object source ,String fieldName,Object destination)
    {
       boolean wasPrivate=false;

        try {
            Field f=source.getClass().getDeclaredField(fieldName);
            if(!f.isAccessible())
            {
             f.setAccessible(true);
             wasPrivate=true;
            }
            f.set(source,destination);
            if(wasPrivate)
            {
                f.setAccessible(false);
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
