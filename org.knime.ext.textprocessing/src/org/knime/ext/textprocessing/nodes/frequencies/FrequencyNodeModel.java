/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2010
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, version 2, as 
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ---------------------------------------------------------------------
 * 
 * History
 *   17.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.frequencies;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;

/**
 * This class represents the super class of all text frequency computation node 
 * models. Classes which extend <code>FrequencyNodeModel</code> need to 
 * implement the method
 * {@link FrequencyNodeModel#initCellFactory(BufferedDataTable, ExecutionContext)} 
 * and take care of a proper initialization of the used 
 * {@link org.knime.ext.textprocessing.nodes.frequencies.FrequencyCellFactory} 
 * instance. The configure and execute procedure is done by the 
 * <code>FrequencyNodeModel</code>, classes extending this model do not
 * need to care about that. Once the used <code>FrequencyCellFactory</code> 
 * instance is initialized properly the rest is done automatically.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public abstract class FrequencyNodeModel extends NodeModel {

    private static final int INDATA_INDEX = 0;
    
    
    private int m_documentColIndex = -1;
    
    private int m_termColIndex = -1;
    
    private SettingsModelString m_documentColModel =
        FrequenciesNodeSettingsPane.getDocumentColumnModel();
    
    
    
    /**
     * The cell factory creating the cells containing a certain frequency value.
     */
    protected FrequencyCellFactory m_cellFac;
    
    
    /**
     * Creates a new instance of <code>FrequencyNodeModel</code> with given
     * column name and flag if an integer column has to be appended or a
     * double column.
     */
    public FrequencyNodeModel() {
        super(1, 1);
    }
    
    /**
     * Initializes the underlying <code>FrequencyCellFactory</code> and passes 
     * through the in data and an execution context.
     * 
     * @param inData The input data.
     * @param exec An execution context to monitor the progress.
     * @throws CanceledExecutionException If the user canceled the execution.
     */
    protected abstract void initCellFactory(BufferedDataTable inData,
            ExecutionContext exec) throws CanceledExecutionException;
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected final DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        checkDataTableSpec(inSpecs[INDATA_INDEX]);
        return new DataTableSpec[]{createDataTableSpec(inSpecs[INDATA_INDEX])};
    }

    private final void checkDataTableSpec(final DataTableSpec spec) 
    throws InvalidSettingsException {
        DataTableSpecVerifier verifier = new DataTableSpecVerifier(spec);
        verifier.verifyMinimumDocumentCells(1, true);
        verifier.verifyTermCell(true);
        m_termColIndex = verifier.getTermCellIndex();
        
        m_documentColIndex = spec.findColumnIndex(
                m_documentColModel.getStringValue());
        if (m_documentColIndex < 0) {
            throw new InvalidSettingsException(
                    "Index of specified document column is not valid! " 
                    + "Check your settings!");
        } 
    }
    
    /**
     * Creates a new <code>DataTableSpec</code>.
     * 
     * @param inDataSpec The input <code>DataTableSpec</code>.
     * @return The new created output <code>DataTableSpec</code>.
     */
    protected abstract DataTableSpec createDataTableSpec(
            final DataTableSpec inDataSpec);
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected final BufferedDataTable[] execute(
            final BufferedDataTable[] inData, final ExecutionContext exec) 
    throws Exception {
        BufferedDataTable inDataTable = inData[INDATA_INDEX];
        
        m_documentColIndex = inDataTable.getDataTableSpec().findColumnIndex(
                m_documentColModel.getStringValue());
        
        // initializes the corresponding cell factory
        initCellFactory(inDataTable, exec);
        if (m_cellFac == null) {
            throw new NullPointerException(
                    "CellFactory instance may not be null!");
        }
        
        // compute frequency and add column
        ColumnRearranger rearranger = new ColumnRearranger(
                inDataTable.getDataTableSpec());
        rearranger.append(m_cellFac);
        
        return new BufferedDataTable[] {
                exec.createColumnRearrangeTable(inDataTable, rearranger, 
                exec)};
    }

    /**
     * @return the documentColIndex
     */
    public int getDocumentColIndex() {
        return m_documentColIndex;
    }

    /**
     * @return the termColIndex
     */
    public int getTermColIndex() {
        return m_termColIndex;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_documentColModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_documentColModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_documentColModel.validateSettings(settings);
    }
}
