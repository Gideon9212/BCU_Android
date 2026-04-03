package common.util;

import common.util.anim.AnimI;
import common.util.anim.EAnimI;
import common.io.json.JsonClass;
import common.io.json.JsonField;
import common.pack.Source.ResourceLocation;

@JsonClass
public abstract class Animable<A extends AnimI<A, T>, T extends AnimI.AnimType<A, T>> extends ImgCore {

	@JsonField(alias = ResourceLocation.class)
	public A anim;

	public abstract EAnimI getEAnim(T t);

}
