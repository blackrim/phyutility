package jebl.evolution.align.scores;

import java.lang.reflect.Constructor;

public class ScoresFactory {
	
	/**
	 * For any matrix.
	 * 
	 * @param nameVal name and value of the matrix in String form. (eg Blosum45).
	 * @return	substitution matrix of given name.
	 */
	public static Scores generateScores(String nameVal) {
		int i = 0;
		while((int)nameVal.charAt(i) > 64) { //while(charAt(i) is a letter)
			i++;
		}
		Scores sub = null;
		String name = nameVal.substring(0, i);
		String val = nameVal.substring(i, nameVal.length());
		try {
			if(val.indexOf(".") != -1)
				sub = generateScores(name, Float.parseFloat(val));
			else
				sub = generateScores(name, Integer.parseInt(val));
		}
		catch(Exception e) {
			System.out.println("no such substitution matrix!\n" + e);
		}
		return sub;
	}

	/**
	 * For Blosum and Pam matrices
	 * 
	 * @param name "Blosum" or "Pam"
	 * @param val currently 45 - 90 or 100 - 250
	 * @return	substitution matrix given by name and val.
	 */
	public static Scores generateScores(String name, int val) {
		
		Scores sub = null;
		try {
			Class c = Class.forName("jebl.evolution.align.scores." + name + val);
			sub = (Scores)(c.newInstance());
		}
		catch(Exception e) {
			System.out.println("no such substitution matrix!\n" + e);
		}

		return sub;
		
	}
	
	/**
	 * For calculated nucleotide matrices.
	 * 
	 * @param name Currently only JukesCantor
	 * @param val val used to calculate matrix. eg. evolutionary distance d.
	 * @return substitution matrix calculated using val.
	 */
	public static Scores generateScores(String name, float val) {
		
		Scores sub = null;
		try {
			Class c = Class.forName("jebl.evolution.align.scores." + name);
			Constructor con[] = c.getConstructors();
			sub = (Scores)(con[0].newInstance(new Object[] {new Float(val)}));
		}
        
        catch(Exception e) {
			System.out.println("no such substitution matrix!\n" + e);
		}

		return sub;
	}

    public static AminoAcidScores[] getAvailableAminoAcidScores () {
        return new AminoAcidScores[] {new Blosum45(), new Blosum50(), new Blosum55 (), new Blosum60 (),
        new Blosum62 (), new Blosum65 (), new Blosum70 (), new Blosum75 (), new Blosum80 (),
        new Blosum85 (), new Blosum90 (), new Pam100(),new Pam110 (), new Pam120 (),
        new Pam130(),new Pam140 (), new Pam150 (), new Pam160 (), new Pam170 (),
        new Pam180(), new Pam190 (), new Pam200 (), new Pam210 (), new Pam220 (),
        new Pam230 (), new Pam240 (), new Pam250 ()};
    }
    public static NucleotideScores[] getAvailableNucleotideScores () {
        return new NucleotideScores[] {
                new NucleotideScores("51% similarity", 5.0f, -3.0f),
                new NucleotideScores("65% similarity", 5.0f, -4.0f),
               // new NucleotideScores("70% similarity (IUB)", 1.0f, -0.9f),

                // This seems like a bad choice as it implies a very high kappa
               //  new NucleotideScores("93% similarity, Transition/Transversion", 1, -1,-5, 0),

                new NucleotideScores("70% similarity (IUB)", 5 * 1.0f, 5 * -0.9f),

               // new NucleotideScores("93% similarity, Transition/Transversion", 5*1, 5*-1, 5*-5, 0),

              //  new NucleotideScores("88% similarity", 5.0f, -7.2810419984342278f),
                new NucleotideScores("93% similarity", 5.0f, -9.0261674571825044f),


//                new NucleotideScores("Assembly", 10.0f, -9.0f),

/*                new NucleotideScores("Identity",1,0, 0, 0),*/

                /*,new Hamming()*/};
    }
}