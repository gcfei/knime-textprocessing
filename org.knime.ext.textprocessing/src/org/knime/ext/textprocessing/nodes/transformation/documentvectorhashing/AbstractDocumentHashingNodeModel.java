package org.knime.ext.textprocessing.nodes.transformation.documentvectorhashing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.collection.CollectionCellFactory;
import org.knime.core.data.collection.ListCell;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortType;
import org.knime.core.node.streamable.InputPortRole;
import org.knime.core.node.streamable.OutputPortRole;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.data.Paragraph;
import org.knime.ext.textprocessing.data.Section;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.util.BagOfWordsDataTableBuilder;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;
import org.knime.ext.textprocessing.util.DocumentDataTableBuilder;

/**
 * @author Julian
 *
 */
public abstract class AbstractDocumentHashingNodeModel extends NodeModel {

    /**
     * The default document column to use.
     */
    public static final String DEFAULT_DOCUMENT_COLNAME = BagOfWordsDataTableBuilder.DEF_ORIG_DOCUMENT_COLNAME;

    /**
     * The default value to the as collection flag.
     */
    public static final boolean DEFAULT_ASCOLLECTION = false;

    private int m_documentColIndex = -1;

    private static final DoubleCell DEFAULT_CELL = new DoubleCell(0.0);

    private final SettingsModelString m_docCol = DocumentHashingNodeDialog2.getDocumentColModel();

    private final SettingsModelBoolean m_asCol = DocumentHashingNodeDialog2.getAsCollectionModel();

    private int m_seed;

    private int m_dim;

    private String m_hashFunc;

    private String m_vectVal;

    private InputPortRole[] m_inPortRoles;

    private OutputPortRole[] m_outPortRoles;

    /**
     * @param inPortTypes
     * @param outPortTypes
     * @param streamableInportIdx
     * @param streamableOutportIdx
     */
    public AbstractDocumentHashingNodeModel(final PortType[] inPortTypes,
        final PortType[] outPortTypes, final int streamableInportIdx, final int streamableOutportIdx) {
        super(inPortTypes, outPortTypes);
        m_inPortRoles = new InputPortRole[inPortTypes.length];
        m_outPortRoles = new OutputPortRole[outPortTypes.length];
        Arrays.fill(m_inPortRoles, InputPortRole.NONDISTRIBUTED_NONSTREAMABLE);
        Arrays.fill(m_outPortRoles, OutputPortRole.NONDISTRIBUTED);
        m_inPortRoles[streamableInportIdx] = InputPortRole.DISTRIBUTED_STREAMABLE;
        m_outPortRoles[streamableOutportIdx] = OutputPortRole.DISTRIBUTED;
    }

    /**
     * @param spec
     * @return
     * @throws InvalidSettingsException
     */
    protected ColumnRearranger createColumnRearranger(final DataTableSpec spec) throws InvalidSettingsException {
        final DataTableSpecVerifier verifier = new DataTableSpecVerifier(spec);
        verifier.verifyMinimumDocumentCells(1, true);

        m_documentColIndex = spec.findColumnIndex(m_docCol.getStringValue());
        if (m_documentColIndex < 0) {
            throw new InvalidSettingsException(
                "Index of specified document column is not valid! " + "Check your settings!");
        }

        final ColumnRearranger rearranger = new ColumnRearranger(spec);

        DataColumnSpec[] specs = m_asCol.getBooleanValue() ? createSpecAsCollection() : createSpecAsColumns();
        rearranger.append(new AbstractCellFactory(specs) {

            private final int m_idx = spec.findColumnIndex(m_docCol.getStringValue());

            @Override
            public DataCell[] getCells(final DataRow row) {
                final DocumentValue doc = (DocumentValue)row.getCell(m_idx);
                return createVector(m_dim, doc.getDocument());
            }

        });

        return rearranger;
    }

    private DataCell[] createVector(final int dim, final Document doc) {
        Double totalTerms = 0.0;
        final HashingFunction hashFunction = HashingFunctionFactory.getInstance().getHashFunction(m_hashFunc);

        List<Integer> output = new ArrayList<Integer>(Collections.nCopies(dim, 0));
        List<Integer> occupiedIndexes = new ArrayList<Integer>();

        for (final Section s : doc.getSections()) {
            for (final Paragraph p : s.getParagraphs()) {
                final List<Sentence> sentences = p.getSentences();
                for (final Sentence sentence : sentences) {
                    final List<Term> terms = sentence.getTerms();
                    for (final Term term : terms) {
                        int idx = hashFunction.hash(term.getText(), m_seed) % dim;

                        if (idx < 0) {
                            idx += dim;
                        }
                        output.set(idx, output.get(idx) + 1);
                        if (!occupiedIndexes.contains(idx)) {
                            occupiedIndexes.add(idx);
                        }
                    }
                }
            }
        }
        for (int index : occupiedIndexes) {
            totalTerms += output.get(index);
        }
        DataCell[] cells;
        final DoubleCell zero = new DoubleCell(0);
        final DoubleCell one = new DoubleCell(1);

        if (m_asCol.getBooleanValue()) {
            List<DoubleCell> featureVector = initFeatureVector(dim);
            cells = new DataCell[1];

            if (m_vectVal.equals("Binary")) {
                for (int i = 0, length = featureVector.size(); i < length; i++) {
                    featureVector.set(i, output.get(i) > 0 ? one : zero);
                }
            } else if (m_vectVal.equals("TF-Absolute")) {
                for (int i = 0, length = featureVector.size(); i < length; i++) {
                    if (occupiedIndexes.contains(i)) {
                        featureVector.add(i, new DoubleCell(output.get(i)));
                    }
                }
            } else if (m_vectVal.equals("TF-Relative")) {
                for (int i = 0, length = featureVector.size(); i < length; i++) {
                    if (occupiedIndexes.contains(i)) {
                        featureVector.add(i, new DoubleCell(output.get(i) / totalTerms));
                    }
                }
            }

            cells[0] = CollectionCellFactory.createSparseListCell(featureVector, DEFAULT_CELL);

        } else {
            cells = new DataCell[dim];

            if (m_vectVal.equals("Binary")) {
                for (int i = 0, length = cells.length; i < length; i++) {
                    cells[i] = output.get(i) > 0 ? one : zero;
                }
            } else if (m_vectVal.equals("TF-Absolute")) {
                for (int i = 0, length = cells.length; i < length; i++) {
                    if (occupiedIndexes.contains(i)) {
                        cells[i] = new DoubleCell(output.get(i));
                    } else {
                        cells[i] = DEFAULT_CELL;
                    }
                }
            } else if (m_vectVal.equals("TF-Relative")) {
                for (int i = 0, length = cells.length; i < length; i++) {
                    if (occupiedIndexes.contains(i)) {
                        cells[i] = new DoubleCell(output.get(i) / totalTerms);
                    } else {
                        cells[i] = DEFAULT_CELL;
                    }
                }
            }
        }
        return cells;
    }

    private List<DoubleCell> initFeatureVector(final int size) {
        final List<DoubleCell> featureVector = new ArrayList<DoubleCell>(size);
        for (int i = 0; i < size; i++) {
            featureVector.add(i, DEFAULT_CELL);
        }
        return featureVector;
    }

    private DataColumnSpec[] createSpecAsColumns() {
        DataColumnSpecCreator creator = new DataColumnSpecCreator("Col1", DoubleCell.TYPE);
        final int dim = m_dim;
        final DataColumnSpec[] specs = new DataColumnSpec[dim];

        for (int i = 0; i < dim; i++) {
            creator.setName("Col" + i);
            specs[i] = creator.createSpec();
        }

        return specs;
    }

    private DataColumnSpec[] createSpecAsCollection() {
        final int dim = m_dim;
        DataColumnSpec[] columnSpecs;
        columnSpecs = new DataColumnSpec[1];

        // add feature vector columns
        DataColumnSpecCreator columnSpecCreator = new DataColumnSpecCreator(
            DocumentDataTableBuilder.DEF_DOCUMENT_VECTOR_COLNAME, ListCell.getCollectionType(DoubleCell.TYPE));

        String[] elemNames = new String[dim];
        for (int i = 0; i < dim; i++) {
            elemNames[i] = "Col" + i;
        }
        columnSpecCreator.setElementNames(elemNames);
        columnSpecs[0] = columnSpecCreator.createSpec();

        return columnSpecs;
    }

    @Override
    public InputPortRole[] getInputPortRoles() {
        return m_inPortRoles;
    }

    @Override
    public OutputPortRole[] getOutputPortRoles() {
        return m_outPortRoles;
    }

    /**
     * @param dim
     * @param seed
     * @param hashFunc
     * @param vectVal
     *
     */
    protected void setValues(final int dim, final int seed, final String hashFunc, final String vectVal) {
        m_dim = dim;
        m_seed = seed;
        m_hashFunc = hashFunc;
        m_vectVal = vectVal;
    }

    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_docCol.saveSettingsTo(settings);
        m_asCol.saveSettingsTo(settings);
    }

    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_docCol.validateSettings(settings);
        m_asCol.validateSettings(settings);
    }

    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_docCol.loadSettingsFrom(settings);
        m_asCol.loadSettingsFrom(settings);
    }

}
