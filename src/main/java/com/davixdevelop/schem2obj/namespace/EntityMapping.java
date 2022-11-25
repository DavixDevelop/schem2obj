package com.davixdevelop.schem2obj.namespace;

import com.davixdevelop.schem2obj.namespace.json.JsonEntity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;

public class EntityMapping {
    HashMap<String, Namespace> entityMapping;

    public EntityMapping(){
        entityMapping = new HashMap<>();

        Gson gson = new Gson();

        //Get the input stream of entities.json
        //entities.json was generated using ExtractBlocks (https://github.com/DavixDevelop/ExtractBlocks)
        InputStream entitiesStream = this.getClass().getClassLoader().getResourceAsStream("assets/minecraft/entities.json");

        if(entitiesStream != null){
            //Get reader
            Reader reader = new InputStreamReader(entitiesStream);
            //Deserialize  JSON to List
            List<JsonEntity> entities = gson.fromJson(reader, new TypeToken<List<JsonEntity>>(){}.getType());

            for(JsonEntity entity : entities){
                String resourcePath = entity.getResourcePath();
                String entityDomain = resourcePath.substring(0, resourcePath.indexOf(":"));
                String entityType = resourcePath.substring(resourcePath.indexOf(":") + 1);

                entityMapping.put(entity.getID(), new Namespace(entity.getID(), entityDomain, entityType, entityType,  new HashMap<>(), 0.0));
            }
        }
    }

    public Namespace getEntityNamespace(String ID){ return entityMapping.get(ID); };
}
