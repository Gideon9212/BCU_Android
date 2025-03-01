package common.system.files;

public class VFileRoot extends VFile {

	public VFileRoot(String str) {
		super(str);
	}

	public VFile build(String str, FileData fd) {
		String[] strs = str.split("/|\\\\");
		VFile par = this;
		for (int i = 1; i < strs.length; i++) {
			VFile next = null;
			for (VFile ch : par.list())
				if (ch.name.equals(strs[i]))
					next = ch;
			if (next == null)
				if (i == strs.length - 1)
					if (fd != null)
						return new VFile(par, strs[i], fd);
					else
						return new VFile(par, strs[i]);
				else
					next = new VFile(par, strs[i]);
			if (i == strs.length - 1) {
				if (fd == null)
					return next;
				next.setData(fd);
				return next;
			}
			par = next;
		}
		return null;
	}

	public VFile find(String str) {
		String[] strs = str.split("/|\\\\");
		VFile par = this;
		for (int i = 1; i < strs.length; i++) {
			VFile next = null;
			for (VFile ch : par.list())
				if (ch.name.equals(strs[i]))
					next = ch;
			if (next == null)
				return null;
			if (i == strs.length - 1)
				return next;
			par = next;
		}
		return this;
	}
}
