package it.nextworks.nephele.OFTranslator;

public class Help {
	
	public static void message(){
		
		System.out.println("To print this help message call");
		System.out.println("java -jar Translate.jar help");
		System.out.println("");
		System.out.println("Usage is as follows:");
		System.out.println("");
		System.out.println("java -jar Translate.jar [$PATH] [$podN] [$torN]");
		System.out.println("");
		System.out.println("$PATH is the path of the input.txt file. Default is application directory, in this case " + System.getProperty("user.dir") + ".");
		System.out.println("Relative paths (i.e. in UNIX, those that do not start with \"/\") will also be read as starting in the application directory.");
		System.out.println("");
		System.out.println("Required format of the input.txt file:");
		System.out.println("");
		System.out.println("First five rows must be of the form \"$c = $d\", where $c$ is the name of a topology constant, and $d$ is its value.");
		System.out.println("The constants must be assigne one per line and in the order P,I,W,T,r, hence the first five rows of the file must look like this:");
		System.out.println("");
		System.out.println("P = 8  //number of pods");
		System.out.println("I = 3  //number of planes");
		System.out.println("W = 5  //number of available wavelenghts");
		System.out.println("T = 80 //number of timeslots per working period");
		System.out.println("r = 10 //number of servers per rack");
		System.out.println("");
		System.out.println("Spaces are optional, and everything after the value will be ignored.");
		System.out.println("Subsequent lines contain the flow matrix A, i.e. the output of the offline computation engine, as described in deliverable 5.1 section 10.3.1.2.");
		System.out.println("The entries must be given left-to right, then top-to bottom, as usual. Hence, assuming a 6x8 matrix, it would look like this:");
		System.out.println("");
		System.out.println("1, 2, 3, 4, 5, 6, 7, 8");
		System.out.println("2, 3, 4, 5, 6, 7, 8, 1");
		System.out.println("3, 4, 5, 6, 7, 8, 1, 2");
		System.out.println("4, 5, 6, 7, 8, 1, 2, 3");
		System.out.println("5, 6, 7, 8, 1, 2, 3, 4");
		System.out.println("6, 7, 8, 1, 2, 3, 4, 5");
		System.out.println("");
		System.out.println("In this syntax spaces can be omitted, or they can be used alone, omitting the commas. Both \"2,3\" and \"2 3\" are accepted syntaxes.");
		System.out.println("If the matrix size does not match the parameters, i.e. it is not of size (T*I)x(P*w), an error will be thrown.");
		System.out.println("");
		System.out.println("At last, the (optional) arguments $podN and $torN are used for testing and debugging. A call of the form");
		System.out.println("java -jar Translate.jar debug 1 2");
		System.out.println("or");
		System.out.println("java -jar Translate.jar $PATH 1 2");
		System.out.println("will result in the program compiling the tables and then dumping the flow tables of POD switch 1 and ToR switch 2 in a file called output.txt. The output file will be created (and\\or overwritten) in the working directory if the first argument is debug, or in the same folder as input.txt if a path is specified.");
		System.out.println("POD switches are counted by pod id, then by plane, starting from 0; i.e. pod number P is the first pod of the second plane, while pod number 2*P+1 is the second of the third plane.");
		System.out.println("ToRs are counted starting from 0, by wavelenght, and then by pod; so ToR 0 is under pod 0 and its associated wavelenght is wave number 0, while ToR number W+1 is on wavelenght 1 (the second one) under pod 1 (again, the second one).");
	}
	

}
