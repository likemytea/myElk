public class Test {

	public static void main(String[] args) {
		TTTTT t = new TTTTT();
		String result = testString(t, "b", "c", "a", "f", "j");
		System.out.println(result);
	}

	private static String testString(Object... a) {
		String res = null;
		TTTTT tt = (TTTTT) a[0];

		return tt.getAaa() + a[4];
	}

	private static void testQiuyu() {

		long x = 100000;
		for (int i = 0; i < x; i++) {
			if (i % 1000 == 0) {
				System.out.println(i);
			}
		}

	}
}
