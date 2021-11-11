import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

public class RBAC {

    public static BufferedReader input;
    public static ArrayList<String> asc = new ArrayList<String>(); //ascendants
    public static ArrayList<String> des = new ArrayList<String>(); //descendents
    public static ArrayList<String> roles = new ArrayList<String>(); //descendents
    //public static String[][] ROM;
    public static int cols;
    public static int rows;

    public static void main(String[] args) throws IOException {
        ArrayList<String> temp = new ArrayList<String>();
        ArrayList<String> sortedDes = new ArrayList<String>();
        String[][] ROM;
        confirm_LRH();
        //putting des into a hashset removes duplicates
        HashSet rmDupes = new HashSet();
        rmDupes.addAll(des);
        // put back into an arraylist and sort them
        sortedDes.addAll(rmDupes);
        sortedDes = roleSort(sortedDes);

        System.out.println("Role Hierarchy Top Down");
        for (String sortedDe : sortedDes) {
            for (int j = 0; j < des.size(); j++) {
                if (des.get(j).equals(sortedDe)) {
                    temp.add(asc.get(j));
                }
            }
            System.out.println(sortedDe + "--->" + roleSort(temp));
            temp.clear();
        }
        rmDupes.addAll(asc);
        roles.addAll(rmDupes);
        roles = roleSort(roles);
        ROM = roleObjectMatrix();
        addPermissions(ROM);

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

    public static String[][] roleObjectMatrix() throws IOException {
        ArrayList<String> resObj = new ArrayList<String>();
        HashSet duplicates = new HashSet();
        Scanner advance = new Scanner(System.in);
        String obj;
        boolean duplicate = false;

        input = new BufferedReader(new FileReader("resourceObjects.txt"));

        obj = input.readLine();
        resObj.addAll(Arrays.asList(obj.split("\t")));
        for (int i = 0; i < resObj.size(); i++) {
            obj = resObj.get(i);
            resObj.set(i, "~");
            if (resObj.contains(obj)){
                duplicate = true;
                duplicates.add(obj);
                //System.out.println("Duplicate object was found: " + obj);
            }
            resObj.set(i, obj); //restores the res array to its original form
        }
        input.close();
        if (duplicate){
            System.out.println("\nRemove these duplicates in another command line: "
                    + duplicates + "\nPress ENTER to read it again or restart the program");
            advance.nextLine();
            resObj.clear();
            roleObjectMatrix();
        }

        resObj.addAll(0, roles);
        rows = roles.size() + 1;
        cols = resObj.size() + 1;
        String[][] ROM = new String[rows][cols];
        for (int i = 0; i < rows; i++) {
            if (i >= 1){
                ROM[i][0] = roles.get(i-1);
            }
            else {
                ROM[0][0] = "  ";
                for (int j = 0; j < cols; j++) {
                    if (j >= 1){
                        ROM[0][j]= resObj.get(j-1);
                    }
                }
            }
        }
        System.out.println("\n");
        printMatrix(ROM);
        return ROM;
    }

    public static void addPermissions(String[][] ROM) throws IOException {
        String divide;
        String role;
        String object;
        String permission;
        int rowIndex = 0;
        int colIndex = 0;

        input = new BufferedReader(new FileReader("permissionsToRoles.txt"));
        while((divide = input.readLine()) != null){
            //split the input file
            role = (divide.split("\t")[0]);
            permission = (divide.split("\t")[1]);
            object = (divide.split("\t")[2]);
            //find row and col index of the permission
            for (int i = 0; i < rows; i++) {
                if (role.equals(ROM[i][0])){
                    rowIndex = i;
                }
            }
            for (int j = 0; j < cols; j++) {
                if (object.equals(ROM[0][j])){
                    colIndex = j;
                }
            }
            //Prevents adding a permission if the object already has it but adds
            //the permission to any others the object already has

            if (ROM[rowIndex][colIndex] != null && !ROM[rowIndex][colIndex].equals(permission)){
                    ROM[rowIndex][colIndex] = ROM[rowIndex][colIndex] + "\t" + permission;
            }
            else if(ROM[rowIndex][colIndex] == null ){
                ROM[rowIndex][colIndex] = permission;
            }
        }
        input.close();
        printMatrix(ROM);

    }

   /* public static int rowIndex(String title, String[][] ROM){
        int rowIndex = 0;
        for (int i = 0; i < rows; i++) {
            if (title.equals(ROM[i][0])){
                rowIndex = i;
            }
        }
        return rowIndex;
    }
    public static int colIndex(String title, String[][] ROM){
        int colIndex = 0;
        for (int i = 0; i < cols; i++) {
            if (title.equals(ROM[0][i])){
                colIndex = i;
            }
        }
        return colIndex;
    } */

    public static void inherit(String ascendantRole){
        int index = 0;
        if (!asc.contains(ascendantRole)){
            System.out.println("~~~~~~~ NOT AN ASCENDANT ~~~~~~~");
            return;
        }
        for (int i = 0; i < asc.size(); i++) {
            if (asc.get(i).equals(ascendantRole)){
                index = i;
            }
        }


        inherit(des.get(index));

    }

    public static ArrayList<String> roleSort(ArrayList<String> roles){
        Integer[] nums = new Integer[roles.size()];
        for (int i = 0; i < roles.size(); i++) {
            roles.set(i, roles.get(i).replace("R", ""));
            nums[i] = Integer.valueOf(roles.get(i));
        }
        Arrays.sort(nums);
        for (int i = 0; i < nums.length; i++) {
            roles.set(i, "R" + nums[i]);
        }
        return roles;
    }

    public static void printMatrix(String matrix[][])
    {
        List<ArrayList<String>> fullCols= new ArrayList<>();
        ArrayList<Integer> colIndex = new ArrayList<>();
        ArrayList<String> colRights;
        String[] row = new String[cols];
        int mostRights = 0;
        int size;

        matrix[0][0] = "  "; //pads corner cell
        row[0] = addSpaces(5);
        for (int i = 0; i < rows; i++) {
            if(!fullCols.isEmpty()){//means there is one or more objects with multiple permissions
                for (int j = 1; j < mostRights; j++) { //makes sure all permissions are displayed
                    //if there are multiple objects in a row that both have multiple permissions
                    //a blank row is added with permission j in its correct column for each object
                    for (int k = 0; k < fullCols.size(); k++) {
                        colRights = fullCols.get(k);
                        if (colRights.get(j) != null){
                            row[colIndex.get(k)] = colRights.get(j);
                        }
                    }
                    if (j > 1) System.out.println();
                    System.out.print(row[0]); //padding added for column 0
                    //displays the new row with empty space at null entries and the permission under
                    //the objects previous permission
                    for (int k = 1; k < row.length; k++) {
                        if (row[k] == null){
                            System.out.print(addSpaces(10));
                        }
                        else {
                            size = (row[k].length());
                            size = 10 - size;
                            System.out.print(row[k] + addSpaces(size));
                        }
                    }
                }
                mostRights = 0;
                colIndex.clear();
                fullCols.clear();
                Arrays.fill(row, null);
                row[0] = addSpaces(5);
                System.out.println();
            }
            for (int j = 0; j < cols; j++) {
                //top row resource objects
                if(i == 0 && j > 0 && matrix[0][j].length() == 2){
                    System.out.print(matrix[i][j] + "        ");
                }
                //top row resource objects if the object has char length 3
                else if(i == 0 && j > 0 && matrix[0][j].length() == 3){
                    System.out.print(matrix[i][j] + "       ");
                }
                //1st column roles 1-9
                else if (i < 10 && j == 0){
                    System.out.print(matrix[i][j] + "   ");
                }
                //1st column roles >= 10
                else if (j == 0){
                    System.out.print(matrix[i][0] + "  ");
                }
                //Determine padding for a cell that is not null
                else if (matrix[i][j] != null){
                    if (matrix[i][j].contains("\t")){
                        colRights = new ArrayList<>();
                        Collections.addAll(colRights, matrix[i][j].split("\t"));
                        size = (colRights.get(0)).length();
                        size = 10 - size;
                        colIndex.add(j);
                        if (mostRights < colRights.size()){
                            mostRights = colRights.size();
                        }
                        System.out.print(colRights.get(0) + addSpaces(size));
                        fullCols.add(colRights);
                    }
                    else{
                        size = (matrix[i][j]).length();
                        size = 10 - size;
                        System.out.print(matrix[i][j] + addSpaces(size));
                    }

                }
                //Spaces everything else in the table
                else System.out.print(matrix[i][j] + "      ");
            }
            System.out.println();
        }
        System.out.println("\n");
    }

    public static String addSpaces(int num){
        String padding = "";
        for (int k = 0; k < num; k++) {
            padding = " " + padding;
        }
        return padding;
    }


}
