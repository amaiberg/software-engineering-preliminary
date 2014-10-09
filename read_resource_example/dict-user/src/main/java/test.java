import java.io.*;
public class test {
  public static void main(String args[]) throws Exception {
    // Without the forward slash
    InputStream is = 
	    test.class.getClassLoader().getResourceAsStream("dict.txt");
    BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"));
    System.out.println("Reading the first time:");
    String ln;
    while ((ln = br.readLine()) != null) {
      System.out.println(ln);
    }

    br.close();
    System.out.println("Reading the second time:");
    // With the forward slash
    is = 
	    test.class.getResourceAsStream("/dict.txt");
    br = new BufferedReader(new InputStreamReader(is, "utf-8"));
    while ((ln = br.readLine()) != null) {
      System.out.println(ln);
    }
  }
};
