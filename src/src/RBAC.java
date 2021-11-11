import java.io.*;
import java.util.*;

public class RBAC {

    public static BufferedReader input;
    public static ArrayList<String> asc = new ArrayList<String>(); //ascendants
    public static ArrayList<String> des = new ArrayList<String>(); //descendents

    public static void main(String[] args) throws IOException {
        ArrayList<String> temp = new ArrayList<String>();
        ArrayList<String> sortedDes = new ArrayList<String>();

        confirm_LRH();
        //putting des into a hashset removes duplicates
        HashSet uniqueDescendants = new HashSet();
        uniqueDescendants.addAll(des);
        // put back into an arraylist and sort them
        sortedDes.addAll(uniqueDescendants);
        Collections.sort(sortedDes);

        System.out.println("Role Hierarchy Top Down");
        for (String sortedDe : sortedDes) {
            for (int j = 0; j < des.size(); j++) {
                if (des.get(j).equals(sortedDe)) {
                    temp.add(asc.get(j));
                }
            }
            System.out.println(sortedDe + "--->" + temp);
            temp.clear();
        }

    }

    public static void confirm_LRH() throws IOException {
        boolean invalid = false;
        ArrayList<String> duplicates = new ArrayList<String>();
        Scanner advance = new Scanner(System.in);
        String divide;
        String compare;

        input = new BufferedReader(new FileReader("roleHierarchy.txt"));

        //split each line of the input file on tab to get asc and des
        while((divide = input.readLine()) != null){
            asc.add(divide.split("\t")[0]);
            des.add(divide.split("\t")[1]);
        }
        input.close();

        //checks if theres a duplicate in the ascendants, if an ascendant
        // has multiple descendants it violates NIST LRH
        for (int i = 0; i < asc.size(); i++) {
            compare = asc.get(i);
            asc.set(i, "~");
            if (asc.contains(compare)){
                invalid = true;
                duplicates.add(compare + " On Line " + i);
            }
            asc.set(i, compare); //restores the asc array to its original form
        }

        if (invalid){
            System.out.println("The following roles and the line they appear on violate " +
                    "limited role hierarchy:\n" + duplicates +"\nThese ascendants have " +
                    "multiple descendants, fix these instances in another\ncommand line " +
                    "and press ENTER to read it again or alternatively restart the program");
            advance.nextLine();
            asc.clear();
            des.clear();
            duplicates.clear();
            confirm_LRH();
        }

    }
}
