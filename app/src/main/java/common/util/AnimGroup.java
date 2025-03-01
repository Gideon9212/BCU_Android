package common.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import common.CommonStatic;
import common.util.anim.AnimCE;
import common.pack.Context;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class AnimGroup {
    public static final AnimGroup workspaceGroup = new AnimGroup();

    public static void readGroupData() {
        File json = CommonStatic.ctx.getUserFile("animGroup.json");

        if(!json.exists()) {
            workspaceGroup.resetGroup();
        } else {
            try {
                JsonObject elem = JsonParser.parseReader(new FileReader(json)).getAsJsonObject();

                workspaceGroup.parseJson(elem);
            } catch (Exception e) {
                CommonStatic.ctx.printErr(Context.ErrType.CORRUPT, "Anim Group Data has been corrupted. New Group Data will be generated.");
                e.printStackTrace();
                workspaceGroup.resetGroup();
            }
        }
    }

    public static void writeAnimGroup() {
        File json = CommonStatic.ctx.getUserFile("animGroup.json");

        try {
            if(!json.exists()) {
                boolean res = json.createNewFile();

                if(!res)
                    return;
            }

            JsonObject obj = workspaceGroup.parseAnimGroup();

            OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(json), StandardCharsets.UTF_8);
            fw.write(obj.toString());
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public final Map<String, ArrayList<AnimCE>> groups = new TreeMap<>();

    public void resetGroup() {
        groups.clear();

        ArrayList<AnimCE> anims = new ArrayList<>(AnimCE.map().values());

        groups.put("", anims);
    }

    public void renewGroup() {
        ArrayList<String> emptyGroup = new ArrayList<>();

        for(String groupName : groups.keySet()) {
            ArrayList<AnimCE> anims = groups.get(groupName);

            if(anims == null || anims.isEmpty())
                emptyGroup.add(groupName);
        }

        groups.clear();

        groups.put("", new ArrayList<>());

        for(String groupName : emptyGroup) {
            groups.put(groupName, new ArrayList<>());
        }

        for(AnimCE anim : AnimCE.map().values()) {
            String group = anim.group;

            ArrayList<AnimCE> g;

            if(groups.containsKey(group)) {
                g = groups.get(group);

                if(g == null)
                    g = new ArrayList<>();
            } else {
                g = new ArrayList<>();
            }

            g.add(anim);

            groups.put(group, g);
        }

        for(ArrayList<AnimCE> group : groups.values()) {
            if(!group.isEmpty()) {
                group.sort(Comparator.comparing(a -> a.id.id));
            }
        }
    }

    public JsonObject parseAnimGroup() {
        JsonObject obj = new JsonObject();

        ArrayList<AnimCE> baseAnim = groups.get("");

        if(!baseAnim.isEmpty()) {
            JsonArray arr = new JsonArray();

            for(AnimCE anim : baseAnim) {
                arr.add(anim.id.id);
            }

            obj.add("base", arr);
        }

        JsonObject otherGroup = new JsonObject();

        for(String groupName : groups.keySet()) {
            if(groupName.equals(""))
                continue;

            ArrayList<AnimCE> group = groups.get(groupName);

            if(group == null)
                continue;

            JsonArray arr = new JsonArray();

            for(AnimCE anim : group) {
                arr.add(anim.id.id);
            }

            otherGroup.add(groupName, arr);
        }

        obj.add("custom", otherGroup);

        return obj;
    }

    public void parseJson(JsonObject obj) {
        groups.clear();

        groups.put("", new ArrayList<>());

        if(obj.has("base")) {
            JsonArray arr = obj.getAsJsonArray("base");

            for(int i = 0; i < arr.size(); i++) {
                String id = arr.get(i).getAsString();

                AnimCE anim = AnimCE.map().get(id);

                if(anim != null) {
                    anim.group = "";

                    ArrayList<AnimCE> baseGroup = groups.get("");

                    if(baseGroup == null)
                        baseGroup = new ArrayList<>();

                    baseGroup.add(anim);

                    groups.put("", baseGroup);
                }
            }
        }

        if(obj.has("custom")) {
            JsonObject custom = obj.getAsJsonObject("custom");

            for(String groupName : custom.keySet()) {
                JsonArray arr = custom.getAsJsonArray(groupName);

                if(arr.size() == 0) {
                    groups.put(groupName, new ArrayList<>());
                } else {
                    for(int i = 0; i < arr.size(); i++) {
                        String id = arr.get(i).getAsString();

                        AnimCE anim = AnimCE.map().get(id);

                        if(anim != null) {
                            anim.group = groupName;

                            ArrayList<AnimCE> customGroup = groups.get(groupName);

                            if(customGroup == null)
                                customGroup = new ArrayList<>();

                            customGroup.add(anim);

                            groups.put(groupName, customGroup);
                        }
                    }
                }
            }
        }

        for(ArrayList<AnimCE> group : groups.values()) {
            if(!group.isEmpty()) {
                group.sort(Comparator.comparing(a -> a.id.id));
            }
        }
    }

    public String validateGroupName(String groupName) {
        int i = 0;

        while(true) {
            String name = i == 0 ? groupName : groupName+" "+i;

            if(groups.containsKey(name))
                i++;
            else
                return name;
        }
    }
}
