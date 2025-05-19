package common.util.stage;

import common.CommonStatic;
import common.battle.StageBasis;
import common.battle.entity.EAnimCont;
import common.battle.entity.EEnemy;
import common.battle.entity.Entity;
import common.io.json.JsonClass;
import common.io.json.JsonDecoder;
import common.io.json.JsonField;
import common.pack.Identifier;
import common.util.Data;
import common.util.pack.EffAnim;
import common.util.pack.Soul;
import common.util.unit.AbEnemy;

@JsonClass(noTag = JsonClass.NoTag.LOAD)
public class Revival extends Data {
    public Identifier<AbEnemy> enemy;
    public Identifier<Music> bgm;
    public Identifier<Soul> soul;
    @JsonField(defval = "100")
    public int mhp = 100, matk = 100;
    public byte boss;
    public Revival rev;
    @JsonField(block = true)
    public Revival par; //Connects revivals as nodes for easier traveling. Do not edit

    @JsonClass.JCConstructor
    public Revival() {
    }

    public Revival(Identifier<AbEnemy> e) {
        enemy = e;
    }

    public Revival(Revival rev, Identifier<AbEnemy> e) {
        par = rev;
        enemy = e;
    }

    @JsonDecoder.OnInjected
    public void onInjected() {
        if (rev != null)
            rev.par = this;
    }

    public void triggerRevival(StageBasis b, float mul, int layer, int group, float pos) {
        if (boss >= 1) {
            if (CommonStatic.getConfig().shake && boss == 2 && b.shakeCoolDown[1] == 0) {
                b.shake = SHAKE_MODE_BOSS;
                b.shakeDuration = SHAKE_MODE_BOSS[SHAKE_DURATION];
                b.shakeCoolDown[1] = SHAKE_MODE_BOSS[SHAKE_COOL_DOWN];
            }
            if (b.st.bossGuard)
                b.baseBarrier++;

            for (Entity entity : b.le)
                if (entity.dire == -1 && (entity.touchable() & TCH_N) > 0) {
                    entity.interrupt(INT_SW, KB_DIS[INT_SW]);
                    entity.postUpdate();
                }
            b.lea.add(new EAnimCont(pos, 9, effas().A_SHOCKWAVE.getEAnim(EffAnim.DefEff.DEF)));
            CommonStatic.setSE(SE_BOSS);
        }

        float multi = (mhp == 0 ? 100 : mhp) * mul * 0.01f;
        float mulatk = (mhp == 0 ? 100 : matk) * mul * 0.01f;
        AbEnemy e = Identifier.getOr(enemy, AbEnemy.class);

        EEnemy ee = e.getEntity(b, this, multi, mulatk, layer, layer, boss);
        ee.group = group;
        ee.rev = rev;
        ee.added(1, pos);
        b.le.add(ee);

        if (bgm != null) {
            b.mus = bgm;
            b.themeTime = -1;
        }
    }
}
