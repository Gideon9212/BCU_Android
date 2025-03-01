package common.util.pack.bgeffect;

import common.CommonStatic;
import common.pack.UserProfile;
import common.system.BattleRange;
import common.system.P;
import common.system.fake.FakeGraphics;
import common.system.fake.FakeTransform;
import common.util.Data;
import common.util.anim.EAnimD;
import common.util.pack.Background;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class BGEffectHandler {
    protected static final P origin = new P(0, 0);

    private final BGEffectSegment segment;
    private final BGEffectAnim[] anims;

    private int count;

    protected final List<EAnimD<BGEffectAnim.BGEffType>> animation = new ArrayList<>();

    protected P[] position;
    protected float[] angle;
    protected P[] size;
    protected int[] opacity;
    /**
     * false if behind, true if front
     */
    private boolean[] zOrder;

    //this will be null if moveAngle and v aren't null
    private P[] velocity;

    //below two will be null if moveAngle and v are null
    private int[] v;
    private float[] moveAngle;

    private float[] angleVelocity;
    private int[] lifeTime;
    private int[] destroyLeft, destroyTop, destroyRight, destroyBottom;

    private int[] wait;
    private boolean[] startWaitDone;


    private final List<Integer> capture = new LinkedList<>();
    private final Random r = new Random();

    public BGEffectHandler(BGEffectSegment segment, int bgId) {
        this.segment = segment;

        if(segment.model != null) {
            Background bg = UserProfile.getBCData().bgs.get(bgId);

            anims = new BGEffectAnim[segment.model.length];

            for(int i = 0; i < segment.model.length; i++) {
                anims[i] = new BGEffectAnim(
                        "./org/img/bg/bg"+Data.trio(bgId)+".png",
                        "./org/battle/bg/bg"+Data.duo(bg.ic)+".imgcut",
                        "./org/battle/bg/bg"+ Data.duo(bg.ic)+"_"+Data.duo(segment.model[i])+".mamodel",
                        "./org/battle/bg/bg"+ Data.duo(bg.ic)+"_"+Data.duo(segment.model[i])+".maanim"
                );
            }
        } else if(segment.files != null) {
            anims = new BGEffectAnim[] {
                    new BGEffectAnim(
                            segment.files[BGEffectSegment.BGFile.IMAGE.ordinal()],
                            segment.files[BGEffectSegment.BGFile.IMGCUT.ordinal()],
                            segment.files[BGEffectSegment.BGFile.MODEL.ordinal()],
                            segment.files[BGEffectSegment.BGFile.ANIME.ordinal()]
                    )
            };
        } else {
            throw new IllegalStateException("Segment has both null model and files variable!");
        }
    }

    public void check() {
        for (BGEffectAnim anim : anims)
            anim.check();
    }

    public void initialize(int w, float h, float midH) {
        if(count != 0) {
            for(int i = 0; i < count; i++) {
                P.delete(position[i]);
                P.delete(size[i]);

                if(velocity != null)
                    P.delete(velocity[i]);
            }
        }

        count = segment.count.getRangeI(w);

        animation.clear();

        position = new P[count];
        angle = new float[count];
        size = new P[count];
        opacity = new int[count];
        zOrder = new boolean[count];

        if(segment.moveAngle != null && segment.velocity != null) {
            velocity = null;
            moveAngle = new float[count];
            v = new int[count];
        } else if(segment.velocityX != null || segment.velocityY != null || segment.startVelocityX != null || segment.startVelocityY != null) {
            velocity = new P[count];
            moveAngle = null;
            v = null;
        } else {
            velocity = null;
            moveAngle = null;
            v = null;
        }

        if(segment.lifeTime != null)
            lifeTime = new int[count];
        else
            lifeTime = null;

        if(segment.destroyLeft != null)
            destroyLeft = new int[count];
        else
            destroyLeft = null;

        if(segment.destroyTop != null)
            destroyTop = new int[count];
        else
            destroyTop = null;

        if(segment.destroyRight != null)
            destroyRight = new int[count];
        else
            destroyRight = null;

        if(segment.destroyBottom != null)
            destroyBottom = new int[count];
        else
            destroyBottom = null;

        if(segment.angleVelocity != null)
            angleVelocity = new float[count];
        else
            angleVelocity = null;

        if(segment.wait != null || segment.startWait != null)
            wait = new int[count];
        else
            wait = null;

        if(segment.startWait != null)
            startWaitDone = new boolean[count];
        else
            startWaitDone = null;

        for(int i = 0; i < count; i++) {
            EAnimD<BGEffectAnim.BGEffType> anim = anims[Math.min(anims.length - 1, r.nextInt(anims.length))].getEAnim(BGEffectAnim.BGEffType.DEF);
            anim.removeBasePivot();

            int time = 0;

            if(segment.startFrame != null) {
                time = segment.startFrame.getAnimFrame(anim);
            } else if(segment.frame != null) {
                time = segment.frame.getAnimFrame(anim);
            }

            anim.setTime(time - 1);
            anim.update(false);

            animation.add(anim);

            float x;
            float y;

            if(segment.startX != null) {
                x = segment.startX.getRangeX(w);
            } else {
                x = segment.x.getRangeX(w);
            }

            if(segment.startY != null) {
                y = segment.startY.getRangeY(h, midH);
            } else {
                y = segment.y.getRangeY(h, midH);
            }

            position[i] = P.newP(x, y);

            if(segment.angle != null) {
                angle[i] = segment.angle.getRangeF(w);
            }

            float sx = 1f;
            float sy = 1f;

            if(segment.startScale != null) {
                float s = segment.startScale.getRangeF(w);

                sx *= s;
                sy *= s;
            } else if(segment.scale != null) {
                float s = segment.scale.getRangeF(w);

                sx *= s;
                sy *= s;
            }

            if (segment.startScaleX != null) {
                sx *= segment.startScaleX.getRangeF(w);
            } else if(segment.scaleX != null) {
                sx *= segment.scaleX.getRangeF(w);
            }

            if(segment.scaleY != null) {
                sy *= segment.scaleY.getRangeF(w);
            }

            sx /= animation.get(i).getBaseSizeX();
            sy /= animation.get(i).getBaseSizeY();

            size[i] = P.newP(sx, sy);

            if(segment.opacity != null) {
                opacity[i] = (int) (segment.opacity.getRangeF(w));
            } else {
                opacity[i] = 255;
            }

            zOrder[i] = segment.zOrder.isFront();

            if(velocity != null) {
                float vx;
                float vy;

                if(segment.startVelocityX != null) {
                    vx = segment.startVelocityX.getRangeF(w);
                } else if(segment.velocityX != null) {
                    vx = segment.velocityX.getRangeF(w);
                } else {
                    vx = 0;
                }

                if(segment.startVelocityY != null) {
                    vy = segment.startVelocityY.getRangeF(w);
                } else if(segment.velocityY != null) {
                    vy = segment.velocityY.getRangeF(w);
                } else {
                    vy = 0;
                }

                velocity[i] = P.newP(vx, vy);
            } else if(v != null && moveAngle != null) {
                if(segment.moveAngle != null) {
                    moveAngle[i] = segment.moveAngle.getRangeF(w);
                } else {
                    moveAngle[i] = 0;
                }

                if(segment.startVelocity != null) {
                    v[i] = segment.startVelocity.getRangeI(w);
                } else if(segment.velocity != null) {
                    v[i] = segment.velocity.getRangeI(w);
                } else {
                    v[i] = 0;
                }
            }

            if(segment.lifeTime != null) {
                lifeTime[i] = segment.lifeTime.getAnimFrame(animation.get(i));

                if(lifeTime[i] == 0)
                    lifeTime[i] = -1;
                else
                    lifeTime[i] = Math.max(1, lifeTime[i] - time);
            }

            if(segment.destroyLeft != null) {
                destroyLeft[i] = (int) segment.destroyLeft.getRangeX(w);
            }

            if(segment.destroyTop != null) {
                destroyTop[i] = (int) segment.destroyTop.getRangeY(h, midH);
            }

            if(segment.destroyRight != null) {
                destroyRight[i] = (int) segment.destroyRight.getRangeX(w);
            }

            if(segment.destroyBottom != null) {
                destroyBottom[i] = (int) segment.destroyBottom.getRangeY(h, midH);
            }

            if(segment.angleVelocity != null) {
                angleVelocity[i] = segment.angleVelocity.getRangeF(w);
            }
        }
    }

    public void update(int w, float h, float midH, float timeFlow) {
        capture.clear();

        for(int i = 0; i < count; i++)
            if(wait != null && wait[i] != 0) {
                wait[i]--;

                if(wait[i] == 0) {
                    EAnimD<BGEffectAnim.BGEffType> anim = anims[Math.min(anims.length - 1, r.nextInt(anims.length))].getEAnim(BGEffectAnim.BGEffType.DEF);
                    anim.removeBasePivot();

                    animation.set(i, anim);

                    if(segment.frame != null) {
                        int time = segment.frame.getAnimFrame(anim);

                        anim.setTime(time - 1);
                        anim.update(false, timeFlow);

                        reInitialize(i, w, h, midH, time);
                    } else
                        reInitialize(i, w, h, midH, 0);
                }
            } else if (checkDestroy(i)) {
                capture.add(i);
            } else {
                updateChunk(i, timeFlow);

                if(lifeTime != null)
                    lifeTime[i]--;
            }

        if(!capture.isEmpty())
            for (int ind : capture) {
                if (segment.wait != null || segment.startWait != null) {
                    if (segment.wait != null && segment.startWait != null) {
                        if (!startWaitDone[ind]) {
                            wait[ind] = segment.startWait.getPureRangeI();
                            startWaitDone[ind] = true;
                        } else
                            wait[ind] = segment.wait.getPureRangeI();
                    } else if (segment.wait != null)
                        wait[ind] = segment.wait.getPureRangeI();
                    else
                        wait[ind] = segment.startWait.getPureRangeI();
                } else {
                    EAnimD<BGEffectAnim.BGEffType> anim = anims[Math.min(anims.length - 1, r.nextInt(anims.length))].getEAnim(BGEffectAnim.BGEffType.DEF);
                    anim.removeBasePivot();

                    animation.set(ind, anim);

                    if (segment.frame != null) {
                        int time = segment.frame.getAnimFrame(anim);

                        anim.setTime(time - 1);
                        anim.update(false, timeFlow);

                        reInitialize(ind, w, h, midH, time);
                    } else
                        reInitialize(ind, w, h, midH, 0);
                }
            }
    }

    public void updateAnimation(float timeFlow) {
        for(int i = 0; i < count; i++)
            if (wait == null || wait[i] == 0)
                updateChunk(i, timeFlow);
    }

    public void updateChunk(int i, float timeFlow) {
        animation.get(i).update(false, timeFlow);

        if (CommonStatic.getConfig().fps60) {
            if(v != null && moveAngle != null) {
                position[i].x += v[i] * Math.cos(moveAngle[i]) / 2.0;
                position[i].y += v[i] * Math.sin(moveAngle[i]) / 2.0;
            } else if(velocity != null) {
                position[i].x += velocity[i].x / 2.0;
                position[i].y += velocity[i].y / 2.0;
            }
            if(angleVelocity != null)
                angle[i] += angleVelocity[i] / 2.0;
        } else {
            if(v != null && moveAngle != null) {
                position[i].x += v[i] * Math.cos(moveAngle[i]);
                position[i].y += v[i] * Math.sin(moveAngle[i]);
            } else if(velocity != null) {
                position[i].x += velocity[i].x;
                position[i].y += velocity[i].y;
            }
            if(angleVelocity != null)
                angle[i] += angleVelocity[i];
        }
    }

    public void draw(FakeGraphics g, P rect, float siz, boolean post) {
        FakeTransform at = g.getTransform();

        for(int i = 0; i < count; i++) {
            if(zOrder[i] == post) {
                if(segment.spacer != null) {
                    segment.spacer.drawWithSpacer(g, rect, siz, this, i);
                } else {
                    EAnimD<BGEffectAnim.BGEffType> anim = animation.get(i);

                    g.translate(convertP(position[i].x, siz) + rect.x, convertP(position[i].y, siz) - rect.y);
                    g.rotate(angle[i]);
                    anim.drawBGEffect(g, origin, siz * 0.8f, opacity[i], size[i].x, size[i].y);
                }
            }

            g.setTransform(at);
        }

        g.delete(at);
    }

    private boolean checkDestroy(int index) {
        if(lifeTime != null && lifeTime[index] == 0)
            return true;

        if(destroyLeft != null && position[index].x < destroyLeft[index])
            return true;

        if(destroyTop != null && position[index].y < destroyTop[index])
            return true;

        if(destroyRight != null && position[index].x > destroyRight[index])
            return true;

        return destroyBottom != null && position[index].y > destroyBottom[index];
    }

    /**
     * Convert battle unit to pixel unit
     * @param p Position in battle
     * @param siz Size of battle
     * @return Converted pixel
     */
    private static int convertP(float p, float siz) {
        return (int) (p * BattleRange.battleRatio * siz);
    }

    private void reInitialize(int ind, int w, float h, float midH, int time) {
        position[ind].x = segment.x.getRangeX(w);
        position[ind].y = segment.y.getRangeY(h, midH);

        size[ind].x = 1f / animation.get(ind).getBaseSizeX();
        size[ind].y = 1f / animation.get(ind).getBaseSizeY();

        if(segment.scale != null) {
            float s = segment.scale.getRangeF(w);
            size[ind].x *= s;
            size[ind].y *= s;
        }

        if(segment.scaleX != null)
            size[ind].x *= segment.scaleX.getRangeF(w);

        if(segment.scaleY != null)
            size[ind].y *= segment.scaleY.getRangeF(w);

        if(segment.opacity != null)
            opacity[ind] = (int) (segment.opacity.getRangeF(w));

        zOrder[ind] = segment.zOrder.isFront();

        if(velocity != null) {
            if(segment.startVelocityX != null && segment.velocityX == null)
                velocity[ind].x = segment.startVelocityX.getRangeF(w);
            else if(segment.velocityX != null)
                velocity[ind].x = segment.velocityX.getRangeF(w);
            else
                velocity[ind].x = 0;

            if(segment.startVelocityY != null && segment.velocityY == null)
                velocity[ind].y = segment.startVelocityY.getRangeF(w);
            else if(segment.velocityY != null)
                velocity[ind].y = segment.velocityY.getRangeF(w);
            else
                velocity[ind].y = 0;
        } else if(v != null && moveAngle != null) {
            if(segment.moveAngle != null)
                moveAngle[ind] = segment.moveAngle.getRangeF(w);
            else
                moveAngle[ind] = 0;

            if(segment.velocity != null)
                v[ind] = segment.velocity.getRangeI(w);
            else
                v[ind] = 0;
        }

        if(segment.lifeTime != null) {
            int l = segment.lifeTime.getAnimFrame(animation.get(ind));

            lifeTime[ind] = Math.max(0,l - time);
            if(lifeTime[ind] == 0)
                lifeTime[ind] = -1;
        }

        if(segment.destroyLeft != null)
            destroyLeft[ind] = (int) segment.destroyLeft.getRangeX(w);

        if(segment.destroyTop != null)
            destroyTop[ind] = (int) segment.destroyTop.getRangeY(h, midH);

        if(segment.destroyRight != null)
            destroyRight[ind] = (int) segment.destroyRight.getRangeX(w);

        if(segment.destroyBottom != null)
            destroyBottom[ind] = (int) segment.destroyBottom.getRangeY(h, midH);

        if(segment.angleVelocity != null)
            angleVelocity[ind] = segment.angleVelocity.getRangeF(w);
    }

    public void release() {
        animation.clear();

        for (BGEffectAnim anim : anims)
            anim.unload();
        count = 0;
    }
}
