package common.util.pack.bgeffect;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import common.system.BattleRange;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class BGEffectSegment {
    public enum BGFile {
        IMAGE,
        IMGCUT,
        MODEL,
        ANIME
    }

    public static List<String> tags = Arrays.asList(
            "name", "model", "file", "count", "x", "y", "startX", "startY", "z", "angle", "scale", "scaleX", "scaleY",
            "startScaleX", "startFrame", "frame", "wait", "lifeTime", "startScale", "v", "vx", "vy", "startV", "startVx", "startVy",
            "moveAngle", "alpha", "destroyLeft", "destroyTop", "destroyRight", "destroyBottom", "angularV", "startWait",
            "equallySpaced"
    );

    public String name = "";
    public final String json;

    /**
     * Animation file
     */
    public final int[] model;

    /**
     * Animation file, but with specified file name
     */
    public final String[] files;

    /**
     * Number of components which have to be generated
     */
    public final BattleRange<Integer> count;
    /**
     * Position where component will be drawn. If there is a maximum value, the value will be a random number between the minimum and maximum value.
     */
    public final BattleRange<Integer> x;
    public final BattleRange<Integer> y;
    /**
     * The Z-Order where the component will be drawn.
     */
    public final BattleRange<Integer> zOrder;
    /**
     * The Size of the component to draw. If there is a maximum value, the value will be a random number between the minimum and maximum value.
     */
    public BattleRange<Float> scale;
    /**
     * The start Size of the component along x-axis. If there is a maximum value, the value will be a random number between the minimum and maximum value.
     */
    public BattleRange<Float> startScaleX;

    public BattleRange<Float> scaleX;
    public BattleRange<Float> scaleY;
    /**
     * The Angle of the component to draw. If there is a maximum value, the value will be a random number between the minimum and maximum value.
     */
    public BattleRange<Float> angle;
    /**
     * Frame this component will start to appear.
     */
    public final BattleRange<Integer> startFrame;

    public final BattleRange<Integer> frame;

    public final BattleRange<Integer> wait;
    public final BattleRange<Integer> startWait;

    /**
     * Draw same segment over and over again until reaching battle boundary
     * Count is assumed to be 1
     */
    public final BGEffectSpacer spacer;

    /**
     * The speed this component will rotate. If there is a maximum value, the value will be a random number between the minimum and maximum value.
     */
    public BattleRange<Float> angleVelocity;
    /**
     * Component's velocity.
     */
    public final BattleRange<Float> velocity;
    /**
     * The speed this component will move. If there is a maximum value, the value will be a random number between the minimum and maximum value.
     */
    public final BattleRange<Float> velocityX;
    public final BattleRange<Float> velocityY;
    /**
     * If the component goes beyond these values' direction, it will be destroyed.
     */
    public BattleRange<Integer> destroyTop, destroyBottom, destroyLeft, destroyRight;
    /**
     * The size this component when made. If there is a maximum value, the value will be a random number between the minimum and maximum value.
     */
    public final BattleRange<Float> startScale;
    /**
     * Initial X position. If there is a maximum value, the value will be a random number between the minimum and maximum value.
     */
    public final BattleRange<Integer> startX;
    /**
     * Initial Y position. If there is a maximum value, the value will be a random number between the minimum and maximum value.
     */
    public final BattleRange<Integer> startY;
    /**
     * Initial X velocity. If there is a maximum value, the value will be a random number between the minimum and maximum value.
     */
    public final BattleRange<Float> startVelocityX;
    /**
     * Initial Y velocity. If there is a maximum value, the value will be a random number between the minimum and maximum value.
     */
    public final BattleRange<Float> startVelocityY;
    /**
     * Initial velocity. If there is a maximum value, the value will be a random number between the minimum and maximum value.
     */
    public final BattleRange<Float> startVelocity;
    /**
     * The component will be destroyed if it stays on the field longer than this time.
     */
    public final BattleRange<Integer> lifeTime;
    /**
     * ???
     */
    public final BattleRange<Float> moveAngle;
    /**
     * Component's opacity. If there is a maximum value, the value will be a random number between the minimum and maximum value.
     */
    public final BattleRange<Integer> opacity;

    public BGEffectSegment(JsonObject elem, String json, int bgID) {
        this.json = json;

        if(elem.has("name"))
            name = elem.get("name").getAsString();

        if(elem.has("model")) {
            JsonObject modelObject = elem.getAsJsonObject("model");

            if(modelObject.has("values")) {
                JsonArray modelArray = modelObject.getAsJsonArray("values");

                model = new int[modelArray.size()];
                for(int i = 0; i < modelArray.size(); i++)
                    model[i] = modelArray.get(i).getAsInt();
            } else if(modelObject.has("value"))
                model = new int[] {modelObject.get("value").getAsInt()};
            else {
                System.out.println("W/BGEffectSegment | "+ json +"/ model has weird data type : \"model\" : "+modelObject);
                model = null;
            }
        } else
            model = null;

        if(elem.has("file")) {
            JsonObject fileObject = elem.getAsJsonObject("file");

            files = new String[BGFile.values().length];
            if(fileObject.has("image")) {
                String imageName = fileObject.get("image").getAsString();

                if(imageName.matches("bg\\d{3}\\.png"))
                    files[BGFile.IMAGE.ordinal()] = "./org/img/bg/"+imageName;
                else
                    files[BGFile.IMAGE.ordinal()] = "./org/img/bgEffect/"+fileObject.get("image").getAsString();
            }

            if(fileObject.has("imgcut"))
                files[BGFile.IMGCUT.ordinal()] = "./org/battle/bg/"+fileObject.get("imgcut").getAsString();
            if(fileObject.has("model"))
                files[BGFile.MODEL.ordinal()] = "./org/battle/bg/"+fileObject.get("model").getAsString();
            if(fileObject.has("anime"))
                files[BGFile.ANIME.ordinal()] = "./org/battle/bg/"+fileObject.get("anime").getAsString();

            for (String file : files)
                if (file == null)
                    throw new IllegalStateException("File name isn't fully specified! : " + fileObject);
        } else
            files = null;

        if(files == null && model == null)
            throw new IllegalStateException("Unhandled file/model data found, both are null");
        else if(files != null && model != null)
            throw new IllegalStateException("Unhandled file/model data found, both aren't null");

        if(elem.has("count"))
            count = readRangedJsonObjectI(elem, "count");
        else
            count = new BattleRange<>(1, null, 1, null);

        if(elem.has("x"))
            x = readRangedJsonObjectI(elem, "x", i -> i*4);
        else if(elem.has("startX"))
            x = readRangedJsonObjectI(elem, "startX", i -> i*4);
        else
            x = new BattleRange<>(0, null, 0, null);

        if(elem.has("y"))
            y = readRangedJsonObjectI(elem, "y", i -> i*4);
        else if(elem.has("startY"))
            y = readRangedJsonObjectI(elem, "startY", i -> i*4);
        else
            y = new BattleRange<>(0, null, 0, null);

        if(elem.has("startX"))
            startX = readRangedJsonObjectI(elem, "startX", i -> i*4);
        else
            startX = null;

        if(elem.has("startY"))
            startY = readRangedJsonObjectI(elem, "startY", i -> i*4);
        else
            startY = null;

        if(elem.has("z"))
            zOrder = readRangedJsonObjectI(elem, "z");
        else
            zOrder = new BattleRange<>(0, BattleRange.SNAP.BACK, 0, BattleRange.SNAP.BACK);

        if(elem.has("angle"))
            angle = readRangedJsonObjectD(elem, "angle", d -> (float) Math.toRadians(d));
        else
            angle = null;

        if(elem.has("scale"))
            scale = readRangedJsonObjectD(elem, "scale");
        else
            scale = null;

        if (elem.has("startScaleX"))
            startScaleX = readRangedJsonObjectD(elem, "startScaleX");
        else
            startScaleX = null;

        if(elem.has("scaleX"))
            scaleX = readRangedJsonObjectD(elem, "scaleX");
        else
            scaleX = null;

        if(elem.has("scaleY"))
            scaleY = readRangedJsonObjectD(elem, "scaleY");
        else
            scaleY = null;

        if(elem.has("startFrame"))
            startFrame = readRangedJsonObjectI(elem, "startFrame");
        else
            startFrame = null;

        if(elem.has("frame"))
            frame = readRangedJsonObjectI(elem, "frame");
        else
            frame = null;

        if(elem.has("wait"))
            wait = readRangedJsonObjectI(elem, "wait");
        else
            wait = null;

        if(elem.has("startWait"))
            startWait = readRangedJsonObjectI(elem, "startWait");
        else
            startWait = null;

        if(elem.has("lifeTime"))
            lifeTime = readRangedJsonObjectI(elem, "lifeTime");
        else
            lifeTime = null;

        if(elem.has("startScale")) {
            startScale = readRangedJsonObjectD(elem, "startScale");
            if(elem.getAsJsonObject("startScale").has("randGroup"))
                System.out.println("W/BGEffectSegment | "+json+" / Random group found in start scale -> startScale : "+elem.getAsJsonObject("startScale").get("randGroup").getAsString());
        } else
            startScale = null;

        if(elem.has("v"))
            velocity = readRangedJsonObjectD(elem, "v", d -> d * 4f);
        else
            velocity = null;

        if(elem.has("vx"))
            velocityX = readRangedJsonObjectD(elem, "vx", d -> d * 4f);
        else
            velocityX = null;

        if(elem.has("vy"))
            velocityY = readRangedJsonObjectD(elem, "vy", d -> d * 4f);
        else
            velocityY = null;

        if(elem.has("startV"))
            startVelocity = readRangedJsonObjectD(elem, "startV", d -> d * 4f);
        else
            startVelocity = null;

        if(elem.has("startVx")) {
            startVelocityX = readRangedJsonObjectD(elem, "startVx", d -> d * 4f);
            if(elem.getAsJsonObject("startVx").has("randGroup"))
                System.out.println("W/BGEffectSegment | "+json+" / Random group found in start velocity x -> startVx : "+elem.getAsJsonObject("startVx").get("randGroup").getAsString());
        } else
            startVelocityX = null;

        if(elem.has("startVy")) {
            startVelocityY = readRangedJsonObjectD(elem, "startVy", d -> d * 4f);
            if(elem.getAsJsonObject("startVy").has("randGroup"))
                System.out.println("W/BGEffectSegment | "+json+" / Random group found in start velocity y -> startVy : "+elem.getAsJsonObject("startVy").get("randGroup").getAsString());
        } else
            startVelocityY = null;

        if(elem.has("moveAngle")) {
            moveAngle = readRangedJsonObjectD(elem, "moveAngle", d -> (float) Math.toRadians(d));
            if(velocity == null)
                System.out.println("W/BGEffectSegment | "+json+" / Non-defined velocity data found while moveAngle is defined -> vx == null : "+(velocityX == null)+" | vy == null : "+(velocityY == null));
        } else if(velocity != null)
            moveAngle = new BattleRange<>(0f, null, 0f, null);
        else
            moveAngle = null;

        if(elem.has("alpha"))
            opacity = readRangedJsonObjectI(elem, "alpha");
        else
            opacity = null;

        if(elem.has("destroyLeft"))
            destroyLeft = readRangedJsonObjectI(elem, "destroyLeft");
        else
            destroyLeft = null;

        if(elem.has("destroyTop"))
            destroyTop = readRangedJsonObjectI(elem, "destroyTop");
        else
            destroyTop = null;

        if(elem.has("destroyRight"))
            destroyRight = readRangedJsonObjectI(elem, "destroyRight");
        else
            destroyRight = null;

        if(elem.has("destroyBottom"))
            destroyBottom = readRangedJsonObjectI(elem, "destroyBottom");
        else
            destroyBottom = null;

        if(elem.has("angularV"))
            angleVelocity = readRangedJsonObjectD(elem, "angularV", d -> (float) Math.toRadians(d));
        else
            angleVelocity = null;

        if(elem.has("equallySpaced")) {
            JsonObject obj = elem.getAsJsonObject("equallySpaced");
            BattleRange.SNAP snap;

            switch (obj.get("base").getAsString()) {
                case "default":
                    snap = BattleRange.SNAP.DEFAULT;
                    break;
                case "bgImage":
                    snap = BattleRange.SNAP.BGIMAGE;
                    break;
                default:
                    throw new IllegalStateException("E/BGEffectSegment | "+json+" / Unhandled snap mode for equallySpaced tag : " + obj.get("base").getAsString());
            }

            spacer = new BGEffectSpacer(obj.get("pos1").getAsInt(), obj.get("pos2").getAsInt(), obj.get("value").getAsInt(), snap, bgID);
            if(count.hasRandomValue() || count.getPureRangeI() != 1)
                System.out.println("W/BGEffectSegment | "+json+" / Count isn't 1 while spacer is defined -> Has Random Value : "+ count.hasRandomValue() +" | Count gotten : "+count.getPureRangeI());
        } else
            spacer = null;

        //Check unknown tags
        for(String tag : elem.getAsJsonObject().keySet())
            if(!tags.contains(tag))
                System.out.println("W/BGEffectSegment | "+json+" / Unknown tag found -> " + tag);
    }

    private BattleRange.SNAP getSnap(String base) {
        switch (base) {
            case "worldLeft":
                return BattleRange.SNAP.LEFT;
            case "worldRight":
                return BattleRange.SNAP.RIGHT;
            case "worldTop":
                return BattleRange.SNAP.TOP;
            case "worldBottom":
                return BattleRange.SNAP.BOTTOM;
            case "frontChara":
                return BattleRange.SNAP.FRONT;
            case "backChara":
                return BattleRange.SNAP.BACK;
            case "animeInterval":
                return BattleRange.SNAP.INTERVAL;
            case "animeLength":
                return BattleRange.SNAP.LENGTH;
            case "secondToFrame":
                return BattleRange.SNAP.SECOND;
            case "percentToFloat":
                return BattleRange.SNAP.PERCENT;
            default:
                throw new IllegalStateException("Unknown base type found in " + json + " : "+base);
        }
    }

    @Nonnull
    private BattleRange<Integer> readRangedJsonObjectI(JsonObject obj, String mainKeyword) {
        JsonObject xObject = obj.getAsJsonObject(mainKeyword);

        int min;
        BattleRange.SNAP minSnap;
        int max;
        BattleRange.SNAP maxSnap;

        if(xObject.has("min") && xObject.has("max")) {
            if (xObject.has("min")) {
                JsonObject xMinObject = xObject.getAsJsonObject("min");

                min = xMinObject.get("value").getAsInt();
                if (xMinObject.has("base"))
                    minSnap = getSnap(xMinObject.get("base").getAsString());
                else
                    minSnap = null;
            } else {
                min = 0;
                minSnap = null;
            }

            if (xObject.has("max")) {
                JsonObject xMaxObject = xObject.getAsJsonObject("max");

                max = xMaxObject.get("value").getAsInt();
                if (xMaxObject.has("base"))
                    maxSnap = getSnap(xMaxObject.get("base").getAsString());
                else
                    maxSnap = null;
            } else {
                max = 0;
                maxSnap = null;
            }
        } else if(xObject.has("value")) {
            min = max = xObject.get("value").getAsInt();

            if(xObject.has("base"))
                minSnap = maxSnap = getSnap(xObject.get("base").getAsString());
            else
                minSnap = maxSnap = null;
        } else
            throw new IllegalStateException("Unhandled situation while reading bg effect! | Caused while reading x : "+ xObject);

        return new BattleRange<>(min, minSnap, max, maxSnap);
    }

    @Nonnull
    private BattleRange<Integer> readRangedJsonObjectI(JsonObject obj, String mainKeyword, Function<Integer, Integer> func) {
        JsonObject xObject = obj.getAsJsonObject(mainKeyword);

        int min;
        BattleRange.SNAP minSnap;
        int max;
        BattleRange.SNAP maxSnap;

        if(xObject.has("min") && xObject.has("max")) {
            if (xObject.has("min")) {
                JsonObject xMinObject = xObject.getAsJsonObject("min");

                min = func.apply(xMinObject.get("value").getAsInt());
                if (xMinObject.has("base"))
                    minSnap = getSnap(xMinObject.get("base").getAsString());
                else
                    minSnap = null;
            } else {
                min = 0;
                minSnap = null;
            }

            if (xObject.has("max")) {
                JsonObject xMaxObject = xObject.getAsJsonObject("max");

                max = func.apply(xMaxObject.get("value").getAsInt());
                if (xMaxObject.has("base"))
                    maxSnap = getSnap(xMaxObject.get("base").getAsString());
                else
                    maxSnap = null;
            } else {
                max = 0;
                maxSnap = null;
            }
        } else if(xObject.has("value")) {
            min = max = func.apply(xObject.get("value").getAsInt());

            if(xObject.has("base"))
                minSnap = maxSnap = getSnap(xObject.get("base").getAsString());
            else
                minSnap = maxSnap = null;
        } else
            throw new IllegalStateException("Unhandled situation while reading bg effect! | Caused while reading x : "+ xObject);

        return new BattleRange<>(min, minSnap, max, maxSnap);
    }

    private BattleRange<Float> readRangedJsonObjectD(JsonObject obj, String mainKeyword) {
        JsonObject xObject = obj.getAsJsonObject(mainKeyword);

        float min;
        BattleRange.SNAP minSnap;
        float max;
        BattleRange.SNAP maxSnap;

        if(xObject.has("min") && xObject.has("max")) {
            if (xObject.has("min")) {
                JsonObject xMinObject = xObject.getAsJsonObject("min");

                min = xMinObject.get("value").getAsFloat();
                if (xMinObject.has("base"))
                    minSnap = getSnap(xMinObject.get("base").getAsString());
                else
                    minSnap = null;
            } else {
                min = 0;
                minSnap = null;
            }

            if (xObject.has("max")) {
                JsonObject xMaxObject = xObject.getAsJsonObject("max");

                max = xMaxObject.get("value").getAsFloat();
                if (xMaxObject.has("base"))
                    maxSnap = getSnap(xMaxObject.get("base").getAsString());
                else
                    maxSnap = null;
            } else {
                max = 0;
                maxSnap = null;
            }
        } else if(xObject.has("value")) {
            min = max = xObject.get("value").getAsFloat();

            if(xObject.has("base"))
                minSnap = maxSnap = getSnap(xObject.get("base").getAsString());
            else
                minSnap = maxSnap = null;
        } else
            throw new IllegalStateException("Unhandled situation while reading bg effect! | Caused while reading x : "+ xObject);

        return new BattleRange<>(min, minSnap, max, maxSnap);
    }

    private BattleRange<Float> readRangedJsonObjectD(JsonObject obj, String mainKeyword, Function<Integer, Float> func) {
        JsonObject xObject = obj.getAsJsonObject(mainKeyword);

        float min;
        BattleRange.SNAP minSnap;
        float max;
        BattleRange.SNAP maxSnap;

        if(xObject.has("min") && xObject.has("max")) {
            if (xObject.has("min")) {
                JsonObject xMinObject = xObject.getAsJsonObject("min");

                min = func.apply(xMinObject.get("value").getAsInt());
                if (xMinObject.has("base"))
                    minSnap = getSnap(xMinObject.get("base").getAsString());
                else
                    minSnap = null;
            } else {
                min = 0;
                minSnap = null;
            }

            if (xObject.has("max")) {
                JsonObject xMaxObject = xObject.getAsJsonObject("max");

                max = func.apply(xMaxObject.get("value").getAsInt());
                if (xMaxObject.has("base"))
                    maxSnap = getSnap(xMaxObject.get("base").getAsString());
                else
                    maxSnap = null;
            } else {
                max = 0;
                maxSnap = null;
            }
        } else if(xObject.has("value")) {
            min = max = func.apply(xObject.get("value").getAsInt());

            if(xObject.has("base"))
                minSnap = maxSnap = getSnap(xObject.get("base").getAsString());
            else
                minSnap = maxSnap = null;
        } else
            throw new IllegalStateException("Unhandled situation while reading bg effect! | Caused while reading x : "+ xObject);

        return new BattleRange<>(min, minSnap, max, maxSnap);
    }
}
