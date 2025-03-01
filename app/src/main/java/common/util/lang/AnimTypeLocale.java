package common.util.lang;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import common.CommonStatic;
import common.CommonStatic.Lang.Locale;
import common.io.assets.Admin.StaticPermitted;
import common.util.anim.AnimI;
import common.util.anim.AnimU;
import common.util.pack.Background;
import common.util.pack.EffAnim;
import common.util.pack.NyCastle;
import common.util.pack.WaveAnim;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class AnimTypeLocale {

	@StaticPermitted
	public static final Set<AnimI.AnimType<?, ?>> TYPES = new HashSet<>();

	static {
		Collections.addAll(TYPES, Background.BGWvType.values());
		Collections.addAll(TYPES, NyCastle.NyType.values());
		Collections.addAll(TYPES, AnimU.TYPEDEF);
		Collections.addAll(TYPES, AnimU.SOUL);
		Collections.addAll(TYPES, AnimU.BGEFFECT);
		Collections.addAll(TYPES, WaveAnim.WaveType.values());
		Collections.addAll(TYPES, EffAnim.ArmorEff.values());
		Collections.addAll(TYPES, EffAnim.BarrierEff.values());
		Collections.addAll(TYPES, EffAnim.DefEff.values());
		Collections.addAll(TYPES, EffAnim.KBEff.values());
		Collections.addAll(TYPES, EffAnim.SniperEff.values());
		Collections.addAll(TYPES, EffAnim.SpeedEff.values());
		Collections.addAll(TYPES, EffAnim.VolcEff.values());
		Collections.addAll(TYPES, EffAnim.WarpEff.values());
		Collections.addAll(TYPES, EffAnim.WeakUpEff.values());
		Collections.addAll(TYPES, EffAnim.ZombieEff.values());
		Collections.addAll(TYPES, EffAnim.ShieldEff.values());
		Collections.addAll(TYPES, EffAnim.DmgCap.values());
		Collections.addAll(TYPES, EffAnim.LethargyEff.values());
		Collections.addAll(TYPES, EffAnim.RemShieldEff.values());
		Collections.addAll(TYPES, EffAnim.AuraEff.values());
		Collections.addAll(TYPES, EffAnim.RangeShieldEff.values());
		Collections.addAll(TYPES, EffAnim.GuardEff.values());
		Collections.addAll(TYPES, EffAnim.BlastEff.values());
	}

	public static void read() {
		Locale loc = CommonStatic.getConfig().langs[0];
		InputStream f = CommonStatic.ctx.getLangFile("animation_type.json");

		JsonElement je = JsonParser.parseReader(new InputStreamReader(f, StandardCharsets.UTF_8));
		for (AnimI.AnimType<?, ?> type : TYPES) {
			JsonObject obj = je.getAsJsonObject().get(type.getClass().getSimpleName()).getAsJsonObject();
			String val = obj.get(type.toString().toUpperCase()).getAsString();
			MultiLangCont.getStatic().ANIMNAME.put(loc, type, val);
		}
	}

}