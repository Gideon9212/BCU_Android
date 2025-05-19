package common.util.lang;

import common.CommonStatic;
import common.battle.BasisSet;
import common.io.assets.Admin.StaticPermitted;
import common.io.json.JsonClass;
import common.io.json.JsonEncoder;
import common.io.json.JsonField;
import common.pack.Context.ErrType;
import common.pack.Identifier;
import common.pack.SortedPackSet;
import common.util.Data;
import common.util.Data.Proc;
import common.util.unit.AbEnemy;
import common.util.unit.Trait;
import common.util.unit.Unit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

public class Formatter {
	@JsonClass
	public static class Context {

		public final String left = "(";
		public final String right = ")";
		public final String sqleft = "[";
		public final String sqright = "]";
		public final String crleft = "{";
		public final String crright = "}";

		@JsonField
		public final boolean isEnemy;
		@JsonField
		public final boolean useSecond;
        @JsonField
        public final double[] magnification;
		public final double fruitMag;

		public DecimalFormat df = new DecimalFormat("#.##");

		public Context(boolean ene, boolean sec, double[] magnif, SortedPackSet<Trait> trs) {
			isEnemy = ene;
			useSecond = sec;
			magnification = magnif;

			if (isEnemy || trs == null)
				fruitMag = 1;
			else
				fruitMag = 1 + BasisSet.current().t().getFruit(trs) * 0.2 / 3;
		}

		public String abs(double v) {
			return "" + Math.abs(v);
		}

		public String id(Identifier<?> id) {
			return Identifier.get(id) + "";
		}

		public String dispTimeD(double time) {
			if (useSecond)
				return toSecond((int)time) + "s";
			return (int)time + "f";
		}

		public String dispTime(int time) {
			if (useSecond)
				return toSecond(time) + "s";
			return time + "f";
		}

		public String dispTimeF(int time) {
			String tim = dispTime(time);
			if (fruitMag > 1) {
				if (useSecond)
					return tim + " [" + toSecond((int)(time * fruitMag)) + "s]";
				return tim + " [" + (int)(time * fruitMag) + "f]";
			}
			return tim;
		}

		public String entity(Identifier<?> id, int form) {
			Object obj = Identifier.get(id);
			if (obj instanceof Unit)
				return ((Unit)obj).forms[form - 1].toString();
			return Identifier.get(id) + "";
		}

		public String toSecond(int time) {
			return df.format(time / 30.0);
		}

		public String summonMagnification(boolean fix_buff, int buff, Identifier<?> id) {
			boolean isEnemy = id == null ? this.isEnemy : id.get() instanceof AbEnemy;
			if (!isEnemy) {
				if (fix_buff)
					return "Lv " + buff;
				if (magnification.length == 1)
					return "Lv " + (int) (magnification[0] + buff); //Because entityAbilities is a thing
				return "Lv " + (int) (magnification[1] + buff);
			}
			if (fix_buff)
				return buff + "%";
			if (magnification.length == 1 || magnification[0] == magnification[1])
				return (int) (buff * magnification[0]) + "%";
			return "{" + (int) (buff * magnification[0]) + "%, " + (int) (buff * magnification[1]) + "%}";
		}

		public String shield(int hp) {
			return "" + (int) (hp * magnification[0]);
		}

		public String waveRange(int lv) {
			return df.format((isEnemy ? 467.25 : 332.5) + 200 * (lv - 1));
		}

		public String resDiv(double mult) {
			return df.format(mult / 100.0);
		}

		public String bitMask(int ind) {
			StringBuilder str = new StringBuilder("[");
			for(int i = 0; i < 32; i++) {
				if (((1 << i) & ind) > 0)
					str.append(i).append(", ");
			}
			return str.toString().substring(0, str.length() - 2) + "]";
		}
		public String proc(Proc p) {
			StringBuilder str = new StringBuilder("[");
			for(int i = 0; i < Data.PROC_TOT; i++) {
				Proc.ProcItem item = p.getArr(i);
				if(!item.def_exists())
					continue;
				String format = ProcLang.get().get(i).format;
				String formatted = format(format, item, this);
				str.append(formatted).append(", ");
			}
			return str.toString().substring(0, str.length() - 2) + "]";
		}
		public String toString(Object obj) {
			return obj.toString();
		}
		public boolean strEqual(Object obj, String str) {
			return String.valueOf(obj).equals(str);
		}
	}

	private class BoolElem extends Comp {

		public BoolElem(int start, int end) {
			super(start, end);
		}

		private boolean eval() throws Exception {
			for (int i = 0; i < MATCH.length; i++) {
				for (int j = p0; j < p1 - MATCH[i].length() + 1; j++)
					if (test(i, j)) {
						int fi0 = new IntExp(p0, j).eval();
						int fi1 = new IntExp(j + MATCH[i].length(), p1).eval();
						if (i == 0)
							return fi0 >= fi1;
						if (i == 1)
							return fi0 <= fi1;
						if (i == 2)
							return fi0 == fi1;
						if (i == 3)
							return fi0 != fi1;
						if (i == 4)
							return (fi1 & fi0) > 0;
						if (i == 5)
							return fi0 > fi1;
						return fi0 < fi1;
					}
			}
			Object o = new RefObj(p0, p1).eval();
			if (o instanceof Boolean)
				return (Boolean)o;
			if (o instanceof Collection)
				return !((Collection<?>)o).isEmpty();
			if (o instanceof Proc.ProcID)
				return !((Proc.ProcID)o).isEmpty();
			return o != null;
		}

		private boolean test(int i, int j) {
			for (int k = 0; k < MATCH[i].length(); k++)
				if (str.charAt(j + k) != MATCH[i].charAt(k))
					return false;
			return true;
		}
	}

	private class BoolExp extends Comp {

		private int ind;

		public BoolExp(int start, int end) {
			super(start, end);
			ind = p0;
		}

		private boolean eval() throws Exception {
			Stack<Boolean> stack = new Stack<>();
			stack.push(nextElem());
			while (ind < p1) {
				char ch = str.charAt(ind++);
				if (ch == '&')
					stack.push(stack.pop() & nextElem());
				else if (ch == '|')
					stack.push(nextElem());
				else if (ch == '^')
					stack.push(stack.pop() ^ nextElem());
				else
					throw new Exception("unknown operator " + ch + " at " + (ind - 1) + " (Location: " + str.substring(p0, p1) + ")");
			}
			boolean b = false;
			for (boolean bi : stack)
				b |= bi;
			return b;
		}

		private boolean nextElem() throws Exception {
			char ch = str.charAt(ind);
			boolean neg = ch == '!';
			if (neg)
				ch = str.charAt(++ind);
			if (ch == '!')
				throw new Exception("double ! at " + ind + " (Location: " + str.substring(p0, p1) + ")");
			if (ch == '(') {
				int depth = 1;
				int pre = ++ind;
				while (depth > 0) {
					char chr = str.charAt(ind++);
					if (chr == '(')
						depth++;
					if (chr == ')')
						depth--;
				}
				return neg ^ new BoolExp(pre, ind - 1).eval();
			}
			int pre = ind;
			StringBuilder collect = new StringBuilder();

			collect.append(ch);

			while (ch != '&' && ch != '|' && ch != '^' && ind < p1) {
				ch = str.charAt(++ind);
				collect.append(ch);

				//Check if collected ch is int field
				//If it's int field, then we can pass these three letters specially
				if(ind < p1 && (str.charAt(ind) == '&' || str.charAt(ind) == '|' || str.charAt(ind) == '^') && test(collect.toString(), pre, ind)) {
					ch = str.charAt(++ind);
					collect.append(ch);
				}
			}
			return neg ^ new BoolElem(pre, ind).eval();
		}

		private boolean test(String ch, int i, int j) {
			//Method? Return false
			if(ch.startsWith("_"))
				return false;

			try {
				return new RefField(i, j).test(obj) instanceof Integer;
			} catch (Exception ignored) {
				return false;
			}
		}
	}

	private class Code implements IElem {

		private final BoolExp cond;
		private final Root data;

		private Code(BoolExp c, Root d) {
			cond = c;
			data = d;
		}

		@Override
		public void build(StringBuilder sb) throws Exception {
			if (cond.eval())
				data.build(sb);
		}

	}

	private class CodeBlock extends Cont {

		private CodeBlock(int start, int end) throws Exception {
			super(start, end);
			int i = p0;
			while (i < p1) {
				char ch = str.charAt(i++);
				if (ch == '(') {
					int depth = 1;
					int pre = i;
					while (depth > 0) {
						if (i >= p1)
							throw new Exception("unfinished codeblock at " + i + " (Location: " + str.substring(p0, p1) + ")");
						char chr = str.charAt(i++);
						if (chr == '(')
							depth++;
						if (chr == ')')
							depth--;
					}
					BoolExp cond = new BoolExp(pre, i - 1);
					while (str.charAt(i++) != '{')
						if (i >= p1)
							throw new Exception("unfinished codeblock at " + i + " (Location: " + str.substring(p0, p1) + ")");
					depth = 1;
					pre = i;
					while (depth > 0) {
						if (i >= p1)
							throw new Exception("unfinished codeblock at " + i + " (Location: " + str.substring(p0, p1) + ")");
						char chr = str.charAt(i++);
						if (chr == '{')
							depth++;
						if (chr == '}')
							depth--;
					}
					Root data = new Root(pre, i - 1);
					list.add(new Code(cond, data));
				}
			}
		}

	}

	private abstract class Comp {

		public final int p0, p1;

		public Comp(int start, int end) {
			p0 = start;
			p1 = end;
		}
	}

	private abstract class Cont extends Elem {

		public final List<IElem> list = new ArrayList<>();

		public Cont(int start, int end) {
			super(start, end);
		}

		@Override
		public void build(StringBuilder sb) throws Exception {
			for (IElem e : list)
				e.build(sb);
		}

	}

	private abstract class Elem extends Comp implements IElem {

		public Elem(int start, int end) {
			super(start, end);
		}

	}

	private interface IElem {

		void build(StringBuilder sb) throws Exception;

	}

	private class IntExp extends Comp {

		private int ind;

		public IntExp(int start, int end) {
			super(start, end);
			ind = p0;
		}

		private int eval() throws Exception {
			Stack<Integer> stack = new Stack<>();
			char prevOp = ' ';
			stack.push(nextElem());
			while (ind < p1) {
				char ch = str.charAt(ind++);
				if (ch == '*')
					stack.push(stack.pop() * nextElem());
				else if (ch == '/')
					stack.push(stack.pop() / nextElem());
				else if (ch == '%')
					stack.push(stack.pop() % nextElem());
				else if (ch == '+' || ch == '-') {
					if (prevOp == '*' || prevOp == '/' || prevOp == '%' && stack.size() >= 2) {
						int b = stack.pop();
						int a = stack.pop();
						if (ch == '+')
							stack.push(a + b);
						else
							stack.push(a - b);
					} else if (ch == '+')
						stack.push(stack.pop() + nextElem());
					else
						stack.push(stack.pop() - nextElem());
				} else if (ch == '&') {
					stack.push(stack.pop() & nextElem());
				} else if (ch == '|') {
					stack.push(stack.pop() | nextElem());
				} else if (ch == '^') {
					stack.push(stack.pop() ^ nextElem());
				} else
					throw new Exception("unknown operator " + ch + " at " + (ind - 1) + " (Location: " + str.substring(p0, p1) + ")");
				prevOp = ch;
			}
			if ((prevOp == '+' || prevOp == '-') && stack.size() >= 2) {
				int b = stack.pop();
				int a = stack.pop();
				if (prevOp == '+')
					stack.push(a + b);
				else
					stack.push(a - b);
			}
			return stack.pop();
		}

		private int nextElem() throws Exception {
			char ch = str.charAt(ind);
			int neg = 1;
			if (ch == '-') {
				neg = -1;
				ch = str.charAt(++ind);
			}
			if (ch == '(') {
				int depth = 1;
				int pre = ++ind;
				while (depth > 0) {
					char chr = str.charAt(ind++);
					if (chr == '(')
						depth++;
					if (chr == ')')
						depth--;
				}
				return neg * new IntExp(pre, ind - 1).eval();
			}
			if (ch >= '0' && ch <= '9')
				return neg * readNumber();
			int pre = ind;
			while (ch != '+' && ch != '-' && ch != '*' && ch != '/' && ch != '%' && ch != '&' && ch != '|' && ch != '^' && ind < p1)
				ch = str.charAt(++ind);
			Object n = new RefObj(pre, ind).eval();
			if (n instanceof Integer)
				return neg * (Integer) new RefObj(pre, ind).eval();
			if (n instanceof Double)
				return (int) (neg * (Double) new RefObj(pre, ind).eval());
			if (n instanceof Enum)
				return neg * ((Enum<?>)new RefObj(pre, ind).eval()).ordinal();
			return (int) (neg * (Float) new RefObj(pre, ind).eval());
		}

		private int readNumber() {
			int ans = 0;
			while (ind < p1) {
				char chr = str.charAt(ind);
				if (chr < '0' || chr > '9')
					break;
				ind++;
				ans = ans * 10 + chr - '0';
			}
			return ans;
		}

	}

	private abstract class RefElem extends Comp {

		public RefElem(int start, int end) {
			super(start, end);
		}

		public abstract Object eval(Object parent) throws Exception;
	}

	private class RefField extends RefElem {

		public RefField(int start, int end) {
			super(start, end);
		}

		@Override
		public Object eval(Object parent) throws Exception {
			if (str.charAt(p0) == '_' && parent != null)
				throw new Exception("global only allowed for bottom level" + " (Location: " + str.substring(p0, p1) + ")");
			if (parent == null)
				if (str.charAt(p0) == '_')
					parent = ctx;
				else
					parent = obj;
			int ind = str.charAt(p0) == '_' || str.charAt(p0) == '`' ? p0 + 1 : p0;
			String name = str.substring(ind, p1);
			if (str.charAt(p0) == '`')
				return name;
			try {
				Field f = parent.getClass().getField(name);
				return f.get(parent);
			} catch (NoSuchFieldException nse) {
				if (CommonStatic.parseIntsN(name).length > 0)
					return new IntExp(ind, p1).eval();
				throw new Exception("Unrecognized field: " + name + " (Location: " + str.substring(p0, p1) + ")");
			}
		}

		public Object test(Object parent) {
			if (parent == null)
				if (str.charAt(p0) == '_')
					parent = ctx;
				else
					parent = obj;

			int ind = str.charAt(p0) == '_' || str.charAt(p0) == '`' ? p0 + 1 : p0;
			String name = str.substring(ind, p1);
			if (str.charAt(p0) == '`')
				return name;
			try {
				return parent.getClass().getField(name).get(parent);
			} catch (NoSuchFieldException | IllegalAccessException nse) {
				return null;
			}
		}
	}

	private class RefFunc extends RefElem {

		private final List<RefObj> list = new ArrayList<>();

		public RefFunc(int start, int end) {
			super(start, end);
		}

		@Override
		public Object eval(Object parent) throws Exception {
			if (str.charAt(p0) == '_' && parent != null)
				throw new Exception("global only allowed for bottom level: at " + p0 + " (Location: " + str.substring(p0, p1) + ")");
			if (parent == null)
				if (str.charAt(p0) == '_')
					parent = ctx;
				else
					parent = obj;
			String name = str.substring(str.charAt(p0) == '_' ? p0 + 1 : p0, p1);
			Method[] ms = parent.getClass().getMethods();
			Object[] args = new Object[list.size()];
			for (int i = 0; i < args.length; i++)
				args[i] = list.get(i).eval();
			for (Method m : ms)
				if (m.getName().equals(name) && m.getParameterTypes().length == list.size())
					return m.invoke(parent, args);
			throw new Exception("function " + name + " not found for class " + parent.getClass() + " (Location: " + str.substring(p0, p1) + ")");
		}
	}

	private class RefObj extends Elem {

		private final List<RefElem> list = new ArrayList<>();

		private RefObj(int start, int end) throws Exception {
			super(start, end);
			int pre = p0, i = p0;
			while (i < p1) {
				char ch = str.charAt(i++);
				if (ch == '.') {
					list.add(new RefField(pre, i - 1));
					pre = i;
				}
				if (ch == '(') {
					RefFunc func = new RefFunc(pre, i - 1);
					pre = i;
					int depth = 1;
					while (depth > 0) {
						if (i >= p1)
							throw new Exception("unfinished RefBlock at " + i + " (Location: " + str.substring(p0, p1) + ")");
						char chr = str.charAt(i++);
						if (chr == '(')
							depth++;
						if (chr == ')') {
							depth--;
							if (depth == 0) {
								if (i - 1 > pre)
									func.list.add(new RefObj(pre, i - 1));
								pre = i;
							}
						}
						if (chr == ',' && depth == 1) {
							func.list.add(new RefObj(pre, i - 1));
							pre = i;
						}
					}
					list.add(func);
				}
			}
			if (pre < p1)
				list.add(new RefField(pre, p1));
		}

		@Override
		public void build(StringBuilder sb) throws Exception {
			sb.append(eval());
		}

		private Object eval() throws Exception {
			Object obj = null;
			for (RefElem e : list) {
				obj = e.eval(obj);
			}
			return obj;
		}

	}

	private class Root extends Cont {

		private Root(int start, int end) throws Exception {
			super(start, end);
			int pre = p0;
			int deepth = 0;
			for (int i = p0; i < p1; i++) {
				char ch = str.charAt(i);
				if (ch == '[') {
					if (deepth == 0 && i > pre) {
						list.add(new TextRef(pre, i));
						pre = i + 1;
					}
					deepth++;
				}
				if (ch == ']') {
					deepth--;
					if (deepth == 0 && i > pre) {
						list.add(new CodeBlock(pre, i));
						pre = i + 1;
					}
				}
			}
			if (pre < p1)
				list.add(new TextRef(pre, p1));
		}

	}

	private class TextPlain extends Elem {

		private TextPlain(int start, int end) {
			super(start, end);
		}

		@Override
		public void build(StringBuilder sb) {
			sb.append(str, p0, p1);
		}

	}

	private class TextRef extends Cont {

		private TextRef(int start, int end) throws Exception {
			super(start, end);
			int pre = p0;
			int depth = 0;
			for (int i = p0; i < p1; i++) {
				char ch = str.charAt(i);
				if (ch == '(') {
					if (depth == 0 && i > pre)
						list.add(new TextPlain(pre, i));
					if (depth == 0)
						pre = i + 1;
					depth++;
				}
				if (ch == ')') {
					depth--;
					if (depth == 0 && i > pre)
						list.add(new RefObj(pre, i));
					if (depth == 0)
						pre = i + 1;
				}
			}
			if (pre < p1)
				list.add(new TextPlain(pre, p1));
		}

	}

	@StaticPermitted
	private static final String[] MATCH = { ">=", "<=", "==", "!=", "#", ">", "<" };

	public static String format(String pattern, Object obj, Object ctx) {
		StringBuilder sb = new StringBuilder();
		try {
			Formatter f = new Formatter(pattern, obj, ctx);
			f.root.build(sb);
		} catch (Exception e) {
			CommonStatic.ctx.noticeErr(e, ErrType.ERROR,
					"err during formatting " + pattern + " with " + JsonEncoder.encode(obj));
		}
		return sb.toString();
	}

	private final String str;
	private final Object obj;
	private final Object ctx;
	private final Root root;

	private Formatter(String pattern, Object object, Object context) throws Exception {
		str = pattern;
		obj = object;
		ctx = context;
		String err = check();
		if (err != null)
			throw new Exception(err);
		root = new Root(0, str.length());
	}

	private String check() {
		Stack<Integer> stack = new Stack<>();
		for (int i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);
			if (ch == '(')
				stack.push(0);
			if (ch == '[')
				stack.push(1);
			if (ch == '{')
				stack.push(2);
			if (ch == ')' && (stack.isEmpty() || stack.pop() != 0))
				return "bracket ) unexpected at " + i + " (Location: " + getLoc(i) + ")";
			if (ch == ']' && (stack.isEmpty() || stack.pop() != 1))
				return "bracket ] unexpected at " + i + " (Location: " + getLoc(i) + ")";
			if (ch == '}' && (stack.isEmpty() || stack.pop() != 2))
				return "bracket } unexpected at " + i + " (Location: " + getLoc(i) + ")";
		}
		return stack.isEmpty() ? null : "unenclosed bracket: " + stack;
	}

	private String getLoc(int i) {
		return str.substring(Math.max(0, i - 15), Math.min(i + 15, str.length()));
	}

}