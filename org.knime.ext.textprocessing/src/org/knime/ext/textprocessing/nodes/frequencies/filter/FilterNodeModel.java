/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2007
 * University of Konstanz, Germany
 * Chair for Bioinformatics and Information Mining (Prof. M. Berthold)
 * and KNIME GmbH, Konstanz, Germany
 *
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner or
 * as specified in the license file distributed with this product.
 *
 * If you have any questions please contact the copyright holder:
 * website: www.knime.org
 * email: contact@knime.org
 * ---------------------------------------------------------------------
 * 
 * History
 *   21.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.frequencies.filter;

import java.io.File;
import java.io.IOException;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.base.node.preproc.filter.row.RowFilterTable;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleRange;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;

/**
 * The model class of the filter node. Providing all default settings of the
 * filters, as well as the management of the filtering process.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class FilterNodeModel extends NodeModel {

    /**
     * Specifies the threshold filtering.
     */
    public static final String SELECTION_THRESHOLD = "Threshold";
    
    /**
     * Specifies the number filtering. 
     */
    public static final String SELECTION_NUMBER = "Number of terms";
    
    /**
     * The default filtering
     */
    public static final String DEF_SELECTION = SELECTION_NUMBER;
    
    
    /**
     * The default number of numbe filtering.
     */
    public static final int DEF_NUMBER = 1000;
    
    /**
     * The min number of number filtering.
     */
    public static final int MIN_NUMBER = 0;
    
    /**
     * The max number of number filtering.
     */
    public static final int MAX_NUMBER = Integer.MAX_VALUE;

    
    /**
     * The default minimum number of threshold filtering.
     */
    public static final double DEF_MIN_THRESHOLD = 0.01;
    
    /**
     * The min minimum number of threshold filtering.
     */
    public static final double MIN_MIN_THRESHOLD = 0;
    
    /**
     * The default maximum number of threshold filtering.
     */
    public static final double DEF_MAX_THRESHOLD = 1.0;
    
    /**
     * The max maximum number of threshold filtering.
     */
    public static final double MAX_MAX_THRESHOLD = 1000;
    
    /**
     * The default settings for deep filtering.
     */
    public static final boolean DEF_DEEP_FILTERING = true;
    
    
    private SettingsModelString m_filterSelectionModel = 
        FilterNodeDialog.getSelectionModel();
    
    private SettingsModelString m_colModel = FilterNodeDialog.getColModel();
    
    private SettingsModelIntegerBounded m_numberModel = 
        FilterNodeDialog.getNumberModel();
    
    private SettingsModelDoubleRange m_minMaxModel = 
        FilterNodeDialog.getMinMaxModel();

    private SettingsModelBoolean m_deepFilteringModel = 
        FilterNodeDialog.getDeepFilteringModel();
    
    private int m_termColIndex = -1;
    
    /**
     * Creates an new instance of <code>FilterNodeModel</code>.
     */
    public FilterNodeModel() {
        super(1, 1);
        
        m_filterSelectionModel.addChangeListener(
                new FilterOptionChangeListener());
        enableModels();
    }
    
    private final void checkDataTableSpec(final DataTableSpec spec) 
    throws InvalidSettingsException {
        DataTableSpecVerifier verifier = new DataTableSpecVerifier(spec);
        verifier.verifyDocumentCell(true);
        verifier.verifyTermCell(true);
        verifier.verifyMinimumNumberCells(1, true);
        m_termColIndex = verifier.getTermCellIndex();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected final DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        checkDataTableSpec(inSpecs[0]);
        return inSpecs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        checkDataTableSpec(inData[0].getDataTableSpec());
        
        int filterColIndex = inData[0].getDataTableSpec().findColumnIndex(
                m_colModel.getStringValue());
        
        // Filtering
        FrequencyFilter filter = FilterFactory.createFilter(
                m_filterSelectionModel.getStringValue(), m_termColIndex, 
                filterColIndex, m_numberModel.getIntValue(), 
                m_minMaxModel.getMinRange(), m_minMaxModel.getMaxRange());
        
        DataTable preprocessedTable = filter.preprocessData(inData[0], exec);
        DataTable filteredTable;
        synchronized (this) {
            filteredTable = new RowFilterTable(preprocessedTable, filter);
        }
        
        // Deep filtering
        if (m_deepFilteringModel.getBooleanValue()) {
            TermPurger purger = new TermPurger(filteredTable, exec);
            return new BufferedDataTable[]{purger.getPurgedDataTable()};
        }
        
        return new BufferedDataTable[]{
                exec.createBufferedDataTable(filteredTable, exec)};
    }

    private void enableModels() {
        if (m_filterSelectionModel.getStringValue().equals(
                FilterNodeModel.SELECTION_NUMBER)) {
            m_numberModel.setEnabled(true);
            m_minMaxModel.setEnabled(false);
        } else if (m_filterSelectionModel.getStringValue().equals(
                FilterNodeModel.SELECTION_THRESHOLD)) {
            m_numberModel.setEnabled(false);
            m_minMaxModel.setEnabled(true);            
        }
    }
    
    private class FilterOptionChangeListener implements ChangeListener {
        /**
         * {@inheritDoc}
         */
        public void stateChanged(final ChangeEvent e) {
            enableModels();
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // TODO Auto-generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_filterSelectionModel.saveSettingsTo(settings);
        m_colModel.saveSettingsTo(settings);
        m_numberModel.saveSettingsTo(settings);
        m_minMaxModel.saveSettingsTo(settings);
        m_deepFilteringModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_filterSelectionModel.validateSettings(settings);
        m_colModel.validateSettings(settings);
        m_numberModel.validateSettings(settings);
        m_minMaxModel.validateSettings(settings);
        m_deepFilteringModel.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_filterSelectionModel.loadSettingsFrom(settings);
        m_colModel.loadSettingsFrom(settings);
        m_numberModel.loadSettingsFrom(settings);
        m_minMaxModel.loadSettingsFrom(settings);
        m_deepFilteringModel.loadSettingsFrom(settings);
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir, 
            final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
        // Nothing to do ...
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir, 
            final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
        // Nothing to do ...
    }
}