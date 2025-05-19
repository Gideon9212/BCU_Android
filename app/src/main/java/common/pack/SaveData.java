package common.pack;

import common.io.json.JsonClass;
import common.io.json.JsonDecoder.OnInjected;
import common.io.json.JsonField;
import common.util.stage.Stage;
import common.util.stage.StageMap;
import common.util.stage.info.CustomStageInfo;
import common.util.unit.*;
import common.util.unit.rand.UREnt;

import java.util.*;

@JsonClass
public class SaveData {

    public final PackData.UserPack pack;
    public final TreeMap<AbUnit, Integer> ulkUni = new TreeMap<>();
    @JsonField(generic = { StageMap.class, Integer.class }, alias = Identifier.class)
    public HashMap<StageMap, Integer> cSt = new HashMap<>();//Integer points the number of stages cleared in the map

    public SaveData(PackData.UserPack pack) {
        this.pack = pack;
    }

    public Collection<?>[] validClear(Stage st) {
        Collection<?>[] flags = new Collection<?>[3];
        ArrayDeque<Form> newForms = new ArrayDeque<>();
        ArrayDeque<Boolean> bForms = new ArrayDeque<>();

        if (st.info instanceof CustomStageInfo)
            for (Form reward : ((CustomStageInfo)st.info).rewards) {
                Integer ind = ulkUni.get(reward.unit);
                if (ind == null || ind < reward.fid) {
                    ulkUni.put(reward.unit, reward.fid);
                    newForms.add(reward);
                    bForms.add(ind != null);
                }
            }

        LinkedList<StageMap> newMaps = new LinkedList<>();
        Integer clm = cSt.get(st.getCont());
        if (clm == null)
            if (st.getCont().unlockReq.isEmpty())
                cSt.put(st.getCont(), clm = 1);
            else
                clm = -1;
        else if (clm == st.id())
            cSt.replace(st.getCont(), ++clm);

        if (clm == st.getCont().list.size()) {//StageMap fully cleared
            for (StageMap sm : pack.mc.maps)
                if (!unlocked(sm)) {
                    boolean addable = true;
                    for (StageMap smp : sm.unlockReq)
                        if (!clear(smp)) {
                            addable = false; //Verify if map is there AND cleared first before adding
                            break;
                        }
                    if (addable) {
                        cSt.put(sm, 0);
                        newMaps.add(sm);
                    }
                }
            for (PackData.UserPack pac : UserProfile.getUserPacks()) {
                if (!pac.syncPar.contains(pack.getSID()))
                    continue;
                for (StageMap sm : pac.mc.maps)
                    if (!pac.save.unlocked(sm)) {
                        boolean addable = true;
                        for (StageMap smp : sm.unlockReq)
                            if (!clear(smp)) {
                                addable = false;
                                break;
                            }
                        if (addable) {
                            pac.save.cSt.put(sm, 0);
                            newMaps.add(sm);
                        }
                    }
            }
        }
        flags[0] = newForms;
        flags[1] = bForms;
        flags[2] = newMaps;
        return flags;
    }

    public boolean locked(AbForm f) {
        if (pack.syncPar.contains(f.getID().pack)) {
            if (!UserProfile.getUserPack(f.getID().pack).save.locked(f))
                return false;
        } else if (f.getID().pack.equals(Identifier.DEF))
            for (String par : pack.syncPar)
                if (!UserProfile.getUserPack(par).save.locked(f))
                    return false;
        return (!pack.defULK.containsKey(f.unit()) || pack.defULK.get(f.unit()) < f.getFid()) &&
                (!ulkUni.containsKey(f.unit()) || ulkUni.get(f.unit()) < f.getFid());
    }

    /**
     * Required chapters still left to unlock a chapter
     * @param sm The chapter
     * @return All uncleared but required chapters, empty list if unlocked
     */
    public LinkedList<StageMap> requirements(StageMap sm) {
        LinkedList<StageMap> cl = new LinkedList<>();
        if (unlocked(sm))
            return cl; //Chapter is unlocked
        for (StageMap lsm : sm.unlockReq)
            if (!clear(lsm))
                cl.add(lsm); //A requirement chapter is uncleared, add
        return cl;
    }

    /**
     * A chapter is locked, but close to unlocking
     * @param sm the chapter
     * @return True if all chapter requirements are unlocked, but this chapter isn't
     */
    public boolean nearUnlock(StageMap sm) {
        if (unlocked(sm))
            return false; //Chapter is unlocked
        for (StageMap lsm : sm.unlockReq)
            if (!unlocked(lsm))
                return false; //A requirement chapter is locked
        return true;
    }

    @JsonField(tag = "pack", io = JsonField.IOType.W)
    public String zser() {
        return pack.desc.id;
    }

    /**
     * Mostly used to lock units, and for startup
     */
    public void resetUnlockedUnits() {
        ulkUni.clear();
        for (StageMap sm : cSt.keySet())
            for (int i = 0; i < Math.min(cSt.get(sm), sm.list.size()); i++)
                if (sm.list.get(i).info instanceof CustomStageInfo)
                    for (Form reward : ((CustomStageInfo)sm.list.get(i).info).rewards) {
                        Integer ind = ulkUni.get(reward.unit);
                        if (ind == null || ind < reward.fid)
                            ulkUni.put(reward.unit, reward.fid);
                    }
    }

    public Stage unlockedAt(Form f) {
        if (pack.defULK.getOrDefault(f.unit, -1) >= f.fid)
            return null;
        return unlockedAtR(f, pack);
    }

    private Stage unlockedAtR(Form f, PackData.UserPack p) {
        for (String s : p.syncPar) {
            Stage st = unlockedAtR(f, UserProfile.getUserPack(s));
            if (st != null)
                return st;
        }
        for (int i = 0; i < p.mc.si.size(); i++) {
            CustomStageInfo csi = p.mc.si.get(i);
            for (Form ff : csi.rewards)
                if (f.unit == ff.unit && ff.fid >= f.fid)
                    return csi.st;
        }
        return null;
    }

    public boolean encountered(Enemy e) {
        for (StageMap sm : pack.mc.maps) {
            if (!unlocked(sm))
                continue;
            int st = Math.min(cSt.getOrDefault(sm, 0), sm.list.size()-1);
            for (int i = 0; i <= st; i++)
                if (sm.list.get(i).contains(e))
                    return true;
        }
        return false;
    }

    public HashMap<AbForm, Stage> getUnlockedsBeforeStage(Stage st, boolean includeRandom) {
        HashMap<AbForm, Stage> ulK = new HashMap<>();
        for (int i = st.id() - 1; i >= 0; i--)
            if (st.getCont().list.get(i).info instanceof CustomStageInfo) {
                CustomStageInfo csi = (CustomStageInfo)st.getCont().list.get(i).info;
                for (Form f : csi.rewards)
                    for (int j = f.fid; j >= 0; j--)
                        ulK.put(f.unit.forms[j], st.getCont().list.get(i));
            }
        for (StageMap uchp : st.getCont().unlockReq)
            locRec(ulK, uchp);

        addPreUnlocks(ulK, pack);
        if (includeRandom)
            getRandsRec(ulK, pack);
        return ulK;
    }

    private static void addPreUnlocks(HashMap<AbForm, Stage> ulK, PackData.UserPack pk) {
        for (Map.Entry<AbUnit, Integer> u : pk.defULK.entrySet())
            for (int i = 0; i <= u.getValue(); i++)
                ulK.put(u.getKey().getForms()[i], null);
        for (String s : pk.syncPar)
            addPreUnlocks(ulK, UserProfile.getUserPack(s));
    }

    private static void locRec(HashMap<AbForm, Stage> ulK, StageMap chp) {
        for (int i = chp.list.size() - 1; i >= 0; i--)
            if (chp.list.get(i).info instanceof CustomStageInfo) {
                CustomStageInfo csi = (CustomStageInfo)chp.list.get(i).info;
                for (Form f : csi.rewards)
                    for (int j = f.fid; j >= 0; j--)
                        ulK.put(f.unit.forms[j], chp.list.get(i));
            }
        for (StageMap uchp : chp.unlockReq)
            locRec(ulK, uchp);
    }
    private static void getRandsRec(HashMap<AbForm, Stage> ulK, PackData.UserPack pk) {
        for (UniRand rand : pk.randUnits)
            getRandRec(ulK, rand);
        for (String par : pk.syncPar)
            getRandsRec(ulK, UserProfile.getUserPack(par));
    }
    private static void getRandRec(HashMap<AbForm, Stage> ulK, UniRand r) {
        boolean add = true;
        for (UREnt e : r.list) {
            if (e.ent instanceof UniRand)
                getRandRec(ulK, (UniRand) e.ent);
            if (!ulK.containsKey(e.ent)) {
                add = false;
                break;
            }
        }
        if (add)
            ulK.put(r, null);
    }

    public boolean unlocked(StageMap sm) {
        if (sm.unlockReq.isEmpty())
            return true;
        if (sm.getID().pack.equals(pack.getSID()))
            return cSt.containsKey(sm);
        return sm.getCont().getSave(true).cSt.containsKey(sm);
    }
    public boolean unlocked(Stage st) {
        if (!unlocked(st.getCont()))
            return false;
        if (st.getID().pack.equals(pack.getSID()))
            return cSt.containsKey(st.getCont());
        return st.getMC().getSave(true).cSt.getOrDefault(st.getCont(), 0) >= st.id();
    }
    public boolean clear(StageMap sm) {
        if (sm.getID().pack.equals(pack.getSID()))
            return cSt.getOrDefault(sm, -1) >= sm.list.size();
        return sm.getCont().getSave(true).cSt.getOrDefault(sm, -1) >= sm.list.size();
    }

    @OnInjected //Just like every game ever, update save data if something new is added designed for a point below the one you're at
    public void injected() {
        resetUnlockedUnits();
        for (StageMap sm : pack.mc.maps)
            if (!sm.unlockReq.isEmpty() && !cSt.containsKey(sm)) {
                boolean addable = true;
                for (StageMap smp : sm.unlockReq)
                    if (!clear(smp)) { //Verify if map is there AND cleared first before adding
                        addable = false;
                        break;
                    }
                if (addable)
                    cSt.put(sm, 0);
            }
    }
}
