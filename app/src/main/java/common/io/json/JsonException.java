package common.io.json;

import java.lang.reflect.Method;

public class JsonException extends Exception {

	private static final long serialVersionUID = 6451473277106188516L;

	public JsonException(Object sus, Exception cause) {
		super(getMain(true, sus), cause.getCause());
		setStackTrace(cause.getCause().getStackTrace()); //Funny MethodInvocationException
	}

	public JsonException(Object sus, Exception cause, Object f, Object elem) {
		super(getMain(true, sus) + " in Field " + f, cause.getCause());
		if (cause.getCause() != null)
			setStackTrace(cause.getCause().getStackTrace()); //Funny MethodInvocationException
		System.out.println("Element causing error below: " + elem); //Keeps walls of text off the UI
	}

	public JsonException(boolean decoding, Object sus, String cause) {
		super(getMain(decoding, sus) + cause);
	}

	public JsonException(boolean decoding, Object fail, String cause, Method m) {
		super(getMain(decoding, fail) + "Function " + m + " on " + m.getDeclaringClass() + ": " + cause);
	}

	public static String getMain(boolean decoding, Object obj) {
		try {
			return "Failed " + (decoding ? "decoding " : "encoding ") + obj + ": ";
		} catch (Exception e) {//Sometimes object.toString does a stupid
			return "Failed " + (decoding ? "decoding " : "encoding ") + obj.getClass() + ": ";
		}
	}
}
