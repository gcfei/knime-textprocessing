/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   31.10.2016 (andisadewi): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.stanfordlemmatizer;

import org.apache.commons.lang3.StringUtils;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.ext.textprocessing.nodes.preprocessing.StreamableProcessingWithInternalsNodeModel;
import org.knime.ext.textprocessing.nodes.preprocessing.TermPreprocessing;

/**
 * The node model for the Stanford Lemmatizer.
 *
 * @author Andisa Dewi, KNIME.com, Berlin, Germany
 */
class StanfordLemmatizerNodeModel extends StreamableProcessingWithInternalsNodeModel<WarningMessage> {
    private SettingsModelBoolean m_failModel = StanfordLemmatizerNodeDialog.getFailModel();
    private StanfordLemmatizer m_lemma;

    /**
     * Creates a new node model.
     */
    public StanfordLemmatizerNodeModel() {
        super(WarningMessage.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected TermPreprocessing createPreprocessing(final WarningMessage internals) throws Exception {
        m_lemma = new StanfordLemmatizer(internals, m_failModel.getBooleanValue());
        return m_lemma;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void afterProcessing() {
        String warningMessage = m_lemma.getWarnMessage().get();
        if (!StringUtils.isEmpty(warningMessage)) {
            setWarningMessage(warningMessage);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected WarningMessage createStreamingOperatorInternals() {
        return new WarningMessage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected WarningMessage mergeStreamingOperatorInternals(final WarningMessage[] operatorInternals) {
        StringBuilder sb = new StringBuilder();
        for (WarningMessage oi : operatorInternals) {
            if (oi.get() != null) {
                sb.append(oi.get());
                sb.append("\n");
            }
        }
        WarningMessage res = new WarningMessage();
        res.set(sb.toString());
        return res;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void finishStreamableExecution(final WarningMessage operatorInternals) {
        WarningMessage warningMessage = operatorInternals;
        if (warningMessage.get() != null && warningMessage.get().length() > 0) {
            setWarningMessage(warningMessage.get());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        super.loadValidatedSettingsFrom(settings);
        m_failModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        super.saveSettingsTo(settings);
        m_failModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        super.validateSettings(settings);
        m_failModel.validateSettings(settings);
    }
}

