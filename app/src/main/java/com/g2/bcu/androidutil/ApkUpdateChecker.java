package com.g2.bcu.androidutil;

import com.google.gson.JsonElement;
import com.g2.bcu.BuildConfig;

import common.io.WebFileIO;
import common.io.assets.AssetLoader;
import common.io.json.JsonClass;
import common.io.json.JsonDecoder;

@JsonClass(noTag = JsonClass.NoTag.LOAD)
public class ApkUpdateChecker {

    @JsonClass(noTag = JsonClass.NoTag.LOAD)
    public static class ApkJson {
        public String ver;
        public String CORE_VER;
        public int FORK_VER;
        public String desc;

        private static int getVersion(String vr) {
            int pow = 1, retver = 0;
            for (int i = vr.length() - 1; i >= 0; i--) {
                if (vr.charAt(i) == '.')
                    continue;
                if (vr.charAt(i) != '0')
                    retver += Integer.parseInt(String.valueOf(vr.charAt(i))) * pow;
                pow *= 10;
            }
            return retver;
        }

        private boolean isNewer() {
            return getVersion(ver) > getVersion(BuildConfig.VERSION_NAME);
        }
        private boolean important() {
            return FORK_VER > AssetLoader.FORK_VER || getVersion(CORE_VER) > getVersion(AssetLoader.CORE_VER);
        }

        @Override
        public String toString() {
            return desc;
        }
    }
    private static final String JAR_CHECK_URL = "https://raw.githubusercontent.com/Gideon9212/bcu-assets/master/apk/check.json";

    public static ApkJson getUpdateJson() {
        try {
            JsonElement json = WebFileIO.read(JAR_CHECK_URL);
            if (json != null) {
                ApkUpdateChecker j = JsonDecoder.decode(json, ApkUpdateChecker.class);
                if (!j.apk_update[0].isNewer())
                    return null;
                return j.apk_update[0];//TODO multiple of them
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ApkJson[] apk_update;
}
