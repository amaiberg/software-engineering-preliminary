import java.io.*;
public class test {
  public static void main(String args[]) throws Exception {
    InputStream is = 
	    test.class.getClassLoader().getResourceAsStream("dict.txt");
    BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"));
    String ln;
    while ((ln = br.readLine()) != null) {
      System.out.println(ln);
    }
  }
};
