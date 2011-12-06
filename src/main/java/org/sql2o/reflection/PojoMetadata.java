package org.sql2o.reflection;

import org.sql2o.Sql2oException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: lars
 * Date: 11/15/11
 * Time: 8:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class PojoMetadata {
    
    private Map<String, Setter> propertySetters;
    private boolean caseSensitive;
    private Class clazz;
    
    private Map<String,String> columnMappings;
    
    public PojoMetadata(Class clazz, boolean caseSensitive, Map<String,String> columnMappings){
        
        this.caseSensitive = caseSensitive;
        this.clazz = clazz;
        this.columnMappings = columnMappings == null ? new HashMap<String, String>() : columnMappings;

        propertySetters = new HashMap<String, Setter>();

        // prepare fields
        Class theClass = clazz;
        do{
            for (Field f : theClass.getDeclaredFields()){
                String propertyName = f.getName();
                propertyName = caseSensitive ? propertyName : propertyName.toLowerCase();
                propertySetters.put(propertyName, new FieldSetter(f));
            }
            
            // prepare methods. Methods will override fields, if both exists.
            for (Method m : theClass.getDeclaredMethods()){
                if (m.getName().startsWith("set")){
                    String propertyName = m.getName().substring(3);
                    if (caseSensitive){
                        propertyName = propertyName.substring(0,1).toLowerCase() + propertyName.substring(1);
                    }
                    else{
                        propertyName = propertyName.toLowerCase();
                    }
                    
                    propertySetters.put(propertyName, new MethodSetter(m));
                }
            }
            theClass = theClass.getSuperclass();
        }while(!theClass.equals(Object.class));
    }
    
    public Setter getPropertySetter(String propertyName){
        
        String name = this.caseSensitive ? propertyName : propertyName.toLowerCase();

        if (this.columnMappings.containsKey(name)){
            name = this.columnMappings.get(name);
        }
        
        if (propertySetters.containsKey(name)){
            return propertySetters.get(name);
        }
        else{
            String errorMsg = "Property with name '" + propertyName + "' not found on class " + this.clazz.toString();
            if (this.caseSensitive){
                errorMsg += " (You have turned on case sensitive property search. Is this intentional?)";
            }
            throw new Sql2oException(errorMsg);
        }
    }
    
    public Class getType(){
        return this.clazz;
    }
    
    // Caching
//    public static Map<Class, PojoMetadata> cachedMetadata = new HashMap<Class, PojoMetadata>();
//
//    public static PojoMetadata getForType(Class clazz, boolean caseSensitive){
//        if (!cachedMetadata.containsKey(clazz)){
//            cachedMetadata.put(clazz, new PojoMetadata(clazz, caseSensitive));
//        }
//
//        return cachedMetadata.get(clazz);
//    }
}

