package common.util.unit;

import common.io.json.JsonClass;
import common.pack.Identifier;
import common.system.VImg;

import javax.annotation.Nullable;

/**
 * Placeholder class to connect RandomUnit with Form
 */
@JsonClass.JCGeneric(AbForm.AbFormJson.class)
@JsonClass(read = JsonClass.RType.FILL)
public interface AbForm {
    @JsonClass(noTag = JsonClass.NoTag.LOAD)
    class AbFormJson {
        public Identifier<AbUnit> uid;
        public int fid;

        @JsonClass.JCConstructor
        public AbFormJson() {
        }

        @JsonClass.JCConstructor
        public AbFormJson(AbForm af) {
            uid = af.getID();
            fid = af.getFid();
        }

        @JsonClass.JCGetter
        public AbForm get() {
            if (uid.get() instanceof UniRand)
                return (UniRand) uid.get();

            try {
                return uid.get().getForms()[fid];
            } catch (Exception e) {
                return null;
            }
        }
    }

    Identifier<AbUnit> getID();

    AbUnit unit();

    default int getFid() {
        return 0;
    }

    VImg getIcon();

    VImg getDeployIcon();

    default Level regulateLv(@Nullable Level src, Level target) {
        return target;
    }
}
