package common.io;

import common.CommonStatic;
import common.io.assets.AssetLoader;
import common.pack.Context;
import common.pack.PackData;
import org.jetbrains.annotations.NotNull;
import common.system.DateComparator;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class Backup {
    public static void createBackup(@Nullable Consumer<Double> prog, @NotNull List<File> files) {
        if(CommonStatic.getConfig().maxBackup != 0 && backups.size() > CommonStatic.getConfig().maxBackup) {
            int i = backups.size();

            while(i > CommonStatic.getConfig().maxBackup) {
                backups.get(backups.size() - 1).delete();
                i--;
            }
        }

        String fileName = getTimeStamp();

        File folder = CommonStatic.ctx.getBackupFile("");

        boolean r = CommonStatic.ctx.noticeErr(() -> {
            if(!folder.exists() && !folder.mkdirs()) {
                throw new IOException("Couldn't create backup folder");
            }
        }, Context.ErrType.WARN, "Failed to create backup folder");

        if(!r)
            return;

        File dst = CommonStatic.ctx.getBackupFile(fileName+".backup.bcuzip");

        boolean result = CommonStatic.ctx.noticeErr(() -> {
            if(!dst.exists() && !dst.createNewFile()) {
                throw new IOException("Couldn't create backup file : "+fileName+".backup.bcuzip");
            }
        }, Context.ErrType.WARN, "Failed to create backup file");

        if(!result)
            return;

        PackData.PackDesc desc = new PackData.PackDesc("backup_"+fileName);
        desc.author = CommonStatic.ctx.getAuthor();
        desc.BCU_VERSION = AssetLoader.CORE_VER;

        CommonStatic.ctx.noticeErr(() -> PackLoader.writePackWithSpecificFiles(dst, files, desc, "backup", prog), Context.ErrType.WARN, "Failed to create backup file");
    }

    public static void loadBackups() {
        File backupFolder = CommonStatic.ctx.getBackupFile("");

        if(!backupFolder.exists())
            return;

        File[] list = backupFolder.listFiles();

        if(list == null)
            return;

        for(File b : list) {
            if(b != null && hasValidFormat(b)) {
                Backup backup = new Backup(b);

                if(backup.load())
                    backups.add(backup);
            }
        }

        backups.sort(new DateComparator());
    }

    public static Backup checkRestore() {
        if(CommonStatic.getConfig().backupFile == null)
            return null;
        else {
            for(Backup b : backups) {
                if (b.getName().equals(CommonStatic.getConfig().backupFile)) {
                    CommonStatic.getConfig().backupFile = null;
                    return b;
                }
            }
        }

        CommonStatic.getConfig().backupFile = null;

        return null;
    }

    private static boolean hasValidFormat(File f) {
        String name = f.getName();

        if(!name.endsWith(".backup.bcuzip"))
            return false;

        String[] dates = name.split("-");

        return dates.length == 6;
    }

    private static String getTimeStamp() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        Date date = new Date();

        return df.format(date);
    }

    public static List<Backup> backups = new ArrayList<>();

    @NotNull
    private final File f;
    public PackLoader.ZipDesc backup;

    public Backup(@NotNull File f) {
        this.f = f;
    }

    public boolean load() {
        return CommonStatic.ctx.noticeErr(() -> {
            backup = PackLoader.readPack(f);
        }, Context.ErrType.WARN, "Failed to load "+f.getName()+" backup file (This corrupted backup will be deleted)", this::delete);
    }

    public boolean safeDelete() {
        return f.delete();
    }

    public boolean delete() {
        backups.remove(this);

        return f.delete();
    }

    public String getName() {
        String[] date = f.getName().replace(".backup.bcuzip", "").split("-");

        return date[0] + "-" +
                date[1] +
                "-" +
                date[2] +
                "/" +
                date[3] +
                ":" +
                date[4] +
                ":" +
                date[5];
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        Backup backup = (Backup) o;

        return f.equals(backup.f);
    }

    @Override
    public int hashCode() {
        return Objects.hash(f);
    }
}
