/*
 * GeneticCode.java
 *
 * (c) 2002-2005 JEBL Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */

package jebl.evolution.sequences;

import java.util.*;

/**
 * A set of standard genetic codes.
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 *
 * @version $Id: GeneticCode.java 719 2007-05-31 04:46:40Z matt_kearse $
 */

public final class GeneticCode {
	/**
	 * Standard genetic code tables from GENBANK
	 * Nucleotides go A, C, G, T - Note: this is not the order used by the Genbank web site
	 * With the first codon position most significant (i.e. AAA, AAC, AAG, AAT, ACA, etc.).
     *
     * The codes for the individual amino acids can be found in AminoAcids.java;
     * For example, "*" stands for a stop codon (AminoAcids.STOP_STATE)
	 */
	private static final String[] GENETIC_CODE_TABLES = {
	    // Universal
	    "KNKNTTTTRSRSIIMIQHQHPPPPRRRRLLLLEDEDAAAAGGGGVVVV*Y*YSSSS*CWCLFLF",
	    // Vertebrate Mitochondrial
	    "KNKNTTTT*S*SMIMIQHQHPPPPRRRRLLLLEDEDAAAAGGGGVVVV*Y*YSSSSWCWCLFLF",
	    // Yeast
	    "KNKNTTTTRSRSMIMIQHQHPPPPRRRRTTTTEDEDAAAAGGGGVVVV*Y*YSSSSWCWCLFLF",
	    // Mold Protozoan Mitochondrial
	    "KNKNTTTTRSRSIIMIQHQHPPPPRRRRLLLLEDEDAAAAGGGGVVVV*Y*YSSSSWCWCLFLF",
	    // Mycoplasma
	    "KNKNTTTTRSRSIIMIQHQHPPPPRRRRLLLLEDEDAAAAGGGGVVVV*Y*YSSSSWCWCLFLF",
	    // Invertebrate Mitochondrial
	    "KNKNTTTTSSSSMIMIQHQHPPPPRRRRLLLLEDEDAAAAGGGGVVVV*Y*YSSSSWCWCLFLF",
	    // Ciliate
	    "KNKNTTTTRSRSIIMIQHQHPPPPRRRRLLLLEDEDAAAAGGGGVVVVQYQYSSSS*CWCLFLF",
	    // Echinoderm Mitochondrial
	    "NNKNTTTTSSSSIIMIQHQHPPPPRRRRLLLLEDEDAAAAGGGGVVVV*Y*YSSSSWCWCLFLF",
	    // Euplotid Nuclear
	    "KNKNTTTTRSRSIIMIQHQHPPPPRRRRLLLLEDEDAAAAGGGGVVVV*Y*YSSSSCCWCLFLF",
	    // Bacterial
	    "KNKNTTTTRSRSIIMIQHQHPPPPRRRRLLLLEDEDAAAAGGGGVVVV*Y*YSSSS*CWCLFLF",
	    // Alternative Yeast
	    "KNKNTTTTRSRSIIMIQHQHPPPPRRRRLLSLEDEDAAAAGGGGVVVV*Y*YSSSS*CWCLFLF",
	    // Ascidian Mitochondrial
	    "KNKNTTTTGSGSMIMIQHQHPPPPRRRRLLLLEDEDAAAAGGGGVVVV*Y*YSSSSWCWCLFLF",
	    // Flatworm Mitochondrial
	    "NNKNTTTTSSSSIIMIQHQHPPPPRRRRLLLLEDEDAAAAGGGGVVVVYY*YSSSSWCWCLFLF",
	    // Blepharisma Nuclear
	    "KNKNTTTTRSRSIIMIQHQHPPPPRRRRLLLLEDEDAAAAGGGGVVVV*YQYSSSS*CWCLFLF"
	};

	/**
	 * Names of the standard genetic code tables from GENBANK
	 */
	private static final String[] GENETIC_CODE_NAMES = {
	    "universal", "vertebrateMitochondrial", "yeast", "moldProtozoanMitochondrial",
	    "mycoplasma", "invertebrateMitochondrial", "ciliate", "echinodermMitochondrial",
	    "euplotidNuclear", "bacterial", "alternativeYeast", "ascidianMitochondrial",
	    "flatwormMitochondrial", "blepharismaNuclear"
	};

	/**
	 * Descriptions of the standard genetic code tables from GENBANK
	 */
	private static final String[] GENETIC_CODE_DESCRIPTIONS = {
	    "Universal", "Vertebrate Mitochondrial", "Yeast", "Mold Protozoan Mitochondrial",
	    "Mycoplasma", "Invertebrate Mitochondrial", "Ciliate", "Echinoderm Mitochondrial",
	    "Euplotid Nuclear", "Bacterial", "Alternative Yeast", "Ascidian Mitochondrial",
	    "Flatworm Mitochondrial", "Blepharisma Nuclear"
	};


    public static final GeneticCode UNIVERSAL = new GeneticCode(GeneticCode.UNIVERSAL_ID);
    public static final GeneticCode VERTEBRATE_MT = new GeneticCode(GeneticCode.VERTEBRATE_MT_ID);
    public static final GeneticCode YEAST = new GeneticCode(GeneticCode.YEAST_ID);
    public static final GeneticCode MOLD_PROTOZOAN_MT = new GeneticCode(GeneticCode.MOLD_PROTOZOAN_MT_ID);
    public static final GeneticCode MYCOPLASMA = new GeneticCode(GeneticCode.MYCOPLASMA_ID);
    public static final GeneticCode INVERTEBRATE_MT = new GeneticCode(GeneticCode.INVERTEBRATE_MT_ID);
    public static final GeneticCode CILIATE = new GeneticCode(GeneticCode.CILIATE_ID);
    public static final GeneticCode ECHINODERM_MT = new GeneticCode(GeneticCode.ECHINODERM_MT_ID);
    public static final GeneticCode EUPLOTID_NUC = new GeneticCode(GeneticCode.EUPLOTID_NUC_ID);
    public static final GeneticCode BACTERIAL = new GeneticCode(GeneticCode.BACTERIAL_ID);
    public static final GeneticCode ALT_YEAST = new GeneticCode(GeneticCode.ALT_YEAST_ID);
    public static final GeneticCode ASCIDIAN_MT = new GeneticCode(GeneticCode.ASCIDIAN_MT_ID);
    public static final GeneticCode FLATWORM_MT = new GeneticCode(GeneticCode.FLATWORM_MT_ID);
    public static final GeneticCode BLEPHARISMA_NUC = new GeneticCode(GeneticCode.BLEPHARISMA_NUC_ID);

    public static final GeneticCode[] GENETIC_CODES = {
        UNIVERSAL, VERTEBRATE_MT, YEAST, MOLD_PROTOZOAN_MT, MYCOPLASMA, INVERTEBRATE_MT,
        CILIATE, ECHINODERM_MT, EUPLOTID_NUC, BACTERIAL, ALT_YEAST, ASCIDIAN_MT,
        FLATWORM_MT, BLEPHARISMA_NUC
    };

	private GeneticCode(int geneticCodeId) {

		this.geneticCodeId = geneticCodeId;
		String codeTable = GENETIC_CODE_TABLES[geneticCodeId];
        Map<CodonState, AminoAcidState> translationMap = new TreeMap<CodonState, AminoAcidState>();

        if (codeTable.length() != 64) {
            throw new IllegalArgumentException("Code Table length does not match number of codon states");
        }

        for (int i = 0; i < codeTable.length(); i++) {
            CodonState codonState = Codons.CANONICAL_STATES[i];
            AminoAcidState aminoAcidState = AminoAcids.getState(codeTable.substring(i, i+1));
            translationMap.put(codonState, aminoAcidState);
        }
		translationMap.put(Codons.getGapState(), AminoAcids.getGapState());
		translationMap.put(Codons.getUnknownState(), AminoAcids.getUnknownState());

        this.translationMap = Collections.unmodifiableMap(translationMap);
	}

	/**
	 * Returns the name of the genetic code
	 */
	public String getName() {
		return GENETIC_CODE_NAMES[geneticCodeId];
	}

	/**
	 * Returns the description of the genetic code
	 */
	public String getDescription() {
		return GENETIC_CODE_DESCRIPTIONS[geneticCodeId];
	}

    /**
     * Returns the description of the genetic code
     */
    public String getCodeTable() {
        return GENETIC_CODE_TABLES[geneticCodeId];
    }

	/**
	 * Returns the state associated with AminoAcid represented by codonState.
	 * Note that the state is the canonical state (generated combinatorially)
	 * @see AminoAcids
	 * @see Codons
	 * @return '?' if codon unknown
	 */
	public AminoAcidState getTranslation(CodonState codonState) {
        //System.out.println(codonState.getCode());
        return translationMap.get(codonState);
	}

    /**
	 * Returns the state associated with AminoAcid represented by the three nucleotides.
	 * If one or more of the nucleotides are ambiguous, and all combinations translate to the
     * same protein, then this method will return that protein
	 * @see AminoAcids
	 * @see Codons
	 * @return '?' if codon unknown
	 */
    public AminoAcidState getTranslation(NucleotideState nucleotide1, NucleotideState nucleotide2, NucleotideState nucleotide3){
        CodonState translateState = null;
        if (nucleotide1.isGap() && nucleotide2.isGap() && nucleotide3.isGap()) {
			translateState = Codons.GAP_STATE;
		}

		if (nucleotide1.isAmbiguous() || nucleotide2.isAmbiguous() || nucleotide3.isAmbiguous()) {
            for(State a : nucleotide1.getCanonicalStates()){
                for(State b : nucleotide2.getCanonicalStates()){
                    for(State c : nucleotide3.getCanonicalStates()){
                        //initial setup
                        if(translateState == null)
                            translateState = Codons.getState(a.getCode() + b.getCode() + c.getCode());
                        if(!translationMap.get(translateState).equals(translationMap.get(Codons.getState(a.getCode() + b.getCode() + c.getCode()))))
                            return translationMap.get(Codons.UNKNOWN_STATE);
                    }
                }
            }
            return translationMap.get(translateState);
        }

	    String code = nucleotide1.getCode() + nucleotide2.getCode() + nucleotide3.getCode();
	    translateState = Codons.getState(code);

        return translationMap.get(translateState);
    }

    /**
	 * Returns the state associated with AminoAcid represented by the three nucleotides.
	 * If one or more of the nucleotides are ambiguous, and all combinations translate to the
     * same protein, then this method will return that protein
     * @param nucleotides a string consisting of exactly 3 residues in any case.
	 * @see AminoAcids
	 * @see Codons
	 * @return '?' if codon unknown
	 */
    public AminoAcidState getTranslation(String nucleotides) {
        if (nucleotides.length()!=3) throw new IllegalArgumentException("getTranslation requires a nucleotide triplet. (given "+nucleotides.length()+" characters)");
        NucleotideState n1=Nucleotides.getState(nucleotides.charAt(0));
        NucleotideState n2=Nucleotides.getState(nucleotides.charAt(1));
        NucleotideState n3=Nucleotides.getState(nucleotides.charAt(2));
        return getTranslation(n1,n2,n3);
    }

    /**
     * Returns true if this is a start codon in this genetic code.
     * WARNING: I don't know how to implement this. It only returns true
     * for ATG currently. But according to Wikipedia, about 23% of E.Coli
     * start codons are not ATG!
     * @param codonState
     * @return true if this is a start codon.
     */
    public boolean isStartCodon(CodonState codonState) {
        if (codonState==null) return false; // to handle codons generated from ambiguous residues where it returns null from Codons.getState()
        return codonState.getCode().equals("ATG");
    }

    /**
	 * Note that the state is the canonical state (generated combinatorially)
     * @return whether the codonState is a stop codon
	 */
	public boolean isStopCodon(CodonState codonState) {
        if (codonState==null) return false; // to handle codons generated from ambiguous residues where it returns null from Codons.getState()
		return (translationMap.get(codonState) == AminoAcids.STOP_STATE);
	}

	/**
	 * @return all the possible codons for a given amino acid
	 */
	public Set<CodonState> getCodonsForAminoAcid(AminoAcidState aminoAcidState) {
        Set<CodonState> aaSet = new HashSet<CodonState>();
        for (CodonState state : translationMap.keySet()) {
            if (translationMap.get(state) == aminoAcidState) {
                aaSet.add(state);
            }
        }
        return aaSet;
	}

	/**
	 * @return the codon states of stops.
	 */
	public Set<CodonState> getStopCodons() {
        Set<CodonState> stopSet = new HashSet<CodonState>();
        for (CodonState state : translationMap.keySet()) {
            if (isStopCodon(state)) {
                stopSet.add(state);
            }
        }
        return stopSet;
	}

	/**
	 * Returns the number of terminator amino acids.
	 */
	public int getStopCodonCount() {
        int count = 0;
        for (AminoAcidState state : translationMap.values()) {
            if (state == AminoAcids.STOP_STATE) {
                count++;
            }
        }
		return count;
	}

    private final int geneticCodeId;
	private final Map<CodonState, AminoAcidState> translationMap;

    /**
     * Constants used to refer to the built in code tables
     */
    private static final int UNIVERSAL_ID = 0;
    private static final int VERTEBRATE_MT_ID = 1;
    private static final int YEAST_ID = 2;
    private static final int MOLD_PROTOZOAN_MT_ID = 3;
    private static final int MYCOPLASMA_ID = 4;
    private static final int INVERTEBRATE_MT_ID = 5;
    private static final int CILIATE_ID = 6;
    private static final int ECHINODERM_MT_ID = 7;
    private static final int EUPLOTID_NUC_ID = 8;
    private static final int BACTERIAL_ID = 9;
    private static final int ALT_YEAST_ID = 10;
    private static final int ASCIDIAN_MT_ID = 11;
    private static final int FLATWORM_MT_ID = 12;
    private static final int BLEPHARISMA_NUC_ID = 13;

    public String toString() {
        return getDescription();
    }


}
