package common.system.fake;

public interface FakeGraphics {

	int RED = 0, YELLOW = 1, BLACK = 2, MAGENTA = 3, BLUE = 4, CYAN = 5, WHITE = 6;
	int DEF = 0, TRANS = 1, BLEND = 2, GRAY = 3, MASK = 4;

	void colRect(float x, float y, float w, float h, int r, int g, int b, int a);

	default void delete(FakeTransform at) {

	}

	void drawImage(FakeImage bimg, float x, float y);

	void drawImage(FakeImage bimg, float x, float y, float d, float e);

	void drawLine(float i, float j, float x, float y);

	void drawOval(float i, float j, float k, float l);

	void drawRect(float x, float y, float w, float h);

	void fillOval(float i, float j, float k, float l);

	void fillRect(float x, float y, float w, float h);

	FakeTransform getTransform();

	void gradRect(float x, float y, float w, float h, float a, float b, int[] c, float d, float e, int[] f);

	void gradRectAlpha(float x, float y, float w, float h, float a, float b, int al, int[] c, float d, float e, int al2, int[] f);

	void rotate(float d);

	void scale(float hf, float vf);

	void setColor(int c);

	void setColor(int r, int g, int b);

	void setComposite(int mode, int p0, int p1);

	void setRenderingHint(int key, int object);

	void setTransform(FakeTransform at);

	void translate(float x, float y);

}