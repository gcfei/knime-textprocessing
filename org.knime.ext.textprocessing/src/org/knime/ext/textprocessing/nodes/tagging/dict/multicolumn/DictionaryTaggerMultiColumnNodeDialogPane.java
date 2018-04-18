/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
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
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
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
 *   Apr 11, 2018 (julian): created
 */
package org.knime.ext.textprocessing.nodes.tagging.dict.multicolumn;

import static org.knime.core.node.util.DataColumnSpecListCellRenderer.createInvalidSpec;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.util.ColumnSelectionSearchableListPanel;
import org.knime.core.node.util.ColumnSelectionSearchableListPanel.ConfigurationRequestEvent;
import org.knime.core.node.util.ColumnSelectionSearchableListPanel.ConfigurationRequestListener;
import org.knime.core.node.util.ColumnSelectionSearchableListPanel.ConfiguredColumnDeterminer;
import org.knime.core.node.util.ColumnSelectionSearchableListPanel.ListModifier;
import org.knime.core.node.util.ColumnSelectionSearchableListPanel.SearchedItemsSelectionMode;
import org.knime.core.node.util.DataColumnSpecListCellRenderer;

/**
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin Germany
 */
public class DictionaryTaggerMultiColumnNodeDialogPane extends NodeDialogPane {

    private static final DictionaryTaggerPanel DUMMY_PANEL = new DictionaryTaggerPanel(
        new DictionaryTaggerColumnSetting("DUMMY"), DataColumnSpecListCellRenderer.createInvalidSpec("DUMMY"));

    private final Map<DataColumnSpec, DictionaryTaggerColumnSetting> m_columnToSettings;

    private final IndividualsPanel m_individualsPanel;

    private final ColumnSelectionSearchableListPanel m_searchableListPanel;

    private final Set<String> m_errornousColNames;

    private DataTableSpec m_orgTableSpec;

    private JScrollPane m_individualsScrollPanel;

    private ListModifier m_searchableListModifier;

    /**
     *
     */
    public DictionaryTaggerMultiColumnNodeDialogPane() {
        super();

        m_columnToSettings = new LinkedHashMap<DataColumnSpec, DictionaryTaggerColumnSetting>();
        m_errornousColNames = new HashSet<String>();

        m_searchableListPanel = new ColumnSelectionSearchableListPanel(SearchedItemsSelectionMode.SELECT_FIRST,
            new ConfiguredColumnDeterminer() {

                @Override
                public boolean isConfiguredColumn(final DataColumnSpec spec) {
                    return m_columnToSettings.containsKey(spec);
                }
            });

        m_searchableListPanel.addConfigurationRequestListener(new ConfigurationRequestListener() {

            @Override
            public void configurationRequested(final ConfigurationRequestEvent searchEvent) {
                switch (searchEvent.getType()) {
                    case CREATION:
                        createAndAddDictTaggerPanelSetting();
                        break;
                    case DELETION:
                        for (DataColumnSpec spec : m_searchableListPanel.getSelectedColumns()) {

                            DictionaryTaggerColumnSetting dictTaggerColumnSetting = m_columnToSettings.get(spec);
                            if (dictTaggerColumnSetting != null) {
                                int indexIndividualIndex = getIndexIndividualIndex(m_columnToSettings.get(spec));
                                removeFromIndividualPanel(
                                    (DictionaryTaggerPanel)m_individualsPanel.getComponent(indexIndividualIndex));
                            }
                        }
                    default:
                        break;
                }
            }
        });

        final JPanel tabPanel = new JPanel(new BorderLayout());
        tabPanel.add(m_searchableListPanel, BorderLayout.CENTER);

        m_individualsPanel = new IndividualsPanel();
        m_individualsScrollPanel = new JScrollPane(m_individualsPanel);
        tabPanel.add(m_individualsScrollPanel, BorderLayout.EAST);

        addTab("Set dictionaries", tabPanel);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadSettingsFrom(final NodeSettingsRO settings, final DataTableSpec[] specs)
        throws NotConfigurableException {
        DataTableSpec spec = specs[0];
        if (spec.getNumColumns() == 0) {
            throw new NotConfigurableException("No columns at input.");
        }

        m_orgTableSpec = specs[0];

        m_columnToSettings.clear();
        m_errornousColNames.clear();
        m_individualsPanel.removeAll();

        m_searchableListModifier = m_searchableListPanel.update(spec);

        NodeSettingsRO subSettings;
        try {
            // this node settings object must contain only entry of type
            // NodeSetting
            subSettings = settings.getNodeSettings(DictionaryTaggerMultiColumnNodeModel.CFG_SUB_CONFIG);
        } catch (InvalidSettingsException ise) {
            subSettings = null;
        }
        if (subSettings != null) {
            // process settings for individual column
            for (String id : subSettings) {

                NodeSettingsRO idSettings;
                String nameForSettings;
                try {
                    // idSettigs address the settings for one particular column
                    idSettings = subSettings.getNodeSettings(id);
                    // the name of the column - must match
                    nameForSettings = idSettings.getString(DictionaryTaggerMultiColumnConfigKeys.CFGKEY_COLUMNNAME);

                } catch (InvalidSettingsException is) {
                    continue;
                }

                final DataColumnSpec orgSpec = m_orgTableSpec.getColumnSpec(nameForSettings);
                final DictionaryTaggerColumnSetting renameColumnSetting =
                    orgSpec == null ? new DictionaryTaggerColumnSetting(nameForSettings) : new DictionaryTaggerColumnSetting(orgSpec.getName());

                renameColumnSetting.loadSettingsFrom(idSettings);

                if (orgSpec == null) {
                    DataColumnSpec invalidSpec = createInvalidSpec(nameForSettings);
                    m_searchableListModifier.addAdditionalColumn(invalidSpec);
                    m_columnToSettings.put(invalidSpec, renameColumnSetting);
                } else {
                    m_columnToSettings.put(orgSpec, renameColumnSetting);

                }
            }
        }

        //add for each setting a panel in the individual panel
        for (Map.Entry<DataColumnSpec, DictionaryTaggerColumnSetting> entries : m_columnToSettings.entrySet()) {
            addToIndividualPanel(new DictionaryTaggerPanel(entries.getValue(), entries.getKey()));
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
        NodeSettingsWO subSettings = settings.addNodeSettings(DictionaryTaggerMultiColumnNodeModel.CFG_SUB_CONFIG);

        clearBorders();

        int i = 0;
        for (DictionaryTaggerColumnSetting colSet : m_columnToSettings.values()) {
            NodeSettingsWO subSub = subSettings.addNodeSettings(Integer.toString(i++));
            colSet.saveSettingsTo(subSub);
        }
    }

    private void createAndAddDictTaggerPanelSetting() {
        // there can only by one or non column selected.
        final DataColumnSpec selected = m_searchableListPanel.getSelectedColumn();
        if (selected != null && !m_columnToSettings.containsKey(selected)) {
            DictionaryTaggerColumnSetting dictTaggerColumnSetting =
                new DictionaryTaggerColumnSetting(selected.getName());
            m_columnToSettings.put(selected, dictTaggerColumnSetting);
            addToIndividualPanel(new DictionaryTaggerPanel(dictTaggerColumnSetting, selected));
        }
    }

    /**
     * resets all marked components.
     */
    private void clearBorders() {
        m_errornousColNames.clear();
        for (int i = 0; i < m_individualsPanel.getComponentCount(); i++) {
            DictionaryTaggerPanel panel = ((DictionaryTaggerPanel)m_individualsPanel.getComponent(i));
            if (panel.hasValidSpec()) {
                panel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
            }
        }
    }

    /**
    *
    * @param colSet the setting
    * @return the index of the panel in the {@link #m_individualsPanel} corresponding to the given column setting or
    *         <code>-1</code>.
    */
   private int getIndexIndividualIndex(final DictionaryTaggerColumnSetting colSet) {
       for (int i = 0; i < m_individualsPanel.getComponentCount(); i++) {
           DictionaryTaggerPanel component = (DictionaryTaggerPanel)m_individualsPanel.getComponent(i);
           if (component.getSettings().getColumnName().equals(colSet.getColumnName())) {
               return i;
           }
       }
       return -1;
   }

    /**
     * @param panel
     */
    protected void removeFromIndividualPanel(final DictionaryTaggerPanel panel) {
        if (m_searchableListPanel.isAdditionalColumn(panel.getColumnSpec())) {
            m_searchableListModifier.removeAdditionalColumn(panel.getColumnSpec().getName());
        }
        m_columnToSettings.remove(panel.getColumnSpec());
        m_individualsPanel.remove(panel);
        m_individualsPanel.revalidate();
        m_individualsPanel.repaint();
        m_searchableListPanel.revalidate();
        m_searchableListPanel.repaint();
    }

    private void addToIndividualPanel(final DictionaryTaggerPanel panel) {
        panel.addPropertyChangeListener(DictionaryTaggerPanel.REMOVE_ACTION, new PropertyChangeListener() {
            /** {@inheritDoc} */
            @Override
            public void propertyChange(final PropertyChangeEvent evt) {
                removeFromIndividualPanel(panel);
            }
        });
        m_individualsPanel.add(panel);
        m_individualsPanel.revalidate();
        m_individualsPanel.ensureLastVisible();
    }

    /**
     * Panel hosting the individual panels. It implements {@link Scrollable} to allow for correct jumping to the next
     * enclosed panel. It allows overwrites getPreferredSize() to return the sum of all individual heights.
     */
    @SuppressWarnings("serial")
    private static class IndividualsPanel extends JPanel implements Scrollable {

        /** Set box layout. */
        public IndividualsPanel() {
            BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
            setLayout(layout);
        }

        /** {@inheritDoc} */
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        /** {@inheritDoc} */
        @Override
        public int getScrollableBlockIncrement(final Rectangle visibleRect, //
            final int orientation, final int direction) {
            int rh = getComponentCount() > 0 ? getComponent(0).getHeight() : 0;
            return (rh > 0) ? Math.max(rh, (visibleRect.height / rh) * rh) : visibleRect.height;
        }

        /** {@inheritDoc} */
        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }

        /** {@inheritDoc} */
        @Override
        public boolean getScrollableTracksViewportWidth() {
            return false;
        }

        /** {@inheritDoc} */
        @Override
        public int getScrollableUnitIncrement(final Rectangle visibleRect, final int orientation, final int direction) {
            return getComponentCount() > 0 ? getComponent(0).getHeight() : 100;
        }

        /** {@inheritDoc} */
        @Override
        public Dimension getPreferredSize() {
            int height = 0;
            int width = 0;
            if (getComponentCount() < 1) {
                return DUMMY_PANEL.getPreferredSize();
            }
            for (Component c : getComponents()) {
                Dimension h = c.getPreferredSize();
                height += h.height;
                width = Math.max(width, h.width);
            }
            return new Dimension(width, height);
        }

        /**
         * ensures that the last added component is visible.
         */
        public void ensureLastVisible() {
            if (getComponentCount() > 2) {
                //the bounds of the last added components is zeroed, so we use the second last.
                Rectangle bounds = getComponent(getComponentCount() - 2).getBounds();
                bounds.y += getPreferredSize().getHeight() / 2;
                scrollRectToVisible(bounds);
            }
        }
    }

}