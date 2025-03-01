package common.system;

import common.CommonStatic;
import common.util.anim.EAnimD;
import common.util.pack.bgeffect.BackgroundEffect;

import javax.annotation.Nullable;
import java.util.Random;

public class BattleRange<T extends Number> {
    public static final float battleRatio = 0.2f;
    public static final float battleHeightOffset = 1020f / battleRatio;
    public static final float battleOffset = 400f / battleRatio;
    private final Random r = new Random();

    public enum SNAP {
        LEFT,
        RIGHT,
        TOP,
        BOTTOM,
        INTERVAL,
        LENGTH,
        FRONT,
        BACK,
        SECOND,
        PERCENT,
        DEFAULT,
        BGIMAGE
    }

    private final T min;
    private final T max;
    private final SNAP minSnap, maxSnap;

    public BattleRange(T min, @Nullable SNAP minSnap, T max, @Nullable SNAP maxSnap) {
        this.min = min;
        this.max = max;
        this.minSnap = minSnap;
        this.maxSnap = maxSnap;

        if(
            !(isXAxis(minSnap) && isXAxis(maxSnap)) &&
            !(isYAxis(minSnap) && isYAxis(maxSnap)) &&
            !(isZAxis(minSnap) && isZAxis(maxSnap)) &&
            !(isSecond(minSnap) && isSecond(maxSnap)) &&
            !(isPercentage(minSnap) && isPercentage(maxSnap)) &&
            !(isInterval(minSnap) && isInterval(maxSnap)) &&
            !(isLength(minSnap) && isLength(maxSnap))
        ) {
            throw new IllegalStateException("Snap direction must be either top/bottom or left/right or front/back or second or percentage! -> minSnap : "+minSnap+" | maxSnap : "+maxSnap);
        } else if((minSnap == SNAP.FRONT && maxSnap == SNAP.BACK) || (minSnap == SNAP.BACK && maxSnap == SNAP.FRONT)) {
            throw new IllegalStateException("Unhandled z-order snap situation! -> minSnap : "+minSnap+" | maxSnap : "+maxSnap);
        }
    }

    public int getRangeI(int len) {
        int mi;

        if(minSnap != null) {
            switch (minSnap) {
                case LEFT:
                    mi = (int) (-battleOffset + min.intValue());
                    break;
                case RIGHT:
                    mi = (int) (len + battleOffset + min.intValue());
                    break;
                case BOTTOM:
                    mi = (int) (battleHeightOffset + min.intValue());
                    break;
                case SECOND:
                    mi = (int) (min.intValue() / 30.0);
                    break;
                default:
                    mi = min.intValue();
            }
        } else {
            mi = min.intValue();
        }

        int ma;

        if(maxSnap != null) {
            switch (maxSnap) {
                case LEFT:
                    ma = (int) (-battleOffset + max.intValue());
                    break;
                case RIGHT:
                    ma = (int) (len + battleOffset + max.intValue());
                    break;
                case BOTTOM:
                    ma = (int) (battleHeightOffset + max.intValue());
                    break;
                case SECOND:
                    ma = (int) (max.intValue() / 30.0);
                    break;
                default:
                    ma = max.intValue();
            }
        } else {
            ma = max.intValue();
        }

        if(mi == ma) {
            return mi;
        }

        return mi + nextFlexibleInt(r, mi, ma);
    }

    public float getRangeF(int len) {
        float mi;

        if(minSnap != null) {
            switch (minSnap) {
                case LEFT:
                    mi = -battleOffset + min.floatValue();
                    break;
                case RIGHT:
                    mi = len + battleOffset + min.floatValue();
                    break;
                case BOTTOM:
                    mi = battleHeightOffset + min.floatValue();
                    break;
                case SECOND:
                    mi = min.floatValue() / 30f;
                    break;
                case PERCENT:
                    mi = min.floatValue() / 100f;
                    break;
                default:
                    mi = min.floatValue();
            }
        } else {
            mi = min.floatValue();
        }

        float ma;

        if(maxSnap != null) {
            switch (maxSnap) {
                case LEFT:
                    ma = -battleOffset + max.floatValue();
                    break;
                case RIGHT:
                    ma = len + battleOffset + max.floatValue();
                    break;
                case BOTTOM:
                    ma = battleHeightOffset + max.floatValue();
                    break;
                case SECOND:
                    ma = max.floatValue() / 30f;
                    break;
                case PERCENT:
                    ma = max.floatValue() / 100f;
                    break;
                default:
                    ma = max.floatValue();
            }
        } else {
            ma = max.floatValue();
        }

        if(mi == ma) {
            return mi;
        }

        return mi + r.nextFloat() * (ma - mi);
    }

    public float getRangeX(int len) {
        float mi;

        if(minSnap != null) {
            if(minSnap == SNAP.LEFT) {
                mi = min.floatValue();
            } else if(minSnap == SNAP.RIGHT) {
                mi = (len * CommonStatic.BattleConst.ratio / battleRatio) + battleOffset + min.floatValue();
            } else {
                mi = battleOffset / 2f + min.floatValue();
            }
        } else {
            mi = battleOffset / 2f + min.floatValue();
        }

        float ma;

        if(maxSnap != null) {
            if(maxSnap == SNAP.LEFT) {
                ma = max.floatValue();
            } else if(maxSnap == SNAP.RIGHT) {
                ma = max.floatValue() + (len * CommonStatic.BattleConst.ratio / battleRatio) + battleOffset;
            } else {
                ma = battleOffset / 2f + max.floatValue();
            }
        } else {
            ma = battleOffset / 2f + max.floatValue();
        }

        if(mi == ma) {
            return mi;
        }

        return mi + r.nextFloat() * (ma - mi);
    }

    public float getRangeY(float height, float midH) {
        float mi;

        if(minSnap != null) {
            if(minSnap == SNAP.TOP) {
                mi = min.floatValue() + (BackgroundEffect.BGHeight * 3 - height + midH) / battleRatio;
            } else if(minSnap == SNAP.BOTTOM) {
                mi = min.floatValue() + (BackgroundEffect.BGHeight * 3 + midH) / battleRatio;
            } else {
                mi = min.floatValue();
            }
        } else {
            mi = battleHeightOffset + min.floatValue();
        }

        float ma;

        if(maxSnap != null) {
            if(maxSnap == SNAP.TOP) {
                ma = max.floatValue() + (BackgroundEffect.BGHeight * 3 - height + midH) / battleRatio;
            } else if(maxSnap == SNAP.BOTTOM) {
                ma = max.floatValue() + (BackgroundEffect.BGHeight * 3 + midH) / battleRatio;
            } else {
                ma = max.floatValue();
            }
        } else {
            ma = battleHeightOffset + max.floatValue();
        }

        if(mi == ma) {
            return mi;
        }

        return mi + r.nextFloat() * (ma - mi);
    }

    public int getPureRangeI() {
        int mi = min.intValue();

        int ma = max.intValue();

        if (ma == mi)
            return mi;

        return mi + nextFlexibleInt(r, mi, ma);
    }

    public int getAnimFrame(EAnimD<?> anim) {
        int mi;

        if (minSnap == SNAP.INTERVAL) {
            mi = min.intValue() + anim.len() * 5;
        } else if(minSnap == SNAP.LENGTH) {
            mi = (int) (min.intValue() * anim.len() / 100.0);
        } else {
            mi = min.intValue();
        }

        int ma;

        if(maxSnap == SNAP.INTERVAL) {
            ma = max.intValue() + anim.len() * 5;
        } else if(maxSnap == SNAP.LENGTH) {
            ma = (int) (max.intValue() * anim.len() / 100.0);
        } else {
            ma = max.intValue();
        }

        if(mi == ma) {
            return mi;
        }

        return mi + nextFlexibleInt(r, mi, ma);
    }

    public boolean isFront() {
        return minSnap == maxSnap && maxSnap == SNAP.FRONT;
    }

    public boolean hasRandomValue() {
        return !max.equals(min);
    }

    private boolean isXAxis(SNAP snap) {
        return snap == null || snap == SNAP.LEFT || snap == SNAP.RIGHT;
    }

    private boolean isYAxis(SNAP snap) {
        return snap == null || snap == SNAP.TOP || snap == SNAP.BOTTOM;
    }

    private boolean isZAxis(SNAP snap) {
        return snap == null || snap == SNAP.FRONT || snap == SNAP.BACK;
    }

    private boolean isPercentage(SNAP snap) {
        return snap == null || snap == SNAP.PERCENT;
    }

    private boolean isSecond(SNAP snap) {
        return snap == null || snap == SNAP.SECOND;
    }

    private boolean isInterval(SNAP snap) {
        return snap == null || snap == SNAP.INTERVAL;
    }

    private boolean isLength(SNAP snap) {
        return snap == null || snap == SNAP.LENGTH;
    }

    private int nextFlexibleInt(Random r, int min, int max) {
        if(max < min) {
            return -r.nextInt(min - max);
        } else {
            return r.nextInt(max - min);
        }
    }
}
