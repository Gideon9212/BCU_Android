package common.util.lang;

import common.io.json.JsonClass;
import common.io.json.JsonClass.NoTag;

public class LocaleCenter {

	public interface Binder {

		String getNameID();

		String getNameValue();

		String getTooltipID();

		String getToolTipValue();

		Binder refresh();

		void setNameValue(String str);

		void setToolTipValue(String str);

	}

	public interface Displayable {

		String getName();

		String getTooltip();

		void setName(String str);

		void setTooltip(String str);

	}

	@JsonClass(noTag = NoTag.LOAD)
	public static class DisplayItem implements Displayable {
		public String name, tooltip;

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getTooltip() {
			return tooltip == null ? null : "<html><p width=\"500\">" + tooltip + "</p></html>";
		}

		@Override
		public void setName(String str) {
			name = str;
		}

		@Override
		public void setTooltip(String str) {
			tooltip = str;
		}

	}

	public static class ObjBinder implements Binder {

		public interface BinderFunc {

			Binder refresh(String name);

		}

		public final String name;
		public final Displayable item;
		public final BinderFunc func;

		public ObjBinder(Displayable item, String name, BinderFunc func) {
			this.item = item;
			this.name = name;
			this.func = func;
		}

		@Override
		public String getNameID() {
			return name;
		}

		@Override
		public String getNameValue() {
			return item.getName();
		}

		@Override
		public String getTooltipID() {
			return name;
		}

		@Override
		public String getToolTipValue() {
			return item.getTooltip();
		}

		@Override
		public Binder refresh() {
			return func.refresh(name);
		}

		@Override
		public void setNameValue(String str) {
			item.setName(str);
		}

		@Override
		public void setToolTipValue(String str) {
			item.setTooltip(str);
		}

	}

}
