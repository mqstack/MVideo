package org.mqstack.mvideo.bind;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.Pair;
import android.view.View;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mq on 16/7/26.
 */

public class DataBind {
    private static Map<Class, List<Pair<Integer, Field>>> fieldMap = new HashMap<>();

    private static Map<Class, List<Pair<Integer, Action2<Object, View>>>> methodMap = new HashMap<>();

    public static void bind(Activity activity) {
        bind(activity, activity.getWindow().getDecorView());
    }

    public static void bind(final Object target, View view) {
        for (Pair<Integer, Field> pair : getFields(target.getClass(), view.getContext())) {
            Field field = pair.second;
            field.setAccessible(true);
            try {
                field.set(target, view.findViewById(pair.first));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        for (final Pair<Integer, Action2<Object, View>> pair : getMethods(target.getClass(), view.getContext())) {
            view.findViewById(pair.first).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    pair.second.call(target, view);
                }
            });
        }
    }

    private static List<Pair<Integer, Field>> getFields(Class cls, Context context) {
        List<Pair<Integer, Field>> idFields = fieldMap.get(cls);
        if (idFields == null) {
            idFields = new ArrayList<>();
            Resources res = context.getResources();
            String packageName = context.getPackageName();
            for (Field field : cls.getDeclaredFields()) {
                Bind bind = field.getAnnotation(Bind.class);
                if (bind != null) {
//                    int id = res.getIdentifier(bind.value(), "id", packageName);
                    idFields.add(new Pair<>(bind.value(), field));
                }
            }
            fieldMap.put(cls, idFields);
        }
        return idFields;
    }

    private static List<Pair<Integer, Action2<Object, View>>> getMethods(Class cls, Context context) {
        List<Pair<Integer, Action2<Object, View>>> idMethods = methodMap.get(cls);
        if (idMethods == null) {
            idMethods = new ArrayList<>();
            Resources res = context.getResources();
            String packageName = context.getPackageName();
            for (final Method method : cls.getDeclaredMethods()) {
                OnClick onclick = method.getAnnotation(OnClick.class);
                if (onclick != null) {
                    method.setAccessible(true);
//                    int id = res.getIdentifier(onclick.value(), "id", packageName);
                    if (method.getParameterTypes().length == 1) {
                        idMethods.add(new Pair<Integer, Action2<Object, View>>(onclick.value(), new Action2<Object, View>() {

                            @Override
                            public void call(Object obj, View view) {
                                try {
                                    method.invoke(obj, view);
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            }
                        }));
                    } else {
                        idMethods.add(new Pair<Integer, Action2<Object, View>>(onclick.value(), new Action2<Object, View>() {
                            @Override
                            public void call(Object obj, View view) {
                                try {
                                    method.invoke(obj);
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            }
                        }));
                    }
                }
            }
            methodMap.put(cls, idMethods);
        }
        return idMethods;
    }

    public interface Action2<T1, T2> {
        void call(T1 t1, T2 t2);
    }

}
