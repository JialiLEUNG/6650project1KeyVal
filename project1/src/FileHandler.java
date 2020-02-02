
public class FileHandler {
    public static void main(String[] args) throws Exception {
        System.out.println("Reading a text file line by line: ");

        Scanner sc = new Scanner(new File("file.txt"));
        while (sc.hasNext()) {
            String str = sc.nextLine();
            System.out.println(str); }
        sc.close();

    }
}
