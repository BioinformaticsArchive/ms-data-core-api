package uk.ac.ebi.pride.utilities.data.exporters;

import org.apache.commons.lang3.StringUtils;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.MzTabControllerImpl;
import uk.ac.ebi.pride.utilities.data.core.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;


/**
 * Class to convert MzTab files to Bed files. Requires chromosome information to be present.
 *
 * @author Tobias Ternent tobias@ebi.ac.uk
 */
public class MzTabBedConverter {

    private MzTabControllerImpl mzTabController;

    /**
     * Constructor to setup conversion of an mzTabFile into a bed file.
     * @param mzTabFile to be converted to a bed file.
     */
    public MzTabBedConverter(MzTabControllerImpl mzTabFile) {
        this.mzTabController = mzTabFile;

    }

    /**
     * Performs the conversion of the mzTabFile into a bed file.
     * @param outputFile is the generated output bed file,
     * @throws Exception
     */
    public void convert(File outputFile) throws Exception {
        FileWriter file = new FileWriter(outputFile.getPath());
        BufferedWriter bf = new BufferedWriter(file);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.setLength(0);
        for (Comparable proteinID : mzTabController.getProteinIds()) {
            Protein protein = mzTabController.getProteinById(proteinID);
            ArrayList<PeptideEvidence> evidences = new ArrayList<>();
            for (Peptide peptide : protein.getPeptides()) {
                for (PeptideEvidence peptideEvidence : peptide.getPeptideEvidenceList()) {
                    if (!evidences.contains(peptide.getPeptideEvidence())) {
                        evidences.add(peptide.getPeptideEvidence());
                        String chrom = "null", chromstart = "null", chromend = "null", strand = "null", mods = "null", prediction = "null",
                                psmScore = "null";
                        for (UserParam userParam : peptideEvidence.getUserParams()) {
                            switch (userParam.getName()) {
                                case ("chr"):
                                    chrom = userParam.getValue();
                                    break;
                                case ("start_map"):
                                    chromstart = userParam.getValue();
                                    break;
                                case ("end_map"):
                                    chromend = userParam.getValue();
                                    break;
                                case ("strand"):
                                    strand = userParam.getValue();
                                    break;
                                default:
                                    break;
                            }
                            for (CvParam cvParam : peptideEvidence.getCvParams()) {
                                if (cvParam.getAccession().equalsIgnoreCase("MS:1002356")) {
                                    psmScore = cvParam.getValue();
                                    break;
                                }
                            }
                        }
                        ArrayList<String> modifications = new ArrayList<>();
                        for (Modification modification : peptideEvidence.getPeptideSequence().getModifications()) {
                            int location = modification.getLocation();
                            for (CvParam cvParam : modification.getCvParams()) {
                                modifications.add(location + "-" + cvParam.getAccession());
                            }
                        }
                        if (modifications.size() > 0) {
                            mods = StringUtils.join(modifications, ", ");
                        }
                        for (UserParam userParam : protein.getDbSequence().getUserParams()) {
                            switch (userParam.getName()) {
                                case ("prediction"):
                                    prediction = userParam.getValue();
                                    break;
                                default:
                                    break;
                            }
                        }
                        if (!chrom.equalsIgnoreCase("null")) {
                            stringBuilder.append(chrom); // chrom
                            stringBuilder.append('\t');
                            stringBuilder.append(chromstart); // chromstart
                            stringBuilder.append('\t');
                            stringBuilder.append(chromend); // chromend
                            stringBuilder.append('\t');
                            stringBuilder.append(strand); // strand
                            stringBuilder.append('\t');
                            stringBuilder.append(protein.getDbSequence().getName()); // protein_name
                            stringBuilder.append('\t');
                            stringBuilder.append(prediction); // prediction
                            stringBuilder.append('\t');
                            stringBuilder.append(peptideEvidence.getPeptideSequence().getSequence());  // peptide_sequence
                            stringBuilder.append('\t');
                            stringBuilder.append(peptideEvidence.getStartPosition()); // pep_start
                            stringBuilder.append('\t');
                            stringBuilder.append(peptideEvidence.getEndPosition()); // pep_end
                            stringBuilder.append('\t');
                            stringBuilder.append(psmScore); // psm_score
                            stringBuilder.append('\t');
                            stringBuilder.append(mods); // modifications
                            stringBuilder.append('\t');
                            stringBuilder.append(peptide.getPrecursorCharge()); // charge
                            stringBuilder.append('\t');
                            stringBuilder.append(peptide.getSpectrumIdentification().getExperimentalMassToCharge()); // exp_mass_to_charge
                            stringBuilder.append('\t');
                            stringBuilder.append(peptide.getSpectrumIdentification().getCalculatedMassToCharge()); // calc_mass_to_charge
                            stringBuilder.append('\n');
                            bf.write(stringBuilder.toString());
                            bf.flush();
                        }
                        stringBuilder.setLength(0);
                    }
                }
            }
        }
        bf.close();
    }

}
