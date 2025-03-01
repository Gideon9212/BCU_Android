package common.util.pack.bgeffect;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import common.CommonStatic;
import common.io.json.JsonClass;
import common.pack.Identifier;
import common.pack.UserProfile;
import common.system.P;
import common.system.fake.FakeGraphics;
import common.system.files.VFile;
import common.util.Data;
import common.util.pack.Background;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@JsonClass.JCGeneric(Identifier.class)
public class JsonBGEffect extends BackgroundEffect {
    private final List<BGEffectHandler> handlers = new LinkedList<>();
    protected boolean postNeed = false;

    public JsonBGEffect(Identifier<BackgroundEffect> identifier, boolean post) throws IOException {
        super(identifier);
        int jid = identifier.id;
        String jsonName = "bg"+ Data.trio(jid)+".json";

        VFile vf = VFile.get("./org/data/"+jsonName);

        if(vf == null) {
            throw new FileNotFoundException("Such json file not found : ./org/data/"+jsonName);
        }

        try {
            Reader r = new InputStreamReader(vf.getData().getStream(), StandardCharsets.UTF_8);

            JsonElement elem = JsonParser.parseReader(r);

            r.close();

            JsonObject obj = elem.getAsJsonObject();

            if(obj.has("data")) {
                JsonArray arr = obj.getAsJsonArray("data");
                for(int i = 0; i < arr.size(); i++) {
                    BGEffectSegment segment = new BGEffectSegment(arr.get(i).getAsJsonObject(), jsonName, jid);
                    handlers.add(new BGEffectHandler(segment, jid));
                }
            } else if (obj.has("id")) {
                if(post) {
                    int efID = obj.get("id").getAsInt();

                    for (BackgroundEffect bge : UserProfile.getBCData().bgEffects)
                        if (bge instanceof JsonBGEffect && bge.id.id == efID) {
                            handlers.addAll(((JsonBGEffect)bge).handlers);
                            break;
                        }
                } else
                    postNeed = true;
            }
        } catch (Exception ignored) {
            Matcher matcher = Pattern.compile("\\{(\\s+)?\"id\"(\\s+)?:(\\s+)?\\d+(\\s+)?\\}").matcher(new String(vf.getData().getBytes()));

            while(matcher.find()) {
                if(post) {
                    String group = matcher.group();
                    JsonObject obj = JsonParser.parseString(group).getAsJsonObject();

                    if(obj.has("id")) {
                        int efID = obj.get("id").getAsInt();

                        for (BackgroundEffect bge : UserProfile.getBCData().bgEffects)
                            if (bge instanceof JsonBGEffect && bge.id.id == efID) {
                                handlers.addAll(((JsonBGEffect)bge).handlers);
                                break;
                            }
                    } else {
                        throw new IllegalStateException("Unhandled bg effect found for " + jsonName);
                    }
                } else {
                    postNeed = true;
                    break;
                }
            }
        }
    }

    @Override
    public void check() {
        for (BGEffectHandler handler : handlers)
            handler.check();
    }

    @Override
    public void preDraw(FakeGraphics g, P rect, float siz, float midH) {
        for (BGEffectHandler handler : handlers)
            handler.draw(g, rect, siz, false);
    }

    @Override
    public void postDraw(FakeGraphics g, P rect, float siz, float midH) {
        for (BGEffectHandler handler : handlers)
            handler.draw(g, rect, siz, true);
    }

    @Override
    public void draw(FakeGraphics g, float y, float siz, float midH) {
        P pee = P.newP(0, y);
        for (BGEffectHandler handler : handlers) {
            handler.draw(g, pee, siz, false);
            handler.draw(g, pee, siz, true);
        }
        P.delete(pee);
    }

    @Override
    public void update(int w, float h, float midH, float timeFlow) {
        for (BGEffectHandler handler : handlers)
            handler.update(w, h, midH, timeFlow);
    }

    @Override
    public void updateAnimation(int w, float h, float midH, float timeFlow) {
        for (BGEffectHandler handler : handlers)
            handler.updateAnimation(timeFlow);
    }

    @Override
    public void initialize(int w, float h, float midH, Background bg) {
        for (BGEffectHandler handler : handlers)
            handler.initialize(w, h, midH);
    }

    @Override
    public void release() {
        for (BGEffectHandler handler : handlers)
            handler.release();
    }

    @Override
    public String toString() {
        String temp = CommonStatic.def.getUILang(0, "bgjson" + id.id);

        if (temp.equals("bgjson" + id.id))
            temp = CommonStatic.def.getUILang(0, "bgeffdum").replace("_", "" + id.id);
        return temp;
    }
}
